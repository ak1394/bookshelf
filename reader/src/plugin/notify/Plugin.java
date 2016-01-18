/*-
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

package plugin.notify;

import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;

import reader.Engine;
import reader.Event;
import reader.IPlugin;

/**
 * This plugin adds support for 'notifications', the text 
 * messages that appear on the screen for a short while
 * and disappear. The <code>javax.microedition.lcdui.Alert</code>
 * has similar functionality, but on Siemens platforms it
 * produces a loud beep, and that is unwanted.
 * 
 * @author Anton Krasovsky <ak1394@users.sourceforge.net>
 */
public class Plugin implements IPlugin, Runnable
{
    private static final int SLEEP_TIME = 1000;
    Engine engine;
    String message;
    Displayable revertTo;
    
    boolean active = false;
    private static final int VERTICAL_POSITION = 20;

    public Event handleEvent(Event event)
    {
        if (event.getType() == Event.PLUGIN_START)
        {
            engine = (Engine) event.getSrc();
            return event;
        }
        else if (event.getType() == Event.MESSAGE)
        {
            switch(event.getSubtype())
            {
                case Event.MESSAGE_REVERT_CANVAS:
                    revertTo = engine.getCanvas();
                    break;
                case Event.MESSAGE_REVERT_CURRENT:
                    revertTo = engine.getDisplay().getCurrent();
                    break;
            }
            message = (String) event.getParam();
            Thread thread = new Thread(this);
            thread.start();
        }
        
        return event;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
        // TODO localization support
        Form form = new Form("Message");
        form.append(message);
        engine.getDisplay().setCurrent(form);
        try
        {

            Thread.sleep(SLEEP_TIME);
        }
        catch (InterruptedException ex)
        {
        }
        engine.getDisplay().setCurrent(revertTo);
    }
}