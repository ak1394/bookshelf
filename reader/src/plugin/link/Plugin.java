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

package plugin.link;

import java.util.Stack;
import java.util.Vector;

import reader.Link;
import reader.Pager;
import reader.IPlugin;
import reader.Event;
import reader.Engine;

public class Plugin implements IPlugin
{
    public static int aI_KEYCODE_UP;
    public static int aI_KEYCODE_DOWN;
    public static int aI_KEYCODE_FORWARD;
    public static int aI_KEYCODE_BACKWARD;

    private static final int ST_DORMANT = 1;
    private static final int ST_ACTIVE = 2;

    private static final int EV_UNKNOWN = -1;
    private static final int EV_SCROLL_UP = 1;
    private static final int EV_SCROLL_DOWN = 2;
    private static final int EV_NEXT = 3;
    private static final int EV_PREV = 4;
    private static final int EV_FORWARD = 5;
    private static final int EV_BACK = 6;
    private static final int EV_START = 7;
    private static final int EV_REFRESH = 8;

    private Engine engine;
    private Pager pager;

    private static final int LINKS_PER_PAGE = 10;
    private static final int LINKS_INCREMENT = 4;
    private static final int HISTORY_SIZE = 10;

    private Vector links = new Vector(LINKS_PER_PAGE, LINKS_INCREMENT);
    private Stack history = new Stack();

    private int currentLink = 0;

    private int state = ST_DORMANT;

    private int parse(Event event)
    {
        int type = event.getType();
        int k = event.getSubtype();

        if (type == Event.KEY_PRESSED || type == Event.KEY_REPEATED)
        {
            if (k == aI_KEYCODE_UP)
            {
                return EV_PREV;
            }

            if (k == aI_KEYCODE_DOWN)
            {
                return EV_NEXT;
            }

            if (k == aI_KEYCODE_FORWARD && type == Event.KEY_PRESSED)
            {
                return EV_FORWARD;
            }

            if (k == aI_KEYCODE_BACKWARD && type == Event.KEY_PRESSED)
            {
                return EV_BACK;
            }
        }

        if (type == Event.SCROLL)
        {
            if (k > 0)
            {
                return EV_SCROLL_DOWN;
            }
            else if (k < 0)
            {
                return EV_SCROLL_UP;
            }
            else
            {
                return EV_REFRESH;
            }
        }

        if (type == Event.PLUGIN_START)
        {
            return EV_START;
        }

        return EV_UNKNOWN;
    }

    public Event handleEvent(Event event) throws Exception
    {
        int ev = parse(event);
        switch (state)
        {
        case ST_DORMANT:
            switch (ev)
            {
            case EV_START:
                engine = (Engine) event.getSrc();
                pager = engine.getPager();
                return event;
            case EV_REFRESH:
            case EV_SCROLL_DOWN:
                updateVisibleLinks();
                if (links.size() > 0)
                {
                    state = ST_ACTIVE;
                    currentLink = 0;
                    engine.activatePlugin(this);
                    showLink();
                }
                return event;
            case EV_SCROLL_UP:
                updateVisibleLinks();
                if (links.size() > 0)
                {
                    state = ST_ACTIVE;
                    currentLink = links.size() - 1;
                    engine.activatePlugin(this);
                    showLink();
                }
                return event;
            case EV_BACK:
                backHistory();
                return null;
            }
            break;
        case ST_ACTIVE:
            switch (ev)
            {
            case EV_REFRESH:
                showLink();
                return event;
            case EV_SCROLL_DOWN:
                if (!scrollDown())
                {
                    state = ST_DORMANT;
                    engine.deactivatePlugin(this);
                }
                return event;
            case EV_SCROLL_UP:
                if (!scrollUp())
                {
                    state = ST_DORMANT;
                    engine.deactivatePlugin(this);
                }
            case EV_NEXT:
                if (!highlightNext())
                {
                    state = ST_DORMANT;
                    engine.deactivatePlugin(this);
                    return event;
                }
                return null;
            case EV_PREV:
                if (!highlightPrevious())
                {
                    state = ST_DORMANT;
                    engine.deactivatePlugin(this);
                    return event;
                }
                return null;
            case EV_FORWARD:
                int dest = ((Link) links.elementAt(currentLink)).destination;
                // save current page
                history.push(new Integer(pager.getCurrentPage()));
                if (history.size() > HISTORY_SIZE)
                {
                    history.removeElementAt(0);
                }
                engine.getPager().setCurrentPage(dest);
                engine.getCanvas().repaint();
                return null;
            case EV_BACK:
                backHistory();
                return null;
            }
        }
        // event is not processed
        return event;
    }

    private void showLink() throws Exception
    {
        pager.refresh();
        pager.highlight((Link) links.elementAt(currentLink));
        engine.getCanvas().repaint();
    }

    private void updateVisibleLinks() throws Exception
    {
        links.removeAllElements();
        for (int page = pager.getCurrentPage(); pager.isPageVisible(page); page++)
        {
            Vector l = pager.getLinks(page);
            for (int i = 0; i < l.size(); i++)
            {
                if (pager.isLinkVisible((Link) l.elementAt(i)))
                {
                    links.addElement(l.elementAt(i));
                }
            }
        }
    }

    private boolean highlightNext() throws Exception
    {
        if (currentLink < links.size() - 1)
        {
            currentLink++;
            if (pager.isLinkVisible((Link) links.elementAt(currentLink)))
            {
                showLink();
                return true;
            }
        }
        Link l = (Link) links.elementAt(currentLink);
        updateVisibleLinks();
        for (int i = 0; i < links.size(); i++)
        {
            Link ll = (Link) links.elementAt(i);
            if (ll.page > l.page || (ll.page == l.page && ll.id > l.id))
            {
                currentLink = i;
                showLink();
                return true;
            }
        }
        return false;
    }

    private boolean highlightPrevious() throws Exception
    {
        if (currentLink > 0)
        {
            currentLink--;
            if (pager.isLinkVisible((Link) links.elementAt(currentLink)))
            {
                showLink();
                return true;
            }
        }
        Link l = (Link) links.elementAt(currentLink);
        updateVisibleLinks();
        for (int i = links.size() - 1; i >= 0; i--)
        {
            Link ll = (Link) links.elementAt(i);
            if (ll.page < l.page || (ll.page == l.page && ll.id < l.id))
            {
                currentLink = i;
                showLink();
                return true;
            }
        }
        return false;
    }

    private boolean scrollUp() throws Exception
    {
        for (int i = currentLink; i >= 0; i--)
        {
            if (pager.isLinkVisible((Link) links.elementAt(i)))
            {
                currentLink = i;
                showLink();
                return true;
            }
        }
        // if we've got here then no visible links anymore
        updateVisibleLinks();
        if (links.size() > 0)
        {
            currentLink = links.size() - 1;
            showLink();
            return true;
        }

        return false;
    }

    private boolean scrollDown() throws Exception
    {
        for (int i = currentLink; i < links.size(); i++)
        {
            if (pager.isLinkVisible((Link) links.elementAt(i)))
            {
                currentLink = i;
                showLink();
                return true;
            }
        }
        // if we've got here then no visible links anymore
        updateVisibleLinks();
        if (links.size() > 0)
        {
            currentLink = 0;
            showLink();
            return true;
        }

        return false;
    }

    private void backHistory() throws Exception
    {
        if (history.isEmpty())
        {
            return;
        }
        int page = ((Integer) history.pop()).intValue();
        engine.getPager().setCurrentPage(page);
        engine.getCanvas().repaint();
    }

} // end of class
