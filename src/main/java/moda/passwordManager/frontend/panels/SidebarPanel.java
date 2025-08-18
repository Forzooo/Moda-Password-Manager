package moda.passwordManager.frontend.panels;

import moda.passwordManager.frontend.GUIState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SidebarPanel extends JPanel {

    // Attributes for the configuration of the panel
    private final int MAX_SIDEBAR;  // Set the maximum size of the sidebar
    private Dimension windowSize;
    private final String CURRENT_VERSION;  // The current version of the software shown in a JLabel

    // Swing components
    private JButton addDataButton;
    private JButton showDataButton;
    private JButton settingsButton;

    // The dynamicState indicates which Panel needs to be switched to from the current one selected
    private GUIState dynamicState;

    public SidebarPanel(Dimension windowSize, String CURRENT_VERSION) {
        super();  // Initialize the Panel

        // Set the attributes given by the JFrame
        this.MAX_SIDEBAR = 300;
        this.windowSize = windowSize;
        this.CURRENT_VERSION = CURRENT_VERSION;

        // Set the initial state of the dynamic part to Show All Panel
        this.dynamicState = GUIState.SHOW_DATA;

        initPanel();
        initComponents();
        initListeners();
    }

    @Override public Dimension getPreferredSize() {
        Container parent = getParent(); // il Frame
        if (parent != null) {
            int larghezza = Math.min(parent.getWidth() / 3, MAX_SIDEBAR);
            return new Dimension(larghezza, parent.getHeight());
        }
        return new Dimension(MAX_SIDEBAR, 0);
    }


    /**
     * Set the configuration of the JPanel
     */
    private void initPanel(){
        setBackground(Color.BLACK);
    }

    /**
     * Initialize the components of the panel
     */
    private void initComponents(){
        // Create the JLabel that displays the name of the Password Manager
        JLabel passwordManagerLabel = new JLabel();
        passwordManagerLabel.setText("MODA");
        passwordManagerLabel.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 80));  // Set the font of the label
        passwordManagerLabel.setForeground(Color.WHITE);  // Set the color of the label

        // Add the current version of the software at the bottom of the sidebar
        JLabel currentVersionLabel = new JLabel();
        currentVersionLabel.setText("Version: " + CURRENT_VERSION);
        currentVersionLabel.setForeground(Color.WHITE);  // Set the color of the label

        JPanel buttonPanel = new JPanel(new GridLayout(3, 1));

        // Create the JButtons used to switch between JPanels of the dynamic part
        Dimension buttonDimension = new Dimension(350, 50);

        this.addDataButton = new JButton();
        this.addDataButton.setText("Add Data");
        this.addDataButton.setMaximumSize(buttonDimension);

        this.showDataButton = new JButton();
        this.showDataButton.setText("Show Data");
        this.showDataButton.setMaximumSize(buttonDimension);

        this.settingsButton = new JButton();  // TODO: Use the settings icon instead of the text
        this.settingsButton.setText("Settings");
        this.settingsButton.setMaximumSize(buttonDimension);
        this.settingsButton.setEnabled(false);  // The Settings panel has not been developed yet

        // Add the components to the Sidebar
        add(Box.createRigidArea(new Dimension(0, 20))); // Add RigidArea to add spacing between components
        add(passwordManagerLabel);

        add(currentVersionLabel);
        add(Box.createRigidArea(new Dimension(220, 20))); // Add RigidArea to add spacing between components

        // Add the section buttons to their JPanel
        buttonPanel.add(addDataButton);
        buttonPanel.add(showDataButton);
        buttonPanel.add(settingsButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Initialize all the listeners on the components
     */
    private void initListeners(){
        this.addDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dynamicState = GUIState.ADD_DATA;
            }
        });

        this.showDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dynamicState = GUIState.SHOW_DATA;
            }
        });

        this.settingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dynamicState = GUIState.SETTINGS;
            }
        });
    }

    public GUIState getDynamicState() {
        return dynamicState;
    }
}
