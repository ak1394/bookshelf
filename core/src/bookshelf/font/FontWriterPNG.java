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

package bookshelf.font;

import javax.imageio.ImageIO;

import bookshelf.book.JarOutput;
import bookshelf.builder.PlatformPackage;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Properties;

/**
 * @author anton
 *  
 */
public class FontWriterPNG implements FontWriter
{
    private static final String FONT_IMAGE_WIDTH = "FONT_IMAGE_WIDTH";
    private static final String FONT_IMAGE_HEIGHT = "FONT_IMAGE_HEIGHT";

    FontInfo fontInfo;
    private ArrayList images;
    private GlyphInfo[] glyphInfo;
    // sensible details
    private int maxWidth = 128;
    private int maxHeight = 128;
    private Font font;
    private int rotation;

    public void setPlatform(PlatformPackage platform) throws Exception
    {
        if (platform.hasProperty(FONT_IMAGE_WIDTH))
        {
            maxWidth = platform.getIntegerProperty(FONT_IMAGE_WIDTH);
        }
        if (platform.hasKey(FONT_IMAGE_HEIGHT))
        {
            maxWidth = platform.getIntegerProperty(FONT_IMAGE_HEIGHT);
        }

        rotation = platform.getRotation();
    }

    public void setPhoneProperties(Properties phoneProperties)
    {

        if (phoneProperties.containsKey(FONT_IMAGE_WIDTH) && phoneProperties.containsKey(FONT_IMAGE_HEIGHT))
        {
            maxWidth = Integer.parseInt(phoneProperties.getProperty(FONT_IMAGE_WIDTH));
            maxWidth = Integer.parseInt(phoneProperties.getProperty(FONT_IMAGE_HEIGHT));
        }
    }

    public void writeFont(JarOutput output, String prefix, Font font) throws Exception
    {
        setFont(font);
        if (rotation == 90)
        {
            rotate90();
        }
        else if (rotation == 270)
        {
            rotate270();
        }
        writeFont(output, prefix, font.getId());
    }

