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

import java.awt.Font;
import java.awt.image.BufferedImage;

/**
 * @author Anton Krasovsky <ak1394@users.sourceforge.net>
 *
 */
public class SubpixelFont extends SystemFont
{
    private transient ClearType clearType;

    /**
     * @param font
     * @param encoding
     * @throws Exception
     */
    public SubpixelFont(Font font, String encoding) throws Exception
    {
        super(font, encoding);
    }

    protected void makeBuffer(int width, int height)
    {
        clearType = new ClearType(awtFont, fontMetrics, height, maxWidth, baseline, false);
    }

    protected BufferedImage makeGlyph(char c)
    {
        return clearType.renderGlyph(c);
    }

    protected BufferedImage makeGlyph(int width, int height)
    {
        return null;
    }
}
