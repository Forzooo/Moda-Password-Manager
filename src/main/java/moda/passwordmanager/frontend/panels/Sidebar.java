package moda.passwordmanager.frontend.panels;

import moda.passwordmanager.frontend.Frontend;
import moda.passwordmanager.frontend.Utilities;
import moda.passwordmanager.frontend.components.ModaButton;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class Sidebar extends JPanel {

    // Swing components
    private ModaButton addDataButton;
    private ModaButton showDataButton;
    private ModaButton settingsButton;

    public Sidebar(){
        super();  // Initialize the Panel

        initPanel();
        initComponents();
        initListeners();
    }

    /**
     * Set the configuration of the JPanel
     */
    private void initPanel(){
        setLayout(new MigLayout("wrap 1, insets 20, fillx",
            "",
            "[][][]push[]"));

        setBackground(UIManager.getColor("Moda.Sidebar.background"));
    }

    /**
     * Initialize the components of the panel
     */
    private void initComponents(){
        // Title Section
        JLabel passwordManagerLabel = new JLabel();  // Create the JLabel that displays the name of the Password Manager
        passwordManagerLabel.setText("MODA");
        passwordManagerLabel.setFont(new Font(UIManager.getString("Moda.GeneralUseFontFamily"), Font.PLAIN, 80));
        passwordManagerLabel.setForeground(UIManager.getColor("Moda.Sidebar.Title.foreground"));

        // Create the JButtons used to switch between JPanels of the dynamic part, where their width is the same as
        // the sidebar
        Dimension buttonDimension = new Dimension((int) getPreferredSize().getWidth(), 70);

        this.addDataButton = new ModaButton(ModaButton.ButtonStyle.CLASSIC_BLACK, buttonDimension, 30);
        this.addDataButton.setText(AddData.getPanelTitle());

        this.showDataButton = new ModaButton(ModaButton.ButtonStyle.CLASSIC_BLACK, buttonDimension, 30);
        this.showDataButton.setText(ShowData.getPanelTitle());

        // Settings + Details section
        this.settingsButton = new ModaButton(ModaButton.ButtonStyle.EMPTY, buttonDimension.height/2, buttonDimension.height/2, 30);
        ImageIcon settingsIcon = Utilities.getIcon("settings_icon.png");
        settingsIcon.setImage(settingsIcon.getImage().getScaledInstance(buttonDimension.height/2,
                buttonDimension.height/2, Image.SCALE_SMOOTH));
        this.settingsButton.setIcon(settingsIcon);
        this.settingsButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        add(passwordManagerLabel, "al center, wrap 150");
        add(this.addDataButton, "grow, al center, wrap 20");
        add(this.showDataButton, "grow, al center");
        add(this.settingsButton);
    }

    /**
     * Initialize all the listeners on the components
     */
    private void initListeners(){
        this.addDataButton.addActionListener(e -> switchPanel(AddData.getPanelTitle()));

        this.showDataButton.addActionListener(e -> switchPanel(ShowData.getPanelTitle()));

        this.settingsButton.addActionListener(e -> switchPanel(Settings.getPanelTitle()));
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
}