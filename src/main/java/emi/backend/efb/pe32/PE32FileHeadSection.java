/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package emi.backend.efb.pe32;

import emi.backend.IEFB;
import emi.backend.StructUtils;

import java.sql.Struct;

/**
 * PE32 file header.
 * Created on 4/29/17.
 */
public class PE32FileHeadSection implements IEFB.IFileSection {
    public short machine, chars;
    public int tds;
    public int symO, symC;

    // Though this can't be used as an actual struct (some values have to be filled in by EFB), this can be used for reflection-based get/set.
    public final String[] pe32FileheadVarivalsStruct = StructUtils.validateStruct(new String[] {
            "size 16",
            "u16 machine",
            "u16 chars",
            "u32 tds",
            "u32 symO",
            "u32 symC",
    });

    public PE32FileHeadSection() {
        machine = (short) 0x5A4D;
        tds = 0;
        symO = 0;
        symC = 0;
    }

    public PE32FileHeadSection(PE32FileHeadSection copy) {
        machine = copy.machine;
        tds = copy.tds;
        symO = copy.symO;
        symC = copy.symC;
    }

    @Override
    public String type() {
        return "header";
    }

    @Override
    public String name() {
        return "PE32 File Header";
    }

    @Override
    public String[] describeKeys() {
        return StructUtils.descKeysFromSU(pe32FileheadVarivalsStruct);
    }

    @Override
    public String getValue(String key) {
        return StructUtils.getStruct(pe32FileheadVarivalsStruct, this, key);
    }

    @Override
    public PE32FileHeadSection changeValue(String key, String value) {
        PE32FileHeadSection copy = new PE32FileHeadSection(this);
        StructUtils.setStruct(pe32FileheadVarivalsStruct, copy, key, value);
        return copy;
    }

    @Override
    public byte[] data() {
        return new byte[0];
    }

    @Override
    public long fileDataLength() {
        return 0x18;
    }

    @Override
    public IEFB.IFileSection changedData(byte[] data) {
        throw new RuntimeException("No data in header to change");
    }
}
