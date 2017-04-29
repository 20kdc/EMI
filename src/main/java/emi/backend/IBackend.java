/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package emi.backend;

import java.io.IOException;

/**
 * Defines a simple, common interface for backend operations that does not expose private details.
 * The idea is that all GUI operations must go through this in order to ensure safety.
 * Another thing to note is that different IBackends can exist for different formats.
 * Created on 4/28/17.
 */
public interface IBackend {
    IBackendFile openFile(byte[] data) throws IOException;

    interface IBackendFile {
        // Can throw a RuntimeException on issue. Other kinds of exception mean a system error.
        String[] runOperation(String[] arguments);

        // All operations prefixed with "dl-" on the command line go here.
        String[] runDLOperation(String[] arguments, byte[] data);

        // All operations prefixed with "ds-" on the command line go here.
        byte[] runDSOperation(String[] arguments);

        // Basically, should the system bring up warnings a lot
        boolean fileContainsRelocationData();
    }
}
