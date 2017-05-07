/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package emi.frontend;

/**
 * The interface for a tool usable by ToolInterface.
 * Created on 5/6/17.
 */
public interface ITool {
    // name & args, in the same format as described in EFBWrapperBackend, with the split done early
    String[] getDefinition();
    // null, in most cases - otherwise, assumed-valid default values.
    String[] getDefaultVals();
    // Note that values includes the command name in definition, but doesn't know about dl-/ds- stuff.
    // Throwing exceptions is fine, and shows error. Returning null indicates success.
    // Returning the ITool object itself indicates cancel (put dialog back up and wait for further instruction)
    // Returning a new ITool object indicates that the mantle is to be passed to a new tool.
    ITool execute(String[] values);

    // If this is true, the tool MUST NOT invoke any other tool, MUST NOT open any dialogs, etc.
    // The entire tool, once provided with arguments, must execute and be done with in a single event without returning to AWT.
    // Furthermore, the tool must not return another tool (except itself, but this case is treated as if it returned null)
    // Default to "false".
    boolean instantResponse();
}
