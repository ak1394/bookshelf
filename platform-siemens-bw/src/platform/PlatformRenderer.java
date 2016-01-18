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

package platform;

import com.siemens.mp.game.ExtendedImage;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import reader.IPlatformRenderer;
import reader.Slice;

public class PlatformRenderer implements IPlatformRenderer
{
    private ExtendedImage glyphImage;
    private ExtendedImage bufferImage;
    private Graphics bufferGraphics;

    private PlatformFont font;
    private PlatformSlice slice;

    private int bufferWidth;
    private int bufferRoundedWidth;
    private int bufferHeight;
    private int bufferByteSize;
    private byte color;
    
    private PlatformFontFabric fontFabric = new PlatformFontFabric();

    public void bufferInitialize(int canvasWidth, int canvasHeight, int rotation)
            throws Exception
    {
        bufferWidth = canvasWidth;
        bufferRoundedWidth = ((bufferWidth + 7) / 8) * 8;
        bufferHeight = canvasHeight;
        bufferByteSize = ((bufferWidth + 7) / 8) * bufferHeight;
        bufferImage = new ExtendedImage(Image.createImage(bufferRoundedWidth,
                bufferHeight));
        bufferGraphics = bufferImage.getImage().getGraphics();
    }

    public void bufferClear() throws Exception
    {
        bufferImage.clear(color);
    }

    public void setFont(String name) throws Exception
    {
        font = fontFabric.load(name);
        glyphImage = new ExtendedImage(Image.createImage(
                (((font.maxWidth + 7) / 8) * 8), font.height));
    }

    public int getFontHeight(String name)
    {
        return font.height;
    }

    public int drawChar(int x, int y, char c) throws Exception
    {
        c = (char) (c - font.firstChar);
        int glyphWidth = font.widthTable[c];
        int glyphWidthRounded = ((glyphWidth + 7) / 8) * 8;

        glyphImage.clear(color);
        glyphImage.setPixels(font.glyphTable[c], 0, 0, glyphWidthRounded,
                font.height);
        bufferGraphics.drawImage(glyphImage.getImage(), x, y, Graphics.TOP
                | Graphics.LEFT);

        return glyphWidth;
    }

    public Slice makeSlice()
    {
        PlatformSlice slice = new PlatformSlice(bufferByteSize);
        return slice;
    }

    public void setSlice(Slice slice) throws Exception
    {
        this.slice = (PlatformSlice) slice;
    }

    public Slice getSlice() throws Exception
    {
        bufferImage.getPixelBytes(slice.image, 0, 0, bufferRoundedWidth,
                bufferHeight);
        return slice;
    }

    public void fillRect(int x, int y, int w, int h)
    {
        bufferGraphics.fillRect(x, y, w, h);
    }

    public void drawLine(int x1, int y1, int x2, int y2)
    {
        bufferGraphics.drawLine(x1, y1, x2, y2);
    }

    public void drawImage(byte[] buffer, int offset, int len, int x, int y)
    {
        Image image = Image.createImage(buffer, offset, len);
        bufferGraphics.drawImage(image, x, y, Graphics.LEFT | Graphics.TOP);
    }

    /* (non-Javadoc)
     * @see reader.IPlatformRenderer#setColor(int)
     */
    public void setColor(int rgb)
    {
        if(rgb == 0xff000000)
        {
            // set current color to black
            color = 1;
        }
        else
        {
            // set current color to white
            color = 0;
        }
    }
}
