package moda.passwordmanager.frontend.dialogs;

import moda.passwordmanager.Application;
import moda.passwordmanager.frontend.panels.settings.*;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class Settings extends JDialog {

    private final static Dimension DIALOG_DIMENSION = new Dimension(800, 700);

    private InterThreadCommunication itc;

    private JPanel sidebarPanel;

    private ArrayList<Section> sections;  // All the sections panels are stored here

    private JPanel sectionPanel;  // The section panel is used to show the section selected

    public Settings(InterThreadCommunication itc) {
        super();  // Initialize the Panel

        this.itc = itc;
        this.sections = new ArrayList<>();

        initDialog();
        initComponents();
    }

    /**
     * Configurate the properties of the JPanel
     */
    private void initDialog(){
        setTitle(Application.getApplicationTitle());
        setIconImage(Application.getIcon());
        setSize(DIALOG_DIMENSION);

        setLayout(new BorderLayout());

        setModal(true);  // Enable modality to block input to other password manager windows
        setLocationRelativeTo(getRootPane());
    }

    /**
     * Initialize the components of the panel
     */
    private void initComponents(){
        this.sidebarPanel = new JPanel();
        this.sidebarPanel.setPreferredSize(new Dimension((int) DIALOG_DIMENSION.getWidth()/4, (int) DIALOG_DIMENSION.getHeight()));
        this.sidebarPanel.setMaximumSize(new Dimension((int) DIALOG_DIMENSION.getWidth()/4, (int) DIALOG_DIMENSION.getHeight()));
        this.sidebarPanel.setLayout(new BoxLayout(this.sidebarPanel, BoxLayout.Y_AXIS));
        this.sidebarPanel.setBackground(Color.WHITE);

        this.sectionPanel = new JPanel();

        addSection(new Data(this.itc));
        addSection(new Database(this.itc));
        addSection(new GoogleDrive(this.itc));
        addSection(new About(this.itc));

        add(this.sidebarPanel, BorderLayout.LINE_START);
        add(this.sectionPanel, BorderLayout.CENTER);
    }

    /**
     * Add a section to the sidebar
     */
    private void addSection(Section section){
        JButton sectionButton = new JButton();
        sectionButton.setText(section.getSectionTitle());
        sectionButton.addActionListener(e -> switchSection(section));
        this.sidebarPanel.add(sectionButton);
        this.sections.add(section);
    }

    /**
     * Switch to another section
     */
    private void switchSection(Section section){
        remove(this.sectionPanel);  // Remove the old panel
        this.sectionPanel = section;  // Set the sectionPanel to be the one selected
        add(this.sectionPanel, BorderLayout.CENTER);  // Add the section to the GUI, then revalidate and repaint
        revalidate();
        repaint();
    }

}
