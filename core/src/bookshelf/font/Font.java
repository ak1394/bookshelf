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
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import org.apache.fop.layout.FontMetric;

public abstract class Font implements Serializable, FontMetric
{
    protected String name;
    protected int height;
    protected int maxWidth;
    protected int firstChar;
    protected int lastChar;
    protected String encoding = "windows-1251";
    protected Color background = Color.WHITE;
    protected Color color = Color.BLACK;

    private static final int FOP_SCALE_CONST = 1000000;

    public abstract void setColor(Color color) throws Exception;

    public abstract void setBackground(Color background) throws Exception;
    
    public abstract BufferedImage getRealGlyph(int index);

    public abstract BufferedImage getDefaultGlyph();
    
    public abstract int getRealGlyphCount();

    public Color getBackground()
    {
        return background;
    }

    public Color getColor()
    {
        return color;
    }
    
    public String getId()
    {
        return name + "-" + encoding + "-" + Integer.toHexString(color.getRGB()) + "-" + Integer.toHexString(background.getRGB());
    }
    
    public int charWidth(int index)
    {
        if (index < firstChar || index > lastChar)
            return getDefaultGlyph().getWidth();
        if (getRealGlyph(index - firstChar) == null)
            return getDefaultGlyph().getWidth();
        else
            return getRealGlyph(index - firstChar).getWidth();
    }

    public int stringWidth(String s)
    {
        try
        {
            int len = 0;
            byte byteString[];
            byteString = s.getBytes(getEncoding());

            for (int i = 0; i < byteString.length; i++)
            {
                len = len + charWidth(0xff & byteString[i]);
            }

            return len;
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Unable to access encoding table " + getEncoding());
        }
    }

    public BufferedImage getGlyph(char ch)
    {
        if (ch < firstChar || ch > lastChar)
            return getDefaultGlyph();
        if (getRealGlyph(ch - firstChar) == null)
            return getDefaultGlyph();
        else
            return getRealGlyph(ch - firstChar);
    }

    /**
     * @return Returns the firstChar.
     */
    public int getFirstChar()
    {
        return firstChar;
    }

    /**
     * @return Returns the height.
     */
    public int getHeight()
    {
        return height;
    }

    /**
     * @return Returns the lastChar.
     */
    public int getLastChar()
    {
        return lastChar;
    }

    /**
     * @return Returns the maxWidth.
     */
    public int getMaxWidth()
    {
        return maxWidth;
    }

    /**
     * @return Returns the name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name
     *            The name to set.
     */
    public void setName(String name)
    {
        this.name = name;
    }
    
    /**
     * @return Returns the encoding.
     */
    public String getEncoding()
    {
        return encoding;
    }
    
    private void writeObject(java.io.ObjectOutputStream out) throws IOException
    {
        out.defaultWriteObject();
    }
    
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
    }
    
    // Methods to implement fop FontMetric
    
    public int getAscender(int arg0)
    {
        return FOP_SCALE_CONST * height;
    }

    public int getCapHeight(int arg0)
    {
        return FOP_SCALE_CONST * height;
    }

    public int getDescender(int arg0)
    {
        return 0;
    }

    public int getXHeight(int arg0)
    {
        return FOP_SCALE_CONST * height;
    }

    public int width(int i, int size)
    {
        // we're fixed font, ignore size
        // but then why we do it different in getWidths?
        return charWidth(i) * FOP_SCALE_CONST;
    }
    
    public int[] getWidths(int size)
    {
        int[] result = new int[getLastChar() - getFirstChar() + 1];
        for (int i = 0; i < result.length; i++)
            result[i] = charWidth(i + firstChar) * 1000 * size; 
        return result;
    }
}