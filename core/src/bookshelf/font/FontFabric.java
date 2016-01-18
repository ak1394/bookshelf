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

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

/**
 * @author Anton Krasovsky <ak1394@mail.ru>
 *  
 */
public class FontFabric
{
    private File dir;

    public FontFabric(String baseDir)
    {
        dir = new File(baseDir);
    }

    public String[] list()
    {
        return dir.list();
    }
    
    public void delete(String fontName)
    {
        File fontFile = new File(dir, fontName);
        if(fontFile.exists())
        {
            fontFile.delete();
        }
    }

    public Font loadFont(String fontName) throws Exception
    {
        File fontFile = new File(dir, fontName);
        ObjectInputStream o = new ObjectInputStream(new FileInputStream(fontFile));
        Font font = (Font) o.readObject();
        o.close();
        return font;
    }
}