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

package bookshelf.book;

import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import bookshelf.book.element.Book;
import bookshelf.book.element.Image;
import bookshelf.book.element.Link;
import bookshelf.book.element.Page;
import bookshelf.book.element.Rectangle;
import bookshelf.book.element.Space;
import bookshelf.book.element.Word;
import bookshelf.font.Font;

/**
 * @author anton
 *  
 */
public class BookWriter
{
    private Book book;
    private Page currentPage;
    private Font currentFont;
    private int position;

    public BookWriter(Dimension pageSize)
    {
        book = new Book(pageSize);
    }

    public void newPage() throws Exception
    {
        if (currentPage != null)
        {
            book.addPage(currentPage);
        }
        position = 0;
        currentPage = new Page();
    }

    public void endBook()
    {
        if (!currentPage.isEmpty())
        {
            book.addPage(currentPage);
        }

        // set link destinations

        for (Iterator i = book.getPages().iterator(); i.hasNext();)
        {
            Map idMap = book.getIdMap();
            Page page = (Page) i.next();
            for (Iterator j = page.getLinkSet().getLinks().iterator(); j.hasNext();)
            {
                Link link = (Link) j.next();
                if (idMap.containsKey(link.getDestinationId()))
                {
                    Page destinationPage = (Page) idMap.get(link.getDestinationId());
                    int pageNumber = book.getPageNumber(destinationPage);
                    link.setDestinationPage(pageNumber);
                }
                else
                {
                    // FIXME unresolved link, complain?
                }
            }
        }
    }

    public Book getBook()
    {
        return book;
    }

    public void writeString(String string) throws Exception
    {
        Word word = new Word(string, currentFont);
        currentPage.add(word);
        position = position + currentFont.stringWidth(string);
    }

    public void writeSpace(int size) throws IOException
    {
        Space space = new Space(size);
        currentPage.add(space);
        position = position + size;
    }

    public void writeVerticalSpace(int size) throws IOException
    {
        writeSpace(size * book.getPageSize().width);
    }

    public void writeResetSpace() throws IOException
    {
        int size = (position % book.getPageSize().width) * -1;
        if (size == 0 && position > 0)
        {
            size = book.getPageSize().width * -1;
        }
        writeSpace(size);
    }

    public void writeRectangle(int x, int y, int width, int height) throws IOException
    {
        Rectangle rectangle = new Rectangle(x, y, width, height);
        currentPage.add(rectangle);
    }

    public void writeImage(int x, int y, byte[] data) throws IOException
    {
        Image image = new Image(x, y, data);
        currentPage.add(image);
    }

    public void writeLink(Link link) throws IOException
    {
        currentPage.getLinkSet().addLink(link);
    }

    public void setPageIdList(ArrayList idList)
    {
        book.setPageIdList(idList, currentPage);
    }

    public void setFont(Font font)
    {
        currentFont = font;
        book.addFont(font);
    }

    public Dimension getPageSize()
    {
        return book.getPageSize();
    }

    /**
     * @param interline
     *            The interline spacing to set.
     */
    public void setInterlineSpacing(int interlineSpacing)
    {
        book.setInterlineSpacing(interlineSpacing);
    }
}