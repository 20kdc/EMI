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
import java.io.FileOutputStream;
import java.util.LinkedList;

/**
 * The interface for using a tool.
 * Created on 5/4/17.
 */
public class ToolInterface {
    final JFrame mainFrame;
    final Runnable onClose;
    public byte[] data;
    public LinkedList<ArgBuilder> argBuilders;

    public ToolInterface(String appPrefix, final ITool tool, Runnable onDie, final IBackend.IBackendFile ibf) {
        final String[] cmd = tool.getDefinition();
        mainFrame = new JFrame(appPrefix + "/" + cmd[0]);

        // Build the dialog. Kind of a monolith?
        LinkedList<JPanel> args = new LinkedList<JPanel>();
        argBuilders = new LinkedList<ArgBuilder>();
        int argIndex = 1;
        while (argIndex < cmd.length) {
            JPanel arg = new JPanel();
            arg.setBorder(BorderFactory.createTitledBorder(cmd[argIndex++]));
            String type = cmd[argIndex++];
            LinkedList<String> words = new LinkedList<String>();
            LinkedList<String> wordsDisplay = new LinkedList<String>();

            // Handle genuine enum/flags
            if (type.equals("enum") || type.equals("flags")) {
                while (!cmd[argIndex].equals(";")) {
                    String w = cmd[argIndex++];
                    words.add(w);
                    wordsDisplay.add(w);
                }
                argIndex++;
            }

            // Generate enum/flags when asked to
            if (type.equals("section-idx")) {
                String[] s = ibf.runOperation(new String[]{"list-sections"});
                int index = 0;
                for (String section : s) {
                    words.add(Integer.toString(index));
                    wordsDisplay.add(index + ": " + section.split(":")[3]);
                    index++;
                }
                type = "enum";
            }

            // type/words/wordsDisplay must be correct at this point for ArgBuilder

            arg.setLayout(new GridLayout(1, 1));
            String defaultval = "";
            if (type.equals("enum"))
                defaultval = words.getFirst();
            ArgBuilder res = new ArgBuilder(type, words, wordsDisplay, defaultval, new Runnable() {
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

        // Arguments dealt with, setup the confirm button & such

        JPanel iFrame = new JPanel();
        iFrame.setLayout(new GridLayout(args.size() + 1, 1));
        for (JPanel x : args)
            iFrame.add(x);
        iFrame.add(Main.newButton("Confirm", new Runnable() {
            @Override
            public void run() {
                mainFrame.setVisible(false);
                try {
                    String[] args = new String[argBuilders.size() + 1];
                    System.err.print(cmd[0]);
                    args[0] = cmd[0];
                    for (int i = 0; i < args.length - 1; i++) {
                        args[i + 1] = argBuilders.get(i).getResult();
                        System.err.print(" " + args[i + 1]);
                    }
                    System.err.println();
                    String str = "";
                    for (String f : tool.execute(args))
                        str += f + "\r\n";
                    onClose.run();
                    Main.showText("Output", str);
                } catch (Throwable e) {
                    Main.report("failed " + cmd[0], e);
                    mainFrame.setVisible(true);
                }
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
        Main.minimize(mainFrame);
        onClose = onDie;
    }

    public void start() {
        mainFrame.setVisible(true);
    }
}
