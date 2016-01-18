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

package bookshelf.preview;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.List;

import bookshelf.book.element.Book;
import bookshelf.book.element.Page;
import bookshelf.font.Font;


/**
 * @author Anton Krasovsky <ak1394@mail.ru>
 *
 */
public class MemoryBook extends reader.JarBook
{
    private Book book;
    
    public MemoryBook(Book book)
    {
        super(0);
        this.book = book;
        List bookFonts = book.getFonts();
        fonts = new String[bookFonts.size()];
        for(int i = 0; i < bookFonts.size(); i++)
        {
            fonts[i] = ((Font) bookFonts.get(i)).getName();
        }
    }

    /* (non-Javadoc)
     * @see reader.IBook#getPageCount()
     */
    public int getPageCount()
    {
        return book.getPages().size();
    }
    
    /* (non-Javadoc)
     * @see reader.IBook#getPageHeight()
     */
    public int getPageHeight()
    {
        return (int) book.getPageSize().getHeight();
    }
    
    /* (non-Javadoc)
     * @see reader.IBook#getPageWidth()
     */
    public int getPageWidth()
    {
        return (int) book.getPageSize().getWidth();
    }
    
    /* (non-Javadoc)
     * @see reader.IBook#getPreferredLineHeight()
     */
    public int getPreferredLineHeight()
    {
        return book.getPreferredLineHeight();
    }
    
    /* (non-Javadoc)
     * @see reader.IBook#getViewportHeight()
     */
    public int getViewportHeight()
    {
        return (int) book.getPageSize().getHeight();
    }
    
    /* (non-Javadoc)
     * @see reader.IBook#getViewportWidth()
     */
    public int getViewportWidth()
    {
        return (int) book.getPageSize().getWidth();
    }
    
    /* (non-Javadoc)
     * @see reader.JarBook#openBook(java.lang.String)
     */
    public void openBook(String bookid) throws Exception
    {
    }
    
    /* (non-Javadoc)
     * @see reader.JarBook#setCurrentPage(int)
     */
    public boolean setCurrentPage(int pageNumber) throws Exception
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos); 
        Page page = (Page) book.getPages().get(pageNumber);
        page.write(dos);
        blockPageContent = baos.toByteArray();
        currentPageStart = 0;
        currentPageEnd = blockPageContent.length;
        return true;
    }
    
    public List getFonts()
    {
        return book.getFonts();
    }
}
