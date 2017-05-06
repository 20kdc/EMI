/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package emi.backend;

import emi.backend.efb.pe32.PE32FileOptHeadSection;

import java.nio.ByteBuffer;
import java.util.LinkedList;

/**
 * Meta-language to simplify simple structures
 * elements:
 * size size, sets structure size
 * data byte, magic numbers & such
 * skip bytes, skips some amount of bytes
 * u(32/16/8) name, accesses property
 * u(32/16/8) name (...), accesses property as a bitfield.
 * fst name size, string of a fixed length - note this is represented in-struct as a byte[], but used as a String!
 * Created on 5/2/17.
 */
public class StructUtils {
    public static String[] descKeysFromSU(String[] struct) {
        LinkedList<String> r = new LinkedList<String>();
        for (String s : struct) {
            String[] args = s.split(" ");
            if (args[0].equals("fst")) {
                r.add(args[1] + " str");
            } else {
                if (args.length == 2) {
                    if (args[0].equals("u32"))
                        r.add(args[1] + " num");
                    else if (args[0].equals("u16"))
                        r.add(args[1] + " num");
                    else if (args[0].equals("u8"))
                        r.add(args[1] + " num");
                } else {
                    if (args[0].equals("u32") || args[0].equals("u16") || args[0].equals("u8")) {
                        // flags!
                        String f = "";
                        for (int i = 2; i < args.length; i++)
                            f += " " + args[i];
                        r.add(args[1] + " flags" + f);
                    }
                }
            }
        }
        return r.toArray(new String[0]);
    }

