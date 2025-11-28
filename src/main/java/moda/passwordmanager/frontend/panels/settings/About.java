package moda.passwordmanager.frontend.panels.settings;

import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;

import javax.swing.*;

public class About extends Section{

    public About(InterThreadCommunication itc){
        super("About", itc);
    }

    @Override
    public JPanel getPanel() {
        return this;
    }
}
