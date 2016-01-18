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

package bookshelf.makefont;

import java.io.RandomAccessFile;
import java.io.IOException;

class PftFont
{

private int fontType;         // font type
private int firstChar;        // ASCII code of first character
private int lastChar;         // ASCII code of last character
private int maxWidth;         // maximum character width
private short kernMax;        // negative of maximum character kern
private short nDescent;       // negative of descent
private int fRectWidth;       // width of font rectangle
private int fRectHeight;      // height of font rectangle
private int offsetWidths;     // offset to offset/width table (in words from _this_ point)
private int ascent;           // ascent
private int descent;          // descent
private int leading;          // leading
private int rowWords;         // row width of bit image / 2

private byte[] imageBitTable;
private int[]  locationTable;
private byte[] widthTable;

public PftFont(RandomAccessFile file) throws IOException
{
    fontType = file.readUnsignedShort();
    firstChar = file.readUnsignedShort();
    lastChar = file.readUnsignedShort();
    maxWidth = file.readUnsignedShort();
    kernMax = file.readShort();
    nDescent = file.readShort();
    fRectWidth = file.readUnsignedShort();
    fRectHeight = file.readUnsignedShort();
    long baseOffsetWidths = file.getFilePointer();
    offsetWidths = file.readUnsignedShort();
    ascent = file.readUnsignedShort();
    descent = file.readUnsignedShort();
    leading = file.readUnsignedShort();
    rowWords = file.readUnsignedShort();

    imageBitTable = new byte[rowWords * fRectHeight * 2];
    locationTable = new int[(lastChar-firstChar+3)];
    widthTable = new byte[(lastChar-firstChar+3) * 2];

    // read font image
    file.read(imageBitTable);

    // read location table
    for(int i=0; i < locationTable.length; i++) locationTable[i] = file.readUnsignedShort();
    file.seek(baseOffsetWidths+(2*offsetWidths));
    file.read(widthTable);
}

public int height()
{
    return fRectHeight;
}

public int width()
{
    return rowWords * 16;
}

public byte[] bitmap()
{
    return imageBitTable;
}

public int firstChar()
{
    return firstChar;
}

public int lastChar()
{
    return lastChar;
}

public int getLocation(int ch)
{
    if(ch < firstChar || ch > lastChar) return -1;
    if(widthTable[((ch - firstChar) * 2) + 1] == -1) return -1;
    return locationTable[ch - firstChar];
}

public int getRealLocation(int ch)
{
    if(widthTable[(ch * 2) + 1] == -1) return -1;
    return locationTable[ch];
}

public int getWidth(int ch)
{
    if(ch < firstChar || ch > lastChar) return -1;
    return widthTable[((ch - firstChar) * 2) + 1];
}

public int getMaxWidth()
{
    return maxWidth;
}

public int getRealWidth(int ch)
{
    return widthTable[(ch * 2) + 1];
}

public int getDefaultLocation()
{
    return locationTable[lastChar + 1 - firstChar];
}

public int getDefaultWidth()
{
    return widthTable[((lastChar + 1 - firstChar) * 2) + 1];
}

} // end of class
