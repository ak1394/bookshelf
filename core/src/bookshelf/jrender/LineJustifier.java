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

import java.util.ArrayList;
import java.util.Iterator;

import bookshelf.jrender.element.IElement;
import bookshelf.jrender.element.Line;
import bookshelf.jrender.element.Space;

/**
 * @author Anton Krasovsky <ak1394@mail.ru>
 *  
 */
public class LineJustifier extends AbstractVisitor implements ILineDecorator
{
    private ArrayList spaceList;
    private int width;

    public LineJustifier()
    {
        spaceList = new ArrayList();
    }

    public Line decorate(Line line) throws Exception
    {
        int lineWidth = line.getWidth();
        int freeSpace = width - lineWidth;
        if (freeSpace > 0)
        {
            for (Iterator iterator = line.childIterator(); iterator.hasNext();)
            {
                ((IElement) iterator.next()).visit(this);
            }

            if (spaceList.size() > 0)
            {
                int perSpace = freeSpace / spaceList.size();
                int lastSpace = perSpace + freeSpace % spaceList.size();
                for (int i = 0; i < spaceList.size() - 1; i++)
                {
                    Space s = (Space) spaceList.get(i);
                    s.setWidth(s.getWidth() + perSpace);
                }
                Space s = (Space) spaceList.get(spaceList.size() - 1);
                s.setWidth(s.getWidth() + lastSpace);
                spaceList.clear();
            }
        }
        return line;
    }

    /*
     * (non-Javadoc)
     * 
     * @see bookshelf.jrender.ILineDecorator#addDecorator(bookshelf.jrender.ILineDecorator)
     */
    public void addDecorator(ILineDecorator decorator)
    {
        // TODO Auto-generated method stub
    }

    public void visitSpace(Space space) throws Exception
    {
        if (space.isResizable())
        {
            spaceList.add(space);
        }
    }

    /**
     * @return Returns the width.
     */
    public int getWidth()
    {
        return width;
    }

    /**
     * @param width
     *            The width to set.
     */
    public void setWidth(int width)
    {
        this.width = width;
    }
}