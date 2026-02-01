package moda.passwordmanager.frontend.panels;

import moda.passwordmanager.frontend.components.ModaButton;
import moda.passwordmanager.frontend.components.ModaTextField;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;
import moda.passwordmanager.interthreadcommunication.Event;
import moda.passwordmanager.frontend.dialogs.MasterPasswordDialog;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Paths;

public class SettingsPanel extends JPanel {

    // Attribute to communicate with the backend
    private InterThreadCommunication itc;

    // Attributes for the configuration of the panel
    private final int MIN_CONTENT_WIDTH;
    private Dimension windowSize;

    // Components of the panel
    private ModaTextField databasePathTextField;  // Read-only state to show the state of the database
    private ModaButton changeDatabaseButton;  // Change to another database, already existing
    private ModaButton newDatabaseButton;  // Create a new database in a directory
    private ModaButton changeMasterPasswordButton;  // Change the master password of the current database
    private ModaButton enableGoogleDriveButton;  // Enable the Google Drive synchronization
    private ModaButton disableGoogleDriveButton;  // Disable the Google Drive synchronization
    private ModaButton synchronizeGoogleDriveButton;  // Manual synchronization with Google Drive
    private ModaButton enableAutomaticSynchronizationButton;  // Enable the automatic synchronization
    private ModaButton disableAutomaticSynchronizationButton;  // Disable the automatic synchronization

    public SettingsPanel(InterThreadCommunication itc, int MIN_CONTENT_WIDTH, Dimension windowSize,
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
        setLayout(new MigLayout("insets 30 30 30 30, fillx"));  // Set its layout

        // Set the preferred size
        setPreferredSize(new Dimension((int) (this.windowSize.getWidth() - sidebarPanelWidth), (int) this.windowSize.getHeight()));

        setBackground(Color.WHITE);

    }

    /**
     * Initialize the components of the panel
     */
    private void initComponents(){
        Dimension buttonDimension = new Dimension(250, 50);  // Define the dimension of any JButton

        int buttonFontSize = 21;
        
        JLabel settingsLabel = new JLabel();
        settingsLabel.setText("Settings:");
        settingsLabel.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 40));

        JLabel databaseInUseLabel = new JLabel();
        databaseInUseLabel.setText("Database in use: ");

        this.databasePathTextField = new ModaTextField(ModaTextField.TestFieldStyle.CLASSIC, 450,50);
//        this.databasePathTextField.setText(getCurrentDatabasePath());
        this.databasePathTextField.setText("C:\\...\\" + simplifiDBPath(getCurrentDatabasePath()));
        this.databasePathTextField.setEditable(false);

        this.newDatabaseButton = new ModaButton(ModaButton.ButtonStyle.CLASSIC_WHITE, (int) buttonDimension.getHeight(), buttonFontSize);
        this.newDatabaseButton.setText("New database");

        this.changeDatabaseButton = new ModaButton(ModaButton.ButtonStyle.CLASSIC_WHITE, (int) buttonDimension.getHeight(), buttonFontSize);
        this.changeDatabaseButton.setText("Change database");

        this.changeMasterPasswordButton = new ModaButton(ModaButton.ButtonStyle.CLASSIC_WHITE, (int) buttonDimension.getHeight(), buttonFontSize);
        this.changeMasterPasswordButton.setText("Change the master password");

        this.enableGoogleDriveButton = new ModaButton(ModaButton.ButtonStyle.CLASSIC_WHITE, (int) buttonDimension.getHeight(), buttonFontSize);
        this.enableGoogleDriveButton.setText("Enable Google Drive");
        this.enableGoogleDriveButton.setVisible(false);  // The visibility it's decided later

        this.disableGoogleDriveButton = new ModaButton(ModaButton.ButtonStyle.CLASSIC_WHITE, (int) buttonDimension.getHeight(), buttonFontSize);
        this.disableGoogleDriveButton.setText("Disable Google Drive");
        this.disableGoogleDriveButton.setVisible(false);  // The visibility it's decided later

        this.synchronizeGoogleDriveButton = new ModaButton(ModaButton.ButtonStyle.CLASSIC_WHITE, (int) buttonDimension.getHeight(), buttonFontSize);
        this.synchronizeGoogleDriveButton.setText("Synchronize");
        this.synchronizeGoogleDriveButton.setEnabled(false);  // The sync is allowed only when Google Drive is enabled

        this.enableAutomaticSynchronizationButton = new ModaButton(ModaButton.ButtonStyle.CLASSIC_WHITE, (int) buttonDimension.getHeight(), buttonFontSize);
        this.enableAutomaticSynchronizationButton.setText("Enable automatic synchronization");
        this.enableAutomaticSynchronizationButton.setVisible(false);  // The visibility it's decided later
        this.enableAutomaticSynchronizationButton.setEnabled(false);  // The sync is allowed only when Google Drive is enabled

        this.disableAutomaticSynchronizationButton = new ModaButton(ModaButton.ButtonStyle.CLASSIC_WHITE, (int) buttonDimension.getHeight(), buttonFontSize);
        this.disableAutomaticSynchronizationButton.setText("Disable automatic synchronization");
        this.disableAutomaticSynchronizationButton.setVisible(false);  // The visibility it's decided later
        this.disableAutomaticSynchronizationButton.setEnabled(false);  // The sync is allowed only when Google Drive is enabled

        // Set the initial visibilities of the buttons that depend on the settings file
        setGoogleDriveVisibility();
        setGoogleDriveAutomaticSynchronizationVisibility();

        add(settingsLabel, "grow, wrap, span");


        JPanel changeDBSection = new JPanel();
        changeDBSection.setLayout(new MigLayout("fillx"));

        JLabel dbSettingsLabel = new JLabel();
        dbSettingsLabel.setText("Database settings:");
        dbSettingsLabel.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 20));

        changeDBSection.add(dbSettingsLabel, "growx, wrap, span");
        changeDBSection.add(databaseInUseLabel, "span 1 2");
        changeDBSection.add(this.databasePathTextField, "growx, span 1 2");
        changeDBSection.add(changeDatabaseButton, "growx, wrap 0");
        changeDBSection.add(newDatabaseButton, "growx");

        add(changeDBSection, "grow, wrap 30");


        JPanel databaseSettings = new JPanel();
        databaseSettings.setLayout(new MigLayout("fillx"));

        databaseSettings.add(changeMasterPasswordButton);

        add(databaseSettings, "grow, wrap 30");

        JPanel driveSyncSection = new JPanel();
        driveSyncSection.setLayout(new MigLayout("fillx"));

        JLabel driveSyncSettingsLabel = new JLabel();
        driveSyncSettingsLabel.setText("Google drive sync settings:");
        driveSyncSettingsLabel.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 20));

        driveSyncSection.add(driveSyncSettingsLabel, "growx, wrap, span");
        driveSyncSection.add(enableGoogleDriveButton, "wrap");
        driveSyncSection.add(enableAutomaticSynchronizationButton);
        driveSyncSection.add(synchronizeGoogleDriveButton);

        add(driveSyncSection, "grow");
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
                if (!MasterPasswordDialog.checkMasterPassword(masterPassword.toCharArray())){
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

    private String simplifiDBPath(String databasePath){
        if (databasePath == null || databasePath.isEmpty()) {
            return "";
        }

        String simplifiedPath = Paths.get(databasePath).getFileName().toString();
        int lastDotIndex = simplifiedPath.lastIndexOf('.');

        if (lastDotIndex <= 0) {
            return simplifiedPath;
        }

        return simplifiedPath.substring(0, lastDotIndex);

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
        MasterPasswordDialog masterPasswordDialog = new MasterPasswordDialog(this.itc, databasePath);
        masterPasswordDialog.setVisible(true);
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
