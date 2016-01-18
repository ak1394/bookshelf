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

import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import bookshelf.book.JarOutput;
import bookshelf.builder.PlatformPackage;

public class FontWriterBitmapTwo implements FontWriter
{
    // TODO this class is not tested
    
    private static final int INT_SIZE = 4;
    private static final int CHUNK_SIZE = 4096;

    Chunk currentChunk;
    ArrayList chunkList = new ArrayList();

    public FontWriterBitmapTwo()
    {
        currentChunk = new Chunk(CHUNK_SIZE);
    }

    public void setPlatform(PlatformPackage platform)
    {
        // do nothing
    }

    public void writeFont(JarOutput output, String prefix, Font font) throws Exception
    {
        prepareChunks(font);
        assert chunkList.size() > 0 : "chunkList must not be empty when trying to write font";

        // write header
        output.putNextEntry(prefix + font.getName());
        output.write(getFontHeader(font));

        // write chunks
        for (int i = 0; i < chunkList.size(); i++)
        {
            output.putNextEntry(prefix + font.getName() + i);
            Chunk chunk = (Chunk) chunkList.get(i);
            output.write(chunk.getBytes());
        }
    }

    private byte[] getFontHeader(Font font) throws Exception
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        out.writeInt(font.getMaxWidth());
        out.writeInt(font.getHeight());
        out.writeInt(font.getFirstChar());
        out.writeInt(font.getLastChar());

        out.writeInt(chunkList.size());

        return baos.toByteArray();
    }

    private void prepareChunks(Font font) throws Exception
    {
        for (char i = 0; i < font.getRealGlyphCount(); i++)
        {
            Glyph glyph = new Glyph(font.getRealGlyph(i).getWidth(), ((DataBufferByte) font.getRealGlyph(i).getRaster()
                    .getDataBuffer()).getData());
            if (!currentChunk.hasSpace(glyph.getSize()))
            {
                chunkList.add(currentChunk);
                currentChunk = new Chunk(CHUNK_SIZE);
            }
            currentChunk.writeGlyph(glyph);
        }
        // save last chunk
        chunkList.add(currentChunk);
    }

    class Glyph
    {
        int width;
        byte[] data;

        Glyph(int width, byte[] data)
        {
            this.width = width;
            this.data = data;
        }

        int getSize()
        {
            return width == -1 ? INT_SIZE : data.length + INT_SIZE;
        }
    }

    class Chunk
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        ArrayList glyphList = new ArrayList();
        int maxSize;
        int currentSize = 0;

        Chunk(int maxSize)
        {
            this.maxSize = maxSize;
        }

        void writeGlyph(Glyph glyph)
        {
            glyphList.add(glyph);
            currentSize = currentSize + glyph.getSize();
        }

        boolean hasSpace(int size)
        {
            return currentSize + size < maxSize ? true : false;
        }

        byte[] getBytes() throws Exception
        {
            // number of glyphs in the chunk
            out.writeInt(glyphList.size());
            for (Iterator i = glyphList.iterator(); i.hasNext();)
            {
                Glyph glyph = (Glyph) i.next();
                out.writeInt(glyph.width);
                if (glyph.width != -1)
                {
                    out.write(glyph.data);
                }
            }

            return baos.toByteArray();
        }
    }
}