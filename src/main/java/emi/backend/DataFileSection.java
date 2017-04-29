/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package emi.backend;

/**
 * Blob of data.
 * Created on 4/28/17.
 */
public class DataFileSection implements IEFB.IFileSection {
    public final byte[] d;
    public final String type, name;

    public DataFileSection(byte[] p, String t, String n) {
        d = p;
        type = t;
        name = n;
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String[] describeKeys() {
        return new String[0];
    }

    @Override
    public String getValue(String key) {
        throw new RuntimeException("no such key");
    }

    @Override
    public DataFileSection changeValue(String key, String value) {
        throw new RuntimeException("no such key");
    }

    @Override
    public byte[] data() {
        return d;
    }

    @Override
    public long fileDataLength() {
        return d.length;
    }

    @Override
    public DataFileSection changedData(byte[] data) {
        return new DataFileSection(data, type, name);
    }
}
