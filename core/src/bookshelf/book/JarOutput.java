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

package bookshelf.book;

import java.io.IOException;
import java.io.OutputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class JarOutput extends OutputStream
{
    private JarOutputStream jos;
    private long writtenCompressed;
    private JarEntry entry;

    public JarOutput(OutputStream outputStream, Manifest manifest) throws Exception
    {
        jos = new JarOutputStream(outputStream, manifest);
    }
    
    public void putNextEntry(String name) throws Exception
    {
        if(entry != null)
        {
            jos.closeEntry();
            writtenCompressed = writtenCompressed + entry.getCompressedSize();
        }
        entry = new JarEntry(name);
        jos.putNextEntry(entry);
    }

    public void write(int b) throws IOException
    {
        jos.write(b);
    }

    public void write(byte[] b) throws IOException
    {
        jos.write(b);
    }

    public void flush() throws IOException
    {
        if(entry != null)
        {
            jos.closeEntry();
            writtenCompressed = writtenCompressed + entry.getCompressedSize();
            entry = null;
        }
        jos.flush();
    }
    
    public void close() throws IOException
    {
        flush();
        jos.close();
    }
    
    public long getWrittenCompressed()
    {
        return writtenCompressed;
    }
}
