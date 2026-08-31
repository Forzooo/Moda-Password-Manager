package moda.passwordmanager.frontend.panels;

import moda.passwordmanager.frontend.Utilities;
import moda.passwordmanager.frontend.panels.settings.*;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class Settings extends JPanel {

    private final InterThreadCommunication ITC;

    public Settings(InterThreadCommunication itc){
        super();

        this.ITC = itc;

        initPanel();
        initComponents();
        initListeners();
    }

    private void initPanel(){
        setLayout(new MigLayout());
    }

    private void initComponents(){
        addSection(new Appearance(this.ITC));
        addSection(new Data(this.ITC));
        addSection(new Database(this.ITC));
        addSection(new GoogleDrive(this.ITC));
        addSection(new About(this.ITC));
    }

    private void initListeners(){

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

        add(separator, "span");
        add(sectionName, "span");
        add(separator, "span, wrap");
        add(section, "span");
    }
}
