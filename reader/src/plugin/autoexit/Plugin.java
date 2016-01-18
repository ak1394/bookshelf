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

package plugin.autoexit;

import java.util.Timer;
import java.util.TimerTask;

import reader.Engine;
import reader.Event;
import reader.IPlugin;

public class Plugin extends TimerTask implements IPlugin
{
    private Timer timer;
    private Engine engine;
    private int counter = 0;
    private static int xI_INTERVAL = 60000;
    private static int BACKLIGHT = 5;
    private static int EXIT = 15;

    public Plugin()
    {
        timer = new Timer();
        timer.scheduleAtFixedRate(this, 0, xI_INTERVAL);
    }

    public Event handleEvent(Event event)
    {
        if (event.getType() == Event.PLUGIN_START)
        {
            engine = (Engine) event.getSrc();
        }
        else if (event.getType() == Event.SCROLL)
        {
            // drop counter to zero is page is scrolled
            counter = 0;
        }
        return event;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.TimerTask#run()
     */
    public void run()
    {
        if(engine != null)
        {
            if (counter >= EXIT)
            {
                // if EXIT or more, leave the midlet
                engine.exit();
            }

            if (counter >= BACKLIGHT)
            {
                // if BACKLIGHT or more, turn off the backlight
                engine.getPlatformCanvas().lightOff();
            }
        }
        counter++;
    }
}
