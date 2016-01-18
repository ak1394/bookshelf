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
public class Page extends AbstractElementContainer
{
    public Page()
    {
        super();
    }

    public int getWidth()
    {
        int width = 0;
        for (Iterator i = childIterator(); i.hasNext();)
        {
            int elementWidth = ((AbstractElement) i.next()).getWidth();
            if (elementWidth > width)
            {
                width = elementWidth;
            }
        }

        return width;
    }

    public void add(IElement element)
    {
        children.add(element);
        setHeight(getHeight() + element.getHeight());
    }

    public String toString()
    {
        String result = "{\n";

        for (Iterator i = childIterator(); i.hasNext();)
        {
            result = result + i.next().toString() + "\n";
        }
        result = result + "}\n";

        return result;
    }

    public void write(BookWriter bookWriter) throws Exception
    {
        bookWriter.newPage();
        for (Iterator i = childIterator(); i.hasNext();)
        {
            ((IElement) i.next()).write(bookWriter);
        }
    }

    public void visit(AbstractVisitor visitor) throws Exception
    {
        visitor.visitPage(this);
    }
}