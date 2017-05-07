/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package emi.frontend;

import emi.backend.IBackend;

import java.util.LinkedList;

/**
 * The actual tool for editing a given section in a given target.
 * Has to transform section key format into tool help format.
 * Created on 5/7/17.
 */
public class EditSpecificSectionTool implements ITool {
    private final String[] definition;
    private final String[] defVals;

    private final IBackend.IBackendFile ibf;
    private final String[] paramNames;
    private final int id;

    public EditSpecificSectionTool(IBackend.IBackendFile target, int i) {
        ibf = target;
        String[] skf = target.runOperation(new String[] {"list-section-keys", Integer.toString(i)});
        // Need to build a full definition
        defVals = new String[skf.length];
        paramNames = new String[skf.length];
        LinkedList<String> def = new LinkedList<String>();
        def.add("edit-section-x");
        int idx = 0;
        for (String ld : skf) {
            String[] paramData = ld.split(" ");
            paramNames[idx] = paramData[0];
            defVals[idx++] = target.runOperation(new String[] {"get-section-value", Integer.toString(i), paramData[0]})[0];
            for (String s : paramData)
                def.add(s);
            boolean semicolonTerminated = paramData[1].equals("enum") || paramData[1].equals("flags");
            if (semicolonTerminated)
                def.add(";");
        }
        definition = def.toArray(new String[0]);
        id = i;
    }

    @Override
    public String[] getDefinition() {
        return definition;
    }

    @Override
    public String[] getDefaultVals() {
        return defVals;
    }

    @Override
    public ITool execute(String[] values) {
        for (int i = 1; i < values.length; i++)
            ibf.runOperation(new String[] {
                    "set-section-value",
                    Integer.toString(id),
                    paramNames[i - 1],
                    values[i]
            });
        return null;
    }
}
