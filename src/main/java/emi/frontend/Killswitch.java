/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package emi.frontend;

/**
 * On windows which have certain events which "terminate" them,
 *  this provides a simple switch that leads to code like this:
 *
 * if (killswitch.armOrFail())
 *  return; // Already armed.
 *
 * killswitch.disarm(); // Operation cancelled or complete, so allow further stuff to happen
 *
 * Note that this object is worth less in terms of it's code, and more in terms of that it's a clear indicator to readers
 *  that the object is trying to avoid being used at the same time as something else.
 * Created on 29/05/17.
 */
public class Killswitch {
    private boolean armed = true; // Starts out armed.
    public boolean armOrFail() {
        if (armed) {
            System.err.println("Inconsistency trap triggered.");
            return true;
        }
        armed = true;
        return false;
    }
    public void disarm() {
        if (!armed)
            throw new RuntimeException("Cannot disarm - wasn't armed.");
        armed = false;
    }
}
