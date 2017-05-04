/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package emi.frontend;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;

/**
 * Responsible for building sub-UI stuff for modification of arguments.
 * Created on 5/4/17.
 */
public class ArgBuilder {
    public Component piece;
    public JTextField jtf;
    public JButton jbt;

    public ArgBuilder(String type, LinkedList<String> words, Runnable hidePar, Runnable showPar) {
        if (type.equals("enum") || type.equals("section-idx")) {
            piece = jbt = new JButton(words.getFirst());
        } else {
            piece = jtf = new JTextField();
        }
    }

    public String getResult() {
        if (jbt != null)
            return jbt.getText();
        return jtf.getText();
    }
}
