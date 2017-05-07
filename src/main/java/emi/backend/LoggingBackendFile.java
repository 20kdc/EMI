/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package emi.backend;

/**
 * Used by the GUI to log all of it's actions.
 * Created on 5/7/17.
 */
public class LoggingBackendFile implements IBackend.IBackendFile {
    public final IBackend.IBackendFile root;
    public LoggingBackendFile(IBackend.IBackendFile r) {
        root = r;
    }

    @Override
    public String[] runOperation(String[] arguments) {
        dumpCommand("", arguments);
        return root.runOperation(arguments);
    }

    @Override
    public String[] runDLOperation(String[] arguments, byte[] data) {
        dumpCommand("dl-", arguments);
        return root.runDLOperation(arguments, data);
    }

    @Override
    public byte[] runDSOperation(String[] arguments) {
        dumpCommand("ds-", arguments);
        return root.runDSOperation(arguments);
    }

    private void dumpCommand(String s, String[] arguments) {
        System.err.print(s);
        System.err.print(arguments[0]);
        for (int i = 1; i < arguments.length; i++)
            System.err.print(" " + arguments[i]);
        System.err.println();
    }
}
