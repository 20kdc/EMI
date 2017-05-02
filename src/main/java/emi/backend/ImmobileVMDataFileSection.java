/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package emi.backend;

/**
 * DataFileSection with a fixed RVA and no BSS
 * Created on 4/28/17.
 */
public class ImmobileVMDataFileSection extends DataFileSection implements IEFB.IVMFileSection {
    public final long rva;

    public ImmobileVMDataFileSection(byte[] p, String t, String n, long vm) {
        super(p, t, n);
        rva = vm;
    }

    @Override
    public long getRVA() {
        return rva;
    }

    @Override
    public boolean canMove() {
        return false;
    }

    @Override
    public IEFB.IFileSection move(long rva) {
        throw new RuntimeException("Cannot move this data section.");
    }

    @Override
    public long getLength() {
        return d.length;
    }

    @Override
    public IEFB.IFileSection changedLength(long newLength) {
        throw new RuntimeException("Cannot change the length of this data section.");
    }

    @Override
    public ImmobileVMDataFileSection changedData(byte[] data) {
        return new ImmobileVMDataFileSection(data, type, name, rva);
    }
}
