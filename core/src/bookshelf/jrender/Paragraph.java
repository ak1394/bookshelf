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
import java.util.List;
import java.util.StringTokenizer;

import bookshelf.font.Font;
import bookshelf.jrender.element.AbstractElement;
import bookshelf.jrender.element.Line;
import bookshelf.jrender.element.Word;

/**
 * @author Anton Krasovsky <ak1394@mail.ru>
 *  
 */

public class Paragraph
{
    private ArrayList body;
    private String delimiters = "\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\u0008\u0009 \t\n\r\f";
    private int indent;
    private Font font;
    private ILineDecorator lineDecorator;

    public Paragraph(String content, Font font, int indent)
    {
        this.indent = indent;
        this.font = font;
        body = new ArrayList();
        StringTokenizer tokenizer = new StringTokenizer(content, delimiters, false);

        while (tokenizer.hasMoreTokens())
        {
            body.add(new Word(tokenizer.nextToken(), font));
        }
    }

    public Iterator iterator()
    {
        return body.iterator();
    }

    public void pushElement(AbstractElement element)
    {
        body.add(0, element);
    }

    public List format(IFormatter formatter) throws Exception
    {
        ArrayList result = new ArrayList();
        Line line = formatter.format(this, indent);
        do
        {
            if (lineDecorator != null)
            {
                lineDecorator.decorate(line);
            }
            result.add(line);
        } while ((line = formatter.format(this)) != null);
        return result;
    }

    /**
     * @return Returns the indent.
     */
    public int getIndent()
    {
        return indent;
    }

    /**
     * @param indent
     *            The indent to set.
     */
    public void setIndent(int indent)
    {
        this.indent = indent;
    }

    /**
     * @return Returns the lineDecorator.
     */
    public ILineDecorator getLineDecorator()
    {
        return lineDecorator;
    }

    /**
     * @param lineDecorator
     *            The lineDecorator to set.
     */
    public void setLineDecorator(ILineDecorator lineDecorator)
    {
        this.lineDecorator = lineDecorator;
    }

    public String toString()
    {
        String s = "";

        for (Iterator i = body.iterator(); i.hasNext();)
        {
            s = s + i.next().toString();
        }
        return s;
    }
}