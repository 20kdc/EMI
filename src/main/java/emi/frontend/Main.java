/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package emi.frontend;

import emi.backend.BackendRegistry;
import emi.backend.IBackend;

import javax.swing.*;
import javax.swing.text.DefaultStyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * GUI frontend.
 * Created on 5/2/17.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        final JFrame jf = new JFrame("EMI");
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel jp = new JPanel();
        jp.setBorder(BorderFactory.createTitledBorder("Load..."));
        String[] backends = BackendRegistry.backends;
        jp.setLayout(new GridLayout(backends.length, 2));
        for (final String s : backends) {
            jp.add(newButton(s, new Runnable() {
                @Override
                public void run() {
                    JFileChooser jfc = new JFileChooser();
                    IBackend ib = BackendRegistry.get(s);
                    if (jfc.showOpenDialog(jf) == JFileChooser.APPROVE_OPTION) {
                        try {
                            FileInputStream fis = new FileInputStream(jfc.getSelectedFile());
                            byte[] data = new byte[fis.available()];
                            if (fis.read(data) != data.length) {
                                fis.close();
                                throw new IOException("Couldn't read everything available in the file.");
                            }
                            fis.close();

                            IBackend.IBackendFile ibf = ib.openFile(data);
                            new PrimaryInterface(ibf, s);
                            jf.setVisible(false);
                        } catch (Throwable e) {
                            Main.report("While loading file", e);
                        }
                    }
                }
            }));
        }
        jf.setContentPane(jp);

        minimize(jf);
        jf.setVisible(true);
    }

    public static JButton newButton(String s, final Runnable runnable) {
        JButton jb = new JButton(s);
        jb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                runnable.run();
            }
        });
        return jb;
    }

    public static void report(String reason, Throwable e) {
        // Show error.
        System.err.println("starting exception handler");
        e.printStackTrace();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println(reason);
        e.printStackTrace(pw);
        showText("system error", sw.toString().replace("\r\n", "\n").replace("\n", "\r\n"));
    }

    public static void showText(String s, String s1) {
        if (!s1.contains("\r\n")) {
            JOptionPane.showMessageDialog(null, s1, s, JOptionPane.PLAIN_MESSAGE);
            return;
        }
        JFrame report = new JFrame(s);
        report.setSize(320, 240);
        report.setContentPane(new JScrollPane(new JTextArea(s1)));
        report.setVisible(true);
    }

    public static void minimize(final JFrame target) {
        target.setSize(320, 240);
        target.pack();

        // This sequence works most of the time, but other times it doesn't.
        // Set an AWT timer so that if for some reason the flipping framework decides "actually how about we don't layout your component today", it's told to shut up.
        target.setSize(target.getWidth() * 2, target.getHeight());
        target.setResizable(false);
        target.doLayout();
        target.invalidate();
        target.layout();
        final Timer t = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                // Spam relayout commands. This ALWAYS resolves correctly but can lead to too-large results.
                // Filed under: "a price one is willing to pay to stop the system getting even worse results", and "fuck swing layouts".
                target.invalidate();
                target.doLayout();
                target.layout();
                int oldW = target.getWidth(), oldH = target.getHeight();
                target.setSize(1, 1);
                target.invalidate();
                target.doLayout();
                target.layout();
                target.setSize(oldW, oldH);
                target.invalidate();
                target.doLayout();
                target.layout();
            }
        });
        t.setRepeats(false);
        t.start();
    }
}
