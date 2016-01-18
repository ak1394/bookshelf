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

package plugin.gopage;

import javax.microedition.lcdui.*;

import reader.Engine;
import reader.Event;

public class Menu extends TextBox implements CommandListener
{
    private Engine engine;

    public Menu(Engine e)
    {
        super(Integer.toString(e.getPager().getCurrentPage() + e.getBook().getStartPage() + 1) + ": " + Integer.toString(e.getBook().getStartPage() + 1)
                + ".." + Integer.toString(e.getBook().getStartPage() + e.getBook().getPageCount()), "", 6,
                TextField.NUMERIC);
        engine = e;
        addCommand(new Command("aS_STRING_OK", Command.OK, 1));
        addCommand(new Command("aS_STRING_BACK", Command.BACK, 2));
        setCommandListener(this);
    }

    public void commandAction(Command c, Displayable d)
    {
        switch (c.getCommandType())
        {
            case Command.OK:
                if (getString().length() > 6 || getString().length() == 0)
                {
                    wrongPage();
                }
                else
                {
                    int newPage = Integer.parseInt(getString()) - engine.getBook().getStartPage() - 1;
                    if (newPage < 0 || newPage >= engine.getBook().getPageCount())
                    {
                        wrongPage();
                    }
                    else
                    {
                        try
                        {
                            engine.getPager().setCurrentPage(newPage);
                        }
                        catch (Exception ex)
                        {
                            // TODO what to do?
                        }
                        engine.getDisplay().setCurrent(engine.getCanvas());
                    }
                }
                break;
            case Command.BACK:
                engine.getDisplay().setCurrent(engine.getCanvas());
                break;
        }
    }

    public void wrongPage()
    {
        engine.sendEvent(new Event(Event.MESSAGE, Event.MESSAGE_REVERT_CURRENT, this, null, "aS_STRING_WRONG_PAGE_TEXT"));
    }

} // end of class
