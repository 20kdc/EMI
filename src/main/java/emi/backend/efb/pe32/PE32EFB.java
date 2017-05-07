/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package emi.backend.efb.pe32;

import emi.backend.intutil.DataFileSection;
import emi.backend.intutil.IEFB;
import emi.backend.LongUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

import static javax.swing.UIManager.getInt;

/**
 * The EFB for 32-bit PE files.
 * Created on 4/28/17.
 */
public class PE32EFB implements IEFB {

    public PE32FileStubSection preHeadSection = new PE32FileStubSection();
    // This is 0x18 bytes and has no data.
    public PE32FileHeadSection headSection = new PE32FileHeadSection();
    public PE32FileOptHeadSection optHeadSection = new PE32FileOptHeadSection();
    public PE32FileSectionHeadersSection secHeadSection = new PE32FileSectionHeadersSection(0);

    public LinkedList<IFileSection> fs = new LinkedList<IFileSection>();

    public PE32EFB() {
        fs.add(preHeadSection);
        fs.add(headSection);
        fs.add(optHeadSection);
        fs.add(secHeadSection);
    }

    @Override
    public void loadFile(byte[] data) throws IOException {
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        int prehead = bb.getInt(0x3C);
        byte[] phd = new byte[prehead];
        bb.get(phd);

        if (bb.getInt() != 0x4550)
            throw new RuntimeException("bad PE header");

        headSection = new PE32FileHeadSection();
        headSection.machine = bb.getShort();
        int sections = bb.getShort() & 0xFFFF;
        headSection.tds = bb.getInt();
        // Unsure if these are file position pointers.
        // If so, it means that the symbol table needs to count as a Section.
        headSection.symO = bb.getInt();
        headSection.symC = bb.getInt();
        int optHeadSize = bb.getShort() & 0xFFFF;
        headSection.chars = bb.getShort();

        byte[] optHead = new byte[optHeadSize];
        bb.get(optHead);
        ByteBuffer bb2 = ByteBuffer.wrap(optHead);
        bb2.order(ByteOrder.LITTLE_ENDIAN);
        optHeadSection = new PE32FileOptHeadSection(bb2);
        preHeadSection = preHeadSection.changedData(phd);
        secHeadSection = new PE32FileSectionHeadersSection(sections);

        fs.clear();
        long rootPoint = bb.position() + secHeadSection.fileDataLength();
        // Used space.
        // The idea is that
        boolean[] map = new boolean[(int) (data.length - rootPoint)];
        // Symbol table would be handled here
        // Add entries...
        final HashMap<IFileSection, Long> fileAddrs = new HashMap<IFileSection, Long>();
        for (int i = 0; i < sections; i++) {
            byte[] name = new byte[8];
            bb.get(name);
            PE32FileSection fs2 = new PE32FileSection();
            fs2.name = name;
            fs2.vSize = bb.getInt();
            fs2.rva = bb.getInt();
            long rdSize = LongUtils.usI(bb.getInt());
            long rdPtr = LongUtils.usI(bb.getInt());
            if (rdPtr != 0) {
                if (rdPtr < rootPoint)
                    throw new RuntimeException("Raw data pointer " + LongUtils.longToHexval(rdPtr) + " < end of section headers " + LongUtils.longToHexval(rootPoint));
                for (int j = 0; j < rdSize; j++)
                    map[(int) ((rdPtr + j) - rootPoint)] = true;
                fs2.data = pullSectionFrom(bb, rdPtr, rdSize);
            } else {
                if (rdSize > fs2.vSize)
                    fs2.vSize = (int) rdSize;
            }

            // Set up a consistent order
            if (fs2.data.length == 0)
                rdPtr = data.length + (i + 1);

            // See notes on symbol tables
            fs2.relocsO = bb.getInt();
            fs2.linesO = bb.getInt();
            fs2.relocsN = bb.getShort();
            fs2.linesN = bb.getShort();
            fs2.chars = bb.getInt();
            fs.add(fs2);
            fileAddrs.put(fs2, rdPtr);
        }
        int startW = 0;
        boolean running = false;
        for (int i = 0; i < map.length; i++) {
            if (!running) {
                if (!map[i]) {
                    startW = i;
                    running = true;
                }
            } else {
                if (map[i]) {
                    IFileSection f = makeWaste(pullSectionFrom(bb, startW + rootPoint, i - startW));
                    fileAddrs.put(f, startW + rootPoint);
                    fs.add(f);
                    running = false;
                }
            }
        }
        if (running) {
            IFileSection f = makeWaste(pullSectionFrom(bb, startW + rootPoint, map.length - startW));
            fileAddrs.put(f, startW + rootPoint);
            fs.add(f);
        }

        Collections.sort(fs, new Comparator<IFileSection>() {
            @Override
            public int compare(IFileSection iFileSection, IFileSection t1) {
                long a = fileAddrs.get(iFileSection);
                long b = fileAddrs.get(t1);
                if (a < b)
                    return -1;
                if (a > b)
                    return 1;
                return 0;
            }
        });

        fs.addFirst(secHeadSection);
        fs.addFirst(optHeadSection);
        fs.addFirst(headSection);
        fs.addFirst(preHeadSection);
    }

