/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package emi.frontend;

import emi.backend.BackendRegistry;
import emi.backend.IBackend;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;

/**
 * The basic control panel.
 * Should provide all the basic tools.
 * Design:
 * TOP BAR
 * -------
 * KEY|VAL
 * KEY|VAL
 * -------
 * LDD|SVD
 * SAV|TLS
 * Created on 5/2/17.
 */
public class PrimaryInterface {
    public JFrame mainFrame;
    public String applicationName;
    public EMISectionManagement management;
    public IBackend.IBackendFile target;

    public PrimaryInterface(IBackend.IBackendFile ibf) {
        applicationName = "EMI/" + ibf.getClass().getSimpleName();

        JPanel jp = new JPanel();
        jp.setLayout(new BorderLayout());
        target = ibf;

        management = new EMISectionManagement(this);
        jp.add(management, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        // write in tools
        final LinkedList<String> bS = new LinkedList<String>();
        final LinkedList<Runnable> bR = new LinkedList<Runnable>();

        int x = (bS.size() + 1) / 2;
        buttonPanel.setLayout(new GridLayout(x, 2));

        for (String s : target.runOperation(new String[]{"help"})) {
            final String[] data = s.split(" ");
            bS.add(data[0]);
            bR.add(new Runnable() {
                @Override
                public void run() {
                    ToolInterface ti = new ToolInterface(data, new Runnable() {
                        @Override
                        public void run() {
                            management.compileSectionList();
                            mainFrame.setVisible(true);
                        }
                    }, target);
                    if (data[0].startsWith("dl-")) {
                        JFileChooser jfc = new JFileChooser();
                        if (jfc.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
                            try {
                                FileInputStream fis = new FileInputStream(jfc.getSelectedFile());
                                byte[] data2 = new byte[fis.available()];
                                if (fis.read(data2) != data2.length) {
                                    fis.close();
                                    throw new IOException("couldn't read full file");
                                }
                                ti.data = data2;
                                fis.close();
                            } catch (Throwable e) {
                                Main.report("While reading data blob", e);
                                return;
                            }
                        } else {
                            mainFrame.setVisible(true);
                            return;
                        }
                    }
                    mainFrame.setVisible(false);
                    ti.start();
                }
            });
        }

        for (int i = 0; i < bR.size(); i++)
            buttonPanel.add(Main.newButton(bS.get(i), bR.get(i)));

        jp.add(buttonPanel, BorderLayout.SOUTH);

        mainFrame = new JFrame(applicationName);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setContentPane(jp);
        mainFrame.setSize(240, 320);
        mainFrame.setVisible(true);
    }
}