    private ImageInfo createImageInfo(int width, int height) throws Exception
    {
        return new ImageInfo(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB));
    }

    private void setFont(Font font) throws Exception
    {
        this.font = font;
        this.fontInfo = new FontInfo(font.getHeight(), font.getFirstChar());
        this.glyphInfo = new GlyphInfo[font.getRealGlyphCount()];
        this.images = new ArrayList();
        int x = 0;
        int y = 0;
        int height = font.getHeight();
        int currentImage = 0;

        ImageInfo info = createImageInfo(maxWidth, maxHeight);

        for (char i = 0; i < font.getRealGlyphCount(); i++)
        {
            BufferedImage glyph = font.getRealGlyph(i);
            // skip glyph if it is not defined in the current font
            if (glyph == null)
            {
                this.glyphInfo[i] = null;
                continue;
            }

            // wrap to a next line if x is too big
            if (x + glyph.getWidth() > maxWidth)
            {
                x = 0;
                y = y + height;
            }

            // wrap to a next image if y is too big
            if (y + height > maxHeight)
            {
                images.add(info);
                info = createImageInfo(maxWidth, maxHeight);
                currentImage++;
                y = 0;
            }

            // remember max width
            if (info.maxWidth < x + glyph.getWidth())
            {
                info.maxWidth = x + glyph.getWidth();
            }

            // remember max height
            if (info.maxHeight < y + height)
            {
                info.maxHeight = y + height;
            }

            info.graphics.drawImage(glyph, null, x, y);

            this.glyphInfo[i] = new GlyphInfo(x, y, glyph.getWidth(), currentImage);

            // do next char
            x = x + glyph.getWidth();
        }
        images.add(info);
    }

    private void writeFont(JarOutput output, String prefix, String name) throws Exception
    {
        ByteArrayOutputStream metricsData = new ByteArrayOutputStream();
        DataOutputStream metrics = new DataOutputStream(metricsData);

        // number of glyphs in font
        metrics.writeShort(this.glyphInfo.length);
        // number of images
        metrics.writeByte(images.size());
        // first char
        metrics.writeByte(this.fontInfo.firstChar);
        // font height
        metrics.writeByte(this.fontInfo.height);
        // glyph metrics
        for (int i = 0; i < this.glyphInfo.length; i++)
        {
            GlyphInfo glyph = this.glyphInfo[i];
            if (glyph == null)
            {
                metrics.writeByte(-1);
            }
            else
            {
                metrics.writeByte(glyph.width);
                metrics.writeByte(glyph.x);
                metrics.writeByte(glyph.y);
                metrics.writeByte(glyph.image);
            }
        }

        output.putNextEntry(prefix + name + ".metrics");
        metrics.flush();
        output.write(metricsData.toByteArray());

        for (int i = 0; i < images.size(); i++)
        {
            ByteArrayOutputStream imageData = new ByteArrayOutputStream();
            // write subimage
            ImageInfo info = (ImageInfo) images.get(i);
            ImageIO.write(info.image.getSubimage(0, 0, info.maxWidth, info.maxHeight), "png", imageData);
            output.putNextEntry(prefix + name + i + ".png");
            output.write(imageData.toByteArray());
        }
    }

    public void rotate90()
    {
        AffineTransform transform[] = new AffineTransform[images.size()];

        for (int i = 0; i < images.size(); i++)
        {
            ImageInfo info = (ImageInfo) images.get(i);
            transform[i] = AffineTransform.getRotateInstance(Math.toRadians(90));
            transform[i].translate(0, info.maxHeight * -1);
            AffineTransformOp transformOp = new AffineTransformOp(transform[i], null);
            BufferedImage resultingImage = new BufferedImage(info.maxHeight, info.maxWidth, info.image.getType());
            transformOp.filter(info.image.getSubimage(0, 0, info.maxWidth, info.maxHeight), resultingImage);
            info.image = resultingImage;
            int tmp = info.maxHeight;
            info.maxHeight = info.maxWidth;
            info.maxWidth = tmp;
        }

        for (int i = 0; i < glyphInfo.length; i++)
        {
            if (glyphInfo[i] != null)
            {
                Point2D point = transform[glyphInfo[i].image]
                        .transform(new Point(glyphInfo[i].x, glyphInfo[i].y), null);
                glyphInfo[i].x = (int) (point.getX() - font.getHeight());
                glyphInfo[i].y = (int) point.getY();
            }
        }
    }

    public void rotate270()
    {
        AffineTransform transform[] = new AffineTransform[images.size()];

        for (int i = 0; i < images.size(); i++)
        {
            ImageInfo info = (ImageInfo) images.get(i);
            transform[i] = AffineTransform.getRotateInstance(Math.toRadians(270));
            transform[i].translate(info.maxWidth * -1, 0);
            AffineTransformOp transformOp = new AffineTransformOp(transform[i], null);
            BufferedImage resultingImage = new BufferedImage(info.maxHeight, info.maxWidth, info.image.getType());
            transformOp.filter(info.image.getSubimage(0, 0, info.maxWidth, info.maxHeight), resultingImage);
            info.image = resultingImage;
            int tmp = info.maxHeight;
            info.maxHeight = info.maxWidth;
            info.maxWidth = tmp;
        }

        for (int i = 0; i < glyphInfo.length; i++)
        {
            if (glyphInfo[i] != null)
            {
                Point2D point = transform[glyphInfo[i].image]
                        .transform(new Point(glyphInfo[i].x, glyphInfo[i].y), null);
                glyphInfo[i].x = (int) point.getX();
                glyphInfo[i].y = (int) point.getY() - glyphInfo[i].width;
            }
        }
    }

    class FontInfo
    {
        int height;
        int firstChar;

        FontInfo(int height, int firstChar)
        {
            this.height = height;
            this.firstChar = firstChar;
        }
    }

    class ImageInfo
    {
        int maxWidth;
        int maxHeight;
        BufferedImage image;
        Graphics2D graphics;

        ImageInfo(BufferedImage image)
        {
            this.image = image;
            graphics = image.createGraphics();
        }
    }

    class GlyphInfo
    {
        int x;
        int y;
        int width;
        int image;

        GlyphInfo(int x, int y, int width, int image)
        {
            this.x = x;
            this.y = y;
            this.width = width;
            this.image = image;
        }
    }
}