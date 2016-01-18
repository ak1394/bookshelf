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

package platform;

import font.PlatformSlice;

import javax.microedition.lcdui.*;

import reader.Engine;
import reader.Event;
import reader.IPlatformCanvas;
import reader.IPlatformRenderer;
import reader.Slice;

public class PlatformCanvas extends Canvas implements IPlatformCanvas
{
    private Image buffer;
    private Graphics bufferGraphics;
    
    private Engine engine;
    
    private int canvasHeight;
    private int canvasWidth;
    private int rotation;

    public void initCanvas(int canvasWidth, int canvasHeight, int rotation)
    {
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        this.rotation = rotation;
        buffer = Image.createImage(canvasWidth, canvasHeight);
        bufferGraphics = buffer.getGraphics();
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

    public void lightOn()
    {
    }

    public void lightOff()
    {
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
}
