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

import java.util.Stack;
import java.util.Vector;

/**
 * Class <code>Renderer</code> reads page contents and draws image of the page
 * into a buffer with help of {@link reader.IPlatformRenderer}.
 * 
 * @author Anton Krasovsky <ak1394@users.sourceforge.net>
 * 
 */
public class Renderer
{
    public static final int R0 = 0;
    public static final int R1 = 1;
    public static final int R2 = 2;
    public static final int R3 = 3;
    public static final int R4 = 4;
    public static final int R5 = 5;
    public static final int R6 = 6;
    public static final int R7 = 7;
    public static final int R8 = 8;
    public static final int R9 = 9;

    public static final int COMMAND = 10;
    public static final int SPACE = 11;
    public static final int RECTANGLE = 12;
    public static final int LINK_SET = 13;
    public static final int IMAGE = 14;

    // new
    public static final int SET_POSITION = 15;
    public static final int SET_SPACE_AJUST = 16;
    
    private static final int SPACE_AJUST_DIVIDER = 100;

    private IPlatformRenderer platformRenderer;
    private JarBook jarBook;
    Stack recycledSlices;

    private boolean reportWords = false;
    private int fontHeight;
    private int color;

    /**
     * Creates <code>Renderer</code> object. Initializes internal cache with
     * specified number of <code>Slice</code> objects.
     * 
     * @param platformRenderer
     *            platform renderer
     * @param cacheSize
     *            size of <code>Slice</code> cache
     * @throws Exception
     */
    public Renderer(IPlatformRenderer platformRenderer, int cacheSize) throws Exception
    {
        this.platformRenderer = platformRenderer;
        // prepare pool of slices, to make
        // most memory expensive allocations at the start
        recycledSlices = new Stack();
        recycledSlices.ensureCapacity(cacheSize);
        for (int i = 0; i < cacheSize; i++)
        {
            Slice slice = platformRenderer.makeSlice();
            slice.links = new Vector();
            slice.words = new Vector();
            recycledSlices.push(slice);
        }
    }

    /**
     * Sets the page source.
     * 
     * @param jarBook
     *            <code>JarBook</code> instance
     * @throws Exception
     */
    public void setBook(JarBook jarBook) throws Exception
    {
        this.jarBook = jarBook;
        platformRenderer.setFont(jarBook.fonts[0]);
        fontHeight = platformRenderer.getFontHeight(jarBook.fonts[0]);
    }

    /**
     * Sets background color.
     * 
     * @param r
     *            red component
     * @param g
     *            green component
     * @param b
     *            blue component
     */
    public void setBackground(int color)
    {
        this.color = color;
    }

