package moda.passwordmanager.frontend.panels;

import moda.passwordmanager.frontend.Utilities;
import moda.passwordmanager.frontend.panels.settings.*;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class Settings extends JPanel {

    private final InterThreadCommunication ITC;

    public Settings(InterThreadCommunication itc){
        super();

        this.ITC = itc;

        initPanel();
        initSections();
    }

    /**
     * Initialize the panel
     */
    private void initPanel(){
        setLayout(new MigLayout("debug"));
        setBorder(new EmptyBorder(10, 10, 10, 10));
    }

    /**
     * Initialize the sections
     */
    private void initSections(){
        addSection(new Appearance(this.ITC));
        addSection(new Data(this.ITC));
        addSection(new GoogleDrive(this.ITC));
        addSection(new About(this.ITC));
    }

    /**
     * Returns the title of the panel
     */
    public static String getPanelTitle() {
        return Utilities.getLocaleString("Moda.Settings.panelTitle");
    }


    private void addSection(Section section){
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        JLabel sectionName = new JLabel(section.getTitle());

        add(sectionName, "span");
        add(separator, "span, grow, wrap");
        add(section, "span");
    }
}
