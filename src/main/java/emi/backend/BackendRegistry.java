/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package emi.backend;

import emi.backend.efb.MSDOSEFB;
import emi.backend.efb.pe32.PE32EFB;

/**
 * Contains the backend singletons.
 * Meant to be accessed with reflection.
 * Created on 4/28/17.
 */
public class BackendRegistry {
    public static IBackend dos_exe = new EFBWrapperBackend(new MSDOSEFB());
    public static IBackend pe32_exe = new EFBWrapperBackend(new PE32EFB());
}
