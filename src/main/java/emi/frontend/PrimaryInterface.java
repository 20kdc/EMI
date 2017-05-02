/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package emi.frontend;

import emi.backend.IBackend;

import javax.swing.*;
import java.awt.*;

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
    public IBackend.IBackendFile target;

    public PrimaryInterface(IBackend.IBackendFile ibf) {
        JPanel jp = new JPanel();
        jp.setLayout(new BorderLayout());
        target = ibf;

        management = new EMISectionManagement(this);
        jp.add(management, BorderLayout.NORTH);

        mainFrame = new JFrame(ibf.toString());
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setContentPane(jp);
        mainFrame.setSize(240, 320);
        mainFrame.setVisible(true);
    }
}
