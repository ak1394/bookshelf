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
public class Line extends AbstractElementContainer
{
    public Line()
    {
        super();
    }

    public int getWidth()
    {
        int width = 0;
        for (Iterator i = childIterator(); i.hasNext();)
        {
            width = width + ((IElement) i.next()).getWidth();
        }

        return width;
    }

    public void add(IElement element)
    {
        children.add(element);
        if (getHeight() < element.getHeight())
        {
            setHeight(element.getHeight());
        }
    }

    public String toString()
    {
        String result = "[";

        for (Iterator i = childIterator(); i.hasNext();)
        {
            result = result + "'" + i.next().toString() + "'" + ",";
        }
        result = result + "]";

        return result;
    }

    public void write(BookWriter bookWriter) throws Exception
    {
        for (Iterator i = childIterator(); i.hasNext();)
        {
            ((IElement) i.next()).write(bookWriter);
        }
        bookWriter.writeResetSpace();
        bookWriter.writeVerticalSpace(getHeight());
    }

    public void visit(AbstractVisitor visitor) throws Exception
    {
        visitor.visitLine(this);
    }
}