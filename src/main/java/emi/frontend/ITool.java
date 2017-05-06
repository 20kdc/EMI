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
    // Note that values includes the command name in definition, but doesn't know about dl-/ds- stuff.
    // Throwing exceptions is fine. Returning null is allowed in this particular case, to mean "cancellation during a prompt"
    String[] execute(String[] values);
}
