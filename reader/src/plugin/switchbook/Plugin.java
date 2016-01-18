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

package plugin.switchbook;

import reader.IPlugin;
import reader.Event;
import reader.Engine;

public class Plugin implements IPlugin
{
    private Engine engine;
    
    public Event handleEvent(Event event) throws Exception
    {
        if(event.getType() == Event.PLUGIN_START)
        {
            engine = (Engine) event.getSrc();
            return event;
        }
    
        if(event.getType() == Event.MENU && event.getSubtype() == Event.MENU_REQUEST)
        {
            ((IPlugin)event.getSrc()).handleEvent(new Event(Event.MENU, Event.MENU_REPLY, this, null, "aS_STRING_SWITCH"));
            return event;
        }
    
        if(event.getType() == Event.MENU && event.getSubtype() == Event.MENU_SELECTED &&  event.getDst() == this)
        {
            Menu menu = new Menu(engine);
            engine.getDisplay().setCurrent(menu);
            return null;
        }
    
        return event;
    }

} // end of class
