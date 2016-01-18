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

package bookshelf.builder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import bookshelf.book.ChunkedBookPartWriter;
import bookshelf.book.JarOutput;
import bookshelf.book.element.Book;
import bookshelf.font.FontWriter;

public class MidletBuilder
{
    private static final String BOOK_PREFIX = "book";
    private static final String FONT_PREFIX = "fonts/";
    private static final int JAR_OVERHEAD = 6144;

    private ArrayList books = new ArrayList();
    private ArrayList plugins = new ArrayList();
    private HashMap fonts = new HashMap();
    private ReaderPackage reader;
    private PlatformPackage platform;
    private byte[] buffer = new byte[8192];
    private ChunkedBookPartWriter bookWriter;
    private FontWriter fontWriter;
    private DescriptorBuilder descriptorBuilder;
    private long reservedSize;
    private int currentBook;
    private String language;

    public MidletBuilder(ReaderPackage reader, PlatformPackage platform) throws Exception
    {
        this.reader = reader;
        this.platform = platform;
        fontWriter = platform.getFontWriter();
        bookWriter = new ChunkedBookPartWriter(fontWriter);
        bookWriter.setMaxBlockSize(platform.getBlockSize());
        descriptorBuilder = new DescriptorBuilder(platform);
    }

    public void add(Book book)
    {
        books.add(book);
    }
    
    public void setLanguage(String language)
    {
        this.language = language;
    }

    public void addPlugin(String plugin)
    {
        plugins.add(plugin);
    }

    public void write(File basename, boolean makeNewFolder) throws Exception
    {
        String midletBaseName = basename.getName();
        File jarFile;
        File jadFile;

        // how much space taken by the midlet
        reservedSize = JAR_OVERHEAD + reader.getCompressedSize() + platform.getCompressedSize();
        for (Iterator i = plugins.iterator(); i.hasNext();)
        {
            reservedSize = reservedSize + reader.getPlugin((String) i.next()).getCompressedSize();
        }

        // write first midlet
        if (makeNewFolder)
        {
            basename.mkdir();
            jarFile = new File(basename, midletBaseName + ".jar");
            jadFile = new File(basename, midletBaseName + ".jad");
        }
        else
        {
            jarFile = new File(basename.getParent(), midletBaseName + ".jar");
            jadFile = new File(basename.getParent(), midletBaseName + ".jad");
        }

        writeMidlet(midletBaseName, jarFile, jadFile);

        // if midlet is to be split, write the rest of them
        for (int partId = 1; currentBook < books.size(); partId++)
        {
            String midletName = partId < 10 ? midletBaseName + "0" + partId : midletBaseName + partId;
            if(makeNewFolder)
            {
                File dirname = new File(basename.getParent(), midletName);
                dirname.mkdir();
                jarFile = new File(dirname , midletName + ".jar");
                jadFile = new File(dirname, midletName + ".jad");
            }
            else
            {
                jarFile = new File(basename.getParent(), midletName + ".jar");
                jadFile = new File(basename.getParent(), midletName + ".jad");
            }
            writeMidlet(midletName, jarFile, jadFile);
        }
    }

    private void writeMidlet(String midletName, File jarFile, File jadFile) throws Exception
    {
        JarOutput output = new JarOutput(new FileOutputStream(jarFile), descriptorBuilder.makeManifest(midletName));
        // part content
        List booksPerPart = writeContentPart(output);

        // reader
        writeReader(booksPerPart, output);

        // plugins
        for (Iterator i = plugins.iterator(); i.hasNext();)
        {
            Plugin plugin = reader.getPlugin((String) i.next());
            plugin.setLanguage(language);
            writePlugin(output, plugin);
        }

        // platform
        writePlatform(output);
        output.close();

        // jad
        descriptorBuilder.makeJad(new FileOutputStream(jadFile), midletName, jarFile.length());
    }

    private void writeReader(List booksPerPart, JarOutput output) throws Exception
    {
        for (Iterator i = reader.classNameIterator(); i.hasNext();)
        {
            String className = (String) i.next();
            output.putNextEntry(className);
            if (reader.isEngineClass(className))
            {
                copyStreamPatchEngine(booksPerPart, reader.getClassInputStream(className), output);
            }
            else
            {
                copyStream(reader.getClassInputStream(className), output);
            }
        }
    }

    private void writePlugin(JarOutput output, Plugin plugin) throws Exception
    {
        for (Iterator i = plugin.classNameIterator(); i.hasNext();)
        {
            String className = (String) i.next();
            output.putNextEntry(className);
            copyStreamPatchPlugin(reader.getClassInputStream(className), output, plugin);
        }
    }

    private void writePlatform(JarOutput output) throws Exception
    {
        for (Iterator i = platform.classNameIterator(); i.hasNext();)
        {
            String className = (String) i.next();
            output.putNextEntry(className);
            copyStream(platform.getClassInputStream(className), output);
        }
    }

    private List writeContentPart(JarOutput output) throws Exception
    {
        ArrayList contentBookList = new ArrayList();

        long maxContentSize = platform.getMaxMidletSize() - reservedSize;

        bookWriter.setOutput(output);
        int i;
        for (i = 0; i + currentBook < books.size(); i++)
        {
            bookWriter.setMaxPartSize(maxContentSize);

            if (!bookWriter.hasMorePages())
            {
                bookWriter.setBook((Book) books.get(currentBook + i));
            }
            Book book = bookWriter.writePart(i);
            if (book != null)
            {
                contentBookList.add(book);
            }
            if (bookWriter.hasMorePages())
            {
                break;
            }
            // recalc size
            maxContentSize = maxContentSize - output.getWrittenCompressed();
        }
        currentBook = currentBook + i;

        return contentBookList;
    }

    private void copyStream(InputStream is, OutputStream os) throws Exception
    {
        for (int i = is.read(buffer); i != -1; i = is.read(buffer))
        {
            os.write(buffer, 0, i);
        }
    }

    private void copyStreamPatchEngine(List booksPerPart, InputStream is, OutputStream os) throws Exception
    {
        // construct list of tiles in midlet
        ArrayList titles = new ArrayList();
        for (Iterator i = booksPerPart.iterator(); i.hasNext();)
        {
            Book book = (Book) i.next();
            titles.add(book.getTitle());
        }

        Random random = new Random();
        HashMap stringArrays = new HashMap();
        stringArrays.put("bookTitle", titles);
        stringArrays.put("pluginList", plugins);

        HashMap integers = new HashMap();
        if(platform.getRotation() == 0)
        {
            integers.put("canvasWidth", new Integer(platform.getCanvasDimension().width));
            integers.put("canvasHeight", new Integer(platform.getCanvasDimension().height));
        }
        else
        {
            // swap width and height if rotated
            integers.put("canvasWidth", new Integer(platform.getCanvasDimension().height));
            integers.put("canvasHeight", new Integer(platform.getCanvasDimension().width));
        }
        integers.put("rotation", new Integer(platform.getRotation()));
        integers.put("cacheSize", new Integer(platform.getCacheSize()));
        integers.put("blockSize", new Integer(platform.getBlockSize()));
        integers.put("magic", new Integer(random.nextInt()));

        EnginePatcher.patch(is, os, stringArrays, integers);
    }

    private void copyStreamPatchPlugin(InputStream is, OutputStream os, Plugin plugin) throws Exception
    {
        PluginPatcher.patch(is, os, platform, plugin);
    }
}