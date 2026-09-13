package moda.passwordmanager.frontend.dialogs;

import moda.passwordmanager.Application;
import moda.passwordmanager.frontend.Utilities;
import moda.passwordmanager.interthreadcommunication.Event;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class Startup extends JDialog {

    private final InterThreadCommunication itc;
    private static final Dimension DIALOG_DIMENSION = new Dimension(475, 550);

    // The number of recent databases to show in the recentDatabasesList before a JScrollPane appears
    private static final int RECENT_DATABASES_VISIBLE = 5;

    private JPasswordField masterPasswordPasswordField;
    private JButton loginButton;
    private JLabel currentDatabaseLabel;
    private JButton newDatabaseButton;
    private JButton changeDatabaseButton;

    // The recentDatabases ArrayList is used to retrieve an absolute path from a formatted one
    private ArrayList<String> recentDatabases;
    private JList<String> recentDatabasesList;

    // The last databases used are set as a class attribute because if the user sets a new database, the model has to
    // be updated
    private DefaultListModel<String> recentDatabasesModel;

    public Startup(InterThreadCommunication itc){
        super();
        this.itc = itc;

        initDialog();
        initComponents();
        getRecentDatabases();  // Update the model with the recent databases
        initListeners();
    }

    /**
     * Set the configuration of the Dialog
     */
    private void initDialog(){
        setTitle(Application.getApplicationTitle());
        setIconImage(Application.getIcon());
        setSize(DIALOG_DIMENSION);

        setLayout(new MigLayout("fillx, align center"));

        // Enable modality to block other password manager windows until this one is disposed
        setModalityType(ModalityType.DOCUMENT_MODAL);
        setLocationRelativeTo(getRootPane());
        setAlwaysOnTop(true);
        setResizable(false);
    }

    /**
     * Initialize and add all the Swing components of the Dialog
     */
    private void initComponents(){
        JLabel title = new JLabel();  // Create the JLabel that displays the name of the Password Manager
        title.setText("MODA");
        title.setFont(new Font(UIManager.getString("Moda.GeneralUseFontFamily"), Font.PLAIN, 80));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel subtitle = new JLabel();
        subtitle.setText("Password Manager");
        subtitle.setFont(new Font("Arial Bold", Font.PLAIN, 32));
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel loginLabel = new JLabel();
        loginLabel.setText(Utilities.getLocaleString("Moda.Startup.loginLabel"));

        this.masterPasswordPasswordField = new JPasswordField(20);

        this.loginButton = new JButton();
        this.loginButton.setText(Utilities.getLocaleString("Moda.Startup.loginButton"));

        this.recentDatabasesList = new JList<>();

        // A maximum number of visible rows are set otherwise the JList would continue endlessly, beyond the dialog size
        this.recentDatabasesList.setVisibleRowCount(RECENT_DATABASES_VISIBLE);
        this.recentDatabasesModel = new DefaultListModel<>();
        this.recentDatabasesList.setModel(this.recentDatabasesModel);

        // Add a scrollbar to the JList and add it to the panel
        JScrollPane databaseScrollPane = new JScrollPane(this.recentDatabasesList);

        // Database operations panel
        this.currentDatabaseLabel = new JLabel();
        this.currentDatabaseLabel.setText(Utilities.getLocaleString("Moda.Startup.currentDatabaseLabel") + " " +
                Utilities.getFilenameFromPath(getCurrentDatabase()));

        this.newDatabaseButton = new JButton();
        this.newDatabaseButton.setText(Utilities.getLocaleString("Moda.Startup.newDatabaseButton"));

        this.changeDatabaseButton = new JButton();
        this.changeDatabaseButton.setText(Utilities.getLocaleString("Moda.Startup.changeDatabaseButton"));

        add(title, "span, align center, wrap -25");  // wrap -25 allows the subtitle to be closer to the title
        add(subtitle, "span, align center, sg 1, wrap");
        add(loginLabel, "span, align center, gaptop 25, wrap");
        add(this.masterPasswordPasswordField, "split 2, align center");
        add(this.loginButton, "wrap");
        add(this.currentDatabaseLabel, "span, align center, gaptop 60, wrap");
        add(databaseScrollPane, "align center, sg 1, wrap");
        add(this.newDatabaseButton, "span, split 2, align center");
        add(this.changeDatabaseButton);
    }

    /**
     * Initialize all the listeners on the components
     */
    private void initListeners() {
        // If the user closes the dialog himself, then close the application
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);  // Stop the process
            }
        });

        // Action Listener for the 'Enter' key pressed
        this.masterPasswordPasswordField.addActionListener(e -> {
            char[] masterPasswordChar = masterPasswordPasswordField.getPassword();
            if (Utilities.checkMasterPassword(masterPasswordChar)) {
                sendMasterPassword(masterPasswordPasswordField.getPassword());
                dispose();  // Closes the dialog
            }
        });

        this.loginButton.addActionListener(e -> {
            char[] masterPasswordChar = masterPasswordPasswordField.getPassword();
            if (Utilities.checkMasterPassword(masterPasswordChar)) {
                sendMasterPassword(masterPasswordChar);
                dispose();  // Closes the dialog
            }
        });

        this.recentDatabasesList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                // Only allow double clicks
                if (e.getClickCount() == 2){
                    setDatabase(recentDatabases.get(recentDatabasesList.getSelectedIndex()));
                }
            }
        });

        this.newDatabaseButton.addActionListener(e -> {
            String path = Utilities.newDatabaseFileChooser();

            // Check if the user has created a database
            if (!path.isBlank()){
                setDatabase(path);
            }
        });

        this.changeDatabaseButton.addActionListener(e -> {
            String path = Utilities.openDatabaseFileChooser();

            // Check if the user has selected a database
            if (!path.isBlank()){
                setDatabase(path);
            }
        });
    }

    /**
     * Send the master password the user has entered to the backend thread
     * @param masterPassword The master password the user has entered
     */
    private void sendMasterPassword(char[] masterPassword){
        // Create and send the event to the backend to set the master password
        Event setMasterPassword = new Event("set-master-password", masterPassword);

        // Get the event from the backend to know whether the master password the user entered is correct
        Event confirmEvent = this.itc.request(setMasterPassword);
        boolean masterPasswordFlag = (Boolean) confirmEvent.getData().getFirst();

        // Show an Error message and terminate the execution if the master password entered is wrong
        if (!masterPasswordFlag){
            JOptionPane.showMessageDialog(this, Utilities.getLocaleString("Moda.Startup.setMasterPasswordErrorMessage"),
                    Utilities.getLocaleString("Moda.Startup.setMasterPasswordErrorTitle"), JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    /**
     * Get the path of the database in use
     */
    private String getCurrentDatabase(){
        // Receive the path of the database from the backend
        Event databasePathEvent = this.itc.request(new Event("get-database"));

        return (String) databasePathEvent.getData().getFirst();
    }

    /**
     * Format an absolute path by showing only the drive and the last two subdirectories and the file
     */
    private String formatPath(String path){
        StringBuilder formattedPath = new StringBuilder();
        String[] splitPath = path.split("\\\\");

        if (splitPath.length <= 3){
            return path;
        }

        formattedPath.append(splitPath[0]).append("\\...\\");  // Append the drive to the path

        for (int i = splitPath.length-3; i < splitPath.length-1; i++){
            formattedPath.append(splitPath[i]).append("\\");
        }

        formattedPath.append(splitPath[splitPath.length-1]);  // We have to add the filename and extension outside of
                                                              // for loop as it adds a backslash for each iteration

        return formattedPath.toString();
    }

    /**
     * Change the current database in use
     * @param databasePath The path of the database to use
     */
    private void setDatabase(String databasePath){
        this.itc.request(new Event("set-database", databasePath));  // Wait for the operations to finish before
                                                                             // setting the path in the label
        this.currentDatabaseLabel.setText(formatPath(databasePath));  // Set the new path of the database into the label
        getRecentDatabases();  // Update the recent databases list
    }

    /**
     * Get the last databases used by the user
     */
    private void getRecentDatabases(){
        Event getRecentDatabasesEvent = this.itc.request(new Event("get-recent-databases"));

        this.recentDatabases = (ArrayList<String>) getRecentDatabasesEvent.getData().getFirst();

        this.recentDatabasesModel.clear();  // Clear the model before adding all the elements
        for (String path : this.recentDatabases){
            this.recentDatabasesModel.addElement(formatPath(path));
        }
    }
}
