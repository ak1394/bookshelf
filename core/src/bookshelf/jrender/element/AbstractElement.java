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
import java.util.ListIterator;
import java.util.LinkedList;

import bookshelf.jrender.AbstractVisitor;

/**
 * @author Anton Krasovsky <ak1394@mail.ru>
 *  
 */
public abstract class AbstractElement implements Cloneable, IElement
{
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#clone()
     */
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

    /**
     * @return Returns the height.
     */
    public int getHeight()
    {
        return height;
    }

    /**
     * @param height
     *            The height to set.
     */
    public void setHeight(int height)
    {
        this.height = height;
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
    private IElement parent;
    protected LinkedList children;
    private int width;
    private int height;

    /**
     * @return Returns the parent.
     */
    public IElement getParent()
    {
        return parent;
    }

    /**
     * @param parent
     *            The parent to set.
     */
    public void setParent(IElement parent)
    {
        this.parent = parent;
    }

    public abstract void visit(AbstractVisitor visitor) throws Exception;

    public void visitLeaf(AbstractVisitor visitor) throws Exception
    {
        if (hasChildren())
        {
            for (Iterator iterator = childIterator(); iterator.hasNext();)
            {
                ((IElement) iterator.next()).visitLeaf(visitor);
            }
        } else
        {
            visit(visitor);
        }
    }

    public void visitAll(AbstractVisitor visitor) throws Exception
    {
        visit(visitor);
        if (hasChildren())
        {
            for (Iterator iterator = childIterator(); iterator.hasNext();)
            {
                ((IElement) iterator.next()).visitAll(visitor);
            }
        }
    }

    public boolean hasChildren()
    {
        return children == null ? false : true;
    }

    public ListIterator childIterator()
    {
        return children.listIterator();
    }

    public void removeChild(IElement element)
    {
        children.remove(element);
    }
}