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

import bookshelf.jrender.element.*;

public class Splitter extends AbstractVisitor
{
    private AbstractElement[] result;
    private int width;

    public AbstractElement[] split(AbstractElement element, int width) throws Exception
    {
        this.width = width;
        result = null;
        element.visit(this);
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see bookshelf.jrender.ElementVisitor#visitWord(bookshelf.jrender.Word)
     */
    public void visitWord(Word word) throws Exception
    {
        String content = word.getContent();
        int i;
        for (i = 1; i < content.length(); i++)
        {
            if (word.width(content.substring(0, i)) >= width)
            {
                break;
            }
        }

        if (word.width(content.substring(0, i)) > width)
        {
            i--;
        }

        String head = content.substring(0, i);
        String tail = content.substring(i);

        result = new AbstractElement[2];

        Word headWord = (Word) word.clone();
        headWord.setContent(head);
        result[0] = headWord;

        Word tailWord = (Word) word.clone();
        tailWord.setContent(tail);
        result[1] = tailWord;
    }

} // end of class
