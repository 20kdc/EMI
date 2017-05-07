/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package emi.backend;

/**
 * Basic utilities for conversion of values to/from the standard number format.
 * The idea being that always using this gives consistency.
 * Created on 5/7/17.
 */
public class LongUtils {
    // at some point it ought to have a padding setting
    public static String longToHexval(long l) {
        return Long.toHexString(l);
    }
    public static long hexvalToLong(String s) {
        return Long.decode("0x" + s);
    }

    public static long usI(int anInt) {
        return anInt & 0xFFFFFFFFL;
    }

    public static long usS(short aShort) {
        return aShort & 0xFFFFL;
    }

    public static long usB(byte aByte) {
        return aByte & 0xFFL;
    }
}
