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
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

public abstract class SystemFont extends Font implements Serializable
{
    protected transient BufferedImage[] glyphTable;
    protected transient java.awt.Font awtFont;
    protected transient LineMetrics lineMetrics;
    protected transient FontMetrics fontMetrics;
    protected transient int baseline;
    protected transient BufferedImage buffer;
    protected transient Graphics2D bufferGraphics;
    private transient char[] chars;

    public SystemFont(java.awt.Font font, String encoding) throws Exception
    {
        makeFont(font, encoding);
    }

    protected abstract void makeBuffer(int width, int height);

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

    public void setBackground(Color background) throws Exception
    {
        this.background = background;
        makeFont(awtFont, encoding);
    }

    public void setColor(Color color) throws Exception
    {
        this.color = color;
        makeFont(awtFont, encoding);
    }

    private void makeFont(java.awt.Font awtFont, String codePage) throws Exception
    {
        this.awtFont = awtFont;

        BufferedImage sample = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_BINARY);
        chars = getCodePage(codePage);
        lineMetrics = awtFont.getLineMetrics(chars, 0, chars.length, sample.createGraphics().getFontRenderContext());
        fontMetrics = sample.getGraphics().getFontMetrics(awtFont);

        firstChar = (char) 1;
        lastChar = (char) 255;
        encoding = codePage;
        height = fontMetrics.getHeight();
        maxWidth = fontMetrics.getMaxAdvance();
        baseline = height - fontMetrics.getMaxDescent();

        makeBuffer(maxWidth, height);

        glyphTable = makeGlyphTable();
    }

    private char[] getCodePage(String encoding) throws Exception
    {
        byte[] onebyte = new byte[1];
        char[] result = new char[256];
        for (int i = 0; i < result.length; i++)
        {
            onebyte[0] = (byte) (i & 0xff);
            String onechar = new String(onebyte, encoding);
            result[i] = onechar.charAt(0);
        }

        return result;
    }

    private BufferedImage[] makeGlyphTable() throws Exception
    {
        BufferedImage[] glyphTable = new BufferedImage[chars.length];
        for (int i = 1; i < chars.length; i++)
        {
            char c = chars[i];
            glyphTable[i - 1] = makeGlyph(c);
        }
        glyphTable[chars.length - 1] = makeGlyph('?');
        return glyphTable;
    }

    protected BufferedImage makeGlyph(char c)
    {
        if (awtFont.canDisplay(c))
        {
            String s = Character.toString(c);
            bufferGraphics.setBackground(background);
            bufferGraphics.setColor(color);
            bufferGraphics.setFont(awtFont);
            bufferGraphics.clearRect(0, 0, buffer.getWidth(), buffer.getHeight());

            bufferGraphics.drawString(s, 0, baseline);
            Rectangle2D bounds = fontMetrics.getStringBounds(s, bufferGraphics);
            int width = (int) Math.round(bounds.getWidth());

            if (width == 0)
            {
                return null;
            }

            BufferedImage glyph = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            BufferedImage glyphSubimage = buffer.getSubimage(0, 0, width, height);
            glyphSubimage.copyData(glyph.getRaster());
            return glyph;
        }
        else
        {
            return null;
        }
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException
    {
        out.writeUTF(encoding);
        out.writeObject(awtFont.getAttributes());
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        String encoding = in.readUTF();
        Map attributes = (Map) in.readObject();
        java.awt.Font font = new java.awt.Font(attributes);
        try
        {
            makeFont(font, encoding);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new IOException("Problem reading font: " + e.getMessage());
        }
    }
}