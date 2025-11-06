package moda.passwordmanager.frontend.components;

import javax.swing.*;
import java.awt.*;

/**
 * The CloseTab class is used to set a Component of a JTabbedPane to be closable
 */
public class CloseTab extends JPanel {

    /** The Tabbed Pane where the tab is set */
    private final JTabbedPane TABBED_PANE;

    /** The Tab that is closed when the button is clicked */
    private Component TAB;
    private final String TITLE;  // The title of the tab

    private final static Dimension BUTTON_DIMENSION = new Dimension(17, 17);
    private JButton closeButton;

    public CloseTab(JTabbedPane tabbedPane, Component tab, String title){
        this.TABBED_PANE = tabbedPane;
        this.TAB = tab;
        this.TITLE = title;

        initPanel();
        initComponents();
        initListeners();
    }

    /**
     * Initialize the tab properties
     */
    private void initPanel(){
        setOpaque(false);  // We want to hide the JPanel to show only the components
    }

    /**
     * Initialize the components of the tab
     */
    private void initComponents(){
        JLabel tabTitle = new JLabel();
        tabTitle.setText(this.TITLE);
        tabTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

        this.closeButton = new JButton();
        this.closeButton.setText("X");
        this.closeButton.setPreferredSize(BUTTON_DIMENSION);

        add(tabTitle);
        add(this.closeButton);
    }

    /**
     * Initialize the listeners of the tab
     */
    private void initListeners(){
        this.closeButton.addActionListener(e -> TABBED_PANE.remove(TAB));
    }

    /**
     * Show the Closeable Tab inside the TabbedPane
     */
    public void showTab(){
        this.TABBED_PANE.addTab(null, this.TAB);  // First add the component to the TabbedPane
        this.TABBED_PANE.setTabComponentAt(this.TABBED_PANE.indexOfComponent(this.TAB), this);
    }

}
