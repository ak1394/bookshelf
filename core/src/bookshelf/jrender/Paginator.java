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

import java.util.List;
import java.util.Iterator;

import bookshelf.book.BookWriter;
import bookshelf.jrender.element.Book;
import bookshelf.jrender.element.IElement;
import bookshelf.jrender.element.Page;

/**
 * @author Anton Krasovsky <ak1394@mail.ru>
 *  
 */
public class Paginator
{
    private ParagraphSource paragraphSource;
    private IFormatter formatter;
    private int pageWidth;
    private int pageHeight;
    private int currentX;

    private List lineList;
    private ILineDecorator lineDecorator;
    private Book book;
    private int interline = 0;

    public void write(BookWriter bookWriter) throws Exception
    {
        book = new Book();
        pageWidth = bookWriter.getPageSize().width;
        pageHeight = bookWriter.getPageSize().height;

        book.setInterlineSpacing(interline);

        Page page = new Page();
        Paragraph paragraph = paragraphSource.next();
        while (paragraph != null)
        {
            paragraph.setLineDecorator(lineDecorator);
            lineList = paragraph.format(formatter);
            for (Iterator i = lineList.iterator(); i.hasNext();)
            {
                IElement element = (IElement) i.next();
                element.setHeight(element.getHeight() + interline);

                if (page.getHeight() + element.getHeight() <= pageHeight)
                {
                    page.add(element);
                } else
                {
                    book.add(page);
                    page = new Page();
                    page.add(element);
                }
            }
            paragraph = paragraphSource.next();
        }

        if (page.hasChildren())
        {
            book.add(page);
        }

        book.write(bookWriter);
    }

    /**
     * @return Returns the formatter.
     */
    public IFormatter getFormatter()
    {
        return formatter;
    }

    /**
     * @param formatter
     *            The formatter to set.
     */
    public void setFormatter(IFormatter formatter)
    {
        this.formatter = formatter;
    }

    /**
     * @return Returns the paragraphSource.
     */
    public ParagraphSource getParagraphSource()
    {
        return paragraphSource;
    }

    /**
     * @param paragraphSource
     *            The paragraphSource to set.
     */
    public void setParagraphSource(ParagraphSource paragraphSource)
    {
        this.paragraphSource = paragraphSource;
    }

    /**
     * @param lineDecorator
     */
    public void setDecorator(ILineDecorator lineDecorator)
    {
        this.lineDecorator = lineDecorator;
    }

    /**
     * @param interline
     */
    public void setInterline(int interline)
    {
        this.interline = interline;
    }

}