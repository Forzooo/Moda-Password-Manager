package moda.passwordmanager.frontend.panels.settings;

import moda.passwordmanager.frontend.Utilities;
import moda.passwordmanager.frontend.properties.Languages;
import moda.passwordmanager.frontend.properties.Themes;
import moda.passwordmanager.interthreadcommunication.Event;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;
import raven.modal.Toast;

import javax.swing.*;

public class Appearance extends Section {

    private JComboBox<String> changeThemesComboBox;
    private JButton changeThemesButton;

    private JComboBox<String> changeLanguageComboBox;
    private JButton changeLanguageButton;

    public Appearance(InterThreadCommunication itc){
        super(Utilities.getLocaleString("Moda.Appearance.panelTitle"), itc);

        initComponents();
        setSelectedSettings();

        initListeners();
    }

    /**
     * Initialize the components of the section
     */
    private void initComponents(){
        JPanel themesPanel = new JPanel();

        this.changeThemesComboBox = new JComboBox<>();
        this.changeThemesComboBox.setEditable(false);

        // Add all the themes supported by the application in the combo box
        // The values method must be used to be consistent with changeApplicationTheme method
        for (Themes theme : Themes.values()){
            this.changeThemesComboBox.addItem(Utilities.getLocaleString("Moda.Themes."+theme));
        }

        this.changeThemesButton = new JButton();
        this.changeThemesButton.setText(Utilities.getLocaleString("Moda.Appearance.changeThemesButton"));
        this.changeThemesButton.setMaximumSize(getButtonDimension());

        themesPanel.add(this.changeThemesComboBox);
        themesPanel.add(this.changeThemesButton);

        JPanel languagesPanel = new JPanel();

        this.changeLanguageComboBox = new JComboBox<>();
        this.changeLanguageComboBox.setEditable(false);

        for (Languages language : Languages.values()){
            this.changeLanguageComboBox.addItem(Utilities.getLocaleString("Moda.Languages."+language));
        }

        this.changeLanguageButton = new JButton();
        this.changeLanguageButton.setText(Utilities.getLocaleString("Moda.Appearance.changeLanguageButton"));
        this.changeLanguageButton.setMaximumSize(getButtonDimension());

        languagesPanel.add(this.changeLanguageComboBox);
        languagesPanel.add(this.changeLanguageButton);

        addOption(themesPanel);
        addOption(languagesPanel);
    }

    @Override
    protected void setSelectedSettings() {
        Event themeEvent = getItc().request(new Event("get-application-theme"));
        this.changeThemesComboBox.setSelectedItem(Utilities.getLocaleString("Moda.Themes."+themeEvent.getData().getFirst()));

        Event languageEvent = getItc().request(new Event("get-application-language"));
        this.changeLanguageComboBox.setSelectedItem(Utilities.getLocaleString("Moda.Languages."+languageEvent.getData().getFirst()));
    }

    /**
     * Initialize all the listeners
     */
    private void initListeners(){
        this.changeThemesButton.addActionListener(e -> changeApplicationTheme(changeThemesComboBox.getSelectedIndex()));
        this.changeLanguageButton.addActionListener(e -> changeApplicationLanguage(changeLanguageComboBox.getSelectedIndex()));
    }

    @Override
    public JPanel getPanel() {
        return this;
    }

    /**
     * Change the theme of the application
     * @param themeIndex The index of the theme selected must be used to avoid issues when another language is used
     */
    private void changeApplicationTheme(int themeIndex){
        // It's the same index because when the themes are added, they are in the same order as values() ones
        Event request = new Event("set-application-theme", Themes.values()[themeIndex]);
        getItc().send(request);
        Utilities.showToast(getRootPane(), Toast.Type.INFO, Utilities.getLocaleString("Moda.Toast.themeSelected"));
    }

    /**
     * Change the language of the application
     * @param languageIndex The index of the language selected must be used to avoid issues when another language is used
     */
    private void changeApplicationLanguage(int languageIndex){
        Event request = new Event("set-application-language", Languages.values()[languageIndex]);
        getItc().send(request);
        Utilities.showToast(getRootPane(), Toast.Type.INFO, Utilities.getLocaleString("Moda.Toast.languageSelected"));
    }

}
