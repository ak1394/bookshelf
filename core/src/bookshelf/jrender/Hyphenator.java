/*
 * @@DESCRIPTION@@. Copyright (C) @@COPYRIGHT@@
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package bookshelf.jrender;

import java.util.*;

import bookshelf.jrender.element.*;

public class Hyphenator extends AbstractVisitor
{
    private org.apache.fop.layout.hyphenation.Hyphenator hyphenator;
    private int minLeft = 2;
    private int minRemain = 2;
    private String hyphenChar;
    private int hyphenWidth;
    private AbstractElement[] result;
    private int width;

    public Hyphenator(String lang, String hyphenChar)
    {
        this.hyphenChar = hyphenChar;
        hyphenator = new org.apache.fop.layout.hyphenation.Hyphenator(lang, null, minLeft, minRemain);
    }

    public AbstractElement[] hyphenate(AbstractElement element, int width) throws Exception
    {
        this.width = width;
        result = null;
        element.visit(this);
        return result;
    }

    public void visitWord(Word word) throws Exception
    {
        result = hyphenateWord(word, width);
    }

    private Word[] hyphenateWord(Word word, int maxlength) throws Exception
    {
        hyphenWidth = word.width(hyphenChar);
        ArrayList head = new ArrayList();
        ArrayList strChunks = explode(word.getContent());

        int len = 0;
        String s = null;
        Iterator i = strChunks.iterator();
        while (i.hasNext())
        {
            s = (String) i.next();
            int newlen = len + word.width(s);
            if (newlen <= maxlength)
            {
                head.add(s);
                i.remove();
                len = newlen;
            }
            else
            {
                break;
            }
        }
        // If first unhyphenated chunk is too short, traverse back to long
        // enough
        while (s.length() < minLeft + minRemain && head.size() > 0)
        {
            s = (String) head.remove(head.size() - 1);
            strChunks.add(0, s);
            len -= word.width(s);
        }

        // could be the end of string || next word is too big to fit
        if (strChunks.size() > 0 && s != null && s.length() >= minLeft && len < maxlength)
        {
            // try to hyphenate word
            String[] hyphString = hypenateString(s, word, maxlength - len);
            if (hyphString != null)
            {
                // remove word that just has been hyphenated from words
                strChunks.remove(0);
                // add first part to result
                head.add(hyphString[0]);
                // insert last part back to words
                strChunks.add(0, hyphString[1]);
            }
        }

        String headString = concat(head);
        String tailString = concat(strChunks);

        Word[] result = new Word[2];

        if (tailString.length() >= minLeft)
        {
            Word headWord = (Word) word.clone();
            headWord.setContent(headString);

            Word tailWord = (Word) word.clone();
            tailWord.setContent(tailString);

            result[0] = headWord;
            result[1] = tailWord;
        }
        else
        {
            result[0] = (Word) word.clone();
            result[0].setContent("");
            result[1] = word;
        }

        return result;

    }

    private String[] hypenateString(String string, Word word, int maxlen)
    {
        // ajust maxlen for hyphen character length
        maxlen = maxlen - hyphenWidth;
        if (maxlen <= 0)
        {
            return null;
        }

        org.apache.fop.layout.hyphenation.Hyphenation hyphenation = hyphenator.hyphenate(string);
        if (hyphenation == null)
        {
            return null;
        }

        int[] points = hyphenation.getHyphenationPoints();
        int good = -1;

        for (int i = 0; i < points.length; i++)
        {
            if (word.width(string.substring(0, points[i])) <= maxlen)
            {
                good = i;
            }
            else
            {
                break;
            }
        }

        if (good == -1)
        {
            return null;
        }

        String result[] = new String[2];
        result[0] = hyphenation.getPreHyphenText(good) + hyphenChar;
        result[1] = hyphenation.getPostHyphenText(good);
        return result;
    }

    private ArrayList explode(String string)
    {
        ArrayList result = new ArrayList();
        Tokenizer tokenizer = new Tokenizer(string);

        while (tokenizer.hasMoreElements())
        {
            result.add(tokenizer.nextElement());
        }

        return result;
    }

    private String concat(List list)
    {
        String result = "";
        for (Iterator i = list.iterator(); i.hasNext(); result = result + (String) i.next())
            ;
        return result;
    }
}