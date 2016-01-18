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

package bookshelf.jrender;

import java.util.*;

/**
 * Splits input string into letter strings and delimeters.
 * Delimeter string can contain several characters.
 */
public class Tokenizer implements Enumeration
{
    private String input;
    /**
     * Current position in parsed string.
     */
    private int position;
    
    /**
     * @param input String to split.
     */
    public Tokenizer(String input)
    {
        this.input = input;
        position = 0;
    }

    public boolean hasMoreElements()
    {
        return position < input.length();
    }

    public Object nextElement() throws NoSuchElementException
    {
        if (!hasMoreElements()) {
            throw new NoSuchElementException();
        }
        String token = "";
        boolean tokenType = Character.isLetter(input.charAt(position));
        while (position < input.length() && tokenType == Character.isLetter(input.charAt(position))) {
            token += input.charAt(position);
            position++;
        }
        return token;
    }
}
