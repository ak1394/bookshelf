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

import bookshelf.book.BookWriter;
import bookshelf.font.Font;
import bookshelf.jrender.AbstractVisitor;

/**
 * @author Anton Krasovsky <ak1394@mail.ru>
 *  
 */
public class Word extends AbstractElement
{
    private Font font;
    private String content;

    public Word(String content, Font font)
    {
        this.content = content;
        this.font = font;
    }

    public String toString()
    {
        return content;
    }

    /*
     * (non-Javadoc)
     * 
     * @see bookshelf.jrender.Element#setWidth(int)
     */
    public void setWidth(int width)
    {
        // should throw an exception here
    }

    /*
     * (non-Javadoc)
     * 
     * @see bookshelf.jrender.Element#getWidth()
     */
    public int getWidth()
    {
        return font.stringWidth(content);
    }

    public int width(String s)
    {
        return font.stringWidth(s);
    }

    /*
     * (non-Javadoc)
     * 
     * @see bookshelf.jrender.Element#visit(bookshelf.jrender.ElementVisitor)
     */
    public void visit(AbstractVisitor visitor) throws Exception
    {
        visitor.visitWord(this);
    }

    /**
     * @return Returns the string content of the word content.
     */
    public String getContent()
    {
        return content;
    }

    /**
     * @param content
     *            The content string to set.
     */
    public void setContent(String content)
    {
        this.content = content;
    }

    public void setHeight(int height)
    {
        // do nothing
    }

    public int getHeight()
    {
        return font.getHeight();
    }

    public void write(BookWriter bookWriter) throws Exception
    {
        if (content.length() != 0)
        {
            bookWriter.writeString(content);
        }
    }
}