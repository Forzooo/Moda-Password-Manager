package moda.passwordmanager.frontend.panels.settings;

import moda.passwordmanager.frontend.dialogs.Startup;
import moda.passwordmanager.interthreadcommunication.Event;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Database extends Section {

    private JTextField databasePathTextField;
    private JButton newDatabaseButton;
    private JButton changeDatabaseButton;

    public Database(InterThreadCommunication itc){
        super("Database", itc);

        initComponents();
        initListeners();
    }

    @Override
    public JPanel getPanel() {
        return this;
    }

    private void initComponents(){
        Dimension textFieldDimension = new Dimension(1600, 30);  // Define the dimension of any JTextField
        Dimension buttonDimension = new Dimension(250, 20);  // Define the dimension of any JButton

        JPanel databaseInUsePanel = new JPanel();
        JLabel databaseInUseLabel = new JLabel();
        databaseInUseLabel.setText("Database in use: ");

        this.databasePathTextField = new JTextField();
        this.databasePathTextField.setText(getCurrentDatabasePath());
        this.databasePathTextField.setMaximumSize(textFieldDimension);
        this.databasePathTextField.setEditable(false);

        databaseInUsePanel.add(databaseInUseLabel);
        databaseInUsePanel.add(this.databasePathTextField);

        JPanel databaseOperations = new JPanel();

        this.newDatabaseButton = new JButton();
        this.newDatabaseButton.setText("New database");
        this.newDatabaseButton.setMaximumSize(buttonDimension);

        this.changeDatabaseButton = new JButton();
        this.changeDatabaseButton.setText("Change database");
        this.changeDatabaseButton.setMaximumSize(buttonDimension);

        databaseOperations.add(this.newDatabaseButton);
        databaseOperations.add(this.changeDatabaseButton);

        addOption(databaseInUsePanel);
        addOption(databaseOperations);
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
    }

    /**
     * Change the current master password of the database to a new one
     * @param masterPassword The new master password
     */
    private void changeMasterPassword(String masterPassword){
        // Perform some initial conditions check on the master password
        if (!Startup.checkMasterPassword(masterPassword.toCharArray())){
            return;
        }

        Event event = new Event("change-master-password", masterPassword.toCharArray());
        this.ITC.request(event);  // Wait for the end of the operations in the backend
    }

    /**
     * Get the current database from the backend
     * @return
     */
    private String getCurrentDatabasePath(){
        Event response = this.ITC.request(new moda.passwordmanager.interthreadcommunication.Event("get-database"));
        String databasePath = (String) response.getData().getFirst();  // Retrieve the path of the database
        return databasePath;
    }

    /**
     * Set the new database to the backend
     * @param databasePath The path of the new database
     */
    private void setNewDatabase(String databasePath){
        // Ask the user for the master password of the database before using it
        String masterPassword = JOptionPane.showInputDialog(getRootPane(), "Enter the master password","");

        // Initial checks on the master password entered to ensure that it is a valid string, otherwise abort the
        // operation
        if (masterPassword == null || masterPassword.isBlank()){
            return;
        }

        // Set the path of the database
        Event event = new Event("set-database", databasePath);
        this.ITC.request(event);
        this.databasePathTextField.setText(databasePath);  // Set the new path of the database into the Text Field
        changeMasterPassword(masterPassword);
    }


}
