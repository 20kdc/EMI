/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package emi.frontend;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * The Dialog Singleton, responsible for all UI.
 * An instance of this is owned by Main.
 * Created on 29/05/17.
 */
public class DialogSingleton {
    // Access only for open/close dialogs.
    public final JFrame pureFrame = new JFrame("EMI");
    private Runnable onClose;

    public DialogSingleton() {
        pureFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        pureFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                onClose.run();
            }
        });
    }

    public void prepare(String title, Container iFrame, Runnable runnable) {
        pureFrame.setTitle(title);
        pureFrame.setContentPane(iFrame);
        pureFrame.setSize(480, 640);
        pureFrame.pack();
        onClose = runnable;
    }
    public void prepareLarge(String title, Container iFrame, Runnable runnable) {
        pureFrame.setTitle(title);
        pureFrame.setContentPane(iFrame);
        pureFrame.setSize(480, 640);
        pureFrame.pack();
        pureFrame.setSize(480, 640);
        onClose = runnable;
    }

    public void boot() {
        pureFrame.setVisible(true);
    }
}
