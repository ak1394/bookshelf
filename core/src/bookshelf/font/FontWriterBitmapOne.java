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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import bookshelf.book.JarOutput;
import bookshelf.builder.PlatformPackage;

public class FontWriterBitmapOne implements FontWriter
{
    private static final int CHUNK_SIZE = 4096;

    private static final int MARKER_GLYPH = 1;
    private static final int MARKER_NEXT = 2;
    private static final int MARKER_STOP = 3;
    
    private Font font;
    
    private boolean inverted = false;

    
    public void setPlatform(PlatformPackage platform)
    {
        // do nothing
    }
    
    public void writeFont(JarOutput output, String prefix, Font font) throws Exception
    {
        this.font = font;
    	int chunk_id = 0;
        ByteArrayOutputStream chunkData = new ByteArrayOutputStream();
        DataOutputStream os = new DataOutputStream(chunkData);

        // font header information

        // total characters
        os.writeInt(font.getRealGlyphCount());
        // maxWidth
        os.writeInt(font.getMaxWidth());
        // height
        os.writeInt(font.getHeight());
        // firstChar
        os.writeInt(font.getFirstChar());
        // lastChar
        os.writeInt(font.getLastChar());

        output.putNextEntry(prefix + font.getId() + chunk_id);
        for(char i=0; i < font.getRealGlyphCount(); i++)
        {
            if(os.size() >= CHUNK_SIZE)
            {
                os.writeByte(MARKER_NEXT); // next chunk marker
                os.close();
                output.write(chunkData.toByteArray());
                chunk_id++;
                output.putNextEntry(prefix + font.getId() + chunk_id);
                chunkData = new ByteArrayOutputStream();
                os = new DataOutputStream(chunkData);
            }
            // write glyph
            os.writeByte(MARKER_GLYPH); // glyph start marker
            os.write(getGlyphRecord(i));
        }
        os.writeByte(MARKER_STOP); // stop of font 
        os.close();
        output.write(chunkData.toByteArray());
    }

    private byte[] getGlyphRecord(char i) throws Exception
    {
        // TODO optimize
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream os = new DataOutputStream(baos);
        // write glyph; if tere is no glyph write -1 for width
        if(font.getRealGlyph(i) != null)
        {
            int roundedWidth = ((font.getRealGlyph(i).getWidth() + 7) / 8) * 8;
            BufferedImage tmp = new BufferedImage(roundedWidth, font.getRealGlyph(i).getHeight(), BufferedImage.TYPE_BYTE_BINARY);
            Graphics2D g = tmp.createGraphics();
            g.setColor(font.getBackground());
            g.fillRect(0, 0, roundedWidth, tmp.getHeight());
            g.drawImage(font.getRealGlyph(i), null, 0, 0);
            
            os.writeInt(font.getRealGlyph(i).getWidth());
            byte[] glyph = ((DataBufferByte) tmp.getRaster().getDataBuffer()).getData();
            byte[] result = new byte[glyph.length];
            System.arraycopy(glyph, 0, result, 0, glyph.length);
            // invert
            for(int j=0; j<result.length; j++)
            {
                    result[j] = (byte) (~result[j] & 0xFF);
            }
            os.writeInt(result.length);
            os.write(result);
        }
        else
        {
            os.writeInt(-1);
        }
        os.flush();
        return baos.toByteArray();
    }
    
    
//	/**
//	 * Remove extra bits at the left of every 1bit glyph.
//	 * @param buffer glyph data
//	 * @param width glyph width
//	 */
//	private void cropBits(byte[] buffer, int width)
//	{
//	    int byteStride = (width + 7) / 8;
//	    int bitsToCrop = (byteStride * 8) - width;
//	    
//	    for(int i=byteStride; i<=buffer.length; i=i+byteStride)
//	    {
//	        buffer[i-1] = (byte) (((buffer[i-1] & 0xff) >> bitsToCrop) << bitsToCrop);
//	    }
//	}
}
