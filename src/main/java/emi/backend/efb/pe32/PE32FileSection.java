/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package emi.backend.efb.pe32;

import emi.backend.FlagUtils;
import emi.backend.IEFB;
import emi.backend.StructUtils;

/**
 * Created on 4/28/17.
 */
public class PE32FileSection implements IEFB.IVMFileSection, IEFB.IFileSection {

    public static final String charFlags = "w r x unk28 unk27 unk26 unk25 unk24 unk23 unk22 unk21 unk20 unk19 unk18 unk17 unk16 unk15 unk14 unk13 unk12 unk11 unk10 unk9 unk8 unk7 unk6 unk5 unk4 unk3 unk2 unk1 unk0";
    // Though this can't be used as an actual struct (some values have to be filled in by EFB), this can be used for reflection-based get/set.
    public final String[] pe32SectheadVarivalsStruct = StructUtils.validateStruct(new String[] {
            "size 24",
            "fst name 8",
            "u32 relocsO",
            "u16 relocsN",
            "u32 linesO",
            "u16 linesN",
            "u32 chars " + charFlags
    });

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
        // Order should remain "rwx"!!!
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
        return StructUtils.descKeysFromSU(pe32SectheadVarivalsStruct);
    }

    @Override
    public String getValue(String key) {
        return StructUtils.getStruct(pe32SectheadVarivalsStruct, this, key);
    }

    @Override
    public IEFB.IFileSection changeValue(String key, String value) {
        PE32FileSection pfs = new PE32FileSection(this);
        StructUtils.setStruct(pe32SectheadVarivalsStruct, pfs, key, value);
        return pfs;
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
