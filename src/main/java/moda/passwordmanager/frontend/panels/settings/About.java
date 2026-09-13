package moda.passwordmanager.frontend.panels.settings;

import moda.passwordmanager.Application;
import moda.passwordmanager.frontend.Utilities;
import moda.passwordmanager.interthreadcommunication.Event;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;

import javax.swing.*;

public class About extends Section{

    private JButton checkVersionButton;
    private JCheckBox checkVersionStartupCheckbox;

    public About(InterThreadCommunication itc){
        super(Utilities.getLocaleString("Moda.About.panelTitle"), itc);
        initComponents();
        initListeners();
    }

    /**
     * Initialize the components of the section
     */
    private void initComponents(){
        JLabel authorsLabel = new JLabel();
        authorsLabel.setText(Utilities.getLocaleString("Moda.About.authorsLabel") + " Forzo, Bronte");

        JPanel versionPanel = new JPanel();

        JLabel versionLabel = new JLabel();
        versionLabel.setText(Utilities.getLocaleString("Moda.About.versionLabel") + " " + Application.getVersion());

        this.checkVersionButton = new JButton();
        this.checkVersionButton.setText("Check new version");

        this.checkVersionStartupCheckbox = new JCheckBox();
        this.checkVersionStartupCheckbox.setText("Check version on startup");
        this.checkVersionStartupCheckbox.setSelected(getCheckVersionStartup());  // Set the initial status based on the
                                                                                 // settings file

        versionPanel.add(versionLabel);
        versionPanel.add(this.checkVersionButton);

        addOption(authorsLabel);
        addOption(versionPanel);
        addOption(this.checkVersionStartupCheckbox);
    }

    /**
     * Initialize all the listeners
     */
    private void initListeners(){
        this.checkVersionButton.addActionListener(e -> getItc().send(new Event("check-new-version")));

        this.checkVersionStartupCheckbox.addActionListener(e -> getItc().send(
                new Event("set-check-new-version-on-startup", checkVersionStartupCheckbox.isSelected()))
        );
    }

    /**
     * Get whether the check version on startup is enabled in the settings
     */
    public boolean getCheckVersionStartup(){
        Event getVersion = getItc().request(new Event("is-check-new-version-on-startup"));

        return (boolean) getVersion.getData().getFirst();
    }

    @Override
    public JPanel getPanel() {
        return this;
    }
}
