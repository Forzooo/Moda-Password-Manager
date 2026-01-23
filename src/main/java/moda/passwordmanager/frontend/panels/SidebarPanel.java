package moda.passwordmanager.frontend.panels;

import moda.passwordmanager.frontend.GUIState;
import moda.passwordmanager.frontend.components.ModaButton;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

public class SidebarPanel extends JPanel {

    // Attributes for the configuration of the panel
    private final int MAX_SIDEBAR;  // Set the maximum size of the sidebar
    private Dimension windowSize;
    private final String CURRENT_VERSION;  // The current version of the software shown in a JLabel

    // Swing components
    private ModaButton addDataButton;
    private ModaButton showDataButton;
    private ModaButton settingsButton;

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

        this.setLayout(new MigLayout("debug, align center")); //rimuovi il debug

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
        passwordManagerLabel.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 85));  // Set the font of the label
        passwordManagerLabel.setForeground(Color.WHITE);  // Set the color of the label

        // Add the current version of the software at the bottom of the sidebar
        JLabel currentVersionLabel = new JLabel();
        currentVersionLabel.setText("Version: " + CURRENT_VERSION);
        currentVersionLabel.setForeground(Color.WHITE);  // Set the color of the label

        Dimension buttonDimension = new Dimension(70, 70);

        this.addDataButton = new ModaButton("Classic");
        this.addDataButton.setText("Add Data");
        //this.addDataButton.setMaximumSize(buttonDimension);

        this.showDataButton = new ModaButton("Classic");
        this.showDataButton.setText("Show Data");
        //this.showDataButton.setMaximumSize(buttonDimension);

        this.settingsButton = new ModaButton("Empty");  // TODO: Use the settings icon instead of the text
        this.settingsButton.setMaximumSize(buttonDimension);

        ImageIcon settings_icon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/settings_icon/settings_icon.png")));
        settings_icon.setImage(settings_icon.getImage().getScaledInstance(buttonDimension.width, buttonDimension.height, 0));
        this.settingsButton.setIcon(settings_icon);

        // Add the components to the Sidebar
        add(passwordManagerLabel, "wrap 0, al center");
        add(currentVersionLabel, "wrap 150, al center");

        add(addDataButton, "wrap 50, center, grow, pushy 50");
        add(showDataButton, "wrap 200, center, grow, pushy 50");
        add(settingsButton, "bottom");
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
