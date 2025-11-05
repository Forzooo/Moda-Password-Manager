package moda.passwordmanager.frontend.panels;

import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;
import moda.passwordmanager.interthreadcommunication.Event;
import moda.passwordmanager.frontend.dialogs.MasterPassword;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Settings extends JPanel {

    // Attribute to communicate with the backend
    private InterThreadCommunication itc;

    // Attributes for the configuration of the panel
    private final int MIN_CONTENT_WIDTH;
    private Dimension windowSize;

    // Components of the panel
    private JTextField databasePathTextField;  // Read-only state to show the state of the database
    private JButton changeDatabaseButton;  // Change to another database, already existing
    private JButton newDatabaseButton;  // Create a new database in a directory
    private JButton changeMasterPasswordButton;  // Change the master password of the current database
    private JButton enableGoogleDriveButton;  // Enable the Google Drive synchronization
    private JButton disableGoogleDriveButton;  // Disable the Google Drive synchronization
    private JButton synchronizeGoogleDriveButton;  // Manual synchronization with Google Drive
    private JButton enableAutomaticSynchronizationButton;  // Enable the automatic synchronization
    private JButton disableAutomaticSynchronizationButton;  // Disable the automatic synchronization

    public Settings(InterThreadCommunication itc, int MIN_CONTENT_WIDTH, Dimension windowSize,
                    int sidebarPanelWidth) {
        super();  // Initialize the Panel

        // Set the attributes given by the JFrame
        this.itc = itc;
        this.MIN_CONTENT_WIDTH = MIN_CONTENT_WIDTH;
        this.windowSize = windowSize;

        initPanel(sidebarPanelWidth);
        initComponents();
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
    private void initComponents(){
        Dimension textFieldDimension = new Dimension(1600, 30);  // Define the dimension of any JTextField
        Dimension buttonDimension = new Dimension(250, 20);  // Define the dimension of any JButton

        // Database settings section
        JPanel databasePanel = new JPanel();  // The JPanel used for all the components related to the database
        databasePanel.setBackground(Color.white);
        databasePanel.setPreferredSize(new Dimension(500, 100));
        databasePanel.setLayout(new FlowLayout());

        JLabel databaseInUseLabel = new JLabel();
        databaseInUseLabel.setText("Database in use: ");

        this.databasePathTextField = new JTextField();
        this.databasePathTextField.setText(getCurrentDatabasePath());
        this.databasePathTextField.setMaximumSize(textFieldDimension);
        this.databasePathTextField.setEditable(false);

        this.newDatabaseButton = new JButton();
        this.newDatabaseButton.setText("New database");
        this.newDatabaseButton.setMaximumSize(buttonDimension);

        this.changeDatabaseButton = new JButton();
        this.changeDatabaseButton.setText("Change database");
        this.changeDatabaseButton.setMaximumSize(buttonDimension);

        databasePanel.add(databaseInUseLabel);
        databasePanel.add(this.databasePathTextField);
        databasePanel.add(this.newDatabaseButton);
        databasePanel.add(this.changeDatabaseButton);

        // Master password section
        JPanel masterPasswordPanel = new JPanel();
        masterPasswordPanel.setBackground(Color.white);
        masterPasswordPanel.setPreferredSize(new Dimension(500, 100));
        masterPasswordPanel.setLayout(new FlowLayout());

        this.changeMasterPasswordButton = new JButton();
        this.changeMasterPasswordButton.setText("Change the master password");
        this.changeMasterPasswordButton.setMaximumSize(buttonDimension);

        masterPasswordPanel.add(this.changeMasterPasswordButton);

        // Google Drive section
        JPanel googleDrivePanel = new JPanel();
        googleDrivePanel.setBackground(Color.white);
        googleDrivePanel.setPreferredSize(new Dimension(500, 100));
        googleDrivePanel.setLayout(new FlowLayout());

        this.enableGoogleDriveButton = new JButton();
        this.enableGoogleDriveButton.setText("Enable Google Drive");
        this.enableGoogleDriveButton.setMaximumSize(buttonDimension);
        this.enableGoogleDriveButton.setVisible(false);  // The visibility it's decided later

        this.disableGoogleDriveButton = new JButton();
        this.disableGoogleDriveButton.setText("Disable Google Drive");
        this.disableGoogleDriveButton.setMaximumSize(buttonDimension);
        this.disableGoogleDriveButton.setVisible(false);  // The visibility it's decided later

        this.synchronizeGoogleDriveButton = new JButton();
        this.synchronizeGoogleDriveButton.setText("Synchronize");
        this.synchronizeGoogleDriveButton.setMaximumSize(buttonDimension);
        this.synchronizeGoogleDriveButton.setEnabled(false);  // The sync is allowed only when Google Drive is enabled

        this.enableAutomaticSynchronizationButton = new JButton();
        this.enableAutomaticSynchronizationButton.setText("Enable automatic synchronization");
        this.enableAutomaticSynchronizationButton.setMaximumSize(buttonDimension);
        this.enableAutomaticSynchronizationButton.setVisible(false);  // The visibility it's decided later
        this.enableAutomaticSynchronizationButton.setEnabled(false);  // The sync is allowed only when Google Drive is enabled

        this.disableAutomaticSynchronizationButton = new JButton();
        this.disableAutomaticSynchronizationButton.setText("Disable automatic synchronization");
        this.disableAutomaticSynchronizationButton.setMaximumSize(buttonDimension);
        this.disableAutomaticSynchronizationButton.setVisible(false);  // The visibility it's decided later
        this.disableAutomaticSynchronizationButton.setEnabled(false);  // The sync is allowed only when Google Drive is enabled

        // Set the initial visibilities of the buttons that depend on the settings file
        setGoogleDriveVisibility();
        setGoogleDriveAutomaticSynchronizationVisibility();

        googleDrivePanel.add(this.enableGoogleDriveButton);
        googleDrivePanel.add(this.disableGoogleDriveButton);
        googleDrivePanel.add(this.synchronizeGoogleDriveButton);
        googleDrivePanel.add(this.enableAutomaticSynchronizationButton);
        googleDrivePanel.add(this.disableAutomaticSynchronizationButton);

        add(databasePanel);
        add(masterPasswordPanel);
        add(googleDrivePanel);
    }

    /**
     * Initialize all the listeners on the components
     */
    private void initListeners(){
        this.newDatabaseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Create the File Chooser that opens in the desktop, and saves a .modb file
                JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                fileChooser.setDialogTitle("Create a new database to use");
                fileChooser.setAcceptAllFileFilterUsed(false);  // Don't accept all the types of files

                // Create the filter to save only .modb files
                FileNameExtensionFilter filter = new FileNameExtensionFilter("Moda Password Manager Database (.modb)",
                        ".modb");
                fileChooser.setFileFilter(filter);

                // Open the file chooser
                if (fileChooser.showSaveDialog(getPanel()) == JFileChooser.APPROVE_OPTION){
                    String path = fileChooser.getSelectedFile().getAbsolutePath();  // Retrieve the path chosen

                    // Check whether the database has been chosen
                    if (!path.isEmpty()){
                        // If the file has been saved without setting the extension, set it automatically
                        if (!path.endsWith(".modb")){
                            path = path+".modb";
                        }
                        setNewDatabase(path);
                    }
                }
            }
        });

        this.changeDatabaseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Create the File Chooser that opens in the desktop view, and selects only .modb files
                JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                fileChooser.setDialogTitle("Choose a database to use");
                fileChooser.setAcceptAllFileFilterUsed(false);  // Don't accept all the types of files

                // Create the filter to choose only .modb files
                FileNameExtensionFilter filter = new FileNameExtensionFilter("Moda Password Manager Database (.modb)",
                        "modb");
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
                if (!MasterPassword.checkMasterPassword(masterPassword.toCharArray())){
                    return;
                }

                changeMasterPassword(masterPassword);
            }
        });

        // Let the user choose its credential.json file for the authentication, then send an Event to the backend
        this.enableGoogleDriveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Create the File Chooser to allow user to select the credentials.json file
                JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                fileChooser.setDialogTitle("Choose the OAuth credentials file");
                fileChooser.setAcceptAllFileFilterUsed(false);  // Don't accept all the types of files

                // Create the filter to choose only .modb files
                FileNameExtensionFilter filter = new FileNameExtensionFilter("OAuth Credentials (.json)",
                        "json");
                fileChooser.setFileFilter(filter);

                // Open the file chooser
                if (fileChooser.showOpenDialog(getPanel()) == JFileChooser.APPROVE_OPTION){
                    String path = fileChooser.getSelectedFile().getAbsolutePath();  // Retrieve the path chosen

                    // Check whether the database has been chosen
                    if (!path.isEmpty()){
                        enableGoogleDrive(path);
                        setGoogleDriveVisibility();
                    }
                }
            }
        });

        this.disableGoogleDriveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                disableGoogleDrive();
                setGoogleDriveVisibility();
            }
        });

        this.synchronizeGoogleDriveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                synchronizeGoogleDrive();
            }
        });

        this.enableAutomaticSynchronizationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enableAutomaticSynchronization();
                setGoogleDriveAutomaticSynchronizationVisibility();
            }
        });

        this.disableAutomaticSynchronizationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                disableAutomaticSynchronization();
                setGoogleDriveAutomaticSynchronizationVisibility();
            }
        });
    }

    /**
     * Get the current database from the backend
     * @return
     */
    private String getCurrentDatabasePath(){
        Event response = this.itc.requestAndReceive(new Event("get-database"));
        String databasePath = (String) response.getData().getFirst();  // Retrieve the path of the database
        return databasePath;
    }

    /**
     * Set the new database to the backend
     * @param databasePath The path of the new database
     */
    private void setNewDatabase(String databasePath){
        // Set the path of the database
        Event event = new Event("set-database", databasePath);
        this.itc.requestAndReceive(event);
        this.databasePathTextField.setText(databasePath);  // Set the new path of the database into the Text Field

        // Set the master password of the database before using it
        MasterPassword masterPassword = new MasterPassword(this.itc, databasePath);
        masterPassword.setVisible(true);
    }

    /**
     * Change the current master password of the database to a new one
     * @param masterPassword The new master password
     */
    private void changeMasterPassword(String masterPassword){
        Event event = new Event("change-master-password", masterPassword.toCharArray());
        this.itc.requestAndReceive(event);  // Wait for the end of the operations in the backend
    }

    /**
     * Retrieve from the settings file the current configuration of Google Drive to set the visibilities of the
     * buttons that enable and disable it, and enable/disable the buttons for the synchronization
     */
    private void setGoogleDriveVisibility(){
       // Retrieve from the settings file the configuration of Google Drive visibility
        Event event = new Event("get-google-drive");
        Event response = this.itc.requestAndReceive(event);
        boolean visibility = (boolean) response.getData().getFirst();

        if (!visibility){
            this.enableGoogleDriveButton.setVisible(true);
            this.disableGoogleDriveButton.setVisible(false);
            this.synchronizeGoogleDriveButton.setEnabled(false);
            this.enableAutomaticSynchronizationButton.setEnabled(false);
            this.disableAutomaticSynchronizationButton.setEnabled(false);
        }else{
            this.enableGoogleDriveButton.setVisible(false);
            this.disableGoogleDriveButton.setVisible(true);
            this.synchronizeGoogleDriveButton.setEnabled(true);
            this.enableAutomaticSynchronizationButton.setEnabled(true);
            this.disableAutomaticSynchronizationButton.setEnabled(true);

        }
   }

    /**
     * Retrieve from the settings file the current configuration of Google Drive synchronization to set the visibilities
     * of the buttons that enable and disable it
     */
    private void setGoogleDriveAutomaticSynchronizationVisibility(){
        // Retrieve from the settings file the configuration of Google Drive synchronization visibility
        Event event = new Event("get-google-drive-synchronization");
        Event response = this.itc.requestAndReceive(event);
        boolean visibility = (boolean) response.getData().getFirst();

        if (!visibility){
            this.enableAutomaticSynchronizationButton.setVisible(true);
            this.disableAutomaticSynchronizationButton.setVisible(false);
        }else{
            this.enableAutomaticSynchronizationButton.setVisible(false);
            this.disableAutomaticSynchronizationButton.setVisible(true);
        }
    }

    /**
     * Enable the Google Drive synchronization
     * @param credentialsPath The path of the OAuth credentials used for the authentication
     */
    private void enableGoogleDrive(String credentialsPath){
        Event event = new Event("google-drive-authenticate", credentialsPath);
        this.itc.request(event);
    }

    /**
     * Disable the Google Drive synchronization
     */
    private void disableGoogleDrive(){
        Event event = new Event("google-drive-unauthenticate");
        this.itc.request(event);  // Wait for the end of operations before disabling the button
    }

    /**
     * Perform a synchronization with Google Drive
     */
    private void synchronizeGoogleDrive(){
        Event event = new Event("google-drive-synchronize");
        this.itc.requestAndReceive(event);
    }

    /**
     * Enable the automatic synchronization of Google Drive
     */
    private void enableAutomaticSynchronization(){
        Event event = new Event("enable-google-drive-synchronization");
        this.itc.request(event);
    }

    /**
     * Disable the automatic synchronization of Google Drive
     */
    private void disableAutomaticSynchronization(){
        Event event = new Event("disable-google-drive-synchronization");
        this.itc.request(event);
    }

}
