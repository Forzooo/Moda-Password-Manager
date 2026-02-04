package moda.passwordmanager.frontend.components;

import javax.swing.*;
import java.awt.*;

public class ModaScrollPane extends JScrollPane {

    public ModaScrollPane(Component view) {
        super(view);
    }

    @Override
    public void paint(Graphics g) {

        super.paint(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int arc = 24;

        g2.setColor(new Color(0,0,0,0));
        g2.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), arc, arc);

        float thickness = 3.0f;

        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(thickness));

        int offset = (int) (thickness / 2);
        g2.drawRoundRect(offset, offset, this.getWidth() - (int)thickness, this.getHeight() - (int)thickness, arc, arc);

        g2.dispose();

    }
}
