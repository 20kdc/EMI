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
    public LinkedList<String> words, wordsDisplay;
    public int index = 0;

    public ArgBuilder(String type, LinkedList<String> w, LinkedList<String> wD, Runnable hidePar, Runnable showPar) {
        if (type.equals("enum") || type.equals("section-idx")) {
            words = w;
            wordsDisplay = wD;
            piece = jbt = Main.newButton(wordsDisplay.getFirst(), new Runnable() {
                @Override
                public void run() {
                    index++;
                    index %= words.size();
                    jbt.setText(wordsDisplay.get(index));
                }
            });
        } else {
            piece = jtf = new JTextField();
        }
    }

    public String getResult() {
        if (jbt != null)
            return words.get(index);
        return jtf.getText();
    }
}
