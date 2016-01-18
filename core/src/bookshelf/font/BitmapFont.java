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

package bookshelf.font;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.Serializable;

public class BitmapFont extends Font implements Serializable
{

    private transient BufferedImage[] glyphTable;

    public void setBackground(Color background) throws Exception
    {
        this.background = background;
        updateColors();
    }

    public void setColor(Color color) throws Exception
    {
        this.color = color;
        updateColors();
    }

    /**
     * @param firstChar
     *            The firstChar to set.
     */
    public void setFirstChar(char firstChar)
    {
        this.firstChar = firstChar;
    }

    /**
     * @param glyphTable
     *            The glyphTable to set.
     */
    public void setGlyphTable(BufferedImage[] glyphTable)
    {
        this.glyphTable = glyphTable;
    }

    /**
     * @return the glyphTable
     */
    public BufferedImage[] getGlyphTable()
    {
        return glyphTable;
    }

    /**
     * @param height
     *            The height to set.
     */
    public void setHeight(int height)
    {
        this.height = height;
    }

    /**
     * @param lastChar
     *            The lastChar to set.
     */
    public void setLastChar(char lastChar)
    {
        this.lastChar = lastChar;
    }

    /**
     * @param maxWidth
     *            The maxWidth to set.
     */
    public void setMaxWidth(int maxWidth)
    {
        this.maxWidth = maxWidth;
    }

    /**
     * @param encoding
     *            The encoding to set.
     */
    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException
    {
        out.defaultWriteObject();
        out.writeInt(glyphTable.length);
        for (int i = 0; i < glyphTable.length; i++)
        {
            if (glyphTable[i] != null)
            {
                out.writeBoolean(true);
                BufferedImage glyph = glyphTable[i];

                // write image dimensions and type
                out.writeInt(glyph.getWidth());
                out.writeInt(glyph.getHeight());
                out.writeInt(glyph.getType());

                if (glyph.getType() == BufferedImage.TYPE_BYTE_BINARY)
                {
                    // write image color model
                    IndexColorModel cm = (IndexColorModel) glyph.getColorModel();
                    byte[] colorBuffer = new byte[cm.getMapSize()];
                    out.writeInt(cm.getPixelSize());
                    out.writeInt(cm.getMapSize());
                    cm.getReds(colorBuffer);
                    out.writeUnshared(colorBuffer);
                    cm.getGreens(colorBuffer);
                    out.writeUnshared(colorBuffer);
                    cm.getBlues(colorBuffer);
                    out.writeUnshared(colorBuffer);

                    // write image data
                    DataBufferByte buffer = (DataBufferByte) glyphTable[i].getRaster().getDataBuffer();
                    int banksNumber = buffer.getNumBanks();
                    out.writeInt(banksNumber);
                    for (int j = 0; j < banksNumber; j++)
                    {
                        out.writeObject(buffer.getData(j));
                    }
                }
                else if (glyph.getType() == BufferedImage.TYPE_INT_RGB)
                {
                    // write image data
                    DataBufferInt buffer = (DataBufferInt) glyphTable[i].getRaster().getDataBuffer();
                    out.writeObject(buffer.getData());

                }
                else if (glyph.getType() == BufferedImage.TYPE_BYTE_GRAY)
                {
                    // write image data
                    DataBufferByte buffer = (DataBufferByte) glyphTable[i].getRaster().getDataBuffer();
                    out.writeObject(buffer.getData());

                }
                else
                {
                    throw new IOException("Unknown image type");
                }
            }
            else
            {
                out.writeBoolean(false);
            }
        }
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        glyphTable = new BufferedImage[in.readInt()];
        for (int i = 0; i < glyphTable.length; i++)
        {
            if (in.readBoolean())
            {
                // image dimenstions and type
                int width = in.readInt();
                int height = in.readInt();
                int type = in.readInt();

                if (type == BufferedImage.TYPE_BYTE_BINARY)
                {

                    // color model
                    int bits = in.readInt();
                    int mapSize = in.readInt();
                    byte[] r = (byte[]) in.readUnshared();
                    byte[] g = (byte[]) in.readUnshared();
                    byte[] b = (byte[]) in.readUnshared();
                    IndexColorModel cm = new IndexColorModel(bits, mapSize, r, g, b);

                    glyphTable[i] = new BufferedImage(width, height, type, cm);
                    int banksNumber = in.readInt();
                    for (int j = 0; j < banksNumber; j++)
                    {
                        byte[] bank = (byte[]) in.readObject();
                        DataBufferByte buffer = (DataBufferByte) glyphTable[i].getRaster().getDataBuffer();
                        System.arraycopy(bank, 0, buffer.getData(j), 0, bank.length);
                    }
                }
                else if (type == BufferedImage.TYPE_INT_RGB)
                {
                    // read image data
                    int[] data = (int[]) in.readObject();
                    glyphTable[i] = new BufferedImage(width, height, type);
                    DataBufferInt buffer = (DataBufferInt) glyphTable[i].getRaster().getDataBuffer();
                    System.arraycopy(data, 0, buffer.getData(), 0, data.length);
                }
                else if (type == BufferedImage.TYPE_BYTE_GRAY)
                {
                    // read image data
                    byte[] data = (byte[]) in.readObject();
                    glyphTable[i] = new BufferedImage(width, height, type);
                    DataBufferByte buffer = (DataBufferByte) glyphTable[i].getRaster().getDataBuffer();
                    System.arraycopy(data, 0, buffer.getData(), 0, data.length);
                }
                else
                {
                    throw new IOException("Unknown image type");
                }
            }
            else
            {
                glyphTable[i] = null;
            }
        }
    }

    private void updateColors()
    {
        IndexColorModel colorModel = new IndexColorModel(1, 2, 
                new byte[] { (byte) background.getRed(), (byte) color.getRed() },
                new byte[] { (byte) background.getGreen(), (byte) color.getGreen() },
                new byte[] { (byte) background.getBlue(), (byte) color.getBlue() });
        for (int i = 0; i < glyphTable.length; i++)
        {
            if (glyphTable[i] != null)
            {
                WritableRaster raster = glyphTable[i].getRaster();
                BufferedImage newGlyph = new BufferedImage(colorModel, raster, false, null);
                glyphTable[i] = newGlyph;
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see bookshelf.font.Font#getRealGlyph(int)
     */
    public BufferedImage getRealGlyph(int index)
    {
        return glyphTable[index];
    }

    /*
     * (non-Javadoc)
     * 
     * @see bookshelf.font.Font#getDefaultGlyph()
     */
    public BufferedImage getDefaultGlyph()
    {
        return glyphTable[glyphTable.length - 1];
    }

    /*
     * (non-Javadoc)
     * 
     * @see bookshelf.font.Font#getRealGlyphCount()
     */
    public int getRealGlyphCount()
    {
        return glyphTable.length;
    }
}