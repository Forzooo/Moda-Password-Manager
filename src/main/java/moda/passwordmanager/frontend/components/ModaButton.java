package moda.passwordmanager.frontend.components;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class ModaButton extends JButton {

    String type;

    public ModaButton( String type) {

        this.type = type;

        this.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 30));
        this.setForeground(Color.WHITE);

        this.setContentAreaFilled(false);
        this.setBorderPainted(false);
        this.setFocusPainted(false);
        this.setOpaque(false);

    }

    @Override
    protected void paintComponent(Graphics g) {

        switch (type) {
            case "Classic":
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int arc = 25;

                g2.setColor(Color.black);
                g2.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), arc, arc);

                float thickness = 4.0f;
                if (getModel().isRollover()) {
                    thickness = 8.0f;
                }

                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(thickness));

                int offset = (int) (thickness / 2);
                g2.drawRoundRect(offset, offset, this.getWidth() - (int)thickness, this.getHeight() - (int)thickness, arc, arc);

                g2.setColor(Color.WHITE);
                g2.setFont(getFont());

                g2.dispose();
                break;

            case "Empty":
                break;
        }

        super.paintComponent(g);
    }
}
