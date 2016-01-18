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

import java.io.IOException;
import java.io.InputStream;
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

public class Plugin
{
    private static final String RESOURCES_PREFIX = "resources/plugin/";
    private static final String KEYMAP_RESOURCE = "keymap.properties";
    private static final String PROPERTIES_SUFFIX = ".properties";

    private String name;
    private JarFile jarFile;
    private ArrayList classEntries = new ArrayList();
    private PlatformPackage platform;
    private String language = "";
    private static final String KEY_DELIMITER_CHARACTER = " ";
    private Map keyMap;

    public Plugin(String name, PlatformPackage platform, JarFile jarFile) throws Exception
    {
        this.name = name;
        this.platform = platform;
        this.jarFile = jarFile;
        keyMap = getKeyMap();
    }

    public void setLanguage(String language)
    {
        this.language = language;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return "Description of plugin: " + name;
    }

    public long getCompressedSize()
    {
        long size = 0;
        for (Iterator iterator = classEntries.iterator(); iterator.hasNext();)
        {
            JarEntry entry = (JarEntry) iterator.next();
            size = size + entry.getCompressedSize();
        }
        return size;
    }

    public void addClassEntry(JarEntry classEntry)
    {
        classEntries.add(classEntry);
    }

    public Iterator classNameIterator()
    {
        ArrayList classNames = new ArrayList();
        for (Iterator iterator = classEntries.iterator(); iterator.hasNext();)
        {
            JarEntry entry = (JarEntry) iterator.next();
            classNames.add(entry.getName());
        }
        return classNames.iterator();
    }

    public InputStream getClassInputStream(String name) throws Exception
    {
        return jarFile.getInputStream(new JarEntry(name));
    }
    
    public int getKeyCode(String keyName)
    {
        String rotatedKeyName = "ROTATE_" + platform.getRotation() + "_" + keyName;
        if(platform.getRotation() != 0 && keyMap.containsKey(rotatedKeyName))
        {
            keyName = rotatedKeyName;
        }
        return ((Integer)keyMap.get(keyName)).intValue();
    }

    public Properties getStrings()
    {
        Properties properties = new Properties();
        try
        {
            // load default strings
            InputStream stringInputStream = jarFile.getInputStream(new JarEntry(RESOURCES_PREFIX + name + "/"
                    + "strings" + PROPERTIES_SUFFIX));
            properties.load(stringInputStream);
            
            // load localized strings if available
            stringInputStream = jarFile.getInputStream(new JarEntry(RESOURCES_PREFIX + name + "/" + "strings_"
                    + language + PROPERTIES_SUFFIX));
            if (stringInputStream != null)
            {
                properties.load(stringInputStream);
            }
        }
        catch (IOException e)
        {
            // do nothing return empty properties
        }
        catch (NullPointerException e)
        {
            // do nothing return empty properties
        }
        return properties;
    }

    private Map getKeyMap() throws Exception
    {
        HashMap result = new HashMap();
        Properties keymapProperties = new Properties();
        JarEntry keymapEntry = jarFile.getJarEntry(RESOURCES_PREFIX + name + "/" + KEYMAP_RESOURCE);

        if (keymapEntry == null)
        {
            return result;
        }

        keymapProperties.load(jarFile.getInputStream(keymapEntry));

        for (Enumeration e = keymapProperties.keys(); e.hasMoreElements();)
        {
            String keyName = (String) e.nextElement();
            String keyBindingsString = keymapProperties.getProperty(keyName);
            List keyBindings = parseKeyList(keyBindingsString);
            Integer keyCode = getPlatformKeyBinding(keyBindings);
            if (keyCode == null)
            {
                throw new Exception("Unable to find key mapping for: " + keyName + " tried: " + keyBindingsString);
            }
            else
            {
                result.put(keyName, keyCode);
            }
        }
        return result;
    }

    private Integer getPlatformKeyBinding(List keyBindings) throws Exception
    {
        for (Iterator i = keyBindings.iterator(); i.hasNext();)
        {
            String currentKey = (String) i.next();
            if (platform.hasKey(currentKey))
            {
                return new Integer(platform.getKeyCode(currentKey));
            }
        }
        return null;
    }

    private List parseKeyList(String list)
    {
        List result = new ArrayList();
        StringTokenizer tokenizer = new StringTokenizer(list, KEY_DELIMITER_CHARACTER, false);
        while (tokenizer.hasMoreTokens())
        {
            result.add(tokenizer.nextToken());
        }
        return result;
    }
}