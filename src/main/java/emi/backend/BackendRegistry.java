/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package emi.backend;

import emi.backend.efb.MSDOSEFB;
import emi.backend.efb.pe32.PE32BackendExtension;
import emi.backend.efb.pe32.PE32EFB;
import emi.backend.intutil.IBackendExtension;

/**
 * Contains the backend singletons.
 * Meant to be accessed with reflection.
 * Created on 4/28/17.
 */
public class BackendRegistry {
    public static IBackend dos_exe = new EFBWrapperBackend(new MSDOSEFB());
    public static IBackend pe32_exe = new ExtenderBackend(new EFBWrapperBackend(new PE32EFB()), new IBackendExtension[] {new PE32BackendExtension()});
    public static String[] backends = new String[]{
            "dos_exe",
            "pe32_exe"
    };

    public static IBackend get(String arg) {
        try {
            return (IBackend) (BackendRegistry.class.getField(arg).get(null));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("No such backend: " + arg + ".");
    }
}
