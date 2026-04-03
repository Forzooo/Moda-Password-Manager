package moda.passwordmanager.frontend.components;

import javax.swing.*;
import java.awt.*;

public class ModaTextField extends JTextField {

    public ModaTextField() {
        this.setFont(new Font(UIManager.getString("Moda.GeneralUseFont"), Font.BOLD, 20));

        this.setOpaque(false);
        this.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 5));
    }


    public ModaTextField(int height) {
        this.setFont(new Font(UIManager.getString("Moda.GeneralUseFont"), Font.BOLD, 20));

        this.setOpaque(false);
        this.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 5));

        this.setMinimumSize(new Dimension(getWidth(), height));
    }

    public ModaTextField(int width, int height) {
        this.setFont(new Font(UIManager.getString("Moda.GeneralUseFont"), Font.BOLD, 20));

        this.setOpaque(false);
        this.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 5));

        this.setMinimumSize(new Dimension(width, height));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int arc = 30;

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

        super.paintComponent(g);
    }
}
