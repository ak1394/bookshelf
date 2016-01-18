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

package bookshelf.makefont.pdb;

import java.io.*;
import java.util.*;

public class PDBFile
{

    Vector v = new Vector();
    ArrayList resultList = new ArrayList();

    public PDBFile(File file)
    {
        try
        {
            String dst = file.getName();
            if (dst.endsWith(".pdb") || dst.endsWith(".PDB"))
            {
                dst = dst.substring(0, dst.length() - 4);
            }

            DataInputStream is = new DataInputStream(new FileInputStream(file));

            // DatabaseHdrType
            byte name[] = new byte[32];
            is.read(name);
            short attributes = is.readShort();
            short version = is.readShort();
            int creationDate = is.readInt();
            int modificationDate = is.readInt();
            int lastBackupDate = is.readInt();
            int modificationNumber = is.readInt();
            int appInfoID = is.readInt();
            int sortInfoID = is.readInt();
            byte type[] = new byte[4];
            is.read(type);
            byte creator[] = new byte[4];
            is.read(creator);
            int uniqueIDSeed = is.readInt();

            // RecordListType
            int nextRecordListID = is.readInt();
            short numRecords = is.readShort();
            //System.out.println("numRecords "+numRecords);
            //System.out.println("nextRecordListID "+nextRecordListID);
            if (appInfoID != 0)
            {
                Record r = new Record(dst + "-info");
                r.localChunkID = appInfoID;
                v.addElement(r);
            }
            for (int i = 0; i < numRecords; i++)
            {
                readRecord(is, dst + i);
            }
            is.readShort(); // pad
            int offset = 80 + numRecords * 8;
            int totNumRecords = (short) v.size();
            //System.out.println("totNumRecords "+totNumRecords);
            for (int i = 0; i < totNumRecords; i++)
            {
                int end;
                Record r = (Record) v.elementAt(i);
                int start = r.localChunkID;
                if (i == totNumRecords - 1)
                {
                    end = (int) file.length();
                }
                else
                {
                    end = ((Record) v.elementAt(i + 1)).localChunkID;
                }
                if (offset != start)
                {
                    //System.out.println("Offset "+start+" vs "+offset);
                }
                resultList.add(writeFile(is, r.name, end - start));
                offset += end - start;
            }

        }
        catch (IOException e)
        {
            System.out.println("IOException " + e);
        }
    }

    NamedByteArrayOutputStream writeFile(DataInputStream is, String name,
            int bytes) throws IOException
    {
        byte buf[] = new byte[1024];
        NamedByteArrayOutputStream result = new NamedByteArrayOutputStream(name);
        DataOutputStream os = new DataOutputStream(result);
        while (bytes > 0)
        {
            int read = is.read(buf, 0, bytes > 1024 ? 1024 : bytes);
            os.write(buf, 0, read);
            bytes -= read;
        }
        os.close();
        return result;
    }

    void readRecord(DataInputStream is, String name) throws IOException
    {
        Record r = new Record(name);
        v.addElement(r);
        r.localChunkID = is.readInt();
        r.attributes = is.readByte();
        is.read(r.uniqueID);
    }

    public NamedByteArrayOutputStream[] getResult()
    {
        NamedByteArrayOutputStream result[] = new NamedByteArrayOutputStream[resultList
                .size()];
        return (NamedByteArrayOutputStream[]) resultList.toArray(result);
    }
}

class Record
{
    int localChunkID;
    byte attributes;
    byte uniqueID[];
    String name;

    Record(String name)
    {
        this.name = name;
        uniqueID = new byte[3];
    }
}