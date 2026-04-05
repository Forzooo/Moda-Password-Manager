package moda.passwordmanager.frontend.panels.settings;

import moda.passwordmanager.frontend.Themes;
import moda.passwordmanager.interthreadcommunication.Event;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;

import javax.swing.*;

public class Appearance extends Section {

    private JComboBox<Themes> changeThemesComboBox;
    private JButton changeThemesButton;

    public Appearance(InterThreadCommunication itc){
        super("Appearance", itc);

        initComponents();
        initListeners();
    }

    private void initComponents(){
        JPanel themesPanel = new JPanel();

        this.changeThemesComboBox = new JComboBox<>();
        this.changeThemesComboBox.setEditable(false);

        // Add all the themes supported by the application in the combo box
        for (Themes theme : Themes.values()){
            this.changeThemesComboBox.addItem(theme);
        }

        this.changeThemesButton = new JButton();
        this.changeThemesButton.setText("Change theme");
        this.changeThemesButton.setMaximumSize(getButtonDimension());

        themesPanel.add(this.changeThemesComboBox);
        themesPanel.add(this.changeThemesButton);

        addOption(themesPanel);
    }

    /**
     * Initialize all the listeners
     */
    private void initListeners(){
        this.changeThemesButton.addActionListener(e -> changeApplicationTheme((Themes) changeThemesComboBox.getSelectedItem()));
    }

    @Override
    public JPanel getPanel() {
        return this;
    }

    /**
     * Change the theme of the application
     */
    private void changeApplicationTheme(Themes theme){
        Event request = new Event("set-application-theme", theme.toString());
        getITC().send(request);
    }

}
