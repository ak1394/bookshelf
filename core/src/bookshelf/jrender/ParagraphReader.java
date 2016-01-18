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

package bookshelf.jrender;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ParagraphReader
{
    private final int ST_EMPTY = 1;
    private final int ST_FULL = 2;
    private int state;
    private BufferedReader reader;
    private String paraString;
    private String paraStart;

    public ParagraphReader(InputStreamReader input, int start) throws Exception
    {
        reader = new BufferedReader(input);
        state = ST_EMPTY;
        paraStart = "";
        for (int i = 0; i < start; i++)
        {
            paraStart = paraStart + " ";
        }
    }

    public String read() throws Exception
    {

        String s;
        String result = null;

        while ((s = reader.readLine()) != null)
        {
            switch (state)
            {
                case ST_EMPTY:
                    // verify that string is empty
                    if (s.trim().equals(""))
                    {
                        // do nothing
                        break;
                    }
                    // store and go to full
                    paraString = s.trim();
                    state = ST_FULL;
                    break;
                case ST_FULL:
                    if (s.trim().equals(""))
                    {
                        result = paraString.toString();
                        paraString = null;
                        state = ST_EMPTY;
                        break;
                    }
                    if (s.startsWith(paraStart))
                    {
                        result = paraString.toString();
                        paraString = s.trim();
                        break;
                    }
                    paraString = paraString + " " + s.trim();
                    break;
            }

            if (result != null)
                return result;
        }

        if (state == ST_FULL)
        {
            state = ST_EMPTY;
            return paraString;
        } else
        {
            return null;
        }
    }

} // end of class
