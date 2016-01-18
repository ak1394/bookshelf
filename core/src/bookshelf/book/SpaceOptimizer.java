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

package bookshelf.book;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.Map;
import java.util.Map.Entry;

import bookshelf.book.element.AbstractVisitor;
import bookshelf.book.element.Book;
import bookshelf.book.element.Page;
import bookshelf.book.element.Space;

/**
 * @author Anton Krasovsky <ak1394@mail.ru>
 *  
 */
public class SpaceOptimizer extends AbstractVisitor
{
    private TreeMap spaceMap;
    private final static int REGISTER_SIZE = 9;

    public List optimizeSpaces(Book book) throws Exception
    {
        ArrayList list = new ArrayList();
        ArrayList registers = new ArrayList();

        Merger merger = new Merger();
        merger.mergeSpaces(book);

        spaceMap = new TreeMap();

        for (Iterator iterator = book.getPages().iterator(); iterator.hasNext();)
        {
            Page page = (Page) iterator.next();
            page.visit(this);
        }

        for (Iterator iterator = spaceMap.entrySet().iterator(); iterator.hasNext();)
        {
            Map.Entry entry = (Entry) iterator.next();
            list.add(new SpaceCounter((Integer) entry.getKey(), (Integer) entry.getValue()));
        }

        Collections.sort(list, new SpaceCounterComparator());

        int counter = 0;
        for (Iterator iterator = list.iterator(); iterator.hasNext() && counter < REGISTER_SIZE;)
        {
            SpaceCounter sc = (SpaceCounter) iterator.next();
            registers.add(new Integer(sc.width));
            counter++;
        }

        Updater updater = new Updater();
        updater.updateSpaces(book, registers);

        return registers;
    }

    public void visitSpace(Space space) throws Exception
    {
        Integer width = new Integer(space.getWidth());
        Integer counter = new Integer(1);
        if (spaceMap.containsKey(width))
        {
            counter = (Integer) spaceMap.get(width);
            counter = new Integer(counter.intValue() + 1);
        }
        spaceMap.put(width, counter);
    }

    class SpaceCounter
    {
        int count;
        int width;

        SpaceCounter(Integer width, Integer count)
        {
            this.width = width.intValue();
            this.count = count.intValue();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#toString()
         */
        public String toString()
        {
            return width + ": " + count;
        }

    }

    class SpaceCounterComparator implements Comparator
    {

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(Object o1, Object o2)
        {
            SpaceCounter s1 = (SpaceCounter) o1;
            SpaceCounter s2 = (SpaceCounter) o2;
            if (s1.count == s2.count)
            {
                return 0;
            } else if (s1.count > s2.count)
            {
                return -1;
            } else
            {
                return 1;
            }
        }
    }

    class Merger extends AbstractVisitor
    {
        void mergeSpaces(Book book) throws Exception
        {
            for (Iterator iterator = book.getPages().iterator(); iterator.hasNext();)
            {
                Page page = (Page) iterator.next();
                merge(page);
            }
        }

        void merge(Page page)
        {
            Iterator iterator = page.getContentIterator();
            if (iterator.hasNext())
            {
                Object previous = iterator.next();
                for (; iterator.hasNext();)
                {
                    Object current = iterator.next();
                    if (previous instanceof Space && current instanceof Space)
                    {
                        Space previousSpace = (Space) previous;
                        Space currentSpace = (Space) current;
                        previousSpace.setWidth(previousSpace.getWidth() + currentSpace.getWidth());
                        iterator.remove();
                    } else
                    {
                        previous = current;
                    }
                }
            }
        }
    }

    class Updater extends AbstractVisitor
    {
        List registers;

        void updateSpaces(Book book, List registers) throws Exception
        {
            this.registers = registers;

            for (Iterator iterator = book.getPages().iterator(); iterator.hasNext();)
            {
                Page page = (Page) iterator.next();
                page.visit(this);
            }
        }

        public void visitSpace(Space space) throws Exception
        {
            Integer width = new Integer(space.getWidth());
            if (registers.contains(width))
            {
                space.setRegister(registers.indexOf(width));
            }
        }
    }
}