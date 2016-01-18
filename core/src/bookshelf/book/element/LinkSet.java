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

import reader.Renderer;

/**
 * @author anton
 *  
 */
public class LinkSet extends AbstractElement
{
    private List links;

    public LinkSet()
    {
        links = new ArrayList();
    }

    public void addLink(Link link)
    {
        links.add(link);
    }
    
    public List getLinks()
    {
        return links;
    }

    public void write(DataOutput output) throws Exception
    {
        if (links.size() > 0)
        {
            output.writeByte(Renderer.COMMAND);
            output.writeByte(Renderer.LINK_SET);
            output.writeShort(links.size());
            for (Iterator iterator = links.iterator(); iterator.hasNext();)
            {
                ((Link) iterator.next()).write(output);
            }
        }
    }
}