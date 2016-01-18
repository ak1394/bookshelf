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

package bookshelf.jrender;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import bookshelf.font.Font;

/**
 * @author Anton Krasovsky <ak1394@mail.ru>
 *  
 */
public class ParagraphSource implements IParagraphSource
{
    private String encoding; //  "windows-1251";
    private ParagraphReader pReader;
    private Font font;
    private int indent;

    /**
     * @param file
     * @param encoding
     * @param font
     * @param offset
     * @throws Exception
     */
    public ParagraphSource(File file, String encoding, Font font, int offset, int indent) throws Exception
    {
        this.font = font;
        this.indent = indent;
        InputStreamReader reader = new InputStreamReader(new FileInputStream(file), encoding);
        pReader = new ParagraphReader(reader, offset);
    }

    /*
     * (non-Javadoc)
     * 
     * @see bookshelf.jrender.IParagraphSource#next()
     */
    public Paragraph next() throws Exception
    {
        String para = pReader.read();
        return para == null ? null : new Paragraph(para, font, indent);
    }

}