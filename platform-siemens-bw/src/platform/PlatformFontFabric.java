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

package platform;

import java.io.*;

public class PlatformFontFabric
{
    public static final int MARKER_GLYPH = 1;
    public static final int MARKER_NEXT = 2;
    public static final int MARKER_STOP = 3;
    public static final String FONT_PATH = "/fonts/";
    private PlatformFont font;
    private String currentFontName = "";

    public PlatformFontFabric()
    {
        font = new PlatformFont();
        font.widthTable = new short[256];
        font.glyphTable = new byte[256][48];
    }

    public PlatformFont load(String fontName) throws Exception
    {
        if (!fontName.equals(currentFontName))
        {
            int chunk = 0;

            DataInputStream dis = openChunk(fontName + chunk);

            int totalGlyphs = dis.readInt(); // + 1 default
            font.maxWidth = dis.readInt();
            font.height = dis.readInt();
            font.firstChar = (char) dis.readInt();
            font.lastChar = (char) dis.readInt();

            byte marker;

            int i = 0;
            while ((marker = dis.readByte()) != MARKER_STOP)
            {
                switch (marker)
                {
                case MARKER_GLYPH:
                    int width = dis.readInt();
                    font.widthTable[i] = (short) width;
                    if (width != -1)
                    {
                        int bytes = dis.readInt();
                        dis.readFully(font.glyphTable[i], 0, bytes);
                    }
                    i++;
                    break;
                case MARKER_NEXT:
                    dis.close();
                    chunk++;
                    dis = openChunk(fontName + chunk);
                    break;
                }
            }
            dis.close();
            currentFontName = fontName;
        }
        return font;
    }

    public DataInputStream openChunk(String name) throws Exception
    {
        return new DataInputStream(getClass().getResourceAsStream(
                FONT_PATH + name));
    }
}