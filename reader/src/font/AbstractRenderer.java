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

package font;

import javax.microedition.lcdui.Graphics;

import reader.IPlatformRenderer;
import reader.Slice;

public abstract class AbstractRenderer implements IPlatformRenderer
{
    protected Graphics graphics;

    protected PlatformFont font;
    protected PlatformFontFabric fontFabric = new PlatformFontFabric();

    protected int canvasWidth;
    protected int canvasHeight;
    protected int rotation;

    protected PlatformSlice slice;

    protected  int color;

    public abstract void fillRect(int x, int y, int w, int h);

    public abstract void drawLine(int x1, int y1, int x2, int y2);

    public abstract void drawImage(byte[] buffer, int offset, int len, int x, int y);

    public abstract int drawChar(int x, int y, char c) throws Exception;

    public void bufferInitialize(int canvasWidth, int canvasHeight, int rotation) throws Exception
    {
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        this.rotation = rotation;
    }

    public void bufferClear() throws Exception
    {
        graphics.setColor(color);
        graphics.fillRect(0, 0, canvasWidth, canvasHeight);
    }

    public void setFont(String name) throws Exception
    {
        this.font = fontFabric.load(name);
    }

    public int getFontHeight(String name)
    {
        return font.height;
    }

    public Slice makeSlice()
    {
        PlatformSlice slice = new PlatformSlice(canvasWidth, canvasHeight);
        return slice;
    }

    public void setColor(int color)
    {
        this.color = color;
        graphics.setColor(color);
    }

    /*
     * (non-Javadoc)
     * 
     * @see reader.IPlatformRenderer#setSlice(reader.Slice)
     */
    public void setSlice(Slice slice) throws Exception
    {
        this.slice = (PlatformSlice) slice;
        graphics = this.slice.image.getGraphics();
    }

    /*
     * (non-Javadoc)
     * 
     * @see reader.IPlatformRenderer#getSlice()
     */
    public Slice getSlice() throws Exception
    {
        graphics = null;
        return slice;
    }
}