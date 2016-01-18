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

package bookshelf.jrender.element;

import bookshelf.book.BookWriter;
import bookshelf.jrender.AbstractVisitor;

/**
 * @author Anton Krasovsky <ak1394@mail.ru>
 *  
 */
public class Space extends AbstractElement
{
    private boolean resizable;

    public Space(int width, boolean resizable)
    {
        this.resizable = resizable;
        setWidth(width);
    }

    public Space(int width)
    {
        this(width, true);
    }

    public String toString()
    {
        return isResizable() ? "<s" + getWidth() + ">" : "<s~" + getWidth() + ">";
    }

    /*
     * (non-Javadoc)
     * 
     * @see bookshelf.jrender.Element#visit(bookshelf.jrender.ElementVisitor)
     */
    public void visit(AbstractVisitor visitor) throws Exception
    {
        visitor.visitSpace(this);
    }

    public void write(BookWriter bookWriter) throws Exception
    {
        if (getWidth() > 0)
        {
            bookWriter.writeSpace(getWidth());
        }
    }

    /**
     * @return Returns the resizable.
     */
    public boolean isResizable()
    {
        return resizable;
    }

    /**
     * @param resizable
     *            The resizable to set.
     */
    public void setResizable(boolean resizable)
    {
        this.resizable = resizable;
    }
}