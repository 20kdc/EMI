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
    public EMISectionManagement management;
    public final String applicationName;
    public final IBackend.IBackendFile target;

    public PrimaryInterface(final IBackend.IBackendFile ibf_uol) {
        target = ibf_uol;

        applicationName = "EMI/" + (target.getClass().getName());

        JPanel jp = new JPanel();
        jp.setLayout(new BorderLayout());

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
            if (commandBlacklisted(data[0]))
                continue;
            bS.add(data[0]);
            bR.add(new Runnable() {
                @Override
                public void run() {
                    ToolInterface ti = new ToolInterface(applicationName, new BackendCommandTool(data, target), new Runnable() {
                        @Override
                        public void run() {
                            management.compileSectionList();
                            mainFrame.setVisible(true);
                        }
                    }, target);
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
        // Can't be minimized sanely, guess and allow resize in case we're wrong
        mainFrame.setSize(384, 640);
        mainFrame.setVisible(true);
    }

    private boolean commandBlacklisted(String dat) {
        if (dat.equals("list-section-keys"))
            return false;
        if (dat.equals("get-section-value"))
            return false;
        if (dat.equals("set-section-value"))
            return false;
        return false;
    }
}
