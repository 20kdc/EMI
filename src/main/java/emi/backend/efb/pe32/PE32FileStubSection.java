/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package emi.backend.efb.pe32;

import emi.backend.intutil.IEFB;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * The DOS stub at the start of a PE32 file.
 * Created on 4/29/17.
 */
class PE32FileStubSection implements IEFB.IFileSection {
    public byte[] data;

    public PE32FileStubSection() {
        data = new byte[0x40];
        // generate a minimally right DOS stub header
        data[0] = 0x4D;
        data[1] = 0x5A;
        data[2] = 0x40;
        data[4] = 0x01;
        data[8] = 0x06;
        data[0x3C] = 0x40;
    }

    public PE32FileStubSection(byte[] d) {
        data = d;
    }

    @Override
    public String type() {
        return "data";
    }

    @Override
    public String name() {
        return "DOS Stub";
    }

    @Override
    public String[] describeKeys() {
        return new String[0];
    }

    @Override
    public String getValue(String key) {
        throw new RuntimeException("No such value.");
    }

    @Override
    public IEFB.IFileSection changeValue(String key, String value) {
        throw new RuntimeException("No such value.");
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
    public PE32FileStubSection changedData(byte[] data) {
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        if (bb.getInt(0x3C) != data.length)
            throw new RuntimeException("Data's length must equal the length of it's 0x3C field.");
        return new PE32FileStubSection(data);
    }
}
