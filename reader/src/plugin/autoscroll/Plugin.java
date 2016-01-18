/*
 * @@DESCRIPTION@@. Copyright (C) @@COPYRIGHT@@
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package plugin.autoscroll;

import java.util.Timer;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import reader.Engine;
import reader.Event;
import reader.IPlugin;
import reader.Pager;

public class Plugin implements IPlugin
{
    public static int aI_KEYCODE_FORWARD; // increase autoscroll interval
    public static int aI_KEYCODE_BACKWARD; // decrease autoscroll interval
    public static int aI_KEYCODE_PAUSE; // pause key

    protected static final int PAGE_MAX_INTERVAL = 22000;
    protected static final int PAGE_MIN_INTERVAL = 2000;
    protected static final int PAGE_INCREMENT = 200;

    protected static final int LINE_MAX_INTERVAL = 10000;
    protected static final int LINE_MIN_INTERVAL = 1000;
    protected static final int LINE_INCREMENT = 100;

    protected static final int PIXEL_MAX_INTERVAL = 2100;
    protected static final int PIXEL_MIN_INTERVAL = 100;
    protected static final int PIXEL_INCREMENT = 100;

    private static final int ST_DORMANT = 1;
    private static final int ST_READY = 2;
    private static final int ST_ACTIVE = 3;
    private static final int ST_INDICATOR = 4;
    private static final int ST_PAUSED = 5;

    private static final int EV_UNKNOWN = -1;
    private static final int EV_START = 1;
    private static final int EV_STOP = 2;
    private static final int EV_PAINT = 3;
    private static final int EV_INDICATOR_INC = 4;
    private static final int EV_INDICATOR_DEC = 5;
    private static final int EV_INDICATOR_STOP = 6;
    private static final int EV_ACTIVATE = 7;
    private static final int EV_DEACTIVATE = 8;
    private static final int EV_OTHER_KEY = 9;
    private static final int EV_MENU_REQUEST = 10;
    private static final int EV_MENU_SELECTED = 11;
    private static final int EV_PAUSE = 12;

    private static final int INDICATOR_HEIGHT = 4;
    private static final int INDICATOR_BORDER = 5;
    private static final int INDICATOR_BOTTOM_MARGIN = 5;
    private static final int INDICATOR_FONT_MARGIN = 3;

    private Engine engine;
    private Timer timer;
    private int state = ST_DORMANT;

    private int interval;
    private int maxInterval;
    private int minInterval;
    private int increment;
    private int scrollSize;

    protected void setIntervals(int minInterval, int maxInterval, int increment, int scrollSize)
    {
        this.minInterval = minInterval;
        this.maxInterval = maxInterval;
        this.increment = increment;
        this.scrollSize = scrollSize;
        interval = (maxInterval - minInterval) / 2;
    }

    protected void start()
    {
        state = ST_ACTIVE;
        engine.sendEvent(new Event(Event.MESSAGE, 0, this, null, "aS_STRING_STARTED"));
        engine.activatePlugin(this);
        timerRestart();
        engine.getDisplay().setCurrent(engine.getCanvas());
    }

    private int parse(Event event)
    {
        int type = event.getType();
        int key = event.getSubtype();

        switch (type)
        {
            case Event.PAINT:
                return EV_PAINT;
            case Event.PLUGIN_START:
                return EV_START;
            case Event.PLUGIN_STOP:
                return EV_STOP;
        }

        if (type == Event.KEY_PRESSED || type == Event.KEY_REPEATED)
        {
            if (key == aI_KEYCODE_BACKWARD)
            {
                return EV_INDICATOR_DEC;
            }

            if (key == aI_KEYCODE_FORWARD)
            {
                return EV_INDICATOR_INC;
            }

            if (key == aI_KEYCODE_PAUSE)
            {
                return EV_PAUSE;
            }

            return EV_OTHER_KEY;
        }

        if (type == Event.KEY_RELEASED && (key == aI_KEYCODE_BACKWARD || key == aI_KEYCODE_FORWARD))
        {
            return EV_INDICATOR_STOP;
        }

        if (event.getType() == Event.MENU && event.getSubtype() == Event.MENU_REQUEST)
        {
            return EV_MENU_REQUEST;
        }

        if (event.getType() == Event.MENU && event.getSubtype() == Event.MENU_SELECTED && event.getDst() == this)
        {
            return EV_MENU_SELECTED;
        }

        return EV_UNKNOWN;
    }

    public Event handleEvent(Event event) throws Exception
    {
        int ev = parse(event);
        switch (state)
        {
            // plugin is loaded but not initialized
            case ST_DORMANT:
                switch (ev)
                {
                    case EV_START:
                        state = ST_READY;
                        do_ready(event);
                        return event;
                }
                break;

            // initialized but not active
            case ST_READY:
                switch (ev)
                {
                    case EV_STOP:
                        state = ST_DORMANT;
                        do_dormant(event);
                        return event;
                    case EV_MENU_REQUEST:
                        do_menu(event);
                        return event;
                    case EV_MENU_SELECTED:
                        Menu menu = new Menu(this, engine);
                        engine.getDisplay().setCurrent(menu);
                        return null;
                }
                break;

            // plugin is active
            case ST_ACTIVE:
                switch (ev)
                {
                    case EV_STOP:
                        state = ST_DORMANT;
                        do_dormant(event);
                        return event;
                    case EV_INDICATOR_INC:
                        state = ST_INDICATOR;
                        do_increase();
                        return null;
                    case EV_INDICATOR_DEC:
                        state = ST_INDICATOR;
                        do_decrease();
                        return null;
                    case EV_MENU_REQUEST:
                        state = ST_READY;
                        timerStop();
                        engine.sendEvent(new Event(Event.MESSAGE, 0, this, null, "aS_STRING_STOPPED"));
                        engine.deactivatePlugin(this);
                        Pager pager = engine.getPager();
                        pager.setCurrentPage(pager.getCurrentPage());
                        engine.getCanvas().repaint();
                        return event;
                    case EV_PAUSE:
                        state = ST_PAUSED;
                        timerStop();
                        engine.sendEvent(new Event(Event.MESSAGE, 0, this, null, "aS_STRING_PAUSED"));
                        return null;
                    case EV_OTHER_KEY:
                        // some other key is pressed
                        // reset time counter
                        timerRestart();
                        return event;
                }
                break;

            // plugin paints indicator
            case ST_INDICATOR:
                switch (ev)
                {
                    case EV_INDICATOR_INC:
                        do_increase();
                        return event;
                    case EV_INDICATOR_DEC:
                        do_decrease();
                        return event;
                    case EV_INDICATOR_STOP:
                        state = ST_ACTIVE;
                        engine.getCanvas().repaint();
                        return null;
                    case EV_PAINT:
                        do_paint(event);
                        return null;
                }
            // initialized but not active
            case ST_PAUSED:
                if (event.getType() == Event.KEY_PRESSED)
                {
                    state = ST_ACTIVE;
                    engine.sendEvent(new Event(Event.MESSAGE, 0, this, null, "aS_STRING_RESUMED"));
                    timerRestart();
                    return null;
                }
        }
        // event is not processed
        return event;
    }

    private void do_ready(Event event) throws IOException
    {
        engine = (Engine) event.getSrc();
        DataInputStream is = (DataInputStream) event.getParam();

        if (engine.isNewRecordStore())
        {
            maxInterval = PAGE_MAX_INTERVAL;
            minInterval = PAGE_MIN_INTERVAL;
            interval = (PAGE_MAX_INTERVAL - PAGE_MIN_INTERVAL) / 2;
            increment = PAGE_INCREMENT;
            scrollSize = engine.getBook().getViewportHeight();
        }
        else
        {
            maxInterval = is.readUnsignedShort();
            minInterval = is.readUnsignedShort();
            interval = is.readUnsignedShort();
            increment = is.readUnsignedShort();
            scrollSize = is.readUnsignedShort();
        }
    }

    private void do_dormant(Event event) throws IOException
    {
        // save
        DataOutputStream os = (DataOutputStream) event.getParam();
        os.writeShort(maxInterval);
        os.writeShort(minInterval);
        os.writeShort(interval);
        os.writeShort(increment);
        os.writeShort(scrollSize);
        engine.deactivatePlugin(this);
        engine = null;
    }

    private void do_increase()
    {
        if (interval < maxInterval)
            interval = interval + increment;
        engine.getCanvas().repaint();
    }

    private void do_decrease()
    {
        if (interval > minInterval)
            interval = interval - increment;
        engine.getCanvas().repaint();
    }

    private void do_paint(Event event)
    {
        Graphics graphics = (Graphics) event.getParam();
        int width = engine.getCanvas().getWidth();
        int height = engine.getCanvas().getHeight();
        int indicatorWidth = width - INDICATOR_BORDER * 2;

        Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);

        // clear indicator area
        int indicatorAreaHeight = INDICATOR_BOTTOM_MARGIN + INDICATOR_HEIGHT + INDICATOR_FONT_MARGIN * 2
                + font.getHeight();
        graphics.setColor(255, 255, 255);
        graphics.fillRect(0, height - indicatorAreaHeight, width, indicatorAreaHeight);

        graphics.setColor(0, 0, 0);

        // draw indicator value
        String indicatorValue = Integer.toString(interval / 1000) + "."
                + Integer.toString(interval % 1000).substring(0, 1);

        graphics.drawString(indicatorValue, width / 2, height - INDICATOR_BOTTOM_MARGIN - INDICATOR_BORDER
                - INDICATOR_FONT_MARGIN - font.getHeight(), Graphics.TOP | Graphics.HCENTER);
        // draw empty indicator
        graphics.drawRect(INDICATOR_BORDER, height - (INDICATOR_BOTTOM_MARGIN + INDICATOR_HEIGHT), width
                - (INDICATOR_BORDER * 2), INDICATOR_HEIGHT);
        // draw filled part of indicator
        int step = (maxInterval - minInterval) / (width - (INDICATOR_BORDER * 2));
        int filledWidth = (interval - minInterval) / step;
        graphics.fillRect(INDICATOR_BORDER, height - (INDICATOR_BOTTOM_MARGIN + INDICATOR_HEIGHT), filledWidth,
                INDICATOR_HEIGHT);
    }

    private void do_menu(Event event) throws Exception
    {
        ((IPlugin) event.getSrc()).handleEvent(new Event(Event.MENU, Event.MENU_REPLY, this, null,
                "aS_STRING_AUTOSCROLL"));
    }

    public void flipPage() throws Exception
    {
        engine.getPager().scroll(scrollSize);
        engine.getCanvas().repaint();
        timer.schedule(new Task(this), (long) interval);
    }

    private void timerRestart()
    {
        if (timer != null)
            timer.cancel();
        timer = new Timer();
        timer.schedule(new Task(this), interval);
    }

    private void timerStop()
    {
        if (timer != null)
            timer.cancel();
        timer = null;
    }
}