    public static void setStruct(String[] struct, Object instance, String key, String value) {
        try {
            Class instClass = instance.getClass();
            for (String s : struct) {
                String[] args = s.split(" ");
                boolean u32 = args[0].equals("u32");
                boolean u16 = args[0].equals("u16");
                boolean u8 = args[0].equals("u8");
                if (u32 || u16 || u8) {
                    if (args[1].equals(key)) {
                        if (args.length != 2) {
                            // Wring value through translation to long.
                            String[] f = new String[args.length - 2];
                            for (int i = 0; i < f.length; i++)
                                f[i] = args[i + 2];
                            value = Long.toString(FlagUtils.put(f, value));
                        }
                        if (u32) {
                            instClass.getField(args[1]).setInt(instance, Long.decode(value).intValue());
                            return;
                        } else if (u16) {
                            instClass.getField(args[1]).setShort(instance, Long.decode(value).shortValue());
                            return;
                        } else {
                            instClass.getField(args[1]).setByte(instance, Long.decode(value).byteValue());
                            return;
                        }
                    }
                } else if (args[0].equals("fst")) {
                    if (args[1].equals(key)) {
                        int space = (int) (long) Long.decode(args[2]);
                        byte[] r1 = value.getBytes("UTF-8");
                        if (r1.length > space)
                            throw new RuntimeException("Field " + key + " value too long");
                        byte[] r2 = new byte[space];
                        for (int i = 0; i < r1.length; i++)
                            r2[i] = r1[i];
                        instClass.getField(args[1]).set(instance, r2);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("Couldn't find field " + key);
    }

    public static String getStruct(String[] struct, Object instance, String key) {
        try {
            Class instClass = instance.getClass();
            for (String s : struct) {
                String[] args = s.split(" ");
                boolean u32 = args[0].equals("u32");
                boolean u16 = args[0].equals("u16");
                boolean u8 = args[0].equals("u8");
                if (u32 || u16 || u8) {
                    if (args[1].equals(key)) {
                        String r;
                        if (u32) {
                            r = Long.toString(instClass.getField(args[1]).getInt(instance) & 0xFFFFFFFFL);
                        } else if (u16) {
                            r = Long.toString(instClass.getField(args[1]).getShort(instance) & 0xFFFFL);
                        } else {
                            r = Long.toString(instClass.getField(args[1]).getByte(instance) & 0xFFL);
                        }
                        if (args.length != 2) {
                            // Wring value through translation from long.
                            String[] f = new String[args.length - 2];
                            for (int i = 0; i < f.length; i++)
                                f[i] = args[i + 2];
                            r = FlagUtils.get(f, Long.decode(r));
                        }
                        return r;
                    }
                } else if (args[0].equals("fst")) {
                    if (args[1].equals(key)) {
                        byte[] r = (byte[]) instClass.getField(args[1]).get(instance);
                        if (r.length != Long.decode(args[2]))
                            throw new RuntimeException("invalid situation!");
                        int nzb = 0;
                        byte[] penulti = new byte[r.length];
                        for (byte bb : r)
                            if (bb != 0)
                                penulti[nzb++] = bb;
                        byte[] fin = new byte[nzb];
                        for (int i = 0; i < fin.length; i++)
                            fin[i] = penulti[i];
                        return new String(fin, "UTF-8");
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("Couldn't find field " + key);
    }

    public static void copyStruct(String[] optHeadStruct, Object dst, Object src) {
        for (String s : optHeadStruct) {
            String[] args = s.split(" ");
            boolean copy = args[0].equals("u32") || args[0].equals("u16") || args[0].equals("u8") || args[0].equals("fst");
            if (copy)
                setStruct(optHeadStruct, dst, args[1], getStruct(optHeadStruct, src, args[1]));
        }
    }

    public static void saveStruct(ByteBuffer o, String[] struct, Object instance) {
        try {
            Class instClass = instance.getClass();
            for (String s : struct) {
                String[] args = s.split(" ");
                if (args[0].equals("size")) {
                    // ignored
                } else if (args[0].equals("data")) {
                    o.put(Long.decode(args[1]).byteValue());
                } else if (args[0].equals("skip")) {
                    int space = (int) (long) Long.decode(args[1]);
                    o.position(o.position() + space);
                } else if (args[0].equals("fst")) {
                    byte[] r = (byte[]) instClass.getField(args[1]).get(instance);
                    if (r.length != Long.decode(args[2]))
                        throw new RuntimeException("invalid situation!");
                    o.put(r);
                } else {
                    boolean u32 = args[0].equals("u32");
                    boolean u16 = args[0].equals("u16");
                    boolean u8 = args[0].equals("u8");
                    if (u32 || u16 || u8) {
                        if (u32) {
                            o.putInt(instClass.getField(args[1]).getInt(instance));
                        } else if (u16) {
                            o.putShort(instClass.getField(args[1]).getShort(instance));
                        } else {
                            o.put(instClass.getField(args[1]).getByte(instance));
                        }
                    } else {
                        throw new RuntimeException("Can't understand " + args[0]);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadStruct(ByteBuffer o, String[] struct, Object instance) {
        try {
            Class instClass = instance.getClass();
            for (String s : struct) {
                String[] args = s.split(" ");
                if (args[0].equals("size")) {
                    // ignored
                } else if (args[0].equals("data")) {
                    if (o.get() != Long.decode(args[1]).byteValue())
                        throw new RuntimeException("Bad constant byte");
                } else if (args[0].equals("skip")) {
                    int space = (int) (long) Long.decode(args[1]);
                    o.position(o.position() + space);
                } else if (args[0].equals("fst")) {
                    int l = (int) (long) Long.decode(args[2]);
                    byte[] d = new byte[l];
                    o.get(d);
                    instClass.getField(args[1]).set(instance, d);
                } else {
                    boolean u32 = args[0].equals("u32");
                    boolean u16 = args[0].equals("u16");
                    boolean u8 = args[0].equals("u8");
                    if (u32 || u16 || u8) {
                        if (u32) {
                            instClass.getField(args[1]).setInt(instance, o.getInt());
                        } else if (u16) {
                            instClass.getField(args[1]).setShort(instance, o.getShort());
                        } else {
                            instClass.getField(args[1]).setByte(instance, o.get());
                        }
                    } else {
                        throw new RuntimeException("Can't understand " + args[0]);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String[] validateStruct(String[] struct) {
        long sizeExpected = -1;
        long size = 0;
        try {
            for (String s : struct) {
                String[] args = s.split(" ");
                if (args[0].equals("size")) {
                    sizeExpected = Long.decode(args[1]);
                } else if (args[0].equals("data")) {
                    size++;
                } else if (args[0].equals("skip")) {
                    size += Long.decode(args[1]);
                } else if (args[0].equals("fst")) {
                    size += Long.decode(args[2]);
                } else {
                    boolean u32 = args[0].equals("u32");
                    boolean u16 = args[0].equals("u16");
                    boolean u8 = args[0].equals("u8");
                    if (u32 || u16 || u8) {
                        if (u32) {
                            size += 4;
                        } else if (u16) {
                            size += 2;
                        } else {
                            size += 1;
                        }
                    } else {
                        throw new RuntimeException("Can't understand " + args[0]);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (size != sizeExpected)
            throw new RuntimeException("Structure encoded to wrong size.");
        return struct;
    }
}
