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
 * The <code>ActiveCache</code> class provides both the cache and background
 * rendering service for application. After the <code>ActiveCache</code>
 * started it runs in a separate Thread. While it's <code>run</code> method is
 * active, it tries to render number of pages and store them in the internal
 * cache. It's <code>STRATEGY</code> field controls which pages will be
 * rendered.
 * 
 * @author Anton Krasovsky <ak1394@users.sourceforge.net>
 *  
 */
public class ActiveCache implements Runnable
{
    private static final int STRATEGY[] = { 0, 1, 2, -1, 3, -2 };
    private int capacity;
    private int size = 0;
    private int lookaheadRequest;
    private int lookaheadDepth;
    private boolean running;
    private boolean terminated;
    private Thread thread;
    private CacheElement start;
    private Renderer renderer;

    /**
     * Constructs new <code>ActiveCache</code> of given capacity and
     * initializes its <code>Renderer</code>.
     */
    public ActiveCache(int capacity, Renderer renderer)
    {
        // initialize cache's double linked list
        start = new CacheElement(null, -1);
        start.next = start;
        start.previous = start;

        this.renderer = renderer;
        this.capacity = capacity;

        // find out number of steps for look-ahead
        lookaheadDepth = capacity > STRATEGY.length ? STRATEGY.length : capacity;
    }

    /**
     * Starts background rendering thread. This method returns only after the underlying thread
     * created and started to run.
     */
    public void startCache()
    {
        running = false;
        terminated = false;
        thread = new Thread(this);
        thread.start();
        // wait for a thread to start
        while (true)
        {
            synchronized (this)
            {
                if (running)
                    break;
                try
                {
                    this.wait(100);
                }
                catch(Exception ex)
                {
                    // so what?
                }
            }
        }
    }

    /**
     * Stops background rendering thread and purges cache content.
     * 
     * @throws Exception
     */
    public synchronized void stopCache() throws Exception
    {
        // stop cache
        running = false;
        this.notify();
        while (!terminated)
        {
            this.wait();
        }
        // clear cache
        while (size > 0)
        {
            trim();
        }
    }

    /**
     * Gets <code>Slice</code> from the cache.
     */
    public synchronized Slice getSlice(int id) throws Exception
    {
        // notify background thread of a new page request
        this.notify();
        this.lookaheadRequest = id;

        Slice result = search(id);
        if (result != null)
        {
            return result;
        }

        // if the first attempt is not successfull, then sleep
        // and wait until backgrond thread renders requested 
        // page and wakes us
        this.wait();

        // retrieve result
        result = search(id);
        return result;
    }

    private Slice search(int id)
    {
        CacheElement current = start;
        while (current.next != start)
        {
            current = current.next;
            if (current.id == id)
            {
                // check if current is not at the top of the list already
                // if not, move it onto the top of the list
                if (current != start.next)
                {
                    // cut it from current position
                    current.next.previous = current.previous;
                    current.previous.next = current.next;
                    // put it on top of the list
                    current.next = start.next;
                    current.previous = start;
                    current.next.previous = current;
                    this.start.next = current;
                }
                return current.content;
            }
        }
        return null;
    }

    private void trim()
    {
        Slice recycle = start.previous.content;
        start.previous.previous.next = start;
        start.previous = start.previous.previous;
        size--;
        recycle.links.removeAllElements();
        renderer.recycledSlices.push(recycle);
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
        int strategyStep = 0;
        int lookaheadCurrent;

        // wait until the first request comes
        try
        {
            synchronized (this)
            {
                running = true;
                this.wait();
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        lookaheadCurrent = lookaheadRequest;
        while (running)
        {
            try
            {
                int lookaheadWanted;
                while (running && strategyStep < lookaheadDepth)
                {
                    synchronized (this)
                    {
                        // check if our requested page is still ok
                        // if not, update and restart
                        if (lookaheadRequest != lookaheadCurrent)
                        {
                            lookaheadCurrent = lookaheadRequest;
                            strategyStep = 0;
                            continue;
                        }
                        // get page id according to strategy and check if it is
                        // in the cache
                        // if it's found, do a short sleep and go to the next
                        // one
                        lookaheadWanted = lookaheadCurrent + STRATEGY[strategyStep];
                        if (search(lookaheadWanted) != null)
                        {
                            this.notify();
                            Thread.yield();
                            strategyStep++;
                            continue;
                        }

                        // make sure cache is trimmed before trying to
                        // store in there
                        if (size >= capacity)
                        {
                            trim();
                        }
                    }
                    // render requested page
                    Slice result = renderer.renderSlice(lookaheadWanted);
                    synchronized (this)
                    {
                        if (result != null)
                        {
                            // store result
                            CacheElement element = new CacheElement(result, lookaheadWanted);
                            // put it on top of the list
                            element.next = start.next;
                            element.previous = start;
                            start.next.previous = element;
                            start.next = element;
                            size++;
                        }
                        // check if we've not been preempted while rendering
                        // if not, we can safely notify
                        if (lookaheadRequest == lookaheadCurrent)
                        {
                            // we've not been preempted with the new request
                            // while rendering
                            // so notifiy that request has arrived
                            this.notify();
                            Thread.yield();
                        }
                        else
                        {
                            // we've been preempted
                            // go to start
                            lookaheadCurrent = lookaheadRequest;
                            strategyStep = 0;
                            continue;
                        }
                        strategyStep++;
                    }
                }
                // if the strategy is done and no new requests is here
                // sleep until waken
                synchronized (this)
                {
                    if (running && lookaheadRequest == lookaheadCurrent)
                    {
                        System.gc();
                        this.wait();
                    }
                }
                // we've been awaken, restart strategy and lookaheadCurrent
                lookaheadCurrent = lookaheadRequest;
                strategyStep = 0;
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        // notify that cache has been stopped
        terminated = true;
        synchronized (this)
        {
            this.notify();
        }
    }

    private class CacheElement
    {
        CacheElement previous;
        CacheElement next;
        int id;
        Slice content;

        CacheElement(Slice content, int id)
        {
            this.id = id;
            this.content = content;
        }
    }
}