    private IFileSection makeWaste(byte[] bytes) {
        return new DataFileSection(bytes, "waste", "(Unused Data)");
    }

    private byte[] pullSectionFrom(ByteBuffer bb, long rootPoint, long l) {
        byte[] data = new byte[(int) l];
        int p = bb.position();
        bb.position((int) rootPoint);
        bb.get(data);
        bb.position(p);
        return data;
    }

    @Override
    public IEFB createBlank() {
        return new PE32EFB();
    }

    @Override
    public String[] creatableSections() {
        return new String[]{"waste", "section"};
    }

    @Override
    public IFileSection createSection(String tidx) {
        if (tidx.equals("waste"))
            return makeWaste(new byte[0x1000]);
        if (!tidx.equals("section"))
            throw new RuntimeException("There is only one kind of section creatable.");
        PE32FileSection s = new PE32FileSection();
        long maxRVA = 0;
        for (IFileSection ifs : fs) {
            if (ifs instanceof IVMFileSection) {
                IVMFileSection vm = (IVMFileSection) ifs;
                long point = vm.getRVA() + vm.getLength();
                if (point > maxRVA)
                    maxRVA = point;
            }
        }
        s.rva = (int) maxRVA;
        s.vSize = 0x1000;
        return s;
    }

    @Override
    public IFileSection[] fileSections() {
        return fs.toArray(new IFileSection[0]);
    }

    @Override
    public void changeSections(IFileSection[] file) {
        // Firstly, extract file header, DOS Stub, and Opt. Header (only marked DFS) sections.
        PE32FileStubSection phs = null;
        PE32FileHeadSection hs = null;
        PE32FileOptHeadSection ohs = null;

        fs.clear();
        int sCount = 0;
        for (IFileSection ifs : file) {
            if (ifs instanceof PE32FileStubSection)
                phs = (PE32FileStubSection) ifs;
            if (ifs instanceof PE32FileHeadSection)
                hs = (PE32FileHeadSection) ifs;
            if (ifs instanceof PE32FileOptHeadSection)
                ohs = (PE32FileOptHeadSection) ifs;
            if (ifs instanceof DataFileSection)
                fs.add(ifs);
            if (ifs instanceof PE32FileSection) {
                fs.add(ifs);
                sCount++;
            }
        }
        if (ohs == null)
            throw new RuntimeException("PE32 needs Opt.Header");
        if (hs == null)
            throw new RuntimeException("PE32 needs Header");
        if (phs == null)
            throw new RuntimeException("PE32 needs Stub");
        secHeadSection = new PE32FileSectionHeadersSection(sCount);
        fs.addFirst(secHeadSection);
        optHeadSection = ohs;
        fs.addFirst(optHeadSection);
        headSection = hs;
        fs.addFirst(headSection);
        preHeadSection = phs;
        fs.addFirst(preHeadSection);
    }

    @Override
    public byte[] saveFile() {
        // This will take a while...
        long tally = 0;
        // More or less precise.
        HashMap<IFileSection, Long> positions = new HashMap<IFileSection, Long>();
        LinkedList<PE32FileSection> pfs = new LinkedList<PE32FileSection>();
        for (IFileSection ifs : fs) {
            positions.put(ifs, tally);
            tally += ifs.fileDataLength();
            if (ifs instanceof PE32FileSection)
                pfs.add((PE32FileSection) ifs);
        }
        Collections.sort(pfs, new Comparator<PE32FileSection>() {
            @Override
            public int compare(PE32FileSection pe32FileSection, PE32FileSection t1) {
                if (pe32FileSection.rva < t1.rva)
                    return -1;
                if (pe32FileSection.rva > t1.rva)
                    return 1;
                return 0;
            }
        });
        byte[] r = new byte[(int) tally];
        ByteBuffer bb = ByteBuffer.wrap(r);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        // Begin dump
        bb.put(preHeadSection.data);
        bb.putInt(0x4550);
        bb.putShort(headSection.machine);
        bb.putShort((short) secHeadSection.ds);

        bb.putInt(headSection.tds);
        bb.putInt(headSection.symO);
        bb.putInt(headSection.symC);
        bb.putShort((short) optHeadSection.fileDataLength());
        bb.putShort(headSection.chars);

        // Basic header built...
        optHeadSection.save(bb);
        // Section headers...
        for (PE32FileSection ifs : pfs) {
            bb.put(ifs.name);
            bb.putInt(ifs.vSize);
            bb.putInt(ifs.rva);
            bb.putInt(ifs.data.length);
            if (ifs.data.length != 0) {
                bb.putInt((int) (long) positions.get(ifs));
            } else {
                bb.putInt(0);
            }
            bb.putInt(ifs.relocsO);
            bb.putInt(ifs.linesO);
            bb.putShort(ifs.relocsN);
            bb.putShort(ifs.linesN);
            bb.putInt(ifs.chars);
        }
        // Sections
        for (IFileSection ifs : fs) {
            boolean dumpData = false;
            if (ifs instanceof DataFileSection)
                if (((DataFileSection) ifs).type.equals("waste"))
                    dumpData = true;
            if (ifs instanceof PE32FileSection)
                dumpData = true;
            if (dumpData)
                bb.put(ifs.data());
        }
        return r;
    }
}
