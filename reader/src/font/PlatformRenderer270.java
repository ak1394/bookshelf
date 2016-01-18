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

public class PlatformRenderer270 extends AbstractRenderer
{
    public int drawChar(int x, int y, char c) throws Exception
    {
        int index = c - font.firstChar;
        if (index < 0 || index >= font.width.length || font.width[index] == -1)
        {
            index = font.width.length - 1;
        }
        int glyphX = font.x[index];
        int glyphY = font.y[index];

        int glyphWidth = font.height;
        int glyphHeight = font.width[index];
        int glyphImageIndex = font.image[index];
        int nX = y;
        int nY = canvasHeight - x - glyphHeight;
        graphics.setClip(nX, nY, glyphWidth, glyphHeight);
        graphics.drawImage(font.images[glyphImageIndex], nX - glyphX, nY - glyphY, Graphics.TOP | Graphics.LEFT);
        graphics.setClip(0, 0, canvasWidth, canvasHeight);
        return glyphHeight;
    }

    public void fillRect(int x, int y, int w, int h)
    {
    //    graphics.fillRect(x, y, w, h);
    }

    public void drawLine(int x1, int y1, int x2, int y2)
    {
    //    graphics.drawLine(x1, y1, x2, y2);
    }

    public void drawImage(byte[] buffer, int offset, int len, int x, int y)
    {
    //    Image image = Image.createImage(buffer, offset, len);
    //    graphics.drawImage(image, x, y, Graphics.LEFT | Graphics.TOP);
    }
}