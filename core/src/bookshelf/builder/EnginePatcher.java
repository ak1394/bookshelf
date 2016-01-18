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
import org.apache.bcel.Constants;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class EnginePatcher
{
    public static void patch(InputStream is, OutputStream os, Map stringArrays, Map integerValues) throws Exception
    {
        // load class file
        ClassParser parser = new ClassParser(is, null);
        JavaClass klass = parser.parse();
        ConstantPoolGen cp = new ConstantPoolGen(klass.getConstantPool().getConstantPool());
        InstructionList patch = buildPatch(cp, stringArrays, integerValues);
        insertPatch(klass, cp, patch);
        klass.setConstantPool(cp.getFinalConstantPool());
        // klass.dump will close output stream, so a workaround here
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        klass.dump(baos);
        os.write(baos.toByteArray());
    }

    private static InstructionList buildPatch(ConstantPoolGen cp, Map stringArrays, Map integerValues)
    {
        InstructionFactory factory = new InstructionFactory(cp);
        InstructionList patch = new InstructionList();

        for (Iterator iterator = stringArrays.entrySet().iterator(); iterator.hasNext();)
        {
            Map.Entry entry = (Entry) iterator.next();
            String name = (String) entry.getKey();
            List values = (List) entry.getValue();

            patch.append(InstructionFactory.createLoad(Type.OBJECT, 0));
            patch.append(new PUSH(cp, values.size()));
            patch.append(factory.createNewArray(Type.STRING, (short) 1));
            for (int i = 0; i < values.size(); i++)
            {
                // put each string into array
                patch.append(InstructionConstants.DUP);
                patch.append(new PUSH(cp, i));
                patch.append(new PUSH(cp, (String) values.get(i)));
                patch.append(InstructionConstants.AASTORE);
            }
            // initialize Engine.bookTitles with newly created array
            patch.append(factory.createFieldAccess("reader.Engine", name, new ArrayType(Type.STRING, 1),
                    Constants.PUTFIELD));
        }

        for (Iterator iterator = integerValues.entrySet().iterator(); iterator.hasNext();)
        {
            Map.Entry entry = (Entry) iterator.next();
            String name = (String) entry.getKey();
            Integer value = (Integer) entry.getValue();

            patch.append(new PUSH(cp, value.intValue()));
            patch.append(factory.createPutStatic("reader.Engine", name, Type.INT));
        }

        return patch;
    }

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
                mg.setMaxStack();
                mg.setMaxLocals();
                methods[i] = mg.getMethod();
                break;
            }
        }
    }
}