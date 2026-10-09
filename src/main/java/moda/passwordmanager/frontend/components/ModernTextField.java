package moda.passwordmanager.frontend.components;

import javax.swing.*;
import java.awt.*;

public class ModernTextField extends JTextField {

    private final Color backgroundColor;
    private final Color borderColor;

    public ModernTextField() {
        this.backgroundColor = UIManager.getColor("Moda.TextField.background");
        this.borderColor = UIManager.getColor("Moda.TextField.border");

        setFont(new Font(UIManager.getString("Moda.GeneralUseFontFamily"), Font.BOLD, 20));

        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 5));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int arc = 30;

        // Draw the background
        g2.setColor(this.backgroundColor);
        g2.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), arc, arc);

        // Draw the border
        float thickness = 3.0f;

        g2.setColor(this.borderColor);
        g2.setStroke(new BasicStroke(thickness));

        int offset = (int) (thickness / 2);
        g2.drawRoundRect(offset, offset, this.getWidth() - (int)thickness,
                this.getHeight() - (int)thickness, arc, arc);

        g2.dispose();  // Release the resources as we no longer need them

        super.paintComponent(g);
    }
}