    /**
     * Renders image of a specified page.
     * 
     * @param page
     *            number of page to render
     * @return page image in platform-dependant format
     * @throws Exception
     */
    public synchronized Slice renderSlice(int page) throws Exception
    {
        Slice slice;
        int x = 0, y = 0, currentX = 0;
        int spaceAjust = 0, runningSpaceAjust = 0;
        int wordStartX = 0, wordStartY = 0;
        int wordOffset = 0;
        boolean insideOfWord = false;
        byte[] text;

        if (recycledSlices.empty())
        {
            slice = platformRenderer.makeSlice();
            slice.links = new Vector();
            slice.words = new Vector();
        }
        else
        {
            slice = (Slice) recycledSlices.pop();
        }

        platformRenderer.setSlice(slice);
        platformRenderer.setColor(color);
        platformRenderer.bufferClear();

        if (!jarBook.setCurrentPage(page))
        {
            return null;
        }

        text = JarBook.blockPageContent;
        for (int i = jarBook.currentPageStart; i < jarBook.currentPageEnd;)
        {
            int b = (text[i] & 0xff);
            switch (b)
            {
                case R0:
                case R1:
                case R2:
                case R3:
                case R4:
                case R5:
                case R6:
                case R7:
                case R8:
                case R9:
                    // if (reportWords && insideOfWord)
                    // {
                    // reportWord(slice, page, wordOffset, wordStart, position);
                    // // add
                    // // font
                    // // height
                    // insideOfWord = false;
                    // }
                    // position = position + jarBook.registers[b];
                    // i++;
                    break;
                case COMMAND:
                    if (reportWords && insideOfWord)
                    {
                        reportWord(slice, page, wordOffset, wordStartX, wordStartY, currentX - wordStartX); // add
                        // font
                        // height
                        insideOfWord = false;
                    }
                    i++;
                    switch ((text[i++] & 0xff))
                    {
                        case SET_POSITION:
                            x = ((short) (text[i++] << 8) | (text[i++] & 0xff));
                            y = ((short) (text[i++] << 8) | (text[i++] & 0xff));
                            currentX = x;
                            spaceAjust = 0;
                            break;
                        case SET_SPACE_AJUST:
                            spaceAjust = ((short) (text[i++] << 8) | (text[i++] & 0xff));
                            runningSpaceAjust = 0;
                            break;
                        case RECTANGLE:
                            platformRenderer.fillRect(((short) (text[i++] << 8) | (text[i++] & 0xff)),
                                    ((short) (text[i++] << 8) | (text[i++] & 0xff)),
                                    ((short) (text[i++] << 8) | (text[i++] & 0xff)),
                                    ((short) (text[i++] << 8) | (text[i++] & 0xff)));
                            break;
                        case LINK_SET:
                            i = readLinkSet(slice, text, page, i);
                            break;

                        case IMAGE:
                            i = readImage(slice, text, i);
                            break;
                    }
                    break;
                default:
                    // draw character
                    char c = (char) (text[i] & 0xff);
                    int charWidth = platformRenderer.drawChar(currentX, y, c);
                    if(c == ' ' && spaceAjust > 0)
                    {
                        runningSpaceAjust = runningSpaceAjust + spaceAjust;
                        currentX = currentX + (runningSpaceAjust / SPACE_AJUST_DIVIDER);
                        runningSpaceAjust = runningSpaceAjust % SPACE_AJUST_DIVIDER;
                    }
                    if (reportWords && insideOfWord == false)
                    {
                        // this is the start of the word, remember it
                        wordStartX = currentX;
                        wordStartY = y;
                        wordOffset = i;
                        insideOfWord = true;
                    }
                    currentX = currentX + charWidth;
                    i++;
                    break;
            }
        }
        // if inside of the word report last word end
        if (reportWords && insideOfWord)
        {
            reportWord(slice, page, wordOffset, wordStartX, wordStartY, currentX - wordStartX);
        }
        return platformRenderer.getSlice();
    }

    private int readLinkSet(Slice slice, byte[] buffer, int page, int o)
    {
        // count of links in link set
        int linkCount = ((short) (buffer[o++] << 8) | (buffer[o++] & 0xff));
        // allocate array for links
        for (int i = 0; i < linkCount; i++)
        {
            // link destination
            int destination = ((short) (buffer[o++] << 8) | (buffer[o++] & 0xff));
            // rectangle count
            int rectCount = (buffer[o++] & 0xff);
            Link link = new Link(destination, page, i, rectCount);
            for (int j = 0; j < rectCount; j++)
            {
                // read rectangles
                link.x[j] = ((short) (buffer[o++] << 8) | (buffer[o++] & 0xff));
                link.y[j] = ((short) (buffer[o++] << 8) | (buffer[o++] & 0xff));
                link.width[j] = ((short) (buffer[o++] << 8) | (buffer[o++] & 0xff));
                link.height[j] = ((short) (buffer[o++] << 8) | (buffer[o++] & 0xff));
                platformRenderer.drawLine(link.x[j], link.y[j] + link.height[j], link.x[j] + link.width[j], link.y[j]
                        + link.height[j]);
            }
            slice.links.addElement(link);
        }
        return o;
    }

    private int readImage(Slice slice, byte[] buffer, int o)
    {
        // x coordinate of image
        int x = ((short) (buffer[o++] << 8) | (buffer[o++] & 0xff));

        // y coordinate of image
        int y = ((short) (buffer[o++] << 8) | (buffer[o++] & 0xff));

        // image len
        int len = ((short) (buffer[o++] << 8) | (buffer[o++] & 0xff));

        // draw image here
        platformRenderer.drawImage(buffer, o, len, x, y);

        return o + len;
    }

    private void reportWord(Slice slice, int page, int offset, int x, int y, int width)
    {
        Link link = new Link(page, page, offset, 1);
        link.x[0] = x;
        link.y[0] = y;
        link.width[0] = width;
        link.height[0] = fontHeight; // FIXME to allow different fonts in the
        // same book
        slice.words.addElement(link);
    }
}