/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package emi.backend;

import emi.backend.intutil.IEFB;

import java.io.IOException;
import java.util.LinkedList;

/**
 * Wraps an EFB to get a usable backend.
 * This is responsible for everything important the GUI talks to in order to get user-friendly output.
 * Essentially, this is the interface to the whole system.
 * Created on 4/28/17.
 */
class EFBWrapperBackend implements IBackend {
    public final IEFB b;

    public EFBWrapperBackend(IEFB efb) {
        b = efb;
    }

    private IBackendFile convertEFB(final IEFB r) {
        return new IBackendFile() {
            @Override
            public String[] runOperation(String[] arguments) {
                if (arguments[0].equals("help")) {
                    checkArgs(new boolean[0], arguments);
                    String cse = "";
                    for (String s : r.creatableSections())
                        cse += " " + s;
                    return new String[]{
                            // Format:
                            // <command> <name type...>
                            // where type can be one of:
                            // enum <args separated by spaces> ;
                            // section-idx
                            // hexnum (LongUtils stuff)
                            // num (for indexes)
                            // str
                            // in practice, tools accessible via other means should be blacklisted from GUI
                            // Idea is that this is extendable via decorator backends,
                            //  allowing custom tools to be developed specific to given needs
                            //  and accessible via console + GUI
                            // (PE32 resource relocation!!!)
                            "list-sections",
                            "create-section type enum" + cse + " ;",
                            "set-section-rva section section-idx rva hexnum",
                            "set-section-len section section-idx len hexnum",
                            "list-section-keys section section-idx",
                            "get-section-value section section-idx key str",
                            "set-section-value section section-idx key str value str",
                            "remove-section section section-idx",
                            "swap-sections section section-idx section section-idx",
                            "dl-set-section section section-idx",
                            "ds-get-section section section-idx",
                            "ds-save"
                    };
                }
                if (arguments[0].equals("list-sections")) {
                    checkArgs(new boolean[0], arguments);
                    IEFB.IFileSection[] fs = r.fileSections();
                    String[] r = new String[fs.length];
                    for (int i = 0; i < fs.length; i++) {
                        IEFB.IFileSection s = fs[i];
                        if (s instanceof IEFB.IVMFileSection) {
                            // movable: The section can be moved.
                            // fixed: The section cannot be moved.
                            r[i] = LongUtils.longToHexval(((IEFB.IVMFileSection) s).getRVA()) + " " + LongUtils.longToHexval(((IEFB.IVMFileSection) s).getLength()) + (((IEFB.IVMFileSection) s).canMove() ? " movable" : " fixed");
                        } else {
                            r[i] = "";
                        }
                        r[i] += ":" + LongUtils.longToHexval(s.data().length) + "/" + LongUtils.longToHexval(s.fileDataLength()) + ":" + s.type() + ":" + s.name();
                    }
                    return r;
                }
                if (arguments[0].equals("create-section")) {
                    checkArgs(new boolean[]{false}, arguments);
                    IEFB.IFileSection[] fs = r.fileSections();
                    IEFB.IFileSection[] fs2 = new IEFB.IFileSection[fs.length + 1];
                    for (int i = 0; i < fs.length; i++)
                        fs2[i] = fs[i];
                    fs2[fs.length] = r.createSection(arguments[1]);
                    r.changeSections(fs2);
                    return new String[]{
                            "Successfully created section."
                    };
                }
                if (arguments[0].equals("set-section-rva")) {
                    Long[] sn = checkArgs(new boolean[]{true, false}, arguments);
                    IEFB.IFileSection[] fs = r.fileSections();
                    fs[(int) (long) sn[0]] = ((IEFB.IVMFileSection) fs[(int) (long) sn[0]]).move(LongUtils.hexvalToLong(arguments[2]));
                    r.changeSections(fs);
                    return new String[]{
                            "Successfully moved section."
                    };
                }
                if (arguments[0].equals("set-section-len")) {
                    Long[] sn = checkArgs(new boolean[]{true, false}, arguments);
                    IEFB.IFileSection[] fs = r.fileSections();
                    fs[(int) (long) sn[0]] = ((IEFB.IVMFileSection) fs[(int) (long) sn[0]]).changedLength(LongUtils.hexvalToLong(arguments[2]));
                    r.changeSections(fs);
                    return new String[]{
                            "Successfully moved section."
                    };
                }
                if (arguments[0].equals("list-section-keys")) {
                    Long[] sn = checkArgs(new boolean[]{true}, arguments);
                    IEFB.IFileSection[] fs = r.fileSections();
                    return fs[(int) (long) sn[0]].describeKeys();
                }
                if (arguments[0].equals("get-section-value")) {
                    Long[] sn = checkArgs(new boolean[]{true, false}, arguments);
                    IEFB.IFileSection fs = r.fileSections()[(int) (long) sn[0]];
                    return new String[]{fs.getValue(arguments[2])};
                }
                if (arguments[0].equals("set-section-value")) {
                    Long[] sn = checkArgs(new boolean[]{true, false, false}, arguments);
                    IEFB.IFileSection[] fs = r.fileSections();
                    IEFB.IFileSection fsb = fs[(int) (long) sn[0]];
                    fs[(int) (long) sn[0]] = fsb.changeValue(arguments[2], arguments[3]);
                    r.changeSections(fs);
                    return new String[]{"Value changed successfully."};
                }
                if (arguments[0].equals("remove-section")) {
                    int sn = (int) (long) (checkArgs(new boolean[]{true}, arguments)[0]);
                    IEFB.IFileSection[] fs = r.fileSections();
                    IEFB.IFileSection[] fs2 = new IEFB.IFileSection[fs.length - 1];
                    int i2 = 0;
                    for (int i = 0; i < fs.length; i++)
                        if (i != sn)
                            fs2[i2++] = fs[i];
                    r.changeSections(fs2);
                    return new String[]{
                            "Successfully removed section."
                    };
                }
                if (arguments[0].equals("swap-sections")) {
                    Long[] sn = checkArgs(new boolean[]{true, true}, arguments);
                    IEFB.IFileSection[] fs = r.fileSections();
                    IEFB.IFileSection a = fs[(int) (long) sn[0]];
                    IEFB.IFileSection b = fs[(int) (long) sn[1]];
                    fs[(int) (long) sn[0]] = b;
                    fs[(int) (long) sn[1]] = a;
                    r.changeSections(fs);
                    return new String[]{
                            "Successfully swapped sections."
                    };
                }
                throw new RuntimeException("No such command " + arguments[0]);
            }

            @Override
            public String[] runDLOperation(String[] arguments, byte[] data) {
                if (arguments[0].equals("set-section")) {
                    int sn = (int) (long) (checkArgs(new boolean[]{true}, arguments)[0]);
                    IEFB.IFileSection fs[] = r.fileSections();
                    fs[sn] = fs[sn].changedData(data);
                    r.changeSections(fs);
                    return new String[]{
                            "Accepted new data."
                    };
                }
                throw new RuntimeException("No such data command " + arguments[0]);
            }

            @Override
            public byte[] runDSOperation(String[] arguments) {
                if (arguments[0].equals("get-section")) {
                    int sn = (int) (long) (checkArgs(new boolean[]{true}, arguments)[0]);
                    IEFB.IFileSection fs[] = r.fileSections();
                    return fs[sn].data();
                }
                if (arguments[0].equals("save")) {
                    checkArgs(new boolean[]{}, arguments);
                    return r.saveFile();
                }
                throw new RuntimeException("No such data command " + arguments[0]);
            }

            private Long[] checkArgs(boolean[] ints, String[] args) {
                if ((args.length - 1) != ints.length)
                    throw new RuntimeException("Bad args length");
                LinkedList<Long> ints2 = new LinkedList<Long>();
                for (int i = 0; i < ints.length; i++)
                    if (ints[i])
                        ints2.add(Long.decode(args[i + 1]));
                return ints2.toArray(new Long[0]);
            }
        };
    }

    @Override
    public IBackendFile openFile(byte[] data) throws IOException {
        IEFB r = b.createBlank();
        r.loadFile(data);
        return convertEFB(r);
    }

    @Override
    public IBackendFile createFile() {
        return convertEFB(b.createBlank());
    }
}
