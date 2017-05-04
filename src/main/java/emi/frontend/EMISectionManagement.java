/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package emi.frontend;

import emi.backend.IBackend;

import java.awt.*;

/**
 * Interface for showing section order and size, to give a feel for the actual file data.
 * Management will be done by up/down movement buttons in a list on PrimaryInterface.
 * Created on 5/2/17.
 */
public class EMISectionManagement extends Canvas {
    public PrimaryInterface theInterface;
    private EMISMS[] sections;
    private long totalLen;

    public EMISectionManagement(PrimaryInterface intf) {
        theInterface = intf;
        compileSectionList();
        setMinimumSize(new Dimension(32, 16));
    }

    public void compileSectionList() {
        String[] r = theInterface.target.runOperation(new String[]{
                "list-sections"
        });
        sections = new EMISMS[r.length];
        totalLen = 0;
        for (int i = 0; i < sections.length; i++) {
            sections[i] = new EMISMS();
            String[] tokens = r[i].split(":");
            sections[i].text = r[i];
            sections[i].length = Long.decode(tokens[1].split("/")[1]);
            sections[i].view = getColForType(tokens[2]);
            totalLen += sections[i].length;
        }
        // Expand sections
        long minFrac = totalLen / 8;
        long ntl = 0;
        for (int i = 0; i < sections.length; i++) {
            if (sections[i].length < minFrac)
                sections[i].length = minFrac;
            ntl += sections[i].length;
        }
        totalLen = ntl;
    }

    private Color getColForType(String token) {
        // This isn't essential information, and isn't really accessibility-friendly anyway.
        // In general this class is just eye candy, which is for the best
        if (token.equals("header"))
            return Color.lightGray;
        if (token.equals("waste"))
            return Color.darkGray;
        if (token.equals("x"))
            return Color.blue;
        if (token.equals("w"))
            return Color.green;
        if (token.equals("wx"))
            return Color.cyan;
        if (token.equals("r"))
            return Color.red;
        if (token.equals("rx"))
            return Color.magenta;
        if (token.equals("rw"))
            return Color.yellow;
        if (token.equals("rwx"))
            return Color.white;
        return Color.gray;
    }

    @Override
    public void paint(Graphics graphics) {
        int y = 0;
        for (int i = 0; i < sections.length; i++) {
            EMISMS sms = sections[i];
            double h = sms.length / (double) totalLen;
            h *= getHeight();
            h = Math.floor(h);
            if (i == sections.length - 1)
                h = getHeight() - y;
            graphics.setColor(sms.view);
            graphics.fillRect(0, y, getWidth(), (int) h);
            graphics.setColor(sms.view.darker().darker());
            graphics.fillRect(1, y + 1, getWidth() - 2, (int) h - 2);

            graphics.setColor(Color.white);
            graphics.drawString(sms.text, 4, (int) (y + h) - 4);

            y += (int) h;
        }
    }

    private class EMISMS {
        public String text;
        public long length;
        public Color view;
    }
}
