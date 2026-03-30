package moda.passwordmanager.frontend.panels.settings;

import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;
import net.miginfocom.swing.MigLayout;

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

    /**
     * The current section is the panel where the options are added
     */
    private JPanel currentSection;

    public Section(String title, InterThreadCommunication itc){
        this.SECTION_TITLE = title;
        this.ITC = itc;

        initPanel();
    }

    /**
     * Initialize the properties of the Section panels
     */
    private void initPanel(){
        setLayout(new MigLayout());
        addSection("");  // The first section is already added and has no title
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
     * Create a new section where all the options added will be. Moreover, it automatically ends the previous section, if
     * it exists. Lastly, the first section is already created.
     * @param title The title of the section, displayed next to the JSeparator
     */
    protected void addSection(String title){
        // Add the JSeparator with the title only if the section added is not the first
        if (this.currentSection != null){
            JPanel sectionEnd = new JPanel();
            sectionEnd.setLayout(new FlowLayout());
            JLabel nextSectionTitle = new JLabel();
            nextSectionTitle.setText(title);

            sectionEnd.add(nextSectionTitle);
            sectionEnd.add(new JSeparator(SwingConstants.HORIZONTAL));

            add(sectionEnd, "span, wrap");
        }
        this.currentSection = new JPanel();
        this.currentSection.setLayout(new MigLayout());

        add(this.currentSection, "span, wrap");
    }

    /**
     * Add a component to the current section
     */
    protected void addOption(Component component){
        this.currentSection.add(component, "span, wrap");
    }

    /**
     * Add a component to the current section with some additional constraints
     */
    protected void addOption(Component component, String constraints){
        this.currentSection.add(component, "span, wrap, " + constraints);
    }

    /**
     * Get the ITC object used to communicate
     */
    protected InterThreadCommunication getITC(){
        return this.ITC;
    }
}
