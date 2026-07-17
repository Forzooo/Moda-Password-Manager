package moda.passwordmanager.frontend.components;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

public class ModaScrollPane extends JScrollPane {

    public ModaScrollPane(Component view) {
        super(view);

        getVerticalScrollBar().setUI(new ModaScrollBarUI());
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

    private static class ModaScrollBarUI extends BasicScrollBarUI {

        @Override
        protected void configureScrollBarColors() {
            super.configureScrollBarColors();
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createButtons();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createButtons();
        }

        private JButton createButtons() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }


        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int arc = 20;
            int thickness = 3;
            int margin = 1;

            g2.setColor(UIManager.getColor("Moda.Scrollbar.colour"));
            g2.setStroke(new BasicStroke(thickness));

            int x = thumbBounds.x + margin;
            int y = thumbBounds.y + margin;
            int width = thumbBounds.width - thickness;
            int height = thumbBounds.height - thickness;

            g2.drawRoundRect(x, y, width, height, arc, arc);

            g2.dispose();

        }
    }

}
