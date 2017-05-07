/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package emi.frontend;

import emi.backend.FileUtilities;
import emi.backend.IBackend;

import javax.swing.*;

/**
 * Used to pass a backend command to ToolInterface.
 *
 * Created on 5/6/17.
 */
public class BackendCommandTool implements ITool {
    public final String[] definition;
    public final IBackend.IBackendFile target;

    public BackendCommandTool(String[] def, IBackend.IBackendFile t) {
        definition = def;
        target = t;
    }

    @Override
    public String[] getDefinition() {
        return definition;
    }

    @Override
    public String[] getDefaultVals() {
        return null;
    }

    @Override
    public ITool execute(String[] values) {
        if (values[0].startsWith("dl-")) {
            values[0] = values[0].substring(3);
            JFileChooser jfc = new JFileChooser();
            if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                showDialog(target.runDLOperation(values, FileUtilities.loadFile(jfc.getSelectedFile())));
                return null;
            }
            return this;
        }
        if (values[0].startsWith("ds-")) {
            values[0] = values[0].substring(3);
            JFileChooser jfc = new JFileChooser();
            if (jfc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                byte[] out = target.runDSOperation(values);
                FileUtilities.saveFile(jfc.getSelectedFile(), out);
                showDialog(new String[] {"The data was saved successfully."});
                return null;
            }
            return this;
        }
        // do whatever for ds and other.
        showDialog(target.runOperation(values));
        return null;
    }

    private void showDialog(String[] strings) {
        String str = "";
        for (String f : strings) {
            if (str.length() != 0)
                str += "\r\n";
            str += f;
        }
        Main.showText("Output", str);
    }
}
