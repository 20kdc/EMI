/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package emi.frontend;

import emi.backend.IBackend;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.LinkedList;

/**
 * The interface for using a tool.
 * Created on 5/4/17.
 */
public class ToolInterface {
    final IBackend.IBackendFile file;
    final JFrame mainFrame;
    final Runnable onClose;
    public byte[] data;
    public LinkedList<ArgBuilder> argBuilders;

    public ToolInterface(String[] cmd, Runnable onDie, IBackend.IBackendFile ibf) {
        file = ibf;
        mainFrame = new JFrame("EMI " + cmd[0]);

        // Build the dialog.
        LinkedList<JPanel> args = new LinkedList<JPanel>();
        argBuilders = new LinkedList<ArgBuilder>();
        int argIndex = 1;
        while (argIndex < cmd.length) {
            JPanel arg = new JPanel();
            arg.setBorder(BorderFactory.createTitledBorder(cmd[argIndex++]));
            String type = cmd[argIndex++];
            LinkedList<String> words = new LinkedList<String>();
            if (type.equals("enum"))
                while (!cmd[argIndex].equals(";"))
                    words.add(cmd[argIndex++]);
            arg.setLayout(new GridLayout(1, 1));
            ArgBuilder res = new ArgBuilder(type, words, new Runnable() {
                @Override
                public void run() {
                    mainFrame.setVisible(false);
                }
            }, new Runnable() {
                @Override
                public void run() {
                    mainFrame.setVisible(true);
                }
            });
            arg.add(res.piece);
            args.add(arg);
            argBuilders.add(res);
        }
        JPanel iFrame = new JPanel();
        iFrame.setLayout(new GridLayout(args.size() + 1, 1));
        for (JPanel x : args)
            iFrame.add(x);
        iFrame.add(Main.newButton("Confirm", new Runnable() {
            @Override
            public void run() {
                mainFrame.setVisible(false);
                onClose.run();
            }
        }));
        mainFrame.setContentPane(iFrame);

        mainFrame.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent windowEvent) {
            }

            @Override
            public void windowClosing(WindowEvent windowEvent) {
                onClose.run();
            }

            @Override
            public void windowClosed(WindowEvent windowEvent) {
            }

            @Override
            public void windowIconified(WindowEvent windowEvent) {
            }

            @Override
            public void windowDeiconified(WindowEvent windowEvent) {
            }

            @Override
            public void windowActivated(WindowEvent windowEvent) {
            }

            @Override
            public void windowDeactivated(WindowEvent windowEvent) {
            }
        });
        mainFrame.setSize(320, 240);
        onClose = onDie;
    }

    public void start() {
        mainFrame.setVisible(true);
    }
}
