package moda.passwordmanager.frontend.desktop.panels;

import moda.passwordmanager.frontend.desktop.Utilities;
import moda.passwordmanager.frontend.desktop.panels.settings.*;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.util.ArrayList;

public class Settings extends JPanel {

    private final InterThreadCommunication itc;
    private final ArrayList<Section> sections;

    public Settings(InterThreadCommunication itc){
        super();

        this.itc = itc;
        this.sections = new ArrayList<>();

        initPanel();
        initSections();
    }

    /**
     * Initialize the panel
     */
    private void initPanel(){
        setLayout(new MigLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
    }

    /**
     * Initialize the sections
     */
    private void initSections(){
        addSection(new Appearance(this.itc));
        addSection(new Data(this.itc));
        addSection(new GoogleDrive(this.itc));
        addSection(new About(this.itc));
    }

    /**
     * Returns the title of the panel
     */
    public static String getPanelTitle() {
        return Utilities.getLocaleString("Moda.Settings.panelTitle");
    }

    /**
     * Add a section to the settings panel
     */
    private void addSection(Section section){
        this.sections.add(section);

        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        JLabel sectionName = new JLabel(section.getTitle());

        add(sectionName, "span");
        add(separator, "span, grow, wrap");
        add(section, "span");
    }

    /**
     * Reload the sections after the settings file has been reset
     */
    public void reloadSections(){
        for (Section section : this.sections){
            section.reload();
        }
    }

}
