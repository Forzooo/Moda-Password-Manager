package moda.passwordmanager.frontend.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ModaButton extends JButton {

    private ButtonStyle buttonStyle;

    private int arc = 25;
    private float thickness = 4.0f;

    //Animazione diddio
    private float currentThickness;
    private float currentFontThickness;
    private Timer timerAnimation;

    public ModaButton(ButtonStyle buttonStyle) {

        this.buttonStyle = buttonStyle;

        this.currentThickness = this.thickness;

        this.setFont(new Font(UIManager.getString("Moda.GeneralUseFont"), Font.BOLD, 21));

        this.setContentAreaFilled(false);
        this.setBorderPainted(false);
        this.setFocusPainted(false);
        this.setOpaque(false);

        animation_init();
    }

    public ModaButton(ButtonStyle buttonStyle, int height, int fontSize) {

        this.buttonStyle = buttonStyle;

        this.currentThickness = this.thickness;

        this.setFont(new Font(UIManager.getString("Moda.GeneralUseFont"), Font.BOLD, fontSize));

        this.setContentAreaFilled(false);
        this.setBorderPainted(false);
        this.setFocusPainted(false);
        this.setOpaque(false);

        this.setPreferredSize(new Dimension(getWidth(), height));

        animation_init();
    }

    public ModaButton(ButtonStyle buttonStyle, Dimension dimension, int fontSize) {

        this.buttonStyle = buttonStyle;

        this.currentThickness = this.thickness;

        this.setFont(new Font(UIManager.getString("Moda.GeneralUseFont"), Font.BOLD, fontSize));

        this.setContentAreaFilled(false);
        this.setBorderPainted(false);
        this.setFocusPainted(false);
        this.setOpaque(false);

        this.setPreferredSize(dimension);

        animation_init();
    }

    public ModaButton(ButtonStyle buttonStyle, int width, int height, int fontSize) {

        this.buttonStyle = buttonStyle;

        this.currentThickness = this.thickness;

        this.setFont(new Font(UIManager.getString("Moda.GeneralUseFont"), Font.BOLD, fontSize));

        this.setContentAreaFilled(false);
        this.setBorderPainted(false);
        this.setFocusPainted(false);
        this.setOpaque(false);

        this.setMinimumSize(new Dimension(width, height));

        animation_init();
    }

    private void animation_init(){
        timerAnimation = new Timer(15, event -> {
            float target = getModel().isRollover() ? thickness * 2.5f : thickness;

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

        Color bgColor = Color.BLACK;
        Color borderColor = Color.WHITE;
        Color textColor = Color.WHITE;

        switch (buttonStyle) {
            case CLASSIC_BLACK:
                bgColor = Color.BLACK;
                borderColor = Color.WHITE;
                textColor = Color.WHITE;
                break;
            case CLASSIC_WHITE:
                bgColor = Color.WHITE;
                borderColor = Color.BLACK;
                textColor = Color.BLACK;
                break;
            case EMPTY:
                g2.dispose();
                super.paintComponent(g);
                break;
        }

        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), arc, arc);

        if (currentThickness > 0.1f) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(this.currentThickness));

            int offset = (int) (this.currentThickness / 2);
            g2.drawRoundRect(offset, offset, this.getWidth() - (int) this.currentThickness, this.getHeight() - (int) this.currentThickness, arc, arc);
        }

        setForeground(textColor);
        g2.dispose();

        super.paintComponent(g);
    }

    public void setArc(int arc) {
        this.arc = arc;
    }

    public void setThickness(float thickness) {
        this.thickness = thickness;
    }

    public enum  ButtonStyle {
        CLASSIC_BLACK,
        CLASSIC_WHITE,
        EMPTY,
    }
}
