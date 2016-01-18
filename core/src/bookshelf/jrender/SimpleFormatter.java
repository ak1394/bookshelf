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

import java.util.Iterator;

import bookshelf.jrender.element.*;

/**
 * @author Anton Krasovsky <ak1394@mail.ru>
 *  
 */
public class SimpleFormatter extends AbstractVisitor implements IFormatter
{
    private int width;
    private int space;
    private int margin = 0;

    private Splitter splitter;

    public SimpleFormatter()
    {
        splitter = new Splitter();
    }

    /**
     * Returns a List of Strings that will fit into one line when rendered in
     * specified font.
     * 
     * @return Line
     */
    public Line format(Paragraph para) throws Exception
    {
        return format(para, 0);
    }

    /**
     * Returns a List of Strings that will fit into one line when rendered in
     * specified font.
     * 
     * @param indent
     *            an indentation space for a resulting line
     * @return list of TextChunk objects
     */
    public Line format(Paragraph para, int indent) throws Exception
    {
        assert width > 0;
        assert space > 0;

        int length = indent + margin;
        Line line = new Line();
        line.add(new Space(indent + margin, false));

        // First word in line, check if it fits at all, if not then break it up
        {
            Iterator i = para.iterator();
            if (i.hasNext())
            {
                // if there is a word, we'll put at least part of it to result
                // so, remove from paragraph
                AbstractElement element = (AbstractElement) i.next();
                i.remove();
                int nlen = length + element.getWidth();
                if (nlen > width)
                {
                    // First word of paragraph is too big break it up
                    AbstractElement el[] = splitter.split(element, width - length);
                    length = length + el[0].getWidth();
                    line.add(el[0]);
                    para.pushElement(el[1]);
                    return line;
                } else
                {
                    length = nlen;
                    line.add(element);
                }
            } else
            {
                // paragraph is empty
                return null;
            }
        }

        // Each other next word

        for (Iterator i = para.iterator(); i.hasNext();)
        {
            AbstractElement element = (AbstractElement) i.next();
            // add text to result until there is a space left
            int nlen = length + space + element.getWidth();
            if (nlen <= width)
            {
                line.add(new Space(space));
                line.add(element);
                i.remove();
                length = nlen;
            } else
            {
                break;
            }
        }

        if (!para.iterator().hasNext())
        {
            // this is a last line in paragraph make all spaces not resizable
            for (Iterator iterator = line.childIterator(); iterator.hasNext();)
            {
                ((IElement) iterator.next()).visit(this);
            }
        }

        return line;
    }

    /**
     * @param space
     *            The space to set.
     */
    public void setSpace(int space)
    {
        this.space = space;
    }

    /**
     * @param width
     *            The width to set.
     */
    public void setWidth(int width)
    {
        this.width = width;
    }

    /**
     * @return Returns the space.
     */
    public int getSpace()
    {
        return space;
    }

    /**
     * @return Returns the width.
     */
    public int getWidth()
    {
        return width;
    }

    public void visitSpace(Space space) throws Exception
    {
        // called on the last line of a paragraph
        space.setResizable(false);
    }

    /**
     * @return Returns the margin.
     */
    public int getMargin()
    {
        return margin;
    }

    /**
     * @param margin
     *            The margin to set.
     */
    public void setMargin(int margin)
    {
        this.margin = margin;
    }
}