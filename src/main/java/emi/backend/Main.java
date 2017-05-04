/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package emi.backend;

import java.io.*;
import java.util.LinkedList;

public class Main {
    public static void main(String[] args) throws Exception {
        IBackend r = BackendRegistry.get(args[0]);
        IBackend.IBackendFile ibf;
        if (args.length == 2) {
            FileInputStream fis = new FileInputStream(args[1]);
            byte[] data = new byte[fis.available()];
            if (fis.read(data) != data.length) {
                fis.close();
                throw new IOException("Couldn't read everything available in the file.");
            }
            fis.close();

            ibf = r.openFile(data);
        } else if (args.length == 1) {
            ibf = r.createFile();
        } else {
            throw new RuntimeException("EMI command-line args: <backend> [<file>]");
        }
        if (ibf.fileContainsRelocationData())
            System.out.println("File contains relocation data.");
        System.out.println("File validated - ready!");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String s = br.readLine();
            try {
                String[] tkn = tokenize(s);
                if (tkn[0].startsWith("dl-")) {
                    tkn[0] = tkn[0].substring(3);

                    FileInputStream fis = new FileInputStream(args[1]);
                    byte[] data = new byte[fis.available()];
                    if (fis.read(data) != data.length) {
                        fis.close();
                        throw new IOException("Couldn't read everything available in the file.");
                    }
                    fis.close();

                    for (String o : ibf.runDLOperation(tkn, data))
                        System.out.println(o);
                } else if (tkn[0].startsWith("ds-")) {
                    tkn[0] = tkn[0].substring(3);
                    byte[] d = ibf.runDSOperation(tkn);
                    System.out.println("OK - filename?");
                    String fn = br.readLine();
                    FileOutputStream fos = new FileOutputStream(fn);
                    fos.write(d);
                    fos.close();
                } else {
                    for (String o : ibf.runOperation(tkn))
                        System.out.println(o);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static String[] tokenize(String s) {
        String word = "";
        LinkedList<String> words = new LinkedList<String>();
        boolean quoting = false;
        for (char c : s.toCharArray()) {
            if (quoting) {
                if (c != '"') {
                    word += c;
                } else {
                    quoting = false;
                    words.add(word);
                    word = "";
                }
            } else {
                if (c == ' ') {
                    if (word.length() != 0) {
                        words.add(word);
                        word = "";
                    }
                } else {
                    if (c == '"') {
                        if (word.length() != 0) {
                            words.add(word);
                            word = "";
                        }
                        quoting = true;
                    } else {
                        word += c;
                    }
                }
            }
        }
        if (quoting)
            throw new RuntimeException("Syntax error.");
        if (word.length() != 0)
            words.add(word);
        return words.toArray(new String[0]);
    }
}
