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

package bookshelf.book.element;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import bookshelf.font.Font;

public class Book
{
    private Dimension pageSize;
    private ArrayList fonts;
    private ArrayList pages;
    private int interlineSpacing;
    private String title;
    private HashMap idMap;

    public Book(Dimension pageSize)
    {
        this.pageSize = pageSize;
        fonts = new ArrayList();
        pages = new ArrayList();
        idMap = new HashMap();
    }

    /**
     * @return Returns the title.
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * @param title
     *            The title to set.
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

    public void addPage(Page page)
    {
        pages.add(page);
    }

    public void addFont(Font font)
    {
        fonts.add(font);
    }

    public Dimension getPageSize()
    {
        return pageSize;
    }

    public List getFonts()
    {
        return fonts;
    }

    public List getPages()
    {
        return pages;
    }
    
    public int getPageNumber(Page page)
    {
        int pageNumber = pages.indexOf(page);
        return pageNumber;
    }

    public int getPreferredLineHeight()
    {
        int preferredLineHeight = ((Font) fonts.get(0)).getHeight() + getInterlineSpacing();
        return preferredLineHeight;
    }
    
    public void setPageIdList(ArrayList idList, Page page)
    {
        for(Iterator i = idList.iterator(); i.hasNext();)
        {
            String id = (String) i.next();
            idMap.put(id, page);
        }
    }
    
    public Map getIdMap()
    {
        return idMap;
    }

    /**
     * @return Returns the interline spacing.
     */
    public int getInterlineSpacing()
    {
        return interlineSpacing;
    }

    /**
     * @param interline
     *            The interline spacing to set.
     */
    public void setInterlineSpacing(int interlineSpacing)
    {
        this.interlineSpacing = interlineSpacing;
    }
}