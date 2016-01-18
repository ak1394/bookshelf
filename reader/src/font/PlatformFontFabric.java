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

package font;

import java.io.*;
import javax.microedition.lcdui.Image;

public class PlatformFontFabric
{
    private PlatformFont font;
    private static final String FONT_SUFFIX = ".png";
    private static final String METRICS_SUFFIX = ".metrics";
    private static final String FONT_PATH = "/fonts/";
    private String currentFontName = "";

    public PlatformFontFabric()
    {
        font = new PlatformFont();
    }

    public PlatformFont load(String fontName) throws Exception
    {
        if (!fontName.equals(currentFontName))
        {
            DataInputStream metricsIS = new DataInputStream(getClass()
                    .getResourceAsStream(FONT_PATH + fontName + METRICS_SUFFIX));

            // header
            int glyphCount = metricsIS.readUnsignedShort();
            int imageCount = metricsIS.readUnsignedByte();
            font.firstChar = metricsIS.readUnsignedByte();
            font.height = metricsIS.readUnsignedByte();

            font.width = new byte[glyphCount];
            font.x = new byte[glyphCount];
            font.y = new byte[glyphCount];
            font.image = new byte[glyphCount];

            for (int i = 0; i < glyphCount; i++)
            {
                font.width[i] = metricsIS.readByte();
                if(font.width[i] != -1)
                {
                    font.x[i] = metricsIS.readByte();
                    font.y[i] = metricsIS.readByte();
                    font.image[i] = metricsIS.readByte();
                }
            }
            metricsIS.close();

            font.images = new Image[imageCount];
            for (int i = 0; i < imageCount; i++)
            {
                font.images[i] = Image.createImage(FONT_PATH + fontName + i
                        + FONT_SUFFIX);
            }
            currentFontName = fontName;
        }
        return font;
    }
}