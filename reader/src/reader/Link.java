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
 * Represents a hypertext link. Contains number of rectangle coordinates (used
 * to highlight link on the page) and destination page number.
 * 
 * @author Anton Krasovsky <ak1394@users.sourceforge.net>
 *  
 */
public class Link
{
    public int destination;
    public int page;
    public int id;
    public int rectangleCount;
    public int x[];
    public int y[];
    public int width[];
    public int height[];
    
    public int offset[]; // offset in byte buffer for each highlighted word

    /**
     * Initializes link. Allocates memory for rectangle coordinates.
     * 
     * @param destination
     *            destination page
     * @param page
     *            page where link is located
     * @param id
     *            link id
     * @param count
     *            number of rectangles
     */
    public Link(int destination, int page, int id, int count)
    {
        this.destination = destination;
        this.page = page;
        this.id = id;
        this.rectangleCount = count;
        x = new int[count];
        y = new int[count];
        width = new int[count];
        height = new int[count];
    }
}