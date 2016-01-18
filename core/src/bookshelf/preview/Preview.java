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

import java.awt.image.BufferedImage;

import reader.Renderer;
import reader.Slice;
import bookshelf.book.element.Book;
import bookshelf.font.Font;

/**
 * @author anton
 *
 */
public class Preview 
{
	private Renderer renderer;
	private int current = 0;
	private BufferedImage currentImage;
	private MemoryBook memoryBook;
	private PreviewRenderer previewRenderer;
	
	public Preview(Book book) throws Exception
	{
	    Font font = (Font) book.getFonts().get(0);
	    memoryBook = new MemoryBook(book);
	    previewRenderer = new PreviewRenderer();
	    previewRenderer.bufferInitialize(memoryBook.getViewportWidth(), memoryBook.getViewportHeight(), 0);
        previewRenderer.setFont(font);
		renderer = new Renderer(previewRenderer, 0);
        renderer.setBackground(font.getBackground().getRGB());
		renderer.setBook(memoryBook);
	}
	
	public BufferedImage next() throws Exception
	{
	    if(current+1 < memoryBook.getPageCount())
	    {
			current++;
	    }
	    
	    return current();
	}
	
	public BufferedImage previous() throws Exception
	{
	    if(current > 0)
	    {
			current--;
	    }

	    return current();
	}

	public BufferedImage current() throws Exception
	{
		Slice slice = (Slice) renderer.renderSlice(current);
		currentImage = ((PreviewPlatformSlice)slice).image;
		return currentImage;
	}
}
