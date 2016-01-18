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

package bookshelf.book.element;

import java.io.DataOutput;

import reader.Renderer;

/**
 * @author anton
 *  
 */
public class Rectangle extends AbstractElement
{
    int x;
    int y;
    int width;
    int height;

    /**
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public Rectangle(int x, int y, int width, int height)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void write(DataOutput output) throws Exception
    {
        output.writeByte(Renderer.COMMAND);
        output.writeByte(Renderer.RECTANGLE);
        output.writeShort(x);
        output.writeShort(y);
        output.writeShort(width);
        output.writeShort(height);
    }
}