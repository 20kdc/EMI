/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package emi.backend.efb.pe32;

import emi.backend.intutil.IEFB;
import emi.backend.intutil.StructUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Aka. World's most boring section.
 * Created on 5/2/17.
 */
public class PE32FileOptHeadSection implements IEFB.IFileSection {

    // Main fields accessed via reflection
    public byte majorLinker = 2, minorLinker = 0x38;
    public int sizeCode, sizeInitData, sizeUninitData;
    public int entryPoint, baseCode, baseData, imageBase;
    public int sectionAlignment, fileAlignment;
    public short majorOSVer, minorOSVer, majorImgVer, minorImgVer;
    public short majorSSVer = 4, minorSSVer;
    public int w32Ver, imageSize, headerSize, checksum;
    public short subsystem = 3, dllChars;
    public int stackReserve = 0x200000, stackCommit = 0x1000, heapReserve = 0x100000, heapCommit = 0x1000;
    public int loaderFlags;

    public int[] rvas = new int[0x10], sizes = new int[0x10];

    public static String[] optHeadStruct = StructUtils.validateStruct(new String[]{
            "size 0x5C",
            "data 0x0B",
            "data 0x01",
            "u8 majorLinker",
            "u8 minorLinker",
            "u32 sizeCode",
            "u32 sizeInitData",
            "u32 sizeUninitData",
            "u32 entryPoint",
            "u32 baseCode",
            "u32 baseData",
            "u32 imageBase",
            "u32 sectionAlignment",
            "u32 fileAlignment",
            "u16 majorOSVer",
            "u16 minorOSVer",
            "u16 majorImgVer",
            "u16 minorImgVer",
            "u16 majorSSVer",
            "u16 minorSSVer",
            "u32 w32Ver",
            "u32 imageSize", // hmmm
            "u32 headerSize", // hmmm
            "u32 checksum", // hmmmm
            "u16 subsystem",
            "u16 dllChars",
            "u32 stackReserve",
            "u32 stackCommit",
            "u32 heapReserve",
            "u32 heapCommit",
            "u32 loaderFlags"
    });

    public PE32FileOptHeadSection() {

    }

    public PE32FileOptHeadSection(ByteBuffer bb) {
        StructUtils.loadStruct(bb, optHeadStruct, this);
        rvas = new int[bb.getInt()];
        sizes = new int[rvas.length];
        for (int i = 0; i < rvas.length; i++) {
            rvas[i] = bb.getInt();
            sizes[i] = bb.getInt();
        }
    }

    public PE32FileOptHeadSection(PE32FileOptHeadSection other) {
        StructUtils.copyStruct(optHeadStruct, other, this);
        rvas = other.rvas;
        sizes = other.sizes;
    }

    @Override
    public String type() {
        return "data";
    }

    @Override
    public String name() {
        return "Optional Header";
    }

    @Override
    public String[] describeKeys() {
        return StructUtils.descKeysFromSU(optHeadStruct);
    }

    @Override
    public String getValue(String key) {
        return StructUtils.getStruct(optHeadStruct, this, key);
    }

    @Override
    public IEFB.IFileSection changeValue(String key, String value) {
        PE32FileOptHeadSection gen = new PE32FileOptHeadSection(this);
        StructUtils.setStruct(optHeadStruct, gen, key, value);
        return gen;
    }

    @Override
    public byte[] data() {
        byte[] res = new byte[rvas.length * 8];
        ByteBuffer bb = ByteBuffer.wrap(res);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < rvas.length; i++) {
            bb.putInt(rvas[i]);
            bb.putInt(sizes[i]);
        }
        return res;
    }

    @Override
    public long fileDataLength() {
        return 0x60 + (rvas.length * 8);
    }

    @Override
    public PE32FileOptHeadSection changedData(byte[] data) {
        if (data.length % 8 != 0)
            throw new RuntimeException("Each data directory is 8 bytes.");
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        int[] rv = new int[data.length / 8];
        int[] sz = new int[data.length / 8];
        for (int i = 0; i < rv.length; i++) {
            rv[i] = bb.getInt();
            sz[i] = bb.getInt();
        }
        PE32FileOptHeadSection gen = new PE32FileOptHeadSection(this);
        gen.rvas = rv;
        gen.sizes = sz;
        return gen;
    }

    public void save(ByteBuffer bb) {
        StructUtils.saveStruct(bb, optHeadStruct, this);
        bb.putInt(rvas.length);
        for (int i = 0; i < rvas.length; i++) {
            bb.putInt(rvas[i]);
            bb.putInt(sizes[i]);
        }
    }
}
