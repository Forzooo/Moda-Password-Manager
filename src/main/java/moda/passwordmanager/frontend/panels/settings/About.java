package moda.passwordmanager.frontend.panels.settings;

import moda.passwordmanager.Application;
import moda.passwordmanager.frontend.Utilities;
import moda.passwordmanager.interthreadcommunication.Event;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;
import raven.modal.Toast;

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
        authorsLabel.setText(Utilities.getLocaleString("Moda.About.authorsLabel") + ": Forzo, Bronte");

        JLabel licenseLabel = new JLabel();
        licenseLabel.setText(Utilities.getLocaleString("Moda.About.licenseLabel") + ": GPL-3.0 license");

        JPanel versionPanel = new JPanel();

        JLabel versionLabel = new JLabel();
        versionLabel.setText(Utilities.getLocaleString("Moda.About.versionLabel") + ": " + Application.getVersion());

        this.checkVersionButton = new JButton();
        this.checkVersionButton.setText(Utilities.getLocaleString("Moda.About.checkVersionButton"));

        this.checkVersionStartupCheckbox = new JCheckBox();
        this.checkVersionStartupCheckbox.setText(Utilities.getLocaleString("Moda.About.checkVersionStartupCheckbox"));
        this.checkVersionStartupCheckbox.setSelected(getCheckVersionStartup());  // Set the initial status based on the
                                                                                 // settings file

        versionPanel.add(versionLabel);
        versionPanel.add(this.checkVersionButton);

        addOption(authorsLabel);
        addOption(licenseLabel);
        addOption(versionPanel);
        addOption(this.checkVersionStartupCheckbox);
    }

    /**
     * Initialize all the listeners
     */
    private void initListeners(){
        this.checkVersionButton.addActionListener(e -> checkNewVersion());

        this.checkVersionStartupCheckbox.addActionListener(e -> getItc().send(
                new Event("set-check-new-version-on-startup", checkVersionStartupCheckbox.isSelected()))
        );
    }

    /**
     * Check for a new version and show a toast to let the user know the result
     */
    private void checkNewVersion(){
        Event checkVersionEvent = getItc().request(new Event("check-new-version"));

        if ((boolean) checkVersionEvent.getData().getFirst()) {
            Utilities.showToast(getParent(), Toast.Type.INFO,
                    Utilities.getLocaleString("Moda.Toast.checkNewVersionAvailable") + " "
                            + checkVersionEvent.getData().get(1));
        }else{
            Utilities.showToast(getParent(), Toast.Type.INFO,
                    Utilities.getLocaleString("Moda.Toast.checkNewVersionUnavailable"));
        }
    }

    /**
     * Get whether the check version on startup is enabled in the settings
     */
    private boolean getCheckVersionStartup(){
        Event getVersion = getItc().request(new Event("is-check-new-version-on-startup"));

        return (boolean) getVersion.getData().getFirst();
    }

    @Override
    public JPanel getPanel() {
        return this;
    }
}