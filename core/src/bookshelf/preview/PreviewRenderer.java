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

package bookshelf.preview;

import reader.Slice;
import reader.IPlatformRenderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import bookshelf.font.Font;

public class PreviewRenderer implements IPlatformRenderer
{

    private BufferedImage bufferImage;
    private BufferedImage glyphImage;
    private Graphics2D bufferGraphics;
    private Graphics2D glyphGraphics;

    private Font font;
    private PreviewPlatformSlice slice;

    private int bufferWidth;
    private int bufferRoundedWidth;
    private int bufferHeight;
    private Color color;
    
    public void bufferInitialize(int width, int height, int rotation) throws Exception
    {
        bufferWidth = width;
        bufferRoundedWidth = ((bufferWidth + 7) / 8) * 8;
        bufferHeight = height;
        bufferImage = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_ARGB);
        bufferGraphics = bufferImage.createGraphics();
    }

    public void bufferClear() throws Exception
    {
        bufferGraphics.setBackground(color);
        bufferGraphics.clearRect(0, 0, bufferWidth, bufferHeight);
    }

    public void setFont(String name) throws Exception
    {
        // do nothing
    }
    
    public void setFont(Font font)
    {
        this.font = font;
        glyphImage = new BufferedImage(font.getMaxWidth(), font.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        glyphGraphics = glyphImage.createGraphics();
    }
    
    public int getFontHeight(String name)
    {
        return font.getHeight();
    }

    public int drawChar(int x, int y, char c) throws Exception
    {
        bufferGraphics.drawImage(font.getGlyph(c), null, x, y);
        return font.charWidth(c);
    }

    public Slice makeSlice()
    {
        return new PreviewPlatformSlice();
    }

    public void setSlice(Slice slice) throws Exception
    {
        this.slice = (PreviewPlatformSlice) slice;
    }

    public Slice getSlice() throws Exception
    {
        bufferImage.flush();
        slice.image = new BufferedImage(bufferWidth, bufferHeight,
                BufferedImage.TYPE_INT_ARGB);
        slice.image.createGraphics().drawImage(bufferImage, null, 0, 0);
        return slice;
    }

    public void setColor(int r, int g, int b)
    {
        color = new Color(r, g, b);
        bufferGraphics.setColor(color);
    }

    public void fillRect(int x, int y, int w, int h)
    {
        Color oldColor = bufferGraphics.getColor();
        bufferGraphics.setColor(Color.BLACK);
        bufferGraphics.fillRect(x, y, w, h);
        bufferGraphics.setColor(oldColor);
    }

    public void drawLine(int x1, int y1, int x2, int y2)
    {
        bufferGraphics.drawLine(x1, y1, x2, y2);
    }

    public void drawImage(byte[] buffer, int offset, int len, int x, int y)
    {
        ByteArrayInputStream input = new ByteArrayInputStream(buffer, offset,
                len);
        try
        {
            BufferedImage image = ImageIO.read(input);
            bufferGraphics.drawImage(image, x, y, null);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void setColor(int color)
    {
        bufferGraphics.setColor(new Color(color));
        bufferGraphics.setBackground(new Color(color));
    }
}
