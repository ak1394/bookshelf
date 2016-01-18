package bookshelf.font;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/**
 * @author Anton Krasovsky <ak1394@users.sourceforge.net>
 *  
 */
class ClearType
{
    Font font;
    Font derivedFont;
    BufferedImage wideBuffer;
    Graphics2D wideGraphics;
    BufferedImage normalBuffer;

    int height;
    int baseline;
    int normalBufferWidth;
    int wideBufferWidth;
    private int[] sourcePixels;
    private int[] resultPixels;
    private FontMetrics fontMetrics;

    class DeepCompositeBuffer
    {
        int[] red;
        int[] green;
        int[] blue;

        DeepCompositeBuffer(int[] data, int offset, int length)
        {
            red = new int[length];
            green = new int[length];
            blue = new int[length];
            for (int i = 0; i < length; i++)
            {
                red[i] = (data[i + offset] >>> 16) & 0xFF;
                green[i] = (data[i + offset] >>> 8) & 0xFF;
                blue[i] = data[i + offset] & 0xFF;
            }
        }

        int getRed(int i)
        {
            return red[i];
        }

        int getGreen(int i)
        {
            return green[i];
        }

        int getBlue(int i)
        {
            return blue[i];
        }

        void setRed(int i, int value)
        {
            red[i] = value;
        }

        void setGreen(int i, int value)
        {
            green[i] = value;
        }

        void setBlue(int i, int value)
        {
            blue[i] = value;
        }

        int getRGB(int i)
        {
            return (red[i] << 16) | (green[i] << 8) | blue[i];

        }
    }

    public ClearType(Font font, FontMetrics fontMetrics, int height, int maxWidth, int baseline, boolean antialias)
    {
        this.font = font;
        this.fontMetrics = fontMetrics;
        this.height = height;
        this.baseline = baseline;

        normalBufferWidth = maxWidth;
        wideBufferWidth = normalBufferWidth * 3;

        derivedFont = font.deriveFont(AffineTransform.getScaleInstance(3, 1));
        wideBuffer = new BufferedImage(wideBufferWidth, height, BufferedImage.TYPE_INT_RGB);
        normalBuffer = new BufferedImage(normalBufferWidth, height, BufferedImage.TYPE_INT_RGB);
        wideGraphics = wideBuffer.createGraphics();
        wideGraphics.setFont(derivedFont);
        if (antialias)
        {
            wideGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        else
        {
            wideGraphics
                    .setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        }

        sourcePixels = ((DataBufferInt) wideBuffer.getRaster().getDataBuffer()).getData();
        resultPixels = ((DataBufferInt) normalBuffer.getRaster().getDataBuffer()).getData();
    }

    public BufferedImage renderGlyph(char c)
    {
        if (!font.canDisplay(c))
        {
            return null;
        }

        // clear buffer
        wideGraphics.setColor(Color.white);
        wideGraphics.fillRect(0, 0, wideBuffer.getWidth(), wideBuffer.getHeight());
        wideGraphics.setColor(Color.black);

        wideGraphics.drawString(Character.toString(c), 0, baseline);
        for (int y = 0; y < height; y++)
        {
            DeepCompositeBuffer src = new DeepCompositeBuffer(sourcePixels, y * wideBufferWidth, wideBufferWidth);
            DeepCompositeBuffer dst = new DeepCompositeBuffer(resultPixels, y * normalBufferWidth, normalBufferWidth);

            filterLine(src, dst, normalBufferWidth);

            for (int x = 0; x < normalBufferWidth; x++)
            {
                dst.setRed(x, (dst.getRed(x) + 6) / 12);
                dst.setGreen(x, (dst.getGreen(x) + 6) / 12);
                dst.setBlue(x, (dst.getBlue(x) + 6) / 12);
                resultPixels[(y * normalBufferWidth) + x] = dst.getRGB(x);
            }
        }

        int width = fontMetrics.charWidth(c);
        if (width == 0)
        {
            return null;
        }
        BufferedImage glyph = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        BufferedImage glyphSubimage = normalBuffer.getSubimage(0, 0, fontMetrics.charWidth(c), height);
        glyphSubimage.copyData(glyph.getRaster());

        return glyph;
    }

    private void filterLine(DeepCompositeBuffer src, DeepCompositeBuffer dst, int width)
    {
        int i, i3, c;

        for (i = 0, i3 = 0; i < width; ++i, i3 += 3)
        {
            /* do red */
            if (i == 0)
                c = 4 * src.getRed(0);
            else
                c = src.getRed(i3 - 2) + 3 * src.getRed(i3 - 1);
            c += 4 * src.getRed(i3) + 3 * src.getRed(i3 + 1) + src.getRed(i3 + 2);
            dst.setRed(i, c);

            /* do green */
            if (i == 0)
                c = src.getGreen(0);
            else
                c = src.getGreen(i3 - 1);
            c += 3 * src.getGreen(i3) + 4 * src.getGreen(i3 + 1) + 3 * src.getGreen(i3 + 2);
            if (i == width - 1)
                c += src.getGreen(i3 + 2);
            else
                c += src.getGreen(i3 + 3);
            dst.setGreen(i, c);

            /* do blue */
            c = src.getBlue(i3) + 3 * src.getBlue(i3 + 1) + 4 * src.getBlue(i3 + 2);
            if (i == width - 1)
                c += 4 * src.getBlue(i3 + 2);
            else
                c += 3 * src.getBlue(i3 + 3) + src.getBlue(i3 + 4);
            dst.setBlue(i, c);
        }
    }
}