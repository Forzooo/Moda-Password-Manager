package moda.passwordmanager.frontend.panels;

import moda.passwordmanager.frontend.Frontend;
import moda.passwordmanager.frontend.dialogs.Settings;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Sidebar extends JPanel {

    private final InterThreadCommunication ITC;

    // Swing components
    private JButton addDataButton;
    private JButton showDataButton;
    private JButton settingsButton;

    public Sidebar(InterThreadCommunication itc){
        super();  // Initialize the Panel

        // Set the attributes given by the JFrame
        this.ITC = itc;

        initPanel();
        initComponents();
        initListeners();
    }

    /**
     * Set the configuration of the JPanel
     */
    private void initPanel(){
        setLayout(new MigLayout("debug, wrap 1, insets 20, fillx",
            "[align center]",
            "[][][]push[]"));
    }

    /**
     * Initialize the components of the panel
     */
    private void initComponents(){
        // Title Section
        JLabel passwordManagerLabel = new JLabel();  // Create the JLabel that displays the name of the Password Manager
        passwordManagerLabel.setText("MODA");
        passwordManagerLabel.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 80));

        // Create the JButtons used to switch between JPanels of the dynamic part, where their width is the same as
        // the sidebar
        Dimension buttonDimension = new Dimension((int) getPreferredSize().getWidth(), 60);

        this.addDataButton = new JButton();
        this.addDataButton.setBorderPainted(false);  // The buttons should not have the border
        this.addDataButton.setText("Add Data");
        this.addDataButton.setPreferredSize(buttonDimension);

        this.showDataButton = new JButton();
        this.showDataButton.setBorderPainted(false);
        this.showDataButton.setText("Show Data");
        this.showDataButton.setPreferredSize(buttonDimension);

        // Settings + Details section
        this.settingsButton = new JButton();  // TODO: Use the settings icon
        this.settingsButton.setBorderPainted(false);
        this.settingsButton.setText("Settings");
        this.settingsButton.setPreferredSize(buttonDimension);

        add(passwordManagerLabel);
        add(this.addDataButton, "grow");
        add(this.showDataButton, "grow");
        add(this.settingsButton, "grow");
    }

    /**
     * Initialize all the listeners on the components
     */
    private void initListeners(){
        this.addDataButton.addActionListener(e -> switchPanel(AddData.getPanelTitle()));
        setHoverEffect(this.addDataButton);

        this.showDataButton.addActionListener(e -> switchPanel(ShowData.getPanelTitle()));
        setHoverEffect(this.showDataButton);

        this.settingsButton.addActionListener(e -> openSettings());
        setHoverEffect(this.settingsButton);
    }

    /**
     * Switch the JPanel shown to the new one
     */
    private void switchPanel(String panelTitle){
        // To do this, we first have to get the Frontend object by the getParent method, then call the real
        // switchPanel that is inside the Frontend
        Frontend frontend = (Frontend) getParent();
        frontend.switchPanel(panelTitle);
    }

    /**
     * Open the settings dialog
     */
    private void openSettings(){
        Settings settingsDialog = new Settings(this.ITC);
        settingsDialog.setVisible(true);
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
