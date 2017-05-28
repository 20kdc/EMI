/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package emi.backend.efb.pe32;

import emi.backend.IBackend;
import emi.backend.intutil.IBackendExtension;
import emi.backend.LongUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Adds commands targetted at PE32 files.
 * Created on 5/6/17.
 */
public class PE32BackendExtension implements IBackendExtension {
    @Override
    public String[] addedCommands() {
        return new String[] {
                "remove-reloc",
                "move-rsrc-rva newrva hexnum",
        };
    }

    @Override
    public String[] runCommand(IBackend.IBackendFile ibf, String[] arguments) {
        if (arguments[0].equals("remove-reloc"))
            if (arguments.length == 1) {
                boolean didAnything = false;
                String chars = ibf.runOperation(new String[] {"get-section-value", "1", "chars"})[0];
                long charsOldLong = LongUtils.hexvalToLong(chars);
                if ((charsOldLong & 1) == 0)
                    didAnything = true;
                chars = LongUtils.longToHexval(charsOldLong | 1L);
                ibf.runOperation(new String[] {"set-section-value", "1", "chars", chars});
                // Check for BASE RELOCATION reference and wipe it.
                byte[] data = ibf.runDSOperation(new String[] {"get-section", "2"});
                if (data.length >= 48) {
                    byte[] copy = new byte[data.length];
                    for (int i = 0; i < copy.length; i++)
                        copy[i] = data[i];
                    ByteBuffer bb = ByteBuffer.wrap(copy);
                    bb.order(ByteOrder.LITTLE_ENDIAN);
                    if (bb.getInt(40) != 0)
                        didAnything = true;
                    if (bb.getInt(44) != 0)
                        didAnything = true;
                    bb.putInt(40, 0);
                    bb.putInt(44, 0);
                    ibf.runDLOperation(new String[] {"set-section", "2"}, copy);
                }
                if (didAnything)
                    return new String[] {"Success."};
                return new String[] {"Success, but didn't do anything."};
            } else {
                throw new RuntimeException("bad parameters");
            }
        if (arguments[0].equals("move-rsrc-rva"))
            if (arguments.length == 2) {
                // Get original RVA of RESOURCES
                byte[] data = ibf.runDSOperation(new String[] {"get-section", "2"});
                byte[] dataOHN = new byte[data.length];
                if (data.length >= 0x18) {
                    ByteBuffer bb = ByteBuffer.wrap(dataOHN);
                    bb.order(ByteOrder.LITTLE_ENDIAN);
                    bb.put(data);
                    long oldRva = LongUtils.usI(bb.getInt(0x10));
                    if (oldRva == 0)
                        throw new RuntimeException("No resources to relocate.");
                    long oldLen = LongUtils.usI(bb.getInt(0x14));
                    if (oldLen == 0)
                        throw new RuntimeException("No resources to relocate.");
                    long newRva = LongUtils.hexvalToLong(arguments[1]);
                    bb.putInt(0x10, (int) newRva);
                    String target = null;
                    int targetInd = 0;
                    String expectedV = LongUtils.longToHexval(oldRva);
                    for (String s : ibf.runOperation(new String[] {"list-sections"})) {
                        if (expectedV.equals(s.split(":")[0].split(" ")[0])) {
                            target = s;
                            break;
                        }
                        targetInd++;
                    }
                    if (target != null) {
                        // Found a section. Pull out the data, change the RVA of the section, and insert new data.
                        byte[] d = ibf.runDSOperation(new String[] {"get-section", String.valueOf(targetInd)});
                        byte[] dn = relocateResources(d, (int) (newRva - oldRva));
                        ibf.runOperation(new String[] {"set-section-rva", String.valueOf(targetInd), LongUtils.longToHexval(newRva)});
                        ibf.runDLOperation(new String[] {"set-section", String.valueOf(targetInd)}, dn);
                        ibf.runDLOperation(new String[] {"set-section", "2"}, dataOHN);
                        return new String[] {
                                "Successfully moved resource section."
                        };
                    }
                    throw new RuntimeException("Unable to find a dedicated resource section. (expected " + expectedV + ")");
                } else {
                    throw new RuntimeException("Optional Header too short.");
                }
            } else {
                throw new RuntimeException("bad parameters");
            }
        throw new RuntimeException("unknown command");
    }

    private byte[] relocateResources(byte[] d, int l) {
        // Create new buffer, copy to it, relocate in-place.
        byte[] d2 = new byte[d.length];
        ByteBuffer bb = ByteBuffer.wrap(d2);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(d);
        relocateResourceCore(bb, 0, l);
        return d2;
    }

    private void relocateResourceCore(ByteBuffer bb, int dirAddr, int ofs) {
        bb.position(dirAddr + 12);
        long entries = LongUtils.usI(bb.getShort());
        entries += LongUtils.usI(bb.getShort());
        int[] results = new int[(int) entries];
        for (int i = 0; i < results.length; i++) {
            bb.getInt();
            results[i] = bb.getInt();
        }
        for (int result : results) {
            if ((result & 0x80000000) != 0) {
                relocateResourceCore(bb, result & 0x7FFFFFFF, ofs);
            } else {
                bb.position(0);
                bb.putInt(result, bb.getInt(result) + ofs);
            }
        }
    }
}
