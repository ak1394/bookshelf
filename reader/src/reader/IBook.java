/*-
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

package reader;

/**
 * The <code>IBook</code> interface is the abstraction of the book data that
 * is available to plugins. Amongst the usual parameters, there is the notion of
 * a viewport. I had to invent it to perform clipping of the screen area, to
 * prevent display of the partially shown lines of the text. Viewport height
 * should be the multiple of the line height. Different books, depending on the
 * font used, can have different viewport sizes.
 * 
 * @author Anton Krasovsky <ak1394@users.sourceforge.net>
 *  
 */
public interface IBook
{
    /**
     * Returns the viwport width of this Book.
     * 
     * @return the viewport width in pixels
     */
    public int getViewportWidth();

    /**
     * Returns the viwport height of this Book.
     * 
     * @return the viewport height in pixels
     */
    public int getViewportHeight();

    /**
     * Returns the page height of this Book.
     * 
     * @return the page height in pixels
     */
    public int getPageHeight();

    /**
     * Returns the page width of this Book.
     * 
     * @return the page width in pixels
     */
    public int getPageWidth();

    /**
     * Returns the number of pages in this Book.
     * 
     * @return the number of pages starting from 0
     */
    public int getPageCount();

    /**
     * Since the text can be too big to fit into the single MIDlet, it can be
     * split into srveral parts. <code>getPageCount</code> returns number of
     * the starting page in this book.
     * 
     * @return starting page number, starting from 0
     */
    public int getStartPage();

    /**
     * Book can have more than one font, so the notion of line height can be
     * pretty vague. This method returns value that most likely represents line
     * height. This value is used to calculate offset of one-line scroll.
     * 
     * @return the most common line height throughout the book
     */
    public int getPreferredLineHeight();
    
    /**
     * Returns book background color;
     * 
     * @return rgb color
     */
    public int getColor();
    
}