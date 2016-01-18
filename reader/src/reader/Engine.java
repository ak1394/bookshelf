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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Stack;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Graphics;
import javax.microedition.midlet.MIDlet;
import javax.microedition.rms.RecordStore;

/**
 * The <code>Engine</code> class is a core class of application. It extends
 * {@link javax.microedition.midlet.MIDlet}and provides the base for MIDlet
 * execution on the mobile platform. It controls startup/shutdown of the
 * application and plugins, as well as the access to the persistent storage
 * (RMS).
 * 
 * @author Anton Krasovsky <ak1394@users.sourceforge.net>
 *  
 */
public class Engine extends MIDlet
{
    private static final String STORE_NAME = "store";
    private static final int STORE_BYTES_PER_READER = 6;
    private static final int STORE_BYTES_PER_BOOK = 16;
    private Display display;
    private Canvas canvas;
    private RecordStore recordStore;
    private Stack plugins;

    private Renderer renderer;
    private Pager pager;
    private ActiveCache cache;

    private IPlatformRenderer platformRenderer;
    private IPlatformCanvas platformCanvas;
    private JarBook jarBook;
    private byte store[];
    private boolean newRecordStore;

    // auto vars
    public String[] bookTitle;
    public String[] pluginList;
    public static int canvasWidth;
    public static int canvasHeight;
    public static int cacheSize;
    public static int blockSize;
    public static int magic;
    public static int rotation;

    /**
     * Returns Display object of this MIDlet.
     * 
     * @return Display of the current MIDlet
     */
    public Display getDisplay()
    {
        return display;
    }

    /**
     * Returns Canvas object using the current plaform implementaiton.
     * 
     * @see IPlatformCanvas#getCanvas
     * 
     * @return Canvas
     */
    public Canvas getCanvas()
    {
        return canvas;
    }

    /**
     * Returns IPlatformCanvas object.
     * 
     * @return IPlatformCanvas object
     */
    public IPlatformCanvas getPlatformCanvas()
    {
        return platformCanvas;
    }

    /**
     * Returns true if this MIDlet has been started for the first time and no
     * existing record store has been found. Also returns true if the existing
     * record store has been found, but failed to pass magic number check and no
     * information been read from this record store.
     * 
     * @return true if this MIDlet has not read any information from persitent
     *         record store ; false otherwise
     */
    public boolean isNewRecordStore()
    {
        return newRecordStore;
    }

    /**
     * Returns current IBook.
     * 
     * @return current IBook object
     */
    public IBook getBook()
    {
        return jarBook;
    }

    /**
     * Returns Pager object.
     * 
     * @return Pager object
     */
    public Pager getPager()
    {
        return pager;
    }

    /**
     * Returns title of the current book.
     * 
     * @return title of the current book
     */
    public String getTitle()
    {
        return bookTitle[store[0]];
    }

    /**
     * Returns list of book tiles in this MIDlet.
     * 
     * @return list of book titles
     */
    public String[] getTitles()
    {
        return bookTitle;
    }

    /**
     * Returns number of loaded plugins.
     * 
     * @return number of plugins
     */
    public int getPluginCount()
    {
        return pluginList.length;
    }

    /**
     * Activates specified plugin, by placing additional reference to the plugin
     * onto the top of the plugin stack.
     * 
     * @param plugin
     *            Plugin to activate
     */
    public void activatePlugin(IPlugin plugin)
    {
        plugins.push(plugin);
    }

    /**
     * Tries to remove previously activated plugin, looking from the top of the
     * plugin stack.
     * 
     * @param plugin
     *            Plugin to deactivate
     */
    public void deactivatePlugin(IPlugin plugin)
    {
        for (int i = plugins.size() - 1; i > pluginList.length; i--)
        {
            if (plugins.elementAt(i) == plugin)
            {
                plugins.removeElementAt(i);
                return;
            }
        }
    }

    /**
     * Opens the book specified by index in list of book titles, loads all
     * persistent information associated with it and restats plugins.
     * 
     * @param context
     *            id of the book
     */
    public void setContext(int context)
    {
        try
        {
            stopReader();
            store[0] = (byte) context;
            startReader();

        }
        catch (Exception ex)
        {
            printException("setContext()", ex);
        }
    }

    /**
     * Repaints current Canvas. Sends Event.PAINT message down the stack of
     * plugins. It is up to one of plugins to do actual painting.
     * 
     * @param g
     *            Graphics object
     */
    public void paint(Graphics g)
    {
        sendEvent(new Event(Event.PAINT, -1, this, null, g));
    }

    /**
     * Calls the {@link IPlugin#handleEvent(Event)}method of each plugin
     * stating from top of the stack, for each plugin in the stack until the
     * bottom of the stack is reached or one of the plugins returns null.
     * 
     * @param event
     *            Event object
     */
    public void sendEvent(Event event)
    {
        sendEvent(event, true);
    }

