package moda.passwordmanager.frontend.panels;

import moda.passwordmanager.frontend.Frontend;
import moda.passwordmanager.frontend.GUIState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SidebarPanel extends JPanel {

    // Attributes for the configuration of the panel
    private final static int MAX_SIDEBAR = 300;  // Set the maximum size of the sidebar
    private final String CURRENT_VERSION;  // The current version of the software shown in a JLabel
    private Dimension windowSize;  // The window size is used when the Frontend is not fully initialized yet

    // Sections of the Sidebar
    private JPanel titleSection;
    private JPanel componentsSection;
    private JPanel detailsSection;

    // Swing components
    private JButton addDataButton;
    private JButton showDataButton;
    private JButton settingsButton;

    // The selectedPanel indicates the JPanel that is selected
    private GUIState selectedPanel;

    public SidebarPanel(String CURRENT_VERSION, Dimension windowSize){
        super();  // Initialize the Panel

        // Set the attributes given by the JFrame
        this.CURRENT_VERSION = CURRENT_VERSION;
        this.windowSize = windowSize;

        // Set the initial state of the dynamic part to Show All Panel
        this.selectedPanel = GUIState.SHOW_DATA;

        initPanel();
        initSections();
        initComponents();
        initListeners();
    }

    /**
     * Resizes at runtime the panel to adapt it to the new window size
     */
    @Override
    public Dimension getPreferredSize() {
        Component frontend = getParent(); // Get the Frontend JPanel

        if (frontend != null) {
            // Calculate dynamically the width of the sidebar based on the current width of the frontend and the maximum
            // size of the sidebar
            int width = Math.min(frontend.getWidth() / 3, MAX_SIDEBAR);
            return new Dimension(width, frontend.getHeight());
        }
        // It uses the window size in case the Frontend is not entirely initialized yet
        int width = (int) Math.min(this.windowSize.getWidth() / 3, MAX_SIDEBAR);
        return new Dimension(width, (int) this.windowSize.getHeight());
    }

    /**
     * Set the configuration of the JPanel
     */
    private void initPanel(){
        // Use the BoxLayout to display the sections in vertical alignment
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // TODO: Set a proper background color using FlatLaf themes
    }

    /**
     * Initialize the section panels that are used to divide the sidebar into parts
     */
    private void initSections(){
        Dimension sidebar = getPreferredSize();  // Get the current dimension of the sidebar

        this.titleSection = new JPanel();
        this.componentsSection = new JPanel();

        // We set only the size of the components section because the other two have the same size
        setSectionSizes(this.componentsSection, new Dimension((int) sidebar.getWidth(), (int) (sidebar.getHeight()/2)));

        this.detailsSection = new JPanel();

        add(this.titleSection);
        add(this.componentsSection);
        add(this.detailsSection);
    }

    /**
     * Set the all the sizes, requested by the BoxLayout, of a JPanel
     */
    private void setSectionSizes(JPanel section, Dimension size){
        section.setMinimumSize(size);
        section.setPreferredSize(size);
        section.setMaximumSize(size);
    }

    /**
     * Initialize the components of the panel
     */
    private void initComponents(){

        // Title Section
        JLabel passwordManagerLabel = new JLabel();  // Create the JLabel that displays the name of the Password Manager
        passwordManagerLabel.setText("MODA");
        passwordManagerLabel.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 80));

        // Components Section
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        // Create the JButtons used to switch between JPanels of the dynamic part, where their width is the same as
        // the sidebar
        Dimension buttonDimension = new Dimension((int) getPreferredSize().getWidth(), 60);

        this.addDataButton = new JButton();
        this.addDataButton.setBorderPainted(false);  // The buttons should not have the border
        this.addDataButton.setText("Add Data");
        this.addDataButton.setPreferredSize(buttonDimension);
        this.addDataButton.setMaximumSize(buttonDimension);

        this.showDataButton = new JButton();
        this.showDataButton.setBorderPainted(false);
        this.showDataButton.setText("Show Data");
        this.showDataButton.setPreferredSize(buttonDimension);
        this.showDataButton.setMaximumSize(buttonDimension);

        this.settingsButton = new JButton();  // TODO: Use the settings icon instead of the text
        this.settingsButton.setBorderPainted(false);
        this.settingsButton.setText("Settings");
        this.settingsButton.setPreferredSize(buttonDimension);
        this.settingsButton.setMaximumSize(buttonDimension);

        // Add the section buttons to their JPanel
        buttonPanel.add(addDataButton);
        addSpacing(buttonPanel, new Dimension(0, (int) (buttonDimension.getHeight()/6))); // Add spacing
        buttonPanel.add(showDataButton);
        addSpacing(buttonPanel, new Dimension(0, (int) (buttonDimension.getHeight()/6)));
        buttonPanel.add(settingsButton);

        // Details section
        JLabel currentVersionLabel = new JLabel();  // The current version of the software
        currentVersionLabel.setText("Version: " + CURRENT_VERSION);

        // Add the components to the Sidebar
        this.titleSection.add(passwordManagerLabel);
        this.componentsSection.add(buttonPanel, BorderLayout.SOUTH);
        this.detailsSection.add(currentVersionLabel);
    }

    /**
     * Add some spacing to a panel
     */
    private void addSpacing(JPanel panel, Dimension dimension){
        panel.add(Box.createRigidArea(dimension));
    }

    /**
     * Initialize all the listeners on the components
     */
    private void initListeners(){
        this.addDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedPanel = GUIState.ADD_DATA;
                switchPanel();
            }
        });

        setHoverEffect(this.addDataButton);

        this.showDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedPanel = GUIState.SHOW_DATA;
                switchPanel();
            }
        });

        setHoverEffect(this.showDataButton);

        this.settingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedPanel = GUIState.SETTINGS;
                switchPanel();
            }
        });

        setHoverEffect(this.settingsButton);
    }

    /**
     * Switch the JPanel shown to the new one
     */
    private void switchPanel(){
        // To do this, we first have to get the Frontend object by the getParent method, then call the real
        // switchPanel that is inside the Frontend
        Frontend frontend = (Frontend) getParent();
        frontend.switchPanel(this.selectedPanel);
    }

    /**
     * Set a hover effect on a JButton
     */
    private void setHoverEffect(JButton button){
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                button.setBackground(Color.gray);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                button.setBackground(null);
            }
        });
    }
}
