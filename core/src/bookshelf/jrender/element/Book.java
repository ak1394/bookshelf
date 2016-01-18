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

import java.util.Iterator;

import bookshelf.book.BookWriter;
import bookshelf.jrender.AbstractVisitor;

/**
 * @author Anton Krasovsky <ak1394@mail.ru>
 *  
 */
public class Book extends AbstractElementContainer
{
    private int interlineSpacing;

    public Book()
    {
        super();
    }

    public void add(Page page)
    {
        children.add(page);
    }

    public void write(BookWriter bookWriter) throws Exception
    {
        bookWriter.setInterlineSpacing(interlineSpacing);
        // write pages
        for (Iterator i = childIterator(); i.hasNext();)
        {
            ((Page) i.next()).write(bookWriter);
        }
        bookWriter.endBook();
    }

    public void visit(AbstractVisitor visitor) throws Exception
    {
        visitor.visitBook(this);
    }

    /**
     * @return Returns the interlineSpacing.
     */
    public int getInterlineSpacing()
    {
        return interlineSpacing;
    }

    /**
     * @param interlineSpacing
     *            The interlineSpacing to set.
     */
    public void setInterlineSpacing(int interlineSpacing)
    {
        this.interlineSpacing = interlineSpacing;
    }
}

