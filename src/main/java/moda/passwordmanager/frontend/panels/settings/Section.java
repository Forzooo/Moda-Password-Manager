package moda.passwordmanager.frontend.panels.settings;

import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;

import javax.swing.*;

/**
 * The Section class for the Settings JPanels is used to define the common characteristics of each panel of the
 * Settings dialog, to allow them to be simple and easy to modify.
 */
public abstract class Section extends JPanel {

    /**
     * Defines the title of the section. <br>
     * It is displayed inside the Settings dialog.
     */
    private final String SECTION_TITLE;

    protected final InterThreadCommunication ITC;

    public Section(String title, InterThreadCommunication itc){
        this.SECTION_TITLE = title;
        this.ITC = itc;
    }

    /**
     * Initialize the properties of the Section panels
     */
    private void initPanel(){

    }

    /**
     * Get the title of the section
     */
    public String getSectionTitle(){
        return this.SECTION_TITLE;
    }

    /**
     * Return the current panel.
     */
    public abstract JPanel getPanel();


}
