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

package plugin.autoscroll;

import javax.microedition.lcdui.*;
import reader.Engine;

public class Menu extends List implements CommandListener
{
    private Engine engine;
    private Plugin plugin;

    public Menu(Plugin plugin, Engine engine)
    {
        super("aS_STRING_AUTOSCROLL", Choice.IMPLICIT, new String[] { "aS_STRING_BACK", "aS_STRING_START",
                "aS_STRING_PAGE", "aS_STRING_LINE", "aS_STRING_PIXEL" }, null);
        this.engine = engine;
        this.plugin = plugin;
        setCommandListener(this);
    }

    public void commandAction(Command c, Displayable d)
    {
        switch (getSelectedIndex())
        {
            case 0:
                // back
                engine.getDisplay().setCurrent(engine.getCanvas());
                break;
            case 1:
                // start
                plugin.start();
                break;
            case 2:
                // page
                plugin.setIntervals(Plugin.PAGE_MIN_INTERVAL, Plugin.PAGE_MAX_INTERVAL, Plugin.PAGE_INCREMENT, engine
                        .getBook().getViewportHeight());
                plugin.start();
                break;
            case 3:
                // line
                plugin.setIntervals(Plugin.LINE_MIN_INTERVAL, Plugin.LINE_MAX_INTERVAL, Plugin.LINE_INCREMENT, engine
                        .getBook().getPreferredLineHeight());
                plugin.start();
                break;
            case 4:
                // pixel
                plugin.setIntervals(Plugin.PIXEL_MIN_INTERVAL, Plugin.PIXEL_MAX_INTERVAL, Plugin.PIXEL_INCREMENT, 1);
                plugin.start();
                break;
        }
    }
}