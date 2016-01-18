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
public class HyphenatingFormatter extends SimpleFormatter implements IFormatter
{
    private Hyphenator hyphenator;

    public HyphenatingFormatter()
    {
        super();
    }

    /**
     * Returns a List of Strings that will fit into one line when rendered in
     * specified font, performs hyphenation as needed.
     * 
     * @param indent
     *            an indentation space for a resulting line
     * @return list of TextChunk objects
     */
    public Line format(Paragraph para, int indent) throws Exception
    {
        Line line = super.format(para, indent);
        if (line != null)
        {
            int width = super.getWidth() - line.getWidth() - super.getSpace();

            Iterator i = para.iterator();
            if (i.hasNext())
            {
                AbstractElement element = (AbstractElement) i.next();
                AbstractElement result[] = hyphenator.hyphenate(element, width);
                if (result[0].getWidth() != 0)
                {
                    // successfully hyphenated
                    i.remove();
                    line.add(new Space(getSpace()));
                    line.add(result[0]);
                    para.pushElement(result[1]);
                }
            }

            // make all spaces non-resizable in the last line
            if (!para.iterator().hasNext())
            {
                // this is a last line in paragraph make all spaces not
                // resizable
                for (Iterator iterator = line.childIterator(); iterator.hasNext();)
                {
                    ((IElement) iterator.next()).visit(this);
                }
            }
        }

        /*
         * if(line != null) System.out.println(line.getWidth() + " " +
         * line.toString());
         */
        return line;
    }

    /**
     * @param hyphenator
     *            The ParagraphHyphenator to use.
     */
    public void setHyphenator(Hyphenator hyphenator)
    {
        this.hyphenator = hyphenator;
    }
}