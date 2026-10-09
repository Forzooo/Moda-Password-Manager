package moda.passwordmanager.frontend.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ModernButton extends JButton {

    public enum Style {
        BLACK,
        WHITE,
    }

    private float currentThickness;
    private static final float THICKNESS = 4.0f;

    private final Color backgroundColor;
    private final Color borderColor;
    private final Color textColor;

    private Timer timerAnimation;

    public ModernButton(Style style) {
        this.currentThickness = THICKNESS;

        this.setContentAreaFilled(false);
        this.setBorderPainted(false);
        this.setFocusPainted(false);
        this.setOpaque(false);

        switch (style) {
            case BLACK:
                this.backgroundColor = Color.BLACK;
                this.borderColor = Color.WHITE;
                this.textColor = Color.WHITE;
                break;

            case WHITE:
                this.backgroundColor = Color.WHITE;
                this.borderColor = Color.BLACK;
                this.textColor = Color.BLACK;
                break;

            default:
                this.backgroundColor = null;
                this.borderColor = null;
                this.textColor = null;
        }

        initAnimation();
    }

    private void initAnimation(){
        this.timerAnimation = new Timer(15, event -> {
            float target = getModel().isRollover() ? THICKNESS * 2.5f : THICKNESS;

            float diff = target - currentThickness;
            if (Math.abs(diff) > 0.05f) {
                currentThickness +=  diff * 0.25f;
                repaint();
            } else {
                currentThickness = target;
                timerAnimation.stop();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                timerAnimation.start();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                timerAnimation.start();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int arc = 25;
        g2.setColor(this.backgroundColor);
        g2.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), arc, arc);

        if (this.currentThickness > 0.1f) {
            g2.setColor(this.borderColor);
            g2.setStroke(new BasicStroke(this.currentThickness));

            int offset = (int) (this.currentThickness / 2);
            g2.drawRoundRect(offset, offset, this.getWidth() - (int) this.currentThickness, this.getHeight() - (int) this.currentThickness, arc, arc);
        }

        setForeground(this.textColor);
        g2.dispose();

        super.paintComponent(g);
    }

}
