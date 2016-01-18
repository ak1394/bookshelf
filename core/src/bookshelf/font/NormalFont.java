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
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * @author Anton Krasovsky <ak1394@users.sourceforge.net>
 *
 */
public class NormalFont extends SystemFont
{

    /**
     * @param font
     * @param encoding
     * @throws Exception
     */
    public NormalFont(Font font, String encoding) throws Exception
    {
        super(font, encoding);
    }

    /* (non-Javadoc)
     * @see bookshelf.font.SystemFont#makeBuffer(int, int)
     */
    protected void makeBuffer(int width, int height)
    {
        buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        bufferGraphics = buffer.createGraphics();
        bufferGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    }
}
