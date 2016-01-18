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

package plugin.menu;

import javax.microedition.lcdui.*;

import reader.Engine;
import reader.IPlugin;
import reader.Event;

public class Menu extends List implements CommandListener
{

    private Engine engine;
    private IPlugin[] plugins;
    private int lastItem;

    public Menu(Engine engine, IPlugin[] plugins, String[] items, int lastItem)
    {
        super("aS_STRING_TITLE", List.IMPLICIT);
        this.engine = engine;
        this.plugins = plugins;
        this.lastItem = lastItem;
        for (int i = 0; i <= lastItem; i++)
        {
            append(items[i], null);
        }
        setCommandListener(this);
    }

    public void commandAction(Command c, Displayable d)
    {
        int i = getSelectedIndex();
        if (i == 0)
        {
            engine.getDisplay().setCurrent(engine.getCanvas());
        }
        else if (i == lastItem)
        {
            engine.exit();
        }
        else
        {
            engine.sendEvent(new Event(Event.MENU, Event.MENU_SELECTED, null,
                    plugins[i], null));
        }
    }

} // end of class
