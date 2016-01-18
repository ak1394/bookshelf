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

import javax.microedition.lcdui.*;

import com.siemens.mp.game.ExtendedImage;
import com.siemens.mp.game.Light;

import reader.Engine;
import reader.Event;
import reader.IPlatformCanvas;
import reader.IPlatformRenderer;
import reader.Slice;

public class PlatformCanvas extends Canvas implements IPlatformCanvas
{
    private Graphics graphics;
    private ExtendedImage extendedImage;

    private Engine engine;

    private int canvasWidth;
    private int canvasHeight;
    private int rowByteWidth;
    private int roundedCanvasWidth;

    public void initCanvas(int canvasWidth, int canvasHeight, int rotation)
    {
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;

        rowByteWidth = (canvasWidth + 7) / 8;
        roundedCanvasWidth = rowByteWidth * 8;

        extendedImage = new ExtendedImage(Image.createImage(roundedCanvasWidth, canvasHeight));
        graphics = extendedImage.getImage().getGraphics();
    }

    public void displayBuffer(Graphics g, int x, int y)
    {
        extendedImage.blitToScreen(x, y);
    }

    public void sliceToBuffer(Slice slice, int x, int y, int height)
    {
        int byteOffset = rowByteWidth * y;
        int byteLength = rowByteWidth * height;
        byte image[] = ((PlatformSlice) slice).image;
        
        extendedImage.setPixels(image, x, y, roundedCanvasWidth, height);
    }

    public void clearBuffer()
    {
        extendedImage.clear((byte)0);
    }

    public void lightOff()
    {
        Light.setLightOff();
    }

    public void lightOn()
    {
        Light.setLightOn();
    }

    /*
     * From AbstractReaderCanvas
     *  
     */
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
        return (Canvas) this;
    }

    public void highlight(int x, int y, int width, int height)
    {
        graphics.drawRect(x, y, width, height);
    }

    /* (non-Javadoc)
     * @see reader.IPlatformCanvas#getRenderer()
     */
    public IPlatformRenderer getRenderer()
    {
        return new PlatformRenderer();
    }
}