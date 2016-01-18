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
 * The <code>Event</code> class provides means of communication for plugins,
 * with the <code>Engine</code> and between each other.
 * 
 * @author Anton Krasovsky <ak1394@users.sourceforge.net>
 *  
 */
public class Event
{
    // types
    public static final int KEY_PRESSED = 1;
    public static final int KEY_RELEASED = 2;
    public static final int KEY_REPEATED = 3;
    public static final int MENU = 4;
    public static final int PAINT = 5;
    public static final int SCROLL = 6;
    public static final int PLUGIN_START = 7;
    public static final int PLUGIN_STOP = 8;
    public static final int RENDERED_WORD = 9;
    public static final int MESSAGE = 10;

    // menu subtypes
    public static final int MENU_REQUEST = 1;
    public static final int MENU_REPLY = 2;
    public static final int MENU_SELECTED = 3;

    // message subtypes
    public static final int MESSAGE_REVERT_CANVAS = 0;
    public static final int MESSAGE_REVERT_CURRENT = 1;

    private int type;
    private int subtype;
    private Object src;
    private Object dst;
    private Object param;

    /**
     * Creates the instance of <code>Event</code> with specified parameters.
     * 
     * @param type
     *            event type
     * @param subtype
     *            event subtype, in case of type <code>KEY_*</code> events, a
     *            key code
     * @param src
     *            source of an event, <code>null</code> if sent by
     *            <code>Engine</code>
     * @param dst
     *            destination for an event
     * @param param
     *            additional parameter
     */
    public Event(int type, int subtype, Object src, Object dst, Object param)
    {
        this.type = type;
        this.subtype = subtype;
        this.src = src;
        this.dst = dst;
        this.param = param;
    }

    /**
     * Returns <code>true</code> if event type if one of
     * <code>Event.KEY_PRESSED, Event.KEY_RELEASED, Event.KEY_REPEATED</code>,
     * <code>null</code> otherwise.
     * 
     * @return <code>true</code> if event type
     *         <code>KEY_*</code> <code>null</code> otherwise
     */
    public boolean isKey()
    {
        return type == Event.KEY_PRESSED || type == Event.KEY_RELEASED || type == Event.KEY_REPEATED ? true : false;
    }

    /**
     * Returns the destination for the event.
     * 
     * @return destination for the event
     */
    public Object getDst()
    {
        return dst;
    }

    /**
     * Sets the destination for the event.
     * 
     * @param dst
     *            destination, normally a {@link IPlugin}instance
     */
    public void setDst(Object dst)
    {
        this.dst = dst;
    }

    /**
     * Returns optional parameter.
     * 
     * @return optional parameter
     */
    public Object getParam()
    {
        return param;
    }

    /**
     * Sets the optional parameter.
     * 
     * @param param
     *            optional parameter
     */
    public void setParam(Object param)
    {
        this.param = param;
    }

    /**
     * Returns the source of the event. Source is set to <code>null</code> if
     * the event was sent by <code>Engine</code>
     * 
     * @return source of the event
     */
    public Object getSrc()
    {
        return src;
    }

    /**
     * Sets source of the event.
     * 
     * @param src
     *            source of the event
     */
    public void setSrc(Object src)
    {
        this.src = src;
    }

    /**
     * Returns the subtype.
     * 
     * @return subtype
     */
    public int getSubtype()
    {
        return subtype;
    }

    /**
     * Sets the subtype of the event.
     * 
     * @param subtype
     *            subtype
     */
    public void setSubtype(int subtype)
    {
        this.subtype = subtype;
    }

    /**
     * Returns the type of the event.
     * 
     * @return type
     */
    public int getType()
    {
        return type;
    }

    /**
     * Sets the type of the event.
     * 
     * @param type
     *            type
     */
    public void setType(int type)
    {
        this.type = type;
    }
}