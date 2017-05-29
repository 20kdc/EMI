/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package emi.frontend;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.function.BooleanSupplier;

/**
 * Responsible for building sub-UI stuff for modification of arguments.
 * Type can be "hexnum", "num", "str", "enum", "flags".
 * Created on 5/4/17.
 */
public class ArgBuilder {
    public Component piece;
    public JTextField jtf;
    public JButton jbt;
    public String jbtValue;

    public ArgBuilder(final String type, final LinkedList<String> w, final LinkedList<String> wD, final String oldval, final BooleanSupplier hidePar, final Runnable showPar) {
        if (type.equals("enum") || type.equals("flags")) {
            String text = oldval;
            if (!type.equals("flags"))
                text = wD.get(findWord(w, oldval));
            jbtValue = oldval;
            piece = jbt = Main.newButton(text, new Runnable() {
                @Override
                public void run() {
                    if (hidePar.getAsBoolean())
                        return;
                    final JList jp = new JList();
                    // damned if you do, damned if you don't
                    DefaultListModel dlm = new DefaultListModel();
                    jp.setModel(dlm);
                    for (String s : wD)
                        dlm.addElement(s);
                    if (type.equals("flags")) {
                        showFlags(jp);
                    } else {
                        showEnum(jp);
                    }
                    Main.dialogSingleton.prepare("Select value...", new JScrollPane(jp), new Runnable() {
                        @Override
                        public void run() {
                            if (type.equals("flags")) {
                                finishFlags(jp);
                            } else {
                                finishEnum(jp);
                            }
                            showPar.run();
                        }
                    });
                }

                // This fills the contents of the frame.

                private void showFlags(JList jf) {
                    HashSet<Integer> it = new HashSet<Integer>();
                    for (String s : oldval.split(" "))
                        it.add(findWord(w, s));
                    int[] indices = new int[it.size()];
                    int idx = 0;
                    for (Integer i : it)
                        indices[idx++] = i;
                    jf.setSelectedIndices(indices);
                }

                private void showEnum(JList jf) {
                    jf.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
                    jf.setSelectedIndex(findWord(w, oldval));
                }

                private void finishFlags(JList jf) {
                    String r = "";
                    for (int i : jf.getSelectedIndices()) {
                        if (r.length() != 0)
                            r += " ";
                        r += w.get(i);
                    }
                    jbt.setText(r);
                    jbtValue = r;
                }

                private void finishEnum(JList jf) {
                    jbt.setText(wD.get(jf.getSelectedIndex()));
                    jbtValue = w.get(jf.getSelectedIndex());
                }
            });
        } else {
            piece = jtf = new JTextField(oldval);
        }
    }

    private int findWord(LinkedList<String> w, String oldval) {
        for (int i = 0; i < w.size(); i++)
            if (w.get(i).equals(oldval))
                return i;
        return -1;
    }

    public String getResult() {
        if (jbt != null)
            return jbtValue;
        return jtf.getText();
    }
}
