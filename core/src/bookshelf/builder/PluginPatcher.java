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

import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import java.io.*;
import java.util.Map;

public class PluginPatcher
{
    private static final String STRING_PREFIX = "aS_";
    private static final String STATIC_INT_PREFIX = "aI_";

    public static void patch(InputStream is, OutputStream os, PlatformPackage platform, Plugin plugin)
            throws Exception
    {
        // load class file
        ClassParser parser = new ClassParser(is, null);
        JavaClass klass = parser.parse();
        ConstantPoolGen cp = new ConstantPoolGen(klass.getConstantPool().getConstantPool());

        // patch strings
        patchStrings(cp, plugin.getStrings());

        // patch ints
        InstructionList patch = buildPatch(klass, cp, platform, plugin);
        insertPatch(klass, cp, patch);

        klass.setConstantPool(cp.getFinalConstantPool());

        // klass.dump will close output stream, so a workaround here
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        klass.dump(baos);
        os.write(baos.toByteArray());

    }

    private static void patchStrings(ConstantPoolGen cp, Map data)
    {
        for (short i = 0; i < cp.getSize(); i++)
        {
            Constant constant = cp.getConstant(i);
            if (constant instanceof ConstantUtf8)
            {
                String value = ((ConstantUtf8) constant).getBytes();
                if (value.startsWith(STRING_PREFIX) && data.containsKey(value))
                {
                    ConstantUtf8 newConstant = new ConstantUtf8((String) data.get(value));
                    cp.setConstant(i, newConstant);
                }
            }
        }
    }

    private static InstructionList buildPatch(JavaClass klass, ConstantPoolGen cp, PlatformPackage platform, Plugin plugin)
            throws Exception
    {
        InstructionFactory factory = new InstructionFactory(cp);
        InstructionList patch = new InstructionList();

        Field[] field = klass.getFields();
        for (int i = 0; i < field.length; i++)
        {
            String fieldName = field[i].getName();
            if (fieldName.startsWith(STATIC_INT_PREFIX))
            {
                String keyName = fieldName.substring(STATIC_INT_PREFIX.length());
                // TODO what to do if no key mapping been found
                int keyCode = plugin.getKeyCode(keyName);
                patch.append(new PUSH(cp, keyCode));
                patch.append(factory.createPutStatic(klass.getClassName(), fieldName, Type.INT));
            }
        }
        return patch;
    }

    /*
     * 
     * inserts instruction list at the end of <init> method
     *  
     */
    private static void insertPatch(JavaClass klass, ConstantPoolGen cp, InstructionList patch)
    {
        Method[] methods = klass.getMethods();
        for (int i = 0; i < methods.length; i++)
        {
            if (methods[i].getName().equals("<init>"))
            {
                MethodGen mg = new MethodGen(methods[i], klass.getClassName(), cp);
                InstructionList il = mg.getInstructionList();
                InstructionHandle[] ihs = il.getInstructionHandles();

                for (int j = 1; j < ihs.length; j++)
                {
                    if (ihs[j].getInstruction() instanceof INVOKESPECIAL)
                    {
                        il.append(ihs[j], patch);
                        break;
                    }
                }
                methods[i] = mg.getMethod();
                break;
            }
        }
    }
}
