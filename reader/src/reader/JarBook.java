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

package reader;

import java.io.DataInputStream;

/**
 * This class is used to retrive book pages and other related information. *
 * 
 * @author Anton Krasovsky <ak1394@users.sourceforge.net>
 */
public class JarBook implements IBook
{

    private static final String BOOK_PREFIX = "/book";
    // TODO set size of the block index depending on the max number of blocks in the midlet
    private final static int MAX_BLOCK_INDEX_SIZE = 384;
    private final static int MAX_PAGE_INDEX_SIZE = 256;

    private static final char[] blockIndex = new char[MAX_BLOCK_INDEX_SIZE];
    private static final char blockPageIndex[] = new char[MAX_BLOCK_INDEX_SIZE];

    private String bookid;
    private int startPage;
    private int viwportWidth;
    private int viewportHeight;
    private int pageWidth;
    private int pageHeight;
    private int preferredLineHeight;

    private int blockIndexSize;
    private int currentBlockNumber;
    private int blockFirstPage;
    private int blockLastPage;

    public String[] fonts;
    public int color;
    public static byte blockPageContent[];

    public int[] registers;
    public int currentPageStart;
    public int currentPageEnd;

    /**
     * Initializes <code>JarBook</code> object and creates buffer of
     * <code>blockSize</code> size wich have to be known beforehand.
     * 
     * @param blockSize
     *            buffer size in bytes
     */
    public JarBook(int blockSize)
    {
        blockPageContent = new byte[blockSize];
    }

    /*
     * (non-Javadoc)
     * 
     * @see reader.IBook#getStartPage()
     */
    public int getStartPage()
    {
        return startPage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see reader.IBook#getViewportWidth()
     */
    public int getViewportWidth()
    {
        return viwportWidth;
    }

    /*
     * (non-Javadoc)
     * 
     * @see reader.IBook#getViewportHeight()
     */
    public int getViewportHeight()
    {
        return viewportHeight;
    }

    /*
     * (non-Javadoc)
     * 
     * @see reader.IBook#getPageWidth()
     */
    public int getPageWidth()
    {
        return pageWidth;
    }

    /*
     * (non-Javadoc)
     * 
     * @see reader.IBook#getPageHeight()
     */
    public int getPageHeight()
    {
        return pageHeight;
    }

    /*
     * (non-Javadoc)
     * 
     * @see reader.IBook#getPageCount()
     */
    public int getPageCount()
    {
        return blockIndex[blockIndexSize - 1];
    }

    /*
     * (non-Javadoc)
     * 
     * @see reader.IBook#getPreferredLineHeight()
     */
    public int getPreferredLineHeight()
    {
        return preferredLineHeight;
    }
    
    /* (non-Javadoc)
     * @see reader.IBook#getColor()
     */
    public int getColor()
    {
        return color;
    }

    /**
     * Prepares for reading from the specified book.
     * 
     * @param bookid
     *            id of the book
     * @throws Exception
     */
    public void openBook(String bookid) throws Exception
    {
        this.bookid = bookid;

        DataInputStream indexInput = openDataInput("info");

        // screen
        viwportWidth = indexInput.readInt();
        viewportHeight = indexInput.readInt();

        // page
        pageWidth = indexInput.readInt();
        pageHeight = indexInput.readInt();

        // font list
        fonts = new String[indexInput.readInt()];
        for (int i = 0; i < fonts.length; i++)
        {
            fonts[i] = indexInput.readUTF();
        }
        
        // background color
        color = indexInput.readInt();

        preferredLineHeight = indexInput.readInt();

        // registers
        registers = new int[indexInput.readInt()];
        for (int i = 0; i < registers.length; i++)
        {
            registers[i] = indexInput.readInt();
        }

        // start page
        startPage = indexInput.readInt();

        // index
        blockIndexSize = indexInput.readInt();
        for (int i = 0; i < blockIndexSize; i++)
        {
            blockIndex[i] = indexInput.readChar();
        }

        indexInput.close();

        // reset page info
        blockFirstPage = -1;
        blockLastPage = -1;
    }

    /**
     * Loads specified page to the internal buffer
     * {@link JarBook#blockPageContent}. Start and end of page are specified by
     * {@link JarBook#currentPageStart}and {@link JarBook#currentPageEnd}.
     * 
     * @param pageNumber
     *            number page to load (starting from 0)
     * @return <code>true</code> if page was successfully loaded,
     *         <code>false</code> otherwise
     * @throws Exception
     */
    public boolean setCurrentPage(int pageNumber) throws Exception
    {
        if (pageNumber >= blockFirstPage && pageNumber < blockLastPage)
        {
            // do nothing page is in the current block
        }
        else if (findBlock(pageNumber))
        {
            readBlock();
        }
        else
        {
            return false;
        }

        int pageIndex = pageNumber - blockFirstPage;
        currentPageStart = pageIndex > 0 ? blockPageIndex[pageIndex - 1] : 0;
        currentPageEnd = blockPageIndex[pageIndex];

        return true;
    }

    private void readBlock() throws Exception
    {
        DataInputStream blockInput = openDataInput(Integer.toString(currentBlockNumber));

        // find out starting page number and
        // number of pages from global page index
        blockFirstPage = currentBlockNumber > 0 ? blockIndex[currentBlockNumber - 1] : 0;
        blockLastPage = blockIndex[currentBlockNumber];
        int blockPageCount = blockLastPage - blockFirstPage;
        for (int i = 0; i < blockPageCount; i++)
        {
            blockPageIndex[i] = blockInput.readChar();
        }

        // read block content
        blockInput.readFully(blockPageContent, 0, blockPageIndex[blockPageCount - 1]);
        blockInput.close();
    }

    private boolean findBlock(int pageNumber)
    {
        for (int i = 0; i < blockIndexSize; i++)
        {
            if (blockIndex[i] > pageNumber && pageNumber > -1)
            {
                currentBlockNumber = i;
                return true;
            }
        }
        return false;
    }

    private DataInputStream openDataInput(String name)
    {
        DataInputStream dis = new DataInputStream(getClass().getResourceAsStream(BOOK_PREFIX + bookid + "/" + name));
        return dis;
    }
}