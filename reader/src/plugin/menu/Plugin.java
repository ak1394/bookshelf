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

import reader.IPlugin;
import reader.Event;
import reader.Engine;

public class Plugin implements IPlugin
{
    public static int aI_KEYCODE_MENU;
    private Engine engine;
    private IPlugin plugins[];
    private String items[];
    private int currentItem;

    public Event handleEvent(Event event)
    {
        int type = event.getType();

        if (type == Event.PLUGIN_START)
        {
            engine = (Engine) event.getSrc();
            // carter for every plugin, back, and exit menu items
            plugins = new IPlugin[engine.getPluginCount() + 2];
            items = new String[engine.getPluginCount() + 2];
            return event;
        }

        if (type == Event.KEY_PRESSED && event.getSubtype() == aI_KEYCODE_MENU)
        {
            currentItem = 1;
            engine.sendEvent(new Event(Event.MENU, Event.MENU_REQUEST, this,
                    null, null));
            // display menu, by now plugins[] and items[] will be populated
            plugins[0] = this;
            items[0] = "aS_STRING_BACK";
            plugins[currentItem] = this;
            items[currentItem] = "aS_STRING_EXIT";
            Menu menu = new Menu(engine, plugins, items, currentItem);
            engine.getDisplay().setCurrent(menu);
            return null;
        }

        if (type == Event.MENU && event.getSubtype() == Event.MENU_REPLY)
        {
            plugins[currentItem] = (IPlugin) event.getSrc();
            items[currentItem] = (String) event.getParam();
            currentItem++;
            return null;
        }
        return event;
    }

} // end of class
