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
 * The <code>IPlatformCanvas</code> interface provieds means of accessing
 * platform specific buffer that is used to render page image.
 * 
 * @author Anton Krasovsky <ak1394@users.sourceforge.net>
 *  
 */
public interface IPlatformRenderer
{
    /**
     * Creates the empty {@link Slice}.
     * 
     * @return <code>Slice</code> object
     */
    public Slice makeSlice();

    /**
     * Sets the current <code>Slice</code> to use.
     * 
     * @param slice
     *            the slice object
     * @throws Exception
     */
    public void setSlice(Slice slice) throws Exception;

    /**
     * Gets current <code>Slice</code>.
     * 
     * @return the slice object
     * 
     * @throws Exception
     */
    public Slice getSlice() throws Exception;

    /**
     * Clears contents of the buffer.
     * 
     * @throws Exception
     */
    public void bufferClear() throws Exception;

    /**
     * Initializes the off-screen buffer.
     * 
     * @param viewportWidth
     *            width
     * @param viewportHeight
     *            height
     * @param rotate
     *            rotation of the screen, allowed values are 0, 90, 270 degrees
     * @throws Exception
     */
    public void bufferInitialize(int viewportWidth, int viewportHeight, int rotation) throws Exception;

    /**
     * Draws character using current font.
     * 
     * @param x
     *            X coordinate
     * @param y
     *            Y coordinate
     * @param c
     *            character to draw
     * @return width of the character drawn
     * @throws Exception
     */
    public int drawChar(int x, int y, char c) throws Exception;

    /**
     * Sets platform-dependent font to use.
     * 
     * @param font
     *            font
     * @throws Exception
     */
    public void setFont(String font) throws Exception;

    /**
     * Returns the height of the font;
     * 
     * @param font
     *            font name
     * @return font height
     */
    public int getFontHeight(String font);

    /**
     * Sets current color.
     * 
     */
    public void setColor(int rgb);

    /**
     * Draw filled rectangle
     * 
     * @param x
     *            X coordinate
     * @param y
     *            Y coordinate
     * @param w
     *            width
     * @param h
     *            height
     */
    public void fillRect(int x, int y, int w, int h);

    /**
     * Draws line.
     * 
     * @param x1
     *            X1 coordinate
     * @param y1
     *            Y1 coordinate
     * @param x2
     *            X2 coordinate
     * @param y2
     *            Y2 coordinate
     */
    public void drawLine(int x1, int y1, int x2, int y2);

    /**
     * Draws image in platform supported format.
     * 
     * @param buffer
     *            image data buffer
     * @param offset
     *            offset in the data buffer
     * @param len
     *            length of the image in bytes starting from offset
     * @param x
     *            X coordinate
     * @param y
     *            Y coordinate
     */
    public void drawImage(byte[] buffer, int offset, int len, int x, int y);
}