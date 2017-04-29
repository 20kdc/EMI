/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package emi.backend.efb;

import emi.backend.DataFileSection;
import emi.backend.IEFB;
import emi.backend.ImmobileVMDataFileSection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Implementation of a simple EFB.
 * Acts as both an example and a good testbed for the basic functions.
 * Created on 4/28/17.
 */
public class MSDOSEFB implements IEFB {
    public MSDOSHeaderFileSection header = new MSDOSHeaderFileSection();
    public ImmobileVMDataFileSection data = new ImmobileVMDataFileSection(new byte[0], "rwx", "Code", 0);
    public DataFileSection waste = new DataFileSection(new byte[0], "", "Waste Footer");

    class MSDOSHeaderFileSection implements IFileSection {
        public short numRelocs;
        public short bssParagraphsMin;
        public short bssParagraphsMax;
        public short ssRelative;
        public short spInit;
        public short checksum;
        public short ipInit;
        public short csRelative;
        public short relocOfs;
        public short overlayNum;
        // Data after basic header. Must have a length of 4, 14, 24...
        private byte[] headerExd;

        public MSDOSHeaderFileSection() {
            numRelocs = 0;
            bssParagraphsMin = 0;
            bssParagraphsMax = 0;
            ssRelative = 0;
            spInit = 0;
            checksum = 0;
            ipInit = 0;
            csRelative = 0;
            relocOfs = 0;
            overlayNum = 0;
            headerExd = new byte[4];
        }

        public MSDOSHeaderFileSection(MSDOSHeaderFileSection h) {
            numRelocs = h.numRelocs;
            bssParagraphsMin = h.bssParagraphsMin;
            bssParagraphsMax = h.bssParagraphsMax;
            ssRelative = h.ssRelative;
            spInit = h.spInit;
            checksum = h.checksum;
            ipInit = h.ipInit;
            csRelative = h.csRelative;
            relocOfs = h.relocOfs;
            overlayNum = h.overlayNum;
            headerExd = h.headerExd;
        }

        public MSDOSHeaderFileSection(ByteBuffer b) {
            numRelocs = b.getShort();
            int headerSize = (b.getShort() & 0xFFFF) * 16;
            bssParagraphsMin = b.getShort();
            bssParagraphsMax = b.getShort();
            ssRelative = b.getShort();
            spInit = b.getShort();
            checksum = b.getShort();
            ipInit = b.getShort();
            csRelative = b.getShort();
            relocOfs = b.getShort();
            overlayNum = b.getShort();
            headerSize -= 0x1C;
            if (headerSize < 0)
                headerSize = 0;
            headerExd = new byte[headerSize];
            b.get(headerExd);
        }

        public void writeBB(ByteBuffer bb) {
            bb.putShort(numRelocs);
            int l = (headerExd.length + 0x1C) / 16;
            bb.putShort((short) l);
            bb.putShort(bssParagraphsMin);
            bb.putShort(bssParagraphsMax);
            bb.putShort(ssRelative);
            bb.putShort(spInit);
            bb.putShort(checksum);
            bb.putShort(ipInit);
            bb.putShort(csRelative);
            bb.putShort(relocOfs);
            bb.putShort(overlayNum);
            bb.put(headerExd);
        }

        @Override
        public String type() {
            return "header";
        }

        @Override
        public String name() {
            return "Header";
        }

        @Override
        public String[] describeKeys() {
            return new String[]{
                    "numRelocs num",
                    "bssParagraphsMin num",
                    "bssParagraphsMax num",
                    "ssRelative num",
                    "spInit num",
                    "checksum num",
                    "ipInit num",
                    "csRelative num",
                    "relocOfs num",
                    "overlayNum num"
            };
        }

        @Override
        public String getValue(String key) {
            // All keys are shorts, just use reflection
            try {
                return Integer.toString(((Short) (MSDOSHeaderFileSection.class.getField(key).get(this))) & 0xFFFF);
            } catch (Exception e) {
                e.printStackTrace();
            }
            throw new RuntimeException("Bad read " + key);
        }

        @Override
        public MSDOSHeaderFileSection changeValue(String key, String value) {
            MSDOSHeaderFileSection fs = new MSDOSHeaderFileSection(this);
            try {
                fs.getClass().getField(key).set(fs, (short) Integer.parseInt(value));
                return fs;
            } catch (Exception e) {
                e.printStackTrace();
            }
            throw new RuntimeException("Bad write " + key);
        }

        @Override
        public byte[] data() {
            return headerExd;
        }

        @Override
        public long fileDataLength() {
            return 0x1C + headerExd.length;
        }

        @Override
        public MSDOSHeaderFileSection changedData(byte[] data) {
            if ((data.length + 0x1C) % 0x10 != 0)
                throw new RuntimeException("The length of data + 0x1C must be a multiple of 0x10");
            MSDOSHeaderFileSection md = new MSDOSHeaderFileSection(this);
            md.headerExd = data;
            return md;
        }
    }

    public MSDOSEFB() {
    }

    @Override
    public void loadFile(byte[] dat) throws IOException {
        ByteBuffer b = ByteBuffer.wrap(dat);
        b.order(ByteOrder.LITTLE_ENDIAN);
        if (b.getShort() != (short) 0x5A4D)
            throw new IOException("Bad signature.");
        int usedBytes = b.getShort() & 0xFFFF;
        int blocks = b.getShort() & 0xFFFF;
        header = new MSDOSHeaderFileSection(b);
        if (usedBytes == 0)
            usedBytes = 512;
        if (blocks == 0)
            throw new RuntimeException("Zero-length file");
        int total = ((blocks - 1) * 512) + usedBytes;
        int headlen = header.headerExd.length + 0x1C;
        if (total < headlen)
            total = headlen;
        byte[] d2 = new byte[total - headlen];
        b.get(d2);
        byte[] w = new byte[dat.length - total];
        b.get(w);
        data = data.changedData(d2);
        waste = waste.changedData(w);
    }

    @Override
    public IEFB createBlank() {
        return new MSDOSEFB();
    }

    public String[] creatableSections() {
        return new String[]{};
    }

    @Override
    public IFileSection createSection(int idx) {
        throw new RuntimeException("Cannot create sections.");
    }

    @Override
    public IFileSection[] fileSections() {
        return new IFileSection[]{
                header,
                data,
                waste
        };
    }

    @Override
    public void changeSections(IFileSection[] file) {
        if (file.length != 3)
            throw new RuntimeException("Cannot add or remove sections from MSDOS EXE.");
        if (!(file[0] instanceof MSDOSHeaderFileSection))
            throw new RuntimeException("First section must be a header.");
        if ((!(file[1] instanceof ImmobileVMDataFileSection)) || (!file[1].type().equals("rwx")))
            throw new RuntimeException("Second section must be the code section.");
        if ((!(file[2] instanceof DataFileSection)) || (!file[2].type().equals("")))
            throw new RuntimeException("Third section must be the waste data section.");
        header = (MSDOSHeaderFileSection) file[0];
        data = (ImmobileVMDataFileSection) file[1];
        waste = (DataFileSection) file[2];
    }

    @Override
    public byte[] saveFile() {
        int fileSize = 0x1C + header.headerExd.length + data.d.length;
        int totalSize = fileSize + waste.d.length;
        byte[] b = new byte[totalSize];
        ByteBuffer bb = ByteBuffer.wrap(b);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putShort((short) 0x5A4D);
        int usedBytes = fileSize % 512;
        int blocks = fileSize / 512;
        if (usedBytes != 0)
            blocks++;
        bb.putShort((short) usedBytes);
        bb.putShort((short) blocks);
        header.writeBB(bb);
        bb.put(data.d);
        bb.put(waste.d);
        return b;
    }

    // Technically this is a lie, but the relocation data isn't "traditional" -
    //  removing it will break the program.

    @Override
    public boolean fileContainsRelocationData() {
        return false;
    }

    @Override
    public void removeRelocationData() {
        throw new RuntimeException("Relocation data doesn't exist in this format.");
    }
}