    /**
     * Terminates the MIDlet
     */
    public void exit()
    {
        destroyApp(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.microedition.midlet.MIDlet#pauseApp()
     */
    protected void pauseApp()
    {
        try
        {
            stopReader();
        }
        catch (Exception ex)
        {
            printException("pauseApp()", ex);
        }
        this.notifyPaused();
    }

    /**
     * @see javax.microedition.midlet.MIDlet#startApp()
     */
    protected void startApp()
    {
        try
        {
            initReader();
            startReader();
        }
        catch (Exception ex)
        {
            printException("startApp()", ex);
        }
    }

    /**
     * @see javax.microedition.midlet.MIDlet#destroyApp(boolean)
     */
    protected void destroyApp(boolean b)
    {
        try
        {
            stopReader();
        }
        catch (Exception ex)
        {
            printException("destroyApp()", ex);
        }
        this.notifyDestroyed();
    }

    private void initReader() throws Exception
    {
        // prepare JarBook
        jarBook = new JarBook(blockSize);

        // Display splash screen
        display = Display.getDisplay(this);
        
        // PlatformCanvas
        platformCanvas = (IPlatformCanvas) Class.forName("platform.PlatformCanvas").newInstance();
        platformCanvas.initCanvas(canvasWidth, canvasHeight, rotation);
        
        // PlatformRenderer
        platformRenderer = platformCanvas.getRenderer();
        platformRenderer.bufferInitialize(canvasWidth, canvasHeight, rotation);

        // Init fields
        renderer = new Renderer(platformRenderer, cacheSize);
        cache = new ActiveCache(cacheSize, renderer);

        pager = new Pager(platformCanvas, cache, this);
        canvas = platformCanvas.getCanvas();

        // Read persistent data
        openStore();

        // Load plugins
        plugins = new Stack();

        // first plugin in the list must be the top plugin at the stack
        for (int i = pluginList.length - 1; i >= 0; i--)
        {
            System.gc();
            IPlugin p = (IPlugin) Class.forName("plugin." + pluginList[i] + ".Plugin").newInstance();
            plugins.push(p);
        }
    }

    private void startReader() throws Exception
    {
        // Open book
        jarBook.openBook(Integer.toString(store[0]));
        
        renderer.setBook(jarBook);
        renderer.setBackground(jarBook.getColor());
        cache.startCache();
        pager.setBook(jarBook);

        // Start plugins
        int offset = STORE_BYTES_PER_READER + (STORE_BYTES_PER_BOOK * store[0]);
        DataInputStream is = new DataInputStream(new ByteArrayInputStream(store, offset, STORE_BYTES_PER_BOOK));
        sendEvent(new Event(Event.PLUGIN_START, -1, this, null, is), false);

        // Activate user input
        platformCanvas.setEngine(this);
        display.setCurrent(canvas);
        canvas.repaint();
    }

    private void stopReader() throws Exception
    {
        // Stop plugins
        // prepare buffer for persistent data
        ByteArrayOutputStream baos = new ByteArrayOutputStream(STORE_BYTES_PER_BOOK);
        DataOutputStream dos = new DataOutputStream(baos);
        // tell all plugins to save their data
        sendEvent(new Event(Event.PLUGIN_STOP, -1, this, null, dos), false);
        byte[] data = baos.toByteArray();
        int offset = STORE_BYTES_PER_READER + (STORE_BYTES_PER_BOOK * store[0]);
        System.arraycopy(data, 0, store, offset, data.length);

        // Save persistent data
        saveStore();
        // Stop renderer
        cache.stopCache();
    }

    private void printException(String where, Exception e)
    {
        System.out.println(where + ": " + e.getMessage() + " " + e.getClass());
        e.printStackTrace();
        display.setCurrent(null);
        this.notifyDestroyed();
    }

    private void sendEvent(Event event, boolean active)
    {
        int start;

        // start from active plugin, or start from last plugin in pluginList
        if (active)
        {
            start = this.plugins.size() - 1;
        }
        else
        {
            start = pluginList.length - 1;
        }

        for (int i = start; i >= 0; i--)
        {
            IPlugin plugin = (IPlugin) plugins.elementAt(i);
            try
            {
                if (plugin.handleEvent(event) == null)
                {
                    return;
                }
            }
            catch (Exception ex)
            {
                printException("sendEvent()", ex);
            }
        }
    }

    /*
     * If record store closed immediately, and reopened on destroyApp() on the
     * Seiemens SL45i it will be saved into the wrong place so don't close
     * recordstore until exit
     */
    private void openStore() throws Exception
    {
        recordStore = RecordStore.openRecordStore(STORE_NAME, true);
        if (recordStore.getNumRecords() > 0)
        {
            store = recordStore.getRecord(1);
            int rmsMagic = ((store[2] & 0xff) << 24) | ((store[3] & 0xff) << 16) | ((store[4] & 0xff) << 8)
                    | (store[5] & 0xff);
            if (rmsMagic == magic)
            {
                newRecordStore = false;
                return;
            }
        }
        newRecordStore = true;
        store = new byte[STORE_BYTES_PER_READER + (STORE_BYTES_PER_BOOK * bookTitle.length)];
        store[2] = (byte) (0xff & (magic >> 24));
        store[3] = (byte) (0xff & (magic >> 16));
        store[4] = (byte) (0xff & (magic >> 8));
        store[5] = (byte) (0xff & magic);
    }

    private void saveStore() throws Exception
    {
        if (recordStore.getNumRecords() > 0)
        {
            recordStore.setRecord(1, store, 0, store.length);
        }
        else
        {
            recordStore.addRecord(store, 0, store.length);
        }
    }
}
