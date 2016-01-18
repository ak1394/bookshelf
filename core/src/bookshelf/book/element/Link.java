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

import java.awt.Rectangle;
import java.io.DataOutput;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author anton
 *  
 */
public class Link
{
    private String destinationId;
    private int destinationPage;
    private List rectangles = new ArrayList();

    public Link(String destinationId)
    {
        this.destinationId = destinationId;
    }

    public void addRectangle(int x, int y, int width, int height)
    {
        rectangles.add(new Rectangle(x, y, width, height));
    }

    public Rectangle[] getRectangles()
    {
        Rectangle[] result = new Rectangle[rectangles.size()];
        return (Rectangle[]) rectangles.toArray(result);
    }

    public String getDestinationId()
    {
        return destinationId;
    }

    public int getDestinationPage()
    {
        return destinationPage;
    }

    public void setDestinationPage(int destinationPage)
    {
        this.destinationPage = destinationPage;
    }

    /**
     * @param output
     */
    public void write(DataOutput output) throws Exception
    {
        output.writeShort(destinationPage);
        output.writeByte(rectangles.size());

        for(Iterator iterator = rectangles.iterator(); iterator.hasNext();)
        {
            Rectangle rectangle = (Rectangle) iterator.next();
            output.writeShort((int) rectangle.getX());
            output.writeShort((int) rectangle.getY());
            output.writeShort((int) rectangle.getWidth());
            output.writeShort((int) rectangle.getHeight());
        }
    }
}