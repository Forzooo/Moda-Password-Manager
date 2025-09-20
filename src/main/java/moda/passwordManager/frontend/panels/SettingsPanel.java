package moda.passwordManager.frontend.panels;

import moda.passwordManager.communicationHandler.CommunicationHandler;
import moda.passwordManager.communicationHandler.Event;
import moda.passwordManager.frontend.dialogs.MasterPasswordDialog;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SettingsPanel extends JPanel {

    // Attribute to communicate with the backend
    private CommunicationHandler communicationHandler;

    // Attributes for the configuration of the panel
    private final int MIN_CONTENT_WIDTH;
    private Dimension windowSize;

    // Components of the panel
    private JTextField databasePathTextField;  // Read-only state to show the state of the database
    private JButton changeDatabaseButton;  // Change to another database, already existing
    private JButton newDatabaseButton;  // Create a new database in a directory
    private JButton changeMasterPasswordButton;  // Change the master password of the current database

    public SettingsPanel(CommunicationHandler communicationHandler, int MIN_CONTENT_WIDTH, Dimension windowSize,
                         int sidebarPanelWidth, String currentDatabasePath) {
        super();  // Initialize the Panel

        // Set the attributes given by the JFrame
        this.communicationHandler = communicationHandler;
        this.MIN_CONTENT_WIDTH = MIN_CONTENT_WIDTH;
        this.windowSize = windowSize;

        initPanel(sidebarPanelWidth);
        initComponents(currentDatabasePath);
        initListeners();
    }

    /**
     * Get the layout used for the panel
     * @return BorderLayout
     */
    private BoxLayout getPanelLayout() {
        return new BoxLayout(this, BoxLayout.Y_AXIS);
    }

    @Override
    public Dimension getMinimumSize() {
        // altezza 0 -> “qualsiasi”, conta solo la larghezza minima
        return new Dimension(MIN_CONTENT_WIDTH, 0);
    }

    /**
     * Get the current panel
     * @return The settings panel
     */
    private JPanel getPanel(){
        return this;
    }

    /**
     * Set the configuration of the JPanel
     * @param sidebarPanelWidth 
     */
    private void initPanel(int sidebarPanelWidth){
        setLayout(getPanelLayout());  // Set its layout

        // Set the preferred size
        setPreferredSize(new Dimension((int) (this.windowSize.getWidth() - sidebarPanelWidth), (int) this.windowSize.getHeight()));

        setBackground(Color.WHITE);

        // Set the margin of the Add Data Panel [top: 60, bottom: 60, left: 30, right: 30]
        setBorder(BorderFactory.createEmptyBorder(60,30,60,30));
    }

    /**
     * Initialize the components of the panel
     */
    private void initComponents(String currentDatabasePath){
        Dimension textFieldDimension = new Dimension(1600, 30);  // Define the dimension of any JTextField
        Dimension buttonDimension = new Dimension(250, 20);  // Define the dimension of any JButton

        JPanel databasePanel = new JPanel();  // The JPanel used for all the components related to the database
        databasePanel.setBackground(Color.white);
        databasePanel.setPreferredSize(new Dimension(500, 100));
        databasePanel.setLayout(new FlowLayout());

        JLabel databaseInUseLabel = new JLabel();
        databaseInUseLabel.setText("Database in use: ");

        this.databasePathTextField = new JTextField();
        this.databasePathTextField.setText(currentDatabasePath);
        this.databasePathTextField.setMaximumSize(textFieldDimension);
        this.databasePathTextField.setEditable(false);

        this.newDatabaseButton = new JButton();
        this.newDatabaseButton.setText("New database");
        this.newDatabaseButton.setMaximumSize(buttonDimension);

        this.changeDatabaseButton = new JButton();
        this.changeDatabaseButton.setText("Change database");
        this.changeDatabaseButton.setMaximumSize(buttonDimension);

        this.changeMasterPasswordButton = new JButton();
        this.changeMasterPasswordButton.setText("Change the master password");
        this.changeMasterPasswordButton.setMaximumSize(buttonDimension);

        databasePanel.add(databaseInUseLabel);
        databasePanel.add(this.databasePathTextField);
        databasePanel.add(this.newDatabaseButton);
        databasePanel.add(this.changeDatabaseButton);

        add(databasePanel);
        add(this.changeMasterPasswordButton);
    }

    /**
     * Initialize all the listeners on the components
     */
    private void initListeners(){
        this.newDatabaseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Create the File Chooser that opens in the desktop, and saves a .db file
                JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                fileChooser.setDialogTitle("Create a new database to use");
                fileChooser.setAcceptAllFileFilterUsed(false);  // Don't accept all the types of files

                // Create the filter to save only .db files
                FileNameExtensionFilter filter = new FileNameExtensionFilter("Moda Password Manager Database (.db)",
                        ".db");
                fileChooser.setFileFilter(filter);

                // Open the file chooser
                if (fileChooser.showSaveDialog(getPanel()) == JFileChooser.APPROVE_OPTION){
                    String path = fileChooser.getSelectedFile().getAbsolutePath();  // Retrieve the path chosen

                    // Check whether the database has been chosen
                    if (!path.isEmpty()){
                        // If the file has been saved without setting the extension, set it automatically
                        if (!path.endsWith(".db")){
                            path = path+".db";
                        }
                        setNewDatabase(path);
                    }
                }
            }
        });

        this.changeDatabaseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Create the File Chooser that opens in the desktop view, and selects only .db files
                JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                fileChooser.setDialogTitle("Choose a database to use");
                fileChooser.setAcceptAllFileFilterUsed(false);  // Don't accept all the types of files

                // Create the filter to choose only .db files
                FileNameExtensionFilter filter = new FileNameExtensionFilter("Moda Password Manager Database (.db)",
                        "db");
                fileChooser.setFileFilter(filter);

                // Open the file chooser
                if (fileChooser.showOpenDialog(getPanel()) == JFileChooser.APPROVE_OPTION){
                    String path = fileChooser.getSelectedFile().getAbsolutePath();  // Retrieve the path chosen

                    // Check whether the database has been chosen
                    if (!path.isEmpty()){
                        setNewDatabase(path);
                    }
                }
            }
        });

        this.changeMasterPasswordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String masterPassword = JOptionPane.showInputDialog(getPanel(), "Enter the new master password",
                        "");
                // Perform some initial conditions check on the master password
                if (!MasterPasswordDialog.checkMasterPassword(masterPassword.toCharArray())){
                    return;
                }

                changeMasterPassword(masterPassword);
            }
        });
    }

    /**
     * Set the new database to the backend
     * @param databasePath The path of the new database
     */
    private void setNewDatabase(String databasePath){
        Event event = new Event("set-database", databasePath);
        this.communicationHandler.send(event);
        this.communicationHandler.receive();
        this.databasePathTextField.setText(databasePath);  // Set the new path of the database into the Text Field
//        notifyUser()
    }

    /**
     * Change the current master password of the database to a new one
     * @param masterPassword The new master password
     */
    private void changeMasterPassword(String masterPassword){
        Event event = new Event("change-master-password", masterPassword.toCharArray());
        this.communicationHandler.send(event);
        this.communicationHandler.receive();  // Wait for the end of the operations in the backend
    }
}
