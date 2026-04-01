package moda.passwordmanager.frontend.dialogs;

import moda.passwordmanager.Application;
import moda.passwordmanager.frontend.panels.settings.*;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class Settings extends JDialog {

    private final static Dimension DIALOG_DIMENSION = new Dimension(800, 700);

    private final InterThreadCommunication ITC;

    private JList<String> sidebarSections;
    private DefaultListModel<String> sidebarSectionsModel;

    private JPanel sectionPanel;  // The panel where are showed the sections
    private ArrayList<Section> sections;  // All the sections panels are stored here

    private JButton cancelButton;
    private JButton confirmButton;
    private JButton applyButton;

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

        setLayout(new MigLayout());

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
        this.sectionPanel.setLayout(new CardLayout());

        addSection(new Appearance(this.ITC));
        addSection(new Data(this.ITC));
        addSection(new Database(this.ITC));
        addSection(new GoogleDrive(this.ITC));
        addSection(new About(this.ITC));

        // Dialog operations panel
        JPanel dialogOperations = new JPanel();

        this.cancelButton = new JButton();
        this.cancelButton.setText("Cancel");

        this.confirmButton = new JButton();
        this.confirmButton.setText("Confirm");

        this.applyButton = new JButton();
        this.applyButton.setText("Apply");

        dialogOperations.add(this.confirmButton);
        dialogOperations.add(this.cancelButton);
        dialogOperations.add(this.applyButton);

        add(sidebarPanel);
        add(new JSeparator(SwingConstants.VERTICAL), "grow");
        add(this.sectionPanel, "span, grow, wrap");
        add(new JSeparator(SwingConstants.CENTER), "span, grow, wrap");
        add(dialogOperations, "span, align right");
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

        this.cancelButton.addActionListener(e -> dispose());
    }

    /**
     * Add a section to the sidebar
     */
    private void addSection(Section section){
        this.sidebarSectionsModel.addElement(section.getSectionTitle());
        this.sections.add(section);
        this.sectionPanel.add(section, section.getSectionTitle());
    }

    /**
     * Switch to another section
     */
    private void switchSection(Section section){
        CardLayout cardLayout = (CardLayout) this.sectionPanel.getLayout();
        cardLayout.show(this.sectionPanel, section.getSectionTitle());
    }

}
