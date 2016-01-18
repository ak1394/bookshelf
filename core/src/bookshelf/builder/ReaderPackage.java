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

package bookshelf.builder;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ReaderPackage
{
    private JarFile jarFile;
    private static final String CLASS_PREFIX = "reader/";
    private static final String PLUGIN_PREFIX = "plugin/";
    private static final String FONT_PREFIX = "font/";
    private static final String CLASS_SUFFIX = ".class";
    private static final String PLATFORM_RENDERER_PREFIX = "font/PlatformRenderer";

    private ArrayList classNames = new ArrayList();
    private ArrayList fontClassNames = new ArrayList();
    private HashMap plugins = new HashMap();
    private long compressedSize;
    private PlatformPackage platform;

    public ReaderPackage(String fileName, PlatformPackage platform) throws Exception
    {
        this.jarFile = new JarFile(fileName);
        this.platform = platform;
        readClassNames();
        if (platform.requiresFontClasses())
        {
            readFontClassNames();
        }
        readPluginClassNames();
    }

    public InputStream getClassInputStream(String name) throws Exception
    {
        return jarFile.getInputStream(new JarEntry(name));
    }

    public Iterator classNameIterator()
    {
        return classNames.iterator();
    }

    public Iterator fontClassNameIterator()
    {
        return fontClassNames.iterator();
    }

    public boolean isEngineClass(String className)
    {
        return className.equals(CLASS_PREFIX + "Engine.class") ? true : false;
    }

    public Plugin getPlugin(String plugin) throws Exception
    {
        if (!plugins.containsKey(plugin))
        {
            throw new Exception("Unknown plugin: " + plugin);
        }
        return (Plugin) plugins.get(plugin);
    }

    public List getPlugins() throws Exception
    {
        ArrayList result = new ArrayList();
        for (Iterator iterator = plugins.values().iterator(); iterator.hasNext();)
        {
            result.add(iterator.next());
        }
        return result;
    }

    public long getCompressedSize()
    {
        return compressedSize;
    }

    private void readFontClassNames()
    {
        for (Enumeration e = jarFile.entries(); e.hasMoreElements();)
        {
            JarEntry jarEntry = (JarEntry) e.nextElement();
            String name = jarEntry.getName();
            if (name.startsWith(FONT_PREFIX) && name.endsWith(CLASS_SUFFIX))
            {
                if (name.startsWith(PLATFORM_RENDERER_PREFIX))
                {
                    if(name.equals(PLATFORM_RENDERER_PREFIX + platform.getRotation() + CLASS_SUFFIX))
                    {
                        classNames.add(name);
                        compressedSize = compressedSize + jarEntry.getCompressedSize();
                    }
                }
                else
                {
                    classNames.add(name);
                    compressedSize = compressedSize + jarEntry.getCompressedSize();
                }
            }
        }
    }

    private void readClassNames()
    {
        for (Enumeration e = jarFile.entries(); e.hasMoreElements();)
        {
            JarEntry jarEntry = (JarEntry) e.nextElement();
            String name = jarEntry.getName();
            if (name.startsWith(CLASS_PREFIX) && name.endsWith(CLASS_SUFFIX))
            {
                classNames.add(name);
                compressedSize = compressedSize + jarEntry.getCompressedSize();
            }
        }
    }

    private void readPluginClassNames() throws Exception
    {
        for (Enumeration e = jarFile.entries(); e.hasMoreElements();)
        {
            JarEntry entry = (JarEntry) e.nextElement();
            String name = entry.getName();
            if (name.startsWith(PLUGIN_PREFIX) && name.endsWith(CLASS_SUFFIX))
            {
                // extract plugin name
                String pluginName = name.substring(PLUGIN_PREFIX.length(), name.indexOf('/', PLUGIN_PREFIX.length()));
                if (!plugins.containsKey(pluginName))
                {
                    plugins.put(pluginName, new Plugin(pluginName, platform, jarFile));
                }
                ((Plugin) plugins.get(pluginName)).addClassEntry(entry);
            }
        }
    }
}