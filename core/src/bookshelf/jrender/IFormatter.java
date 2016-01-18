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

import bookshelf.jrender.element.*;

/**
 * @author Anton Krasovsky <ak1394@mail.ru>
 *  
 */
public interface IFormatter
{
    public void setWidth(int width);

    /**
     * Returns a List of Strings that will fit into one line when rendered in
     * specified font.
     * 
     * @return list of ???? objects
     */
    public Line format(Paragraph para) throws Exception;

    /**
     * Returns a List of Strings that will fit into one line when rendered in
     * specified font.
     * 
     * @param indent
     *            an indentation space for a resulting line
     * @return list of ???? objects
     */
    public Line format(Paragraph para, int indent) throws Exception;
}