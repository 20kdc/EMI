/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package emi.backend.efb.pe32;

import emi.backend.IEFB;

/**
 * PE32 file header.
 * Created on 4/29/17.
 */
public class PE32FileHeadSection implements IEFB.IFileSection {
    public short machine, chars;
    public int tds;
    public int symO, symC;

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
        return new String[]{
                "machine num",
                "chars num",
                "tds num"
        };
    }

    @Override
    public String getValue(String key) {
        return null;
    }

    @Override
    public IEFB.IFileSection changeValue(String key, String value) {
        return null;
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
        return null;
    }
}
