package moda.passwordmanager.frontend.panels.settings;

import moda.passwordmanager.frontend.Utilities;
import moda.passwordmanager.interthreadcommunication.Event;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;

import javax.swing.*;
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
        JPanel databaseInUsePanel = new JPanel();
        JLabel databaseInUseLabel = new JLabel();
        databaseInUseLabel.setText("Database in use: ");

        this.databasePathTextField = new JTextField();
        this.databasePathTextField.setText(getCurrentDatabasePath());
        this.databasePathTextField.setMaximumSize(getTextFieldDimension());
        this.databasePathTextField.setEditable(false);

        databaseInUsePanel.add(databaseInUseLabel);
        databaseInUsePanel.add(this.databasePathTextField);

        JPanel databaseOperations = new JPanel();

        this.newDatabaseButton = new JButton();
        this.newDatabaseButton.setText("New database");
        this.newDatabaseButton.setMaximumSize(getButtonDimension());

        this.changeDatabaseButton = new JButton();
        this.changeDatabaseButton.setText("Change database");
        this.changeDatabaseButton.setMaximumSize(getButtonDimension());

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
                String path = Utilities.newDatabaseFileChooser();

                // Check whether the database has been chosen
                if (!path.isEmpty()){
                    // If the file has been saved without setting the extension, set it automatically
                    if (!path.endsWith(".modb")){
                        path = path+".modb";
                    }
                    setNewDatabase(path);
                }
            }
        });

        this.changeDatabaseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String path = Utilities.openDatabaseFileChooser();

                // Check whether the database has been chosen
                if (!path.isEmpty()){
                    setNewDatabase(path);
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
        if (!Utilities.checkMasterPassword(masterPassword.toCharArray())){
            return;
        }

        Event event = new Event("change-master-password", masterPassword.toCharArray());
        getITC().request(event);  // Wait for the end of the operations in the backend
    }

    /**
     * Get the current database from the backend
     * @return
     */
    private String getCurrentDatabasePath(){
        Event response = getITC().request(new Event("get-database"));
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
        getITC().request(event);
        this.databasePathTextField.setText(databasePath);  // Set the new path of the database into the Text Field
        changeMasterPassword(masterPassword);
    }
}
