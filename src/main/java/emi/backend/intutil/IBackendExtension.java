/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package emi.backend.intutil;

import emi.backend.IBackend;

/**
 * Allows adding purpose-specific tools to backends.
 * Note that these tools act "outside of the system".
 * The EFB still has to provide functionality to edit everything - this acts as macros.
 * As they act as macros, there is a system in place (added by the ExtenderBackend) where if a command fails, the EFB is reverted (via a "ds-save" before execution).
 * Created on 5/6/17.
 */
public interface IBackendExtension {
    // Additional help entries.
    String[] addedCommands();
    String[] runCommand(IBackend.IBackendFile root, String[] arguments);
}
