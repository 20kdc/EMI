/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package emi.backend.efb.pe32;

import emi.backend.IEFB;

/**
 * useless section to fill up space & assist filesize accounting
 * Created on 4/29/17.
 */
public class PE32FileSectionHeadersSection implements IEFB.IFileSection {
    public int ds;

    public PE32FileSectionHeadersSection(int sections) {
        ds = sections;
    }

    @Override
    public String type() {
        return "header";
    }

    @Override
    public String name() {
        return "Section Headers";
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
        return new byte[0];
    }

    @Override
    public long fileDataLength() {
        return ds * 0x28;
    }

    @Override
    public IEFB.IFileSection changedData(byte[] data) {
        return null;
    }
}
