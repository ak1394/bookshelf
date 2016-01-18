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
public class Space extends AbstractElement
{
    private int width;
    private int register = -1;

    public Space(int size)
    {
        this.width = size;
    }

    public void visit(AbstractVisitor visitor) throws Exception
    {
        visitor.visitSpace(this);
    }

    public void write(DataOutput output) throws Exception
    {
        if (register == -1)
        {
            output.writeByte(Renderer.COMMAND);
            output.writeByte(Renderer.SPACE);
            output.writeShort(width);
        } else
        {
            output.writeByte(register);
        }
    }

    /**
     * @return Returns the size.
     */
    public int getWidth()
    {
        return width;
    }

    /**
     * @param width
     *            The size to set.
     */
    public void setWidth(int width)
    {
        this.width = width;
    }

    /**
     * @param register
     */
    public void setRegister(int register)
    {
        this.register = register;
    }
}