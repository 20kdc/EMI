/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package emi.backend.efb.pe32;

import emi.backend.FlagUtils;
import emi.backend.IEFB;

/**
 * Created on 4/28/17.
 */
public class PE32FileSection implements IEFB.IVMFileSection, IEFB.IFileSection {
    public static final String[] cflags1 = new String[]{"r", "w", "x"};
    public static final long[] cflags2 = new long[]{0x40000000L, 0x80000000L, 0x20000000L};

    public byte[] name, data;
    public int relocsO, linesO;
    public short relocsN, linesN;
    public int chars, rva, vSize;

    public PE32FileSection() {
        name = new byte[]{0x2E, 0x61, 0x61, 0x61, 0x61, 0x61, 0x61, 0x61};
        data = new byte[0];
        chars = 0xE0000000;
    }

    public PE32FileSection(PE32FileSection copy) {
        name = copy.name;
        data = copy.data;
        relocsO = copy.relocsO;
        relocsN = copy.relocsN;
        linesO = copy.linesO;
        linesN = copy.linesN;
        chars = copy.chars;
        rva = copy.rva;
        vSize = copy.vSize;
    }

    @Override
    public String type() {
        String n = "";
        if ((chars & 0x40000000) != 0)
            n += "r";
        if ((chars & 0x80000000) != 0)
            n += "w";
        if ((chars & 0x20000000) != 0)
            n += "x";
        return n;
    }

    @Override
    public String name() {
        String n = "";
        for (int i = 0; i < name.length; i++)
            if (name[i] != 0)
                n += (char) (name[i]);
        if (n.length() == 0)
            n = "data";
        return n;
    }

    @Override
    public String[] describeKeys() {
        return new String[]{
                "name string",
                "relocsO num",
                "relocsN num",
                "linesO num",
                "linesN num",
                "chars flags r w x",
                "charsEx num",
        };
    }

    @Override
    public String getValue(String key) {
        if (key.equals("name"))
            return name();
        if (key.equals("relocsO"))
            return Long.toString(relocsO & 0xFFFFFFFFL);
        if (key.equals("relocsN"))
            return Long.toString(relocsN & 0xFFFFL);
        if (key.equals("linesO"))
            return Long.toString(linesO & 0xFFFFFFFFL);
        if (key.equals("linesN"))
            return Long.toString(linesN & 0xFFFFL);
        if (key.equals("chars"))
            return FlagUtils.get(cflags1, cflags2, chars);
        if (key.equals("charsEx"))
            return Long.toString(chars & 0xFFFFFFFFL);
        throw new RuntimeException("No such key " + key);
    }

    @Override
    public IEFB.IFileSection changeValue(String key, String value) {
        PE32FileSection pfs = new PE32FileSection(this);
        if (key.equals("name")) {
            byte[] data = new byte[8];
            for (int i = 0; i < key.length(); i++)
                data[i] = (byte) key.charAt(i);
            pfs.name = data;
        }
        if (key.equals("relocsO"))
            pfs.relocsO = (int) (long) Long.decode(value);
        if (key.equals("relocsN"))
            pfs.relocsN = (short) (long) Long.decode(value);
        if (key.equals("linesO"))
            pfs.linesO = (int) (long) Long.decode(value);
        if (key.equals("linesN"))
            pfs.linesN = (short) (long) Long.decode(value);
        if (key.equals("chars"))
            pfs.chars = (int) FlagUtils.put(cflags1, cflags2, value, chars & 0xFFFFFFFFL);
        if (key.equals("charsEx"))
            pfs.chars = (int) (long) Long.decode(value);
        throw new RuntimeException("No such key " + key);
    }

    @Override
    public byte[] data() {
        return data;
    }

    @Override
    public long fileDataLength() {
        return data.length;
    }

    @Override
    public IEFB.IFileSection changedData(byte[] data) {
        PE32FileSection pfs = new PE32FileSection(this);
        pfs.data = data;
        if (vSize < data.length)
            vSize = data.length;
        return pfs;
    }

    @Override
    public long getRVA() {
        return rva;
    }

    @Override
    public boolean canMove() {
        return true;
    }

    @Override
    public IEFB.IFileSection move(long rva2) {
        PE32FileSection pfs = new PE32FileSection(this);
        pfs.rva = (int) rva2;
        return pfs;
    }

    @Override
    public long getLength() {
        if (vSize < data.length)
            return data.length;
        return vSize;
    }

    @Override
    public IEFB.IFileSection changedLength(long nLength) {
        int newLength = (int) nLength;
        if (newLength < data.length)
            newLength = data.length;
        PE32FileSection pfs = new PE32FileSection(this);
        pfs.vSize = newLength;
        return pfs;
    }
}
