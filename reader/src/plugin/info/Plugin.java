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

package plugin.info;

import javax.microedition.lcdui.*;

import java.util.*;

import reader.IPlugin;
import reader.Event;
import reader.Engine;

public class Plugin implements IPlugin
{
    public static int aI_KEYCODE_CENTER;

    private static final int ST_DORMANT = 1;
    private static final int ST_ACTIVE = 2;

    private static final int EV_UNKNOWN = -1;
    private static final int EV_DEACTIVATE = 1;
    private static final int EV_REFRESH = 2;
    private static final int EV_ACTIVATE = 3;
    private static final int EV_PAINT = 4;

    private int state = ST_DORMANT;

    private int parse(Event event)
    {
        if (event.getType() == Event.PAINT)
            return EV_PAINT;

        if (event.getType() == Event.KEY_PRESSED && event.getSubtype() == aI_KEYCODE_CENTER)
            return EV_ACTIVATE;

        if (event.getType() == Event.KEY_REPEATED && event.getSubtype() == aI_KEYCODE_CENTER)
            return EV_REFRESH;

        if (event.getType() == Event.KEY_RELEASED && event.getSubtype() == aI_KEYCODE_CENTER)
            return EV_DEACTIVATE;

        return EV_UNKNOWN;
    }

    private void repaint(Event event)
    {
        ((Engine) event.getSrc()).getCanvas().repaint();
    }

    private void paint(Event event)
    {
        Engine engine = (Engine) event.getSrc();
        Graphics g = (Graphics) event.getParam();

        int height = g.getFont().getHeight();
        int y = 0;
        int offset = 35;
        g.setColor(0xffffff);
        g.fillRect(0, 0, engine.getCanvas().getWidth(), engine.getCanvas().getHeight());
        g.setColor(0x000000);

        g.drawString(engine.getTitle(), engine.getCanvas().getWidth() / 2, y, Graphics.TOP | Graphics.HCENTER);
        y = y + height;

        g.drawString("aS_STRING_TOTAL_PAGES", 0, y, Graphics.TOP | Graphics.LEFT);
        int firstPage = engine.getBook().getStartPage() + 1;
        int lastPage = engine.getBook().getStartPage() + engine.getBook().getPageCount();
        g.drawString(firstPage + ".." + lastPage, offset, y, Graphics.TOP | Graphics.LEFT);
        y = y + height;

        g.drawString("aS_STRING_CURRENT_PAGE", 0, y, Graphics.TOP | Graphics.LEFT);
        g.drawString(Integer.toString(engine.getPager().getCurrentPage() + engine.getBook().getStartPage() + 1),
                offset, y, Graphics.TOP | Graphics.LEFT);
        y = y + height;

        Calendar cal = Calendar.getInstance();
        String time = addZero(cal.get(Calendar.HOUR_OF_DAY)) + ":" + addZero(cal.get(Calendar.MINUTE)) + ":"
                + addZero(cal.get(Calendar.SECOND));
        String date = cal.get(Calendar.YEAR) + "." + addZero(cal.get(Calendar.MONTH) + 1) + "."
                + addZero(cal.get(Calendar.DATE));
        g.drawString("aS_STRING_TIME", 0, y, Graphics.TOP | Graphics.LEFT);
        g.drawString(time, offset, y, Graphics.TOP | Graphics.LEFT);
        y = y + height;
        g.drawString("aS_STRING_DATE", 0, y, Graphics.TOP | Graphics.LEFT);
        g.drawString(date, offset, y, Graphics.TOP | Graphics.LEFT);
    }

    public Event handleEvent(Event event)
    {
        int ev = parse(event);

        switch (state)
        {
            case ST_DORMANT:
                switch (ev)
                {
                    case EV_ACTIVATE:
                        state = ST_ACTIVE;
                        ((Engine) event.getSrc()).activatePlugin(this);
                        repaint(event);
                        return null;
                }
                break;
            case ST_ACTIVE:
                switch (ev)
                {
                    case EV_PAINT:
                        paint(event);
                        return null;
                    case EV_REFRESH:
                        repaint(event);
                        return null;
                    case EV_DEACTIVATE:
                        state = ST_DORMANT;
                        ((Engine) event.getSrc()).deactivatePlugin(this);
                        repaint(event);
                        return null;
                }
        }
        // event is not processed
        return event;
    }

    private String addZero(int i)
    {
        if (i < 10)
        {
            return "0" + Integer.toString(i);
        }
        else
        {
            return Integer.toString(i);
        }
    }

} // end of class
