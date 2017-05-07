/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package emi.backend;

/**
 * Created on 4/28/17.
 */
public class FlagUtils {
    public static String get(String[] flags, long chars) {
        String r = "";
        long top = 1L << (flags.length - 1);
        for (int i = 0; i < flags.length; i++) {
            if ((chars & top) != 0) {
                if (r.equals("")) {
                    r = flags[i];
                } else {
                    r += " " + flags[i];
                }
            }
            top >>= 1;
        }
        return r;
    }

    public static long put(String[] flags, String value) {
        String[] r = value.split(" ");
        if (r.length == 1)
            if (r[0].length() == 0)
                return 0;
        long chars = 0;
        long top = 1L << (flags.length - 1);
        for (String s : r) {
            boolean ok = false;
            for (int i = 0; i < flags.length; i++) {
                if (s.equals(flags[i])) {
                    chars |= top >> i;
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
