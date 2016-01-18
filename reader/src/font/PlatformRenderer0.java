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
import javax.microedition.lcdui.Image;

public class PlatformRenderer0 extends AbstractRenderer
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
        int glyphWidth = font.width[index];
        int glyphImage = font.image[index];
        graphics.setClip(x, y, glyphWidth, font.height);
        graphics.drawImage(font.images[glyphImage], x - glyphX, y - glyphY, Graphics.TOP | Graphics.LEFT);
        graphics.setClip(0, 0, canvasWidth, canvasHeight);
        return glyphWidth;
    }

    public void fillRect(int x, int y, int w, int h)
    {
        graphics.setColor(0x000000); // FIXME
        graphics.fillRect(x, y, w, h);
    }

    public void drawLine(int x1, int y1, int x2, int y2)
    {
        //graphics.drawLine(x1, y1, x2, y2);
    }

    public void drawImage(byte[] buffer, int offset, int len, int x, int y)
    {
        Image image = Image.createImage(buffer, offset, len);
        graphics.drawImage(image, x, y, Graphics.LEFT | Graphics.TOP);
    }
}