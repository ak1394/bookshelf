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

package reader;

/**
 * An <code>IPlugin</code> represents loadable module that interacts with
 * {@link reader.Engine}. Most of functionality is available to plugins through
 * the use of {@link reader.Event}objects.
 * 
 * @author Anton Krasovsky <ak1394@users.sourceforge.net>
 *  
 */
public interface IPlugin
{
    /**
     * Handles the <code>Event</code>. If plugin decides to pass event to the
     * next available plugin it returns <code>Event</code> object, possibly
     * the same it just received. Otherwise if it wishes to be the last one that
     * has seen this <code>Event</code> it return <code>null</code>.
     * 
     * @param e
     *            <code>Event</code> object
     * @return <code>Event</code> object or <code>null</code>
     * @throws Exception
     */
    public Event handleEvent(Event e) throws Exception;
}