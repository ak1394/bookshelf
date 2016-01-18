/*
 * @@DESCRIPTION@@. 
 * Copyright (C) @@COPYRIGHT@@
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package plugin.pager;

import javax.microedition.lcdui.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import reader.IPlugin;
import reader.Event;
import reader.Engine;
import reader.Pager;

public class Plugin implements IPlugin
{
    public static int aI_KEYCODE_UP;
    public static int aI_KEYCODE_UP2;
    public static int aI_KEYCODE_UP3;

    public static int aI_KEYCODE_DOWN;
    public static int aI_KEYCODE_DOWN2;
    public static int aI_KEYCODE_DOWN3;

    public static int aI_KEYCODE_LINE_UP;
    public static int aI_KEYCODE_LINE_DOWN;

    public static int aI_KEYCODE_LIGHT;

    private static final int ST_DORMANT = 1;
    private static final int ST_ACTIVE = 2;

    private static final int EV_UNKNOWN = -1;
    private static final int EV_START = 1;
    private static final int EV_STOP = 2;
    private static final int EV_PAGEUP = 3;
    private static final int EV_PAGEDOWN = 4;
    private static final int EV_LINEUP = 5;
    private static final int EV_LINEDOWN = 6;
    private static final int EV_LIGHT = 7;
    private static final int EV_PAINT = 8;

    private int state = ST_DORMANT;
    private int vewportHeight;
    private int lineHeight;
    private Engine engine;
    private Pager pager;
    private boolean light = false;

    private int parse(Event event)
    {
        int type = event.getType();
        int k = event.getSubtype();

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
            if (k == aI_KEYCODE_UP || k == aI_KEYCODE_UP2 || k == aI_KEYCODE_UP3)
            {
                return EV_PAGEUP;
            }

            if (k == aI_KEYCODE_DOWN || k == aI_KEYCODE_DOWN2 || k == aI_KEYCODE_DOWN3)
            {
                return EV_PAGEDOWN;
            }

            if (k == aI_KEYCODE_LINE_DOWN)
            {
                return EV_LINEDOWN;
            }
            if (k == aI_KEYCODE_LINE_UP)
            {
                return EV_LINEUP;
            }
            if (k == aI_KEYCODE_LIGHT)
            {
                return EV_LIGHT;
            }
        }

        return EV_UNKNOWN;
    }

    public Event handleEvent(Event event) throws Exception
    {
        int ev = parse(event);
        switch (state)
        {
        case ST_DORMANT:
            switch (ev)
            {
            case EV_START:
                state = ST_ACTIVE;
                engine = (Engine) event.getSrc();
                lineHeight = engine.getBook().getPreferredLineHeight();
                pager = engine.getPager();
                vewportHeight = engine.getBook().getViewportHeight();
                engine.activatePlugin(this);
                DataInputStream is = (DataInputStream) event.getParam();
                // restore current page if datastream is available
                if (engine.isNewRecordStore())
                {
                    pager.setCurrentPage(0);
                }
                else
                {
                    pager.setCurrentPage(is.readInt());
                }
                // pass down plugin_start
                engine.getCanvas().repaint();
                return event;
            }
            break;
        case ST_ACTIVE:
            switch (ev)
            {
            case EV_STOP:
                state = ST_DORMANT;
                // save current page
                DataOutputStream dos = (DataOutputStream) event.getParam();
                dos.writeInt(pager.getCurrentPage());
                engine.getPlatformCanvas().clearBuffer();
                // pass down plugin_stop
                return event;

            case EV_PAGEUP:
                pager.scroll(-vewportHeight);
                engine.getCanvas().repaint();
                return null;

            case EV_PAGEDOWN:
                pager.scroll(vewportHeight);
                engine.getCanvas().repaint();
                return null;

            case EV_LINEUP:
                pager.scroll(-lineHeight);
                engine.getCanvas().repaint();
                return null;

            case EV_LINEDOWN:
                pager.scroll(lineHeight);
                engine.getCanvas().repaint();
                return null;

            case EV_LIGHT:
                if (light != false)
                {
                    light = false;
                    engine.getPlatformCanvas().lightOff();
                    engine.sendEvent(new Event(Event.MESSAGE, 0, this, null, "aS_STRING_LIGHT_OFF"));
                }
                else
                {
                    light = true;
                    engine.getPlatformCanvas().lightOn();
                    engine.sendEvent(new Event(Event.MESSAGE, 0, this, null, "aS_STRING_LIGHT_ON"));
                }
                return null;

            case EV_PAINT:
                Graphics g = (Graphics) event.getParam();
                pager.paint(g);
                return null;
            }
        }
        // event is not processed
        return event;
    }
}

