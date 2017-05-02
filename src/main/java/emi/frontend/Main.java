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

/**
 * GUI frontend.
 * Created on 5/2/17.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        final JFrame jf = new JFrame();
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
                            new PrimaryInterface(ibf);
                            jf.setVisible(false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }));
        }
        jf.setContentPane(jp);

        jf.setSize(320, 200);
        jf.setVisible(true);
    }

    private static JButton newButton(String s, final Runnable runnable) {
        JButton jb = new JButton(s);
        jb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                runnable.run();
            }
        });
        return jb;
    }
}
