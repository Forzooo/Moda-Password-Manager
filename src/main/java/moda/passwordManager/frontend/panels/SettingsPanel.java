package moda.passwordManager.frontend.panels;

import moda.passwordManager.backend.Data;
import moda.passwordManager.communicationHandler.CommunicationHandler;
import moda.passwordManager.communicationHandler.Event;
import moda.passwordManager.frontend.components.Placeholder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class SettingsPanel extends JPanel {

    // Attribute to communicate with the backend
    private CommunicationHandler communicationHandler;

    // Attributes for the configuration of the panel
    private final int MIN_CONTENT_WIDTH;
    private Dimension windowSize;

    // Swing components
    private JTextField usernameTextField;
    private JTextField emailAddressTextField;
    private JTextField passwordTextField;
    private JTextField serviceTextField;
    private JTextField additionalDataTextField;

    private Placeholder usernamePlaceholder;
    private Placeholder emailAddressPlaceholder;
    private Placeholder passwordPlaceholder;
    private Placeholder servicePlaceholder;
    private Placeholder additionalDataPlaceholder;

    private JButton resetButton;
    private JButton saveButton;

    public SettingsPanel(CommunicationHandler communicationHandler, int MIN_CONTENT_WIDTH, Dimension windowSize, int sidebarPanelWidth) {
        super();  // Initialize the Panel

        // Set the attributes given by the JFrame
        this.communicationHandler = communicationHandler;
        this.MIN_CONTENT_WIDTH = MIN_CONTENT_WIDTH;
        this.windowSize = windowSize;

        initPanel(sidebarPanelWidth);
        initComponents();
        initActionListener();
    }

    /**
     * Get the layout used for the panel
     * @return BorderLayout
     */
    private BorderLayout getPanelLayout() {
        return new BorderLayout();
    }

    @Override
    public Dimension getMinimumSize() {
        // altezza 0 -> “qualsiasi”, conta solo la larghezza minima
        return new Dimension(MIN_CONTENT_WIDTH, 0);
    }

    /**
     * Set the configuration of the JPanel
     * @param sidebarPanelWidth
     */
    private void initPanel(int sidebarPanelWidth){
        setLayout(getPanelLayout());  // Set its layout

        // Set the preferred size
        setPreferredSize(new Dimension((int) (this.windowSize.getWidth() - sidebarPanelWidth), (int) this.windowSize.getHeight()));

        setBackground(Color.WHITE);

        // Set the margin of the Add Data Panel [top: 60, bottom: 60, left: 30, right: 30]
        setBorder(BorderFactory.createEmptyBorder(60,30,60,30));
    }

    /**
     * Initialize the components of the panel
     */
    private void initComponents(){

    }

    /**
     * Initialize all the action listeners
     */
    private void initActionListener(){

    }
}
