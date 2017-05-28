/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package emi.backend.intutil;

import java.util.LinkedList;

/**
 * Created on 5/9/17.
 */
public class AlignmentUtil {
    public static LinkedList<IEFB.IFileSection> ensureAlignment(LinkedList<IEFB.IFileSection> nfs, IAlignmentPredictate iAlignmentPredictate, int fileAlignment, int sectionAlignment) {
        LinkedList<IEFB.IFileSection> results = new LinkedList<IEFB.IFileSection>();
        long position = 0;
        for (IEFB.IFileSection ifs : nfs) {
            long alignPhys = iAlignmentPredictate.mustAlignPhys(ifs);
            long alignRVA = iAlignmentPredictate.mustAlignRVA(ifs);
            if (alignPhys != 0) {
                long wantedPosition = roundUp(position, alignPhys);
                if (wantedPosition != position)
                    results.add(iAlignmentPredictate.generatePhysicalPadding(wantedPosition - position));
                position = wantedPosition;
            }
            if (alignRVA != 0) {
                IEFB.IVMFileSection ivms = (IEFB.IVMFileSection) ifs;
                if (roundUp(ivms.getRVA(), alignRVA) != ivms.getRVA())
                    throw new RuntimeException("Cannot accept section layout: unaligned RVAs");
            }
            results.add(ifs);
            position += ifs.fileDataLength();
        }
        return results;
    }

    public static long roundUp(long position, long sectionAlignment) {
        if (sectionAlignment < 0)
            throw new RuntimeException("Bad alignment: " + sectionAlignment);
        if (sectionAlignment == 0)
            sectionAlignment = 1;
        if (position % sectionAlignment != 0)
            return (position - (position % sectionAlignment)) + sectionAlignment;
        return position;
    }
}
