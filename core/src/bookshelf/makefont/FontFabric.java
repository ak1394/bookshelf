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

import java.io.File;

import bookshelf.font.AntialiasedFont;
import bookshelf.font.Font;
import bookshelf.font.NormalFont;
import bookshelf.font.SubpixelAntialiasedFont;
import bookshelf.font.SubpixelFont;

public class FontFabric
{
    public Font loadPft(File file, String encoding) throws Exception
    {
        FsPft fontSource = new FsPft();
        Font font = fontSource.loadPftFont(file, encoding);
        return font;
    }

    public Font loadPdb(File file, String encoding) throws Exception
    {
        FsPdb fontSource = new FsPdb();
        Font font = fontSource.loadPdbFont(file, encoding);
        return font;
    }

    public Font loadNormal(java.awt.Font font, String encoding) throws Exception
    {
        Font result = new NormalFont(font, encoding);
        return result;
    }

    public Font loadSubpixel(java.awt.Font font, String encoding) throws Exception
    {
        Font result = new SubpixelFont(font, encoding);
        return result;
    }

    public Font loadAntialiased(java.awt.Font font, String encoding) throws Exception
    {
        Font result = new AntialiasedFont(font, encoding);
        return result;
    }

    public Font loadSubpixelAntialiased(java.awt.Font font, String encoding) throws Exception
    {
        Font result = new SubpixelAntialiasedFont(font, encoding);
        return result;
    }
}