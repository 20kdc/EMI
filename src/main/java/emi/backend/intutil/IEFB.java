/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package emi.backend.intutil;

import java.io.IOException;

/**
 * The "behind the scenes" stuff in an IBackendFile.
 * Note that the immutable parts of the API are an attempt to ensure an EFB always remains in a *consistent state*.
 * Created on 4/28/17.
 */
public interface IEFB {

    // This is meant to be immutable.
    // It can be internally mutable until the object leaves the API.
    interface IFileSection {
        // "" (Waste data), "header", "data"
        // and 6 combinations of r, w and x.
        String type();

        String name();

        // A machine-readable description of section header stuff.
        // First word of each is name, second is type "string", "num", "enum", or "flags".
        // (If "flags", the remaining words are the available flags - if "enum", they are the options available.)
        // Notably, the keys are in the order of values given to createSectionData.
        // Passable to createSection.
        String[] describeKeys();

        String getValue(String key);

        IFileSection changeValue(String key, String value);

        // The user can't write to the byte array given.
        byte[] data();

        // Adding all of these together should give the total length of the file.
        long fileDataLength();

        // The user can't write to the byte array they give.
        IFileSection changedData(byte[] data);
    }

    interface IVMFileSection {
        // Note: If there is no such thing as an RVA in the format, this can actually be VA.
        // Get the RVA of this section.
        long getRVA();

        boolean canMove();

        // Return a version of this section with the RVA moved.
        IFileSection move(long rva);

        // This can't be under data size, but might be over.
        long getLength();

        IFileSection changedLength(long newLength);
    }

    void loadFile(byte[] data) throws IOException;

    IEFB createBlank();

    // The list of sections that can be created.
    String[] creatableSections();

    // Creates a valid section from scratch, positions it somewhere & such.
    IFileSection createSection(String type);

    // File sections in the file, including "dead space".
    // It is safe to modify the returned array.
    IFileSection[] fileSections();

    // Validate and change. Can result in a different configuration than given -
    // it's up to the EFB writer's judgement what happens in invalid cases.
    // Order in the header, for example, is decided by RVA.
    void changeSections(IFileSection[] file);

    byte[] saveFile();

    String[] verifyFile();
}
