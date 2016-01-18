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

package bookshelf.makefont;

import bookshelf.font.Font;
import java.io.*;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        FontFabric fabric = new FontFabric();
        if(args.length != 3)
        {    
            usage();
            System.exit(1);
        }
        
        if(args[0].toLowerCase().endsWith(".pft"))
        {
            // load from pft
            processPftFont(fabric, args);
        }
        else if(true)
        {
            processTtfFont(fabric, args);
        }
        else
        {
            System.err.println("Unknown file extension.");
        }
    }

    /**
     * @param fabric
     * @param args
     * @throws Exception
     * @throws IOException
     * @throws FileNotFoundException
     */
    private static void processPftFont(FontFabric fabric, String[] args) throws Exception, IOException, FileNotFoundException
    {
        Font font = fabric.loadPft(new File(args[0]), args[1]);
        ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(args[2]));
        o.writeObject(font);
        o.close();
    }

    private static void processTtfFont(FontFabric fabric, String[] args) throws Exception, IOException, FileNotFoundException
    {
        java.awt.Font systemFont = java.awt.Font.decode(args[0]);
        Font font = fabric.loadNormal(systemFont, args[1]);
        ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(args[2]));
        o.writeObject(font);
        o.close();
    }

    /**
     * 
     */
    private static void usage()
    {
        System.err.println("Usage: makefont <font file> <encoding> <font name>");
    }
}