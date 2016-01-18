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

package bookshelf.builder;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import bookshelf.font.FontWriter;
import bookshelf.font.FontWriterPNG;

public class PlatformPackage
{
    private JarFile jarFile;
    private static final String CLASS_PREFIX = "platform/";
    private static final String PROPERTIES_PREFIX = "properties/platform/";
    private static final String PROPERTIES_SUFFIX = ".properties";
    private static final String ALIAS_SUFFIX = ".alias";
    private static final String SUPPORTED_PLATFORMS = "SUPPORTED_PLATFORMS";
    private static final String DESCRIPTION = "DESCRIPTION";
    private static final String DELIMITERS = ", ";
    private static final String FONT_WRITER = "FONT_WRITER";
    private static final String COMMON_PROPERTIES = "common.properties";
    private static final String CUSTOM_FONT_ENGINE = "CUSTOM_FONT_ENGINE";

    private ArrayList classNames = new ArrayList();
    private Properties phoneProperties;
    private Properties platformProperties;
    private String brand;
    private String model;
    private int rotation;
    private Map supported;
    private long compressedSize;

    public PlatformPackage(String fileName) throws Exception
    {
        this.jarFile = new JarFile(fileName);
    }

    public InputStream getClassInputStream(String name) throws Exception
    {
        return jarFile.getInputStream(new JarEntry(name));
    }

    public Iterator classNameIterator()
    {
        return classNames.iterator();
    }

    /**
     * @return Returns the brand.
     */
    public String getBrand()
    {
        return brand;
    }

    /**
     * @return Returns the model.
     */
    public String getModel()
    {
        return model;
    }

    public Map getSupported() throws Exception
    {
        if (supported == null)
        {
            supported = new HashMap();

            JarEntry commonEntry = jarFile.getJarEntry(PROPERTIES_PREFIX + COMMON_PROPERTIES);
            assert commonEntry != null : "Can't read common properties";

            platformProperties = new Properties();
            platformProperties.load(jarFile.getInputStream(commonEntry));

            String supportedBrandsProperty = platformProperties.getProperty(SUPPORTED_PLATFORMS);
            StringTokenizer brandTokenizer = new StringTokenizer(supportedBrandsProperty, DELIMITERS);
            while (brandTokenizer.hasMoreTokens())
            {
                String supportedBrand = brandTokenizer.nextToken();
                String supportedModelsProperty = platformProperties.getProperty(SUPPORTED_PLATFORMS + "_"
                        + supportedBrand);
                StringTokenizer modelTokenizer = new StringTokenizer(supportedModelsProperty, DELIMITERS);
                List models = new ArrayList();
                while (modelTokenizer.hasMoreTokens())
                {
                    models.add(modelTokenizer.nextToken());
                }
                supported.put(supportedBrand, models);
            }
        }

        return supported;
    }

    public String getDescription()
    {
        return platformProperties.getProperty(DESCRIPTION);
    }
    
    public int getRotation()
    {
        return rotation;
    }
    
    public void setRotation(int rotation)
    {
        this.rotation = rotation;
    }
    
    public void setBrandModel(String brand, String model) throws Exception
    {
        this.brand = brand;
        this.model = model;
        readClassNames();
        readProperties(brand, model);
    }

    public int getKeyCode(String keyName) throws Exception
    {
        try
        {
            return Integer.parseInt(phoneProperties.getProperty(keyName.toUpperCase()));
        }
        catch (NumberFormatException ex)
        {
            throw new Exception("Invalid value (not integer) for key: " + keyName);
        }
    }

    public boolean hasProperty(String name)
    {
        return phoneProperties.containsKey(name.toUpperCase()) ? true : false;
    }
    
    public boolean hasKey(String keyName)
    {
        return phoneProperties.containsKey(keyName.toUpperCase()) ? true : false;
    }

    public int getIntegerProperty(String name) throws Exception
    {
        try
        {
            return Integer.parseInt(phoneProperties.getProperty(name.toUpperCase()));
        }
        catch (NumberFormatException ex)
        {
            throw new Exception("Unable to find a property " + name);
        }
    }

