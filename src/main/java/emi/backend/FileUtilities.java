/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package emi.backend;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Used to clean up some of the "ABSOLUTELY DO NOT LEAK FDs, and would you also please use RuntimeExceptions because classification is basically useless anyway
 * Created on 5/6/17.
 */
public class FileUtilities {
    public static byte[] loadFile(File f) {
        try {
            FileInputStream fis = new FileInputStream(f);
            byte[] data2 = null;
            try {
                data2 = new byte[fis.available()];
                if (fis.read(data2) != data2.length) {
                    fis.close();
                    throw new IOException("couldn't read full file");
                }
            } catch (IOException e) {
                fis.close();
                throw new RuntimeException(e);
            }
            fis.close();
            return data2;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveFile(File selectedFile, byte[] out) {
        try {
            FileOutputStream fos = new FileOutputStream(selectedFile);
            try {
                fos.write(out);
            } catch (IOException ioe) {
                fos.close();
                throw new RuntimeException(ioe);
            }
            fos.close();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}
