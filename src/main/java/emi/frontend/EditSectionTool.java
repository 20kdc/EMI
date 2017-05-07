/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package emi.frontend;

import emi.backend.IBackend;

/**
 * Stand-in tool that lets you select which section to edit.
 * Created on 5/7/17.
 */
public class EditSectionTool implements ITool {
    private final IBackend.IBackendFile target;
    public EditSectionTool(IBackend.IBackendFile targ) {
        target = targ;
    }

    @Override
    public String[] getDefinition() {
        return "edit-section target section-idx".split(" ");
    }

    @Override
    public String[] getDefaultVals() {
        return null;
    }

    @Override
    public ITool execute(String[] values) {
        return new EditSpecificSectionTool(target, Integer.parseInt(values[1]));
    }
}
