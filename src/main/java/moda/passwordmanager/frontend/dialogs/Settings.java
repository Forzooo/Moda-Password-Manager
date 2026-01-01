package moda.passwordmanager.frontend.dialogs;

import moda.passwordmanager.Application;
import moda.passwordmanager.frontend.panels.settings.*;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class Settings extends JDialog {

    private final static Dimension DIALOG_DIMENSION = new Dimension(800, 700);

    private final InterThreadCommunication ITC;

    private JList<String> sidebarSections;
    private DefaultListModel<String> sidebarSectionsModel;

    private ArrayList<Section> sections;  // All the sections panels are stored here

    private JPanel sectionPanel;  // The section panel is used to show the section selected

    public Settings(InterThreadCommunication itc) {
        super();  // Initialize the Panel

        this.ITC = itc;
        this.sections = new ArrayList<>();

        initDialog();
        initComponents();
        initListeners();
    }

    /**
     * Configurate the properties of the JPanel
     */
    private void initDialog(){
        setTitle(Application.getApplicationTitle());
        setIconImage(Application.getIcon());
        setSize(DIALOG_DIMENSION);

        setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));

        setModal(true);  // Enable modality to block input to other password manager windows
        setLocationRelativeTo(getRootPane());
    }

    /**
     * Initialize the components of the panel
     */
    private void initComponents(){
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setPreferredSize(new Dimension((int) DIALOG_DIMENSION.getWidth()/4, (int) DIALOG_DIMENSION.getHeight()));
        sidebarPanel.setMaximumSize(new Dimension((int) DIALOG_DIMENSION.getWidth()/4, (int) DIALOG_DIMENSION.getHeight()));

        this.sidebarSections = new JList<>();
        this.sidebarSections.setBackground(null);  // The list has a white background while we want to use the FlatLaf one
        this.sidebarSections.setPreferredSize(sidebarPanel.getPreferredSize());

        this.sidebarSectionsModel = new DefaultListModel<>();
        this.sidebarSections.setModel(this.sidebarSectionsModel);

        sidebarPanel.add(this.sidebarSections);

        this.sectionPanel = new JPanel();

        addSection(new Data(this.ITC));
        addSection(new Database(this.ITC));
        addSection(new GoogleDrive(this.ITC));
        addSection(new About(this.ITC));

        add(sidebarPanel, BorderLayout.LINE_START);
        add(new JSeparator(SwingConstants.VERTICAL));
        add(this.sectionPanel, BorderLayout.CENTER);
    }

    /**
     * Initialize all the listeners of the components
     */
    private void initListeners(){
        this.sidebarSections.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                switchSection(sections.get(sidebarSections.getSelectedIndex()));
            }
        });
    }

    /**
     * Add a section to the sidebar
     */
    private void addSection(Section section){
        this.sidebarSectionsModel.addElement(section.getSectionTitle());
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
