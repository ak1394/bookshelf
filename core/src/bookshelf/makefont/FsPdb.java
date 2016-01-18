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

package bookshelf.makefont;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import bookshelf.font.Font;
import bookshelf.makefont.pdb.NamedByteArrayOutputStream;
import bookshelf.makefont.pdb.PDBFile;

public class FsPdb
{
    public Font loadPdbFont(File file, String encoding) throws Exception
    {
        PDBFile pdbFile = new PDBFile(file);
        NamedByteArrayOutputStream result[] = pdbFile.getResult();
        File tmpFile = File.createTempFile("font", null);
        tmpFile.deleteOnExit();
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tmpFile));
        bos.write(result[0].toByteArray());
        bos.close();

        FsPft fsPft = new FsPft();
        Font font = fsPft.loadPftFont(tmpFile, encoding);
        tmpFile.delete();
        return font;
    }
}