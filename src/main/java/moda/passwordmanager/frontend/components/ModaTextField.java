package moda.passwordmanager.frontend.components;

import javax.swing.*;
import java.awt.*;

public class ModaTextField extends JTextField {

    TestFieldStyle style;
    public ModaTextField(TestFieldStyle style, int height) {
        this.style = style;

        this.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 20));

        this.setOpaque(false);
        this.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 5));

        this.setMinimumSize(new Dimension(getWidth(), height));
    }

    public ModaTextField(TestFieldStyle style, int width, int height) {
        this.style = style;

        this.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 20));

        this.setOpaque(false);
        this.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 5));

        this.setMinimumSize(new Dimension(width, height));
    }

    @Override
    protected void paintComponent(Graphics g) {

        Graphics2D g2 = (Graphics2D) g.create();
        switch (style) {
            case CLASSIC:
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int arc = 24;

                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), arc, arc);

                float thickness = 3.0f;

                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(thickness));

                int offset = (int) (thickness / 2);
                g2.drawRoundRect(offset, offset, this.getWidth() - (int)thickness, this.getHeight() - (int)thickness, arc, arc);

                g2.setColor(Color.BLACK);
                g2.setFont(getFont());

                g2.dispose();
                break;
        }


        super.paintComponent(g);
    }

    public enum TestFieldStyle {
        CLASSIC,
        EMPTY,
        SEARCH_BAR,
    }
}
