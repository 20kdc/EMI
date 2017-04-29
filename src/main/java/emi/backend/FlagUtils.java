/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package emi.backend;

/**
 * Created on 4/28/17.
 */
public class FlagUtils {
    public static String get(String[] strings, long[] ints, long chars) {
        String r = "";
        for (int i = 0; i < ints.length; i++) {
            if ((chars & ints[i]) != 0) {
                if (r.equals("")) {
                    r = strings[i];
                } else {
                    r += " " + strings[i];
                }
            }
        }
        return r;
    }

    public static long put(String[] strings, long[] ints, String value, long chars) {
        String[] r = value.split(" ");
        for (int i = 0; i < ints.length; i++) {
            long p = ~ints[i];
            chars &= p;
        }
        for (String s : r) {
            boolean ok = false;
            for (int i = 0; i < strings.length; i++) {
                if (s.equals(strings[i])) {
                    chars |= ints[i];
                    ok = true;
                    break;
                }
            }
            if (!ok)
                throw new RuntimeException("Bad flag string (unknown '" + s + "')");
        }
        return chars;
    }
}
