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

import javax.microedition.lcdui.*;
import reader.Engine;

public class Menu extends List implements CommandListener
{
    private Engine engine;

    public Menu(Engine engine)
    {
        super("aS_STRING_SEL_TITLE", Choice.IMPLICIT, engine.getTitles(), null);
        this.engine = engine;
        insert(0, "aS_STRING_BACK", null);
        setCommandListener(this);
    }

    public void commandAction(Command c, Displayable d)
    {
        int i = getSelectedIndex();
        if (i == 0)
        {
            engine.getDisplay().setCurrent(engine.getCanvas());
        }
        else
        {
            engine.getDisplay().setCurrent(null);
            engine.setContext(getSelectedIndex() - 1);
        }
    }
} 
