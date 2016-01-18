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

package bookshelf.builder;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * @author anton
 *  
 */
public class DescriptorBuilder
{
    private static final String VENDOR_VALUE = "Anton Krasovsky";
    private static final String VERSION_VALUE = "@@VERSION@@";
    private static final String MIDLET1_VALUE = ", , reader.Engine";
    private static final String MANIFEST_VERSION_VALUE = "1.0";
    private static final String PROFILE_VALUE = "MIDP-1.0";
    private static final String CONFIGURATION_VALUE = "CLDC-1.0";
    private static final String PROFILE_VALUE2 = "MIDP-2.0";
    private static final String CONFIGURATION_VALUE2 = "CLDC-1.1";

    private static final String MIDLET_VERSION = "MIDlet-Version";
    private static final String MIDLET_NAME = "MIDlet-Name";
    private static final String MIDLET_VENDOR = "MIDlet-Vendor";
    private static final String MIDLET_MIDLET1 = "MIDlet-1";
    private static final String MIDLET_JAR_SIZE = "MIDlet-Jar-Size";
    private static final String MIDLET_JAR_URL = "MIDlet-Jar-URL";
    private static final String MICROEDITION_CONFIGURATION = "MicroEdition-Configuration";
    private static final String MICROEDITION_PROFILE = "MicroEdition-Profile";
    
    private static final String MIDP_VERSION = "MIDP_VERSION";
    private String profile = PROFILE_VALUE;
    private String configuration = CONFIGURATION_VALUE;
    
    private PlatformPackage platform;

    /**
     * @param platform
     * @throws Exception
     */
    public DescriptorBuilder(PlatformPackage platform) throws Exception 
    {
        this.platform = platform;
        if(platform.hasKey(MIDP_VERSION) && platform.getProperty(MIDP_VERSION).equals("2.0"))
        {
            profile = PROFILE_VALUE2;
        }
    }

    public Manifest makeManifest(String midletName)
    {
        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, MANIFEST_VERSION_VALUE);
        attributes.put(new Attributes.Name(MIDLET_VERSION), VERSION_VALUE);
        attributes.put(new Attributes.Name(MIDLET_VENDOR), VENDOR_VALUE);
        attributes.put(new Attributes.Name(MIDLET_NAME), midletName);
        attributes.put(new Attributes.Name(MIDLET_MIDLET1), midletName + MIDLET1_VALUE);
        attributes.put(new Attributes.Name(MICROEDITION_PROFILE), profile);
        attributes.put(new Attributes.Name(MICROEDITION_CONFIGURATION), configuration);

        return manifest;
    }

    public void makeJad(OutputStream os, String midletName, long size)
    {
        PrintStream out = new PrintStream(os);

        out.println(Attributes.Name.MANIFEST_VERSION + ": " + MANIFEST_VERSION_VALUE);
        out.println(MIDLET_VERSION + ": " + VERSION_VALUE);
        out.println(MIDLET_VENDOR + ": " + VENDOR_VALUE);
        out.println(MIDLET_NAME + ": " + midletName);
        out.println(MIDLET_MIDLET1 + ": " + midletName + MIDLET1_VALUE);
        out.println(MIDLET_JAR_SIZE + ": " + size);
        out.println(MIDLET_JAR_URL + ": " + midletName + ".jar");
        out.println(MICROEDITION_PROFILE + ": " + profile);
        out.println(MICROEDITION_CONFIGURATION + ": " + configuration);

        out.close();
    }
}
