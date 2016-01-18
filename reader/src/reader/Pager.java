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

import java.util.Vector;

import javax.microedition.lcdui.*;

/**
 * Displays page of the book using platform canvas.
 * 
 * @author Anton Krasovsky <ak1394@users.sourceforge.net>
 *  
 */
public class Pager
{
    private IPlatformCanvas platformCanvas;
    private ActiveCache cache;
    private Engine engine;

    private int pageCount;
    private int viewportHeight;
    private int pageHeight;

    private int pixelHeight;
    private int offset;

    /**
     * Creates <code>Pager</code> instance.
     * 
     * @param platformCanvas
     *            platform canvas
     * @param cache
     *            page cache
     * @param engine
     *            engine
     */
    public Pager(IPlatformCanvas platformCanvas, ActiveCache cache, Engine engine)
    {
        this.platformCanvas = platformCanvas;
        this.cache = cache;
        this.engine = engine;
        this.offset = 0;
    }

    /**
     * Tests whether specified link (or rather any of it's rectangles) is
     * visible.
     * 
     * @param link
     *            link to test
     * @return <code>true</code> if link is visible <code>false</code>
     *         otherwise
     */
    public boolean isLinkVisible(Link link)
    {
        int linkTop = link.page * pageHeight + link.y[0];
        int linkBottom = link.page * pageHeight + link.y[link.rectangleCount - 1]
                + link.height[link.rectangleCount - 1];

        return linkTop >= offset && linkBottom <= (offset + viewportHeight) ? true : false;
    }

    /**
     * Tests if specified page is visible.
     * 
     * @param page
     *            page number to test
     * @return <code>true</code> if page is visible <code>false</code>
     *         otherwise
     */
    public boolean isPageVisible(int page)
    {
        int pageTop = page * pageHeight;
        int pageBottom = pageTop + viewportHeight;

        return (pageTop <= offset && pageBottom > offset) || (pageTop >= offset && pageTop < offset + viewportHeight) ? true
                : false;
    }

    /**
     * Highlights link on display.
     * 
     * @param link
     *            link to highlight
     */
    public void highlight(Link link)
    {
        int o = offset % pageHeight + (pageHeight * (getCurrentPage() - link.page));
        for (int i = 0; i < link.rectangleCount; i++)
        {
            //System.out.println("link: " + link.x[i] + " " + (link.y[i] - o) +
            // " " + link.width[i] + " " + link.height[i]);
            platformCanvas.highlight(link.x[i], link.y[i] - o, link.width[i], link.height[i]);
        }
    }

    /**
     * Returns links from the current page.
     * 
     * @param page
     *            page to get links from
     * @return <code>Vector</code> of <code>Link</code> elements
     * @throws Exception
     */
    public Vector getLinks(int page) throws Exception
    {
        return cache.getSlice(page).links;
    }

    /**
     * Scrolls page up or down for number of pixels.
     * 
     * @param pixels
     *            number of pixels, negative value means scroll up (towards the
     *            start of the book)
     *  
     */

    public void scroll(int pixels) throws Exception
    {
        int realOffset = 0;

        if (pixels > 0)
        {
            // scroll down
            int maxOffset = pixelHeight - offset - viewportHeight;
            if (maxOffset > pixels)
            {
                realOffset = pixels;
            }
            else
            {
                realOffset = maxOffset;
            }
        }
        else
        {
            int maxOffset = -offset;
            if (maxOffset < pixels)
            {
                realOffset = pixels;
            }
            else
            {
                realOffset = maxOffset;
            }
        }
        this.offset = this.offset + realOffset;
        refresh();
        engine.sendEvent(new Event(Event.SCROLL, realOffset, null, null, null));
    }

    /**
     * Refreshes screen by refrawing the current page.
     * 
     * @throws Exception
     */
    public void refresh() throws Exception
    {
        platformCanvas.clearBuffer();
        int page = offset / pageHeight;
        int localOffset = -(offset % pageHeight);
        while (localOffset < viewportHeight)
        {
            Slice slice = cache.getSlice(page);
            if (localOffset + pageHeight > viewportHeight)
            {
                platformCanvas.sliceToBuffer(slice, 0, localOffset, viewportHeight - localOffset);
            }
            else
            {
                platformCanvas.sliceToBuffer(slice, 0, localOffset, pageHeight);
            }
            localOffset = localOffset + pageHeight;
            page++;
        }
    }

    /**
     * Draws current page.
     * 
     * @param g
     */
    public void paint(Graphics g)
    {
        platformCanvas.displayBuffer(g, 0, 0);
    }

    /**
     * Sets current page number and redraws the screen.
     * 
     * @param currentPage
     *            page number
     * @throws Exception
     */
    public void setCurrentPage(int currentPage) throws Exception
    {
        // TODO what if page is out of bounds???
        this.offset = this.pageHeight * currentPage;
        refresh();
        engine.sendEvent(new Event(Event.SCROLL, 0, null, null, null));
    }

    /**
     * Returns currrent page number.
     * 
     * @return current page
     */
    public int getCurrentPage()
    {
        return this.offset / this.pageHeight;
    }

    /**
     * Sets book parameters.
     * 
     * @param bookInfo instance of <code>IBook</code>
     */
    public void setBook(IBook bookInfo)
    {
        this.viewportHeight = bookInfo.getViewportHeight();
        this.pageHeight = bookInfo.getPageHeight();
        this.pageCount = bookInfo.getPageCount();
        this.pixelHeight = pageCount * pageHeight;
    }
}