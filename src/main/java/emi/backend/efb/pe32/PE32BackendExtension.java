/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package emi.backend.efb.pe32;

import emi.backend.IBackend;
import emi.backend.IBackendExtension;
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
                "move-rsrc-rva",
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
        throw new RuntimeException("unknown command");
    }
}
