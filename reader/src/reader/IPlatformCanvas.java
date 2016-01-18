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

package reader;

import javax.microedition.lcdui.*;

/**
 * The <code>IPlatformCanvas</code> interface provides abstraction for a
 * platform specific <code>Canvas</code> extensions. Also, platform canvas
 * implements double-buffering.
 * 
 * @author Anton Krasovsky <ak1394@users.sourceforge.net>
 *  
 */
public interface IPlatformCanvas
{
    /**
     * Initializes canvas. Used to pre-allocate buffers, and so on.
     * 
     * @param canvasWidth
     *            canvas width
     * @param canvasHeight
     *            canvas height
     * @param rotation
     *            canvas rotation - 0, 90 or 270 degrees
     */
    public void initCanvas(int canvasWidth, int canvasHeight, int rotation);

    /**
     * Gets instance of <code>Canvas</code>. Each platform specific
     * implementation must provide an access to generic <code>Canvas</code>
     * object.
     * 
     * @return the <code>Canvas</code> object
     */
    public Canvas getCanvas();

    /**
     * Clears internal buffer.
     */
    public void clearBuffer();

    /**
     * Displays contents of internall buffer on the screen in the given
     * position.
     * 
     * @param g
     *            <code>Graphics</code> object
     * @param x
     *            X coordinate
     * @param y
     *            Y coordinate
     */
    public void displayBuffer(Graphics g, int x, int y);

    /**
     * Switches backlight off.
     */
    public void lightOff();

    /**
     * Switches backlight on.
     */
    public void lightOn();

    /**
     * Sets the <code>Engine</code> to use.
     * 
     * @param e
     *            the <code>Engine</code> object
     */
    public void setEngine(Engine e);

    /**
     * Copies contents of {@link Slice}into the internal buffer at given
     * coordinates, and limited by specified height.
     * 
     * @param slice
     *            <code>Slice</code> object
     * @param x
     *            X coordinate
     * @param y
     *            Y coordinate
     * @param height
     *            number of rows from <code>Slice</code> to be copied to
     *            buffer
     */
    public void sliceToBuffer(Slice slice, int x, int y, int height);

    /**
     * Used to highlight the rectangular area on the screen.
     * 
     * @param x
     *            X coordinate
     * @param y
     *            Y coordinate
     * @param width
     *            width
     * @param height
     *            height
     */
    public void highlight(int x, int y, int width, int height);

    /**
     * Returns an instance of appropriate IPlatformRenderer.
     * 
     * @return IPlaformRenderer object
     */
    public IPlatformRenderer getRenderer() throws Exception;
}