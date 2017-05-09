/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package emi.backend.intutil;

/**
 * Created on 5/9/17.
 */
public interface IAlignmentPredictate {
    // 0 means unaligned.
    long mustAlignRVA(IEFB.IFileSection fs);
    long mustAlignPhys(IEFB.IFileSection fs);
    IEFB.IFileSection generatePhysicalPadding(long amount);
}
