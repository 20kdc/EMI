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
    final Runnable startBehavior;
    public final boolean mustHideMainwin;
    public Runnable onDie = new Runnable() {
        @Override
        public void run() {
        }
    };
    public ToolInterface(final String appPrefix, final ITool tool, final IBackend.IBackendFile ibf) {
        final String[] cmd = tool.getDefinition();
        if (cmd.length == 1) {
            // There are no parameters, so go with a special-case.
            mustHideMainwin = !tool.instantResponse();
            startBehavior = new Runnable() {
                @Override
                public void run() {
                    executeTool(appPrefix, onDie, tool, new String[] {cmd[0]}, ibf);
                }
            };
            return;
        }
        mustHideMainwin = true;

        // Essentially everything is in this one mega-routine.
        // Not great, but then again, it does delegate off to classes where it ought to.
        // How to improve...
        // EMI is meant to be a *cleaner* rewrite of PEONS, not a *worse* rewrite.
        // I guess if argument parsing and compiling was in one class, and the "IArg" classes had the UI building stuff?
        // That would solve two problems at once, and the result would be similar to PrimaryInterface in terms of complexity.

        final JFrame mainFrame = new JFrame(appPrefix + "/" + cmd[0]);
        startBehavior = new Runnable() {
            @Override
            public void run() {
                Main.visible(mainFrame);
            }
        };

        // Build the dialog. Kind of a monolith?
        LinkedList<JPanel> args = new LinkedList<JPanel>();
        final LinkedList<ArgBuilder> argBuilders = new LinkedList<ArgBuilder>();

        String[] defvalset = tool.getDefaultVals();

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
                    // Supposed to be used (section-idx is num, not hexnum)
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
            if (defvalset != null)
                defaultval = defvalset[args.size()];
            ArgBuilder res = new ArgBuilder(type, words, wordsDisplay, defaultval, new Runnable() {
                @Override
                public void run() {
                    mainFrame.setVisible(false);
                }
            }, new Runnable() {
                @Override
                public void run() {
                    Main.visible(mainFrame);
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
                String[] args = new String[argBuilders.size() + 1];
                args[0] = cmd[0];
                for (int i = 0; i < args.length - 1; i++)
                    args[i + 1] = argBuilders.get(i).getResult();
                executeTool(appPrefix, new Runnable() {
                    @Override
                    public void run() {
                        Main.visible(mainFrame);
                    }
                }, tool, args, ibf);
            }
        }));
        mainFrame.setContentPane(iFrame);

        mainFrame.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent windowEvent) {
            }

            @Override
            public void windowClosing(WindowEvent windowEvent) {
                onDie.run();
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
    }

    private void executeTool(String appPrefix, Runnable reshow, ITool tool, String[] args, IBackend.IBackendFile ibf) {
        try {
            ITool next = tool.execute(args);
            if (next == null) {
                onDie.run();
            } else if (next != tool) {
                if (!mustHideMainwin)
                    throw new RuntimeException("Invalid tool configuration. (mustHideMainwin false but tool recursed)");
                ToolInterface ti = new ToolInterface(appPrefix, next, ibf);
                ti.onDie = onDie;
                ti.start();
            } else {
                reshow.run();
            }
        } catch (Throwable e) {
            Main.report("failed " + args[0], e);
            reshow.run();
        }
    }

    public void start() {
        startBehavior.run();
    }
}
