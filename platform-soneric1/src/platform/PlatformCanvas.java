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

package platform;

import java.io.InputStream;

import font.PlatformSlice;

import javax.microedition.lcdui.*;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;

import reader.Engine;
import reader.Event;
import reader.IPlatformCanvas;
import reader.IPlatformRenderer;
import reader.Slice;

public class PlatformCanvas extends Canvas implements IPlatformCanvas, CommandListener, Runnable
{
    private Image buffer;
    private Graphics bufferGraphics;

    private Engine engine;

    private int canvasHeight;
    private int canvasWidth;

    private static int KEY_PLATFORM_OK = 1001;
    private static int KEY_PLATFORM_CANCEL = 1002;

    private boolean backlight = false;
    private static final long BACKLIGHT_THREAD_SLEEP = 15000;
    private int rotation;

    public void initCanvas(int canvasWidth, int canvasHeight, int rotation)
    {
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        this.rotation = rotation;
        buffer = Image.createImage(canvasWidth, canvasHeight);
        bufferGraphics = buffer.getGraphics();

        addCommand(new Command("", Command.OK, 1));
        addCommand(new Command("", Command.CANCEL, 1));
        setCommandListener(this);

        Thread backlightThread = new Thread(this);
        backlightThread.start();
    }

    public void displayBuffer(Graphics g, int x, int y)
    {
        g.drawImage(buffer, x, y, Graphics.TOP | Graphics.LEFT);
    }

    public void sliceToBuffer(Slice slice, int x, int y, int height)
    {
        // TODO bufferGraphics.setClip()
        PlatformSlice platformSlice = (PlatformSlice) slice;
        switch(rotation)
        {
            case 90:
                bufferGraphics.drawImage(platformSlice.image, -y, x, Graphics.TOP|Graphics.LEFT);
                break;
            case 270:
                bufferGraphics.drawImage(platformSlice.image, y, x, Graphics.TOP|Graphics.LEFT);
                break;
            default:
                bufferGraphics.drawImage(platformSlice.image, x, y, Graphics.TOP|Graphics.LEFT);
        }
    }

    public void clearBuffer()
    {
        bufferGraphics.setColor(255, 255, 255);
        bufferGraphics.fillRect(0, 0, canvasWidth, canvasHeight);
    }

    public void setEngine(Engine e)
    {
        engine = e;
    }

    public void keyPressed(int keyCode)
    {
        engine.sendEvent(new Event(Event.KEY_PRESSED, keyCode, engine, null, null));
    }

    public void keyReleased(int keyCode)
    {
        engine.sendEvent(new Event(Event.KEY_RELEASED, keyCode, engine, null, null));
    }

    public void keyRepeated(int keyCode)
    {
        engine.sendEvent(new Event(Event.KEY_REPEATED, keyCode, engine, null, null));
    }

    public void paint(Graphics g)
    {
        engine.paint(g);
    }

    public Canvas getCanvas()
    {
        return this;
    }

    public void highlight(int x, int y, int width, int height)
    {
    }

    public void run()
    {
        while (true)
        {
            try
            {
                if (backlight)
                {
                    InputStream is = this.getClass().getResourceAsStream("/platform/lighton.imy");
                    Player player = Manager.createPlayer(is, "audio/imelody");
                    player.setLoopCount(1);
                    player.prefetch();
                    player.start();
                }
                Thread.sleep(BACKLIGHT_THREAD_SLEEP);
            }
            catch (Exception e)
            {
            }
        }
    }

    public void lightOn()
    {
        backlight = true;
    }

    public void lightOff()
    {
        backlight = false;
    }

    /* (non-Javadoc)
     * @see reader.IPlatformCanvas#getRenderer()
     */
    public IPlatformRenderer getRenderer() throws Exception
    {
        String klass;
        
        switch(rotation)
        {
            case 90:
                klass = "font.PlatformRenderer90";
                break;
            case 270:
                klass = "font.PlatformRenderer270";
                break;
            default:
                klass = "font.PlatformRenderer0";
        }
        return (IPlatformRenderer) Class.forName(klass).newInstance();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.microedition.lcdui.CommandListener#commandAction(javax.microedition.lcdui.Command,
     *      javax.microedition.lcdui.Displayable)
     */
    public void commandAction(Command command, Displayable displayable)
    {
        if (command.getCommandType() == Command.CANCEL)
            keyPressed(KEY_PLATFORM_CANCEL);

        if (command.getCommandType() == Command.OK)
            keyPressed(KEY_PLATFORM_OK);
    }
}