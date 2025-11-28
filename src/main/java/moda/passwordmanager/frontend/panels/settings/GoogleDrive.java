package moda.passwordmanager.frontend.panels.settings;

import moda.passwordmanager.interthreadcommunication.Event;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GoogleDrive extends Section {

    private JButton enableGoogleDriveButton;  // Enable the Google Drive synchronization
    private JButton disableGoogleDriveButton;  // Disable the Google Drive synchronization
    private JButton synchronizeGoogleDriveButton;  // Manual synchronization with Google Drive
    private JButton enableAutomaticSynchronizationButton;  // Enable the automatic synchronization
    private JButton disableAutomaticSynchronizationButton;  // Disable the automatic synchronization

    public GoogleDrive(InterThreadCommunication itc){
        super("Google Drive", itc);

        initComponents();
        initListeners();
    }

    @Override
    public JPanel getPanel() {
        return this;
    }

    private void initComponents(){
        Dimension buttonDimension = new Dimension(250, 20);  // Define the dimension of any JButton

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

        add(this.enableGoogleDriveButton);
        add(this.disableGoogleDriveButton);
        add(this.synchronizeGoogleDriveButton);
        add(this.enableAutomaticSynchronizationButton);
        add(this.disableAutomaticSynchronizationButton);

    }

    private void initListeners(){
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
     * Retrieve from the settings file the current configuration of Google Drive to set the visibilities of the
     * buttons that enable and disable it, and enable/disable the buttons for the synchronization
     */
    private void setGoogleDriveVisibility(){
        // Retrieve from the settings file the configuration of Google Drive visibility
        Event event = new Event("get-google-drive");
        Event response = this.ITC.requestAndReceive(event);
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
        Event response = this.ITC.requestAndReceive(event);
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
        this.ITC.request(event);
    }

    /**
     * Disable the Google Drive synchronization
     */
    private void disableGoogleDrive(){
        Event event = new Event("google-drive-unauthenticate");
        this.ITC.request(event);  // Wait for the end of operations before disabling the button
    }

    /**
     * Perform a synchronization with Google Drive
     */
    private void synchronizeGoogleDrive(){
        Event event = new Event("google-drive-synchronize");
        this.ITC.requestAndReceive(event);
    }

    /**
     * Enable the automatic synchronization of Google Drive
     */
    private void enableAutomaticSynchronization(){
        Event event = new Event("enable-google-drive-synchronization");
        this.ITC.request(event);
    }

    /**
     * Disable the automatic synchronization of Google Drive
     */
    private void disableAutomaticSynchronization(){
        Event event = new Event("disable-google-drive-synchronization");
        this.ITC.request(event);
    }

}
