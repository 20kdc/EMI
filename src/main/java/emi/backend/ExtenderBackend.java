/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package emi.backend;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Extends a basic backend (like EFBWrapperBackend in the general case) with additional macro-like operations.
 * Created on 5/6/17.
 */
public class ExtenderBackend implements IBackend {
    public final IBackend root;
    public final IBackendExtension[] extensions;
    public ExtenderBackend(IBackend r, IBackendExtension[] ext) {
        root = r;
        extensions = ext;
    }

    @Override
    public IBackendFile openFile(byte[] data) throws IOException {
        return wrap(root.openFile(data));
    }

    private IBackendFile wrap(final IBackendFile ofile) {
        final HashSet<String> reserved = new HashSet<String>();
        final LinkedList<String> help = new LinkedList<String>();
        for (String s : ofile.runOperation(new String[] {"help"}))
            help.add(s);
        for (IBackendExtension ibe : extensions)
            for (String desc : ibe.addedCommands()) {
                reserved.add(desc.split(" ")[0]);
                help.add(desc);
            }
        return new IBackendFile() {
            private IBackendFile file = ofile;
            @Override
            public String[] runOperation(String[] arguments) {
                if (reserved.contains(arguments[0])) {
                    for (IBackendExtension ibe : extensions)
                        for (String desc : ibe.addedCommands())
                            if (arguments[0].equals(desc.split(" ")[0])) {
                                byte[] fallback = file.runDSOperation(new String[] {
                                        "save"
                                });
                                try {
                                    // let's just hope this doesn't error, OK?
                                    return ibe.runCommand(file, arguments);
                                } catch (Throwable t) {
                                    // Error occurred, clean up. Kind-of. Probably ugly code, but it at least ought to clean up most situations.
                                    System.gc();
                                    try {
                                        file = root.openFile(fallback);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        // Note that EFBs are supposed to always remain in a consistent state,
                                        //  but the *macro* might not have completed.
                                        throw new RuntimeException("The error fallback failed during the cause of this exception! Your data may now be in an uncertain state, but hopefully still valid.", t);
                                    }
                                    throw new RuntimeException(t);
                                }
                            }
                    throw new RuntimeException("Extension-reserved keyword now isn't supported?");
                } else if (arguments[0].equals("help")) {
                    return help.toArray(new String[0]);
                } else {
                    return file.runOperation(arguments);
                }
            }

            @Override
            public String[] runDLOperation(String[] arguments, byte[] data) {
                return file.runDLOperation(arguments, data);
            }

            @Override
            public byte[] runDSOperation(String[] arguments) {
                return file.runDSOperation(arguments);
            }
        };
    }

    @Override
    public IBackendFile createFile() {
        return wrap(root.createFile());
    }
}