    public String getProperty(String name) throws Exception
    {
        try
        {
            return phoneProperties.getProperty(name.toUpperCase());
        }
        catch (NumberFormatException ex)
        {
            throw new Exception("Unable to find a property " + name);
        }
    }

    public FontWriter getFontWriter() throws Exception
    {
        FontWriter fontWriter;
        
        if(hasProperty(FONT_WRITER))
        {
            String fontWriterClassName = getProperty(FONT_WRITER);
            fontWriter = (FontWriter) Class.forName(fontWriterClassName).newInstance();
        }
        else
        {	
            fontWriter = new FontWriterPNG();
        }
        fontWriter.setPlatform(this);
        return fontWriter;
    }

    public long getCompressedSize()
    {
        return compressedSize;
    }

    public Dimension getCanvasDimension()
    {
        int canvas_width = Integer.parseInt(phoneProperties.getProperty("CANVAS_WIDTH"));
        int canvas_height = Integer.parseInt(phoneProperties.getProperty("CANVAS_HEIGHT"));
        if(rotation == 0)
        {
            return new Dimension(canvas_width, canvas_height);
        }
        else
        {
            // swap width and height if canvas are rotated
            return new Dimension(canvas_height, canvas_width);
        }
    }

    public int getCacheSize() throws Exception
    {
        return getIntegerProperty("CACHE_SIZE");
    }

    public int getBlockSize() throws Exception
    {
        return getIntegerProperty("BLOCK_SIZE");
    }

    public long getMaxMidletSize() throws Exception
    {
        if (phoneProperties.containsKey("MAX_MIDLET_SIZE"))
        {
            return getIntegerProperty("MAX_MIDLET_SIZE");
        }
        else
        {
            return Long.MAX_VALUE;
        }
    }

    private void readClassNames()
    {
        for (Enumeration e = jarFile.entries(); e.hasMoreElements();)
        {
            JarEntry jarEntry = (JarEntry) e.nextElement();
            String name = jarEntry.getName();
            if (name.startsWith(CLASS_PREFIX) && name.length() > CLASS_PREFIX.length() && !name.endsWith("/"))
            {
                classNames.add(name);
                compressedSize = compressedSize + jarEntry.getCompressedSize();
            }
        }
    }

    private void readProperties(String brand, String model) throws Exception
    {
        JarEntry commonEntry = jarFile.getJarEntry(PROPERTIES_PREFIX + COMMON_PROPERTIES);
        JarEntry brandEntry = jarFile.getJarEntry(PROPERTIES_PREFIX + brand.toLowerCase() + "/" + COMMON_PROPERTIES);
        JarEntry modelEntry = getModelEntry(brand, model);

        assert commonEntry != null : "Can't read common properties for platform";
        assert brandEntry != null : "Can't read brand properties for platform";
        assert modelEntry != null : "Can't read model properties for platform";

        phoneProperties = new Properties();
        phoneProperties.load(jarFile.getInputStream(commonEntry));
        phoneProperties.load(jarFile.getInputStream(brandEntry));
        phoneProperties.load(jarFile.getInputStream(modelEntry));
    }

    private JarEntry getModelEntry(String brand, String model) throws Exception
    {
        JarEntry aliasEntry = jarFile.getJarEntry(PROPERTIES_PREFIX + brand.toLowerCase() + "/" + model.toLowerCase()
                + ALIAS_SUFFIX);

        if (aliasEntry == null)
        {
            return jarFile.getJarEntry(PROPERTIES_PREFIX + brand.toLowerCase() + "/" + model.toLowerCase()
                    + PROPERTIES_SUFFIX);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(jarFile.getInputStream(aliasEntry)));
        String modelName = reader.readLine();
        reader.close();

        JarEntry modelEntry = jarFile.getJarEntry(PROPERTIES_PREFIX + brand.toLowerCase() + "/" + modelName
                + PROPERTIES_SUFFIX);

        return modelEntry;
    }

    /**
     * @return
     * @throws Exception
     */
    public boolean requiresFontClasses() throws Exception
    {
        if(hasProperty(CUSTOM_FONT_ENGINE) && getIntegerProperty(CUSTOM_FONT_ENGINE) == 1)
        {
            return false;
        }
        else
        {
            return true;
        }
    }
}