package moda.passwordmanager.frontend.panels.settings;

import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;

import javax.swing.*;
import java.awt.*;

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

    private final InterThreadCommunication ITC;

    public Section(String title, InterThreadCommunication itc){
        this.SECTION_TITLE = title;
        this.ITC = itc;

        initPanel();
    }

    /**
     * Initialize the properties of the Section panels
     */
    private void initPanel(){
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
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

    /**
     * Add a component to the section, creating a panel for it
     */
    protected void addOption(Component component){
        JPanel optionPanel = new JPanel();
        optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.Y_AXIS));
        optionPanel.add(component);
        add(optionPanel);
    }

    /**
     * Get the ITC object used to communicate
     */
    protected InterThreadCommunication getITC(){
        return this.ITC;
    }
}
