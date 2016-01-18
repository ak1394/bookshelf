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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author anton
 *  
 */
public class Page extends AbstractElement
{
    private List content;
    private LinkSet linkSet;

    public Page()
    {
        content = new ArrayList();
        linkSet = new LinkSet();
    }

    public void write(DataOutput output) throws Exception
    {
        for (Iterator iterator = content.iterator(); iterator.hasNext();)
        {
            ((AbstractElement) iterator.next()).write(output);
        }
        linkSet.write(output);
    }

    public void visit(AbstractVisitor visitor) throws Exception
    {
        for (Iterator iterator = content.iterator(); iterator.hasNext();)
        {
            ((AbstractElement) iterator.next()).visit(visitor);
        }
    }

    public void add(AbstractElement element)
    {
        content.add(element);
    }

    public LinkSet getLinkSet()
    {
        return linkSet;
    }

    public Iterator getContentIterator()
    {
        return content.iterator();
    }

    public boolean isEmpty()
    {
        return content.size() == 0 ? true : false;
    }
}