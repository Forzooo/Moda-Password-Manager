package moda.passwordmanager.frontend.panels.settings;

import moda.passwordmanager.Application;
import moda.passwordmanager.frontend.Utilities;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;

import javax.swing.*;

public class About extends Section{

    public About(InterThreadCommunication itc){
        super(Utilities.getLocaleString("Moda.About.panelTitle"), itc);
        initComponents();
    }

    private void initComponents(){
        JLabel authorsLabel = new JLabel();
        authorsLabel.setText(Utilities.getLocaleString("Moda.About.authorsLabel") + " Forzo, Bronte");

        JLabel versionLabel = new JLabel();
        versionLabel.setText(Utilities.getLocaleString("Moda.About.versionLabel") + " " + Application.getVersion());

        addOption(authorsLabel);
        addOption(versionLabel);
    }

    @Override
    public JPanel getPanel() {
        return this;
    }
}
