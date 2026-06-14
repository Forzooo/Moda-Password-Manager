package moda.passwordmanager.frontend.panels.settings;

import com.formdev.flatlaf.util.SystemFileChooser;
import moda.passwordmanager.frontend.Utilities;
import moda.passwordmanager.interthreadcommunication.Event;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GoogleDrive extends Section {

    private JCheckBox googleDriveCheckbox;  // Enable or disable the Google Drive module
    private JButton synchronizeButton;  // Manual synchronization with Google Drive
    private JCheckBox automaticSynchronizationCheckbox;  // Enable or disable the automatic synchronization

    public GoogleDrive(InterThreadCommunication itc){
        super(Utilities.getLocaleString("Moda.GoogleDrive.panelTitle"), itc);

        initComponents();
        initListeners();
    }

    @Override
    public JPanel getPanel() {
        return this;
    }

    private void initComponents(){
        this.googleDriveCheckbox = new JCheckBox();

        this.synchronizeButton = new JButton();
        this.synchronizeButton.setText(Utilities.getLocaleString("Moda.GoogleDrive.synchronizeButton"));
        this.synchronizeButton.setMaximumSize(getButtonDimension());
        this.synchronizeButton.setEnabled(false);  // The sync is allowed only when Google Drive is enabled

        this.automaticSynchronizationCheckbox = new JCheckBox();

        // Set the initial configuration of the Google Drive module
        initGoogleDriveCheckbox();
        initAutomaticSynchronizationCheckbox();

        addOption(this.googleDriveCheckbox);
        addOption(this.synchronizeButton);
        addOption(this.automaticSynchronizationCheckbox);
    }

    private void initListeners(){
        // Let the user choose its credential.json file for the authentication, then send an Event to the backend
        this.googleDriveCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (googleDriveCheckbox.isSelected()){
                    // Create the File Chooser to allow user to select the credentials.json file
                    SystemFileChooser fileChooser = new SystemFileChooser();
                    fileChooser.setDialogTitle(Utilities.getLocaleString("Moda.GoogleDrive.googleDriveCheckboxFileChooserTitle"));
                    fileChooser.setAcceptAllFileFilterUsed(false);  // Don't accept all the types of files

                    // Create the filter to choose only .modb files
                    SystemFileChooser.FileNameExtensionFilter filter = new SystemFileChooser.FileNameExtensionFilter(
                            Utilities.getLocaleString("Moda.GoogleDrive.googleDriveCheckboxFileChooserFilter") + " (.json)",
                            "json");
                    fileChooser.setFileFilter(filter);

                    // Open the file chooser
                    if (fileChooser.showOpenDialog(getPanel()) == SystemFileChooser.APPROVE_OPTION){
                        String path = fileChooser.getSelectedFile().getAbsolutePath();  // Retrieve the path chosen

                        // Check whether the database has been chosen
                        if (!path.isBlank()){
                            enableGoogleDrive(path);
                        }else{
                            // The operation was aborted or failed, so we deselect the checkbox
                            googleDriveCheckbox.setSelected(false);
                        }
                    }else{
                        // The operation was aborted or failed, so we deselect the checkbox
                        googleDriveCheckbox.setSelected(false);
                    }
                }else{
                    disableGoogleDrive();
                }
                updatePreferences();
            }
        });

        this.synchronizeButton.addActionListener(e -> synchronizeGoogleDrive());

        this.automaticSynchronizationCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (automaticSynchronizationCheckbox.isSelected()){
                    enableAutomaticSynchronization();
                }else{
                    disableAutomaticSynchronization();
                }
                updatePreferences();
            }
        });
    }

    /**
     * Retrieve from the settings file the current configuration of Google Drive to set the initial state of the
     * checkbox
     */
    private void initGoogleDriveCheckbox(){
        // Retrieve from the settings file the configuration of Google Drive visibility
        Event event = new Event("get-google-drive");
        Event response = getITC().request(event);
        boolean enabled = (boolean) response.getData().getFirst();

        if (enabled){
            this.googleDriveCheckbox.setText(Utilities.getLocaleString("Moda.GoogleDrive.googleDriveCheckboxEnabled"));
            this.googleDriveCheckbox.setSelected(true);
            this.synchronizeButton.setEnabled(true);
            this.automaticSynchronizationCheckbox.setEnabled(true);
        }else{
            this.googleDriveCheckbox.setText(Utilities.getLocaleString("Moda.GoogleDrive.googleDriveCheckboxDisabled"));
            this.googleDriveCheckbox.setSelected(false);
            this.synchronizeButton.setEnabled(false);
            this.automaticSynchronizationCheckbox.setEnabled(false);  // Is allowed only when Google Drive is enabled
        }
    }

    /**
     * Update all the preferences based on the checkboxes enabled
     */
    private void updatePreferences(){
        if (this.googleDriveCheckbox.isSelected()){
            this.googleDriveCheckbox.setText(Utilities.getLocaleString("Moda.GoogleDrive.googleDriveCheckboxEnabled"));
            this.synchronizeButton.setEnabled(true);
            this.automaticSynchronizationCheckbox.setEnabled(true);
        }else{
            this.googleDriveCheckbox.setText(Utilities.getLocaleString("Moda.GoogleDrive.googleDriveCheckboxDisabled"));
            this.synchronizeButton.setEnabled(false);
            this.automaticSynchronizationCheckbox.setEnabled(false);
        }

        if (this.automaticSynchronizationCheckbox.isSelected()){
            this.automaticSynchronizationCheckbox.setText(Utilities.getLocaleString("Moda.GoogleDrive.automaticSynchronizationCheckboxEnabled"));
        }else{
            this.automaticSynchronizationCheckbox.setText(Utilities.getLocaleString("Moda.GoogleDrive.automaticSynchronizationCheckboxDisabled"));
        }
    }

    /**
     * Retrieve from the settings file the current configuration of Google Drive synchronization to set the visibilities
     * of the buttons that enable and disable it
     */
    private void initAutomaticSynchronizationCheckbox(){
        // Retrieve from the settings file the configuration of Google Drive synchronization visibility
        Event event = new Event("get-google-drive-automatic-synchronization");
        Event response = getITC().request(event);
        boolean enabled = (boolean) response.getData().getFirst();

        if (enabled){
            this.automaticSynchronizationCheckbox.setText(Utilities.getLocaleString("Moda.GoogleDrive.automaticSynchronizationCheckboxEnabled"));
            this.automaticSynchronizationCheckbox.setSelected(true);
        }else{
            this.automaticSynchronizationCheckbox.setText(Utilities.getLocaleString("Moda.GoogleDrive.automaticSynchronizationCheckboxDisabled"));
            this.automaticSynchronizationCheckbox.setSelected(false);
        }
    }

    /**
     * Enable the Google Drive synchronization
     * @param credentialsPath The path of the OAuth credentials used for the authentication
     */
    private void enableGoogleDrive(String credentialsPath){
        Event event = new Event("set-google-drive");
        event.addData(true);  // It allows the authentication
        event.addData(credentialsPath);
        getITC().request(event);
    }

    /**
     * Disable the Google Drive synchronization
     */
    private void disableGoogleDrive(){
        Event event = new Event("set-google-drive", false);
        getITC().request(event);  // Wait for the end of operations before disabling the button
    }

    /**
     * Perform a synchronization with Google Drive
     */
    private void synchronizeGoogleDrive(){
        Event event = new Event("google-drive-synchronize");
        getITC().request(event);
    }

    /**
     * Enable the automatic synchronization of Google Drive
     */
    private void enableAutomaticSynchronization(){
        Event event = new Event("set-google-drive-automatic-synchronization", true);
        getITC().request(event);
    }

    /**
     * Disable the automatic synchronization of Google Drive
     */
    private void disableAutomaticSynchronization(){
        Event event = new Event("set-google-drive-automatic-synchronization", true);
        getITC().request(event);
    }

}
