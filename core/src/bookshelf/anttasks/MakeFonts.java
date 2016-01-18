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

package bookshelf.anttasks;

import java.io.*;

import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;

import bookshelf.font.Font;
import bookshelf.makefont.FontFabric;

/**
 * MakeFonts
 */

public class MakeFonts extends MatchingTask
{
    private File sourceDir, targetDir;
    private FontFabric fontFabric;
    private String encoding;

    /**
     * Main method, which is called by ant.
     */
    public void execute() throws org.apache.tools.ant.BuildException
    {
        fontFabric = new FontFabric();
        DirectoryScanner ds = this.getDirectoryScanner(sourceDir);
        String[] files = ds.getIncludedFiles();
        for (int i = 0; i < files.length; i++)
        {
            try
            {
                processFile(files[i].substring(0, files[i].length() - 4));
            }
            catch(Exception e)
            {
                e.printStackTrace();
                throw new BuildException(e.getMessage());
            }
        }
    }

    /**
     * Sets the source directory
     *  
     */
    public void setSourceDir(String sourceDir)
    {
        File dir = getProject().resolveFile(sourceDir);
        if (!dir.exists())
        {
            System.err.println("Fatal Error: source directory " + sourceDir
                    + " for font files doesn't exist.");
            System.exit(1);
        }
        this.sourceDir = dir;
    }

    /**
     * Sets the target directory
     *  
     */
    public void setTargetDir(String targetDir)
    {
        File dir = getProject().resolveFile(targetDir);
        this.targetDir = dir;
    }

    /**
     * Sets the encoding
     *  
     */
    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }

    /*
     * checks whether input or output files exists or the latter is older than
     * input file and start build if necessary
     */
    private void processFile(String filename) throws Exception
    {
        File infile = new File(sourceDir, filename + ".pft");
        File outfile = new File(targetDir, filename);
        long outfileLastModified = outfile.lastModified();
        boolean startProcess = true;

        startProcess = rebuild(infile, outfile);
        if (startProcess)
        {
            System.out.println("Processing " + infile);
            Font font = fontFabric.loadPft(infile, encoding);
            String name = infile.getName();
            font.setName(name.substring(0, name.length()-4));
            ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(outfile));
            o.writeObject(font);
            o.close();
        }
    }

    /**
     * Checks for existence of output file and compares dates with input and
     * stylesheet file
     */
    private boolean rebuild(File infile, File outfile)
    {
        if (outfile.exists())
        {
            // checks whether output file is older than input file
            if (outfile.lastModified() < infile.lastModified()) { return true; }
        } else
        {
            // if output file does not exist, start process
            return true;
        }
        return false;
    } // end rebuild
}