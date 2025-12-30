package moda.passwordmanager.frontend.dialogs;

import com.formdev.flatlaf.util.SystemFileChooser;
import moda.passwordmanager.Application;
import moda.passwordmanager.interthreadcommunication.Event;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Startup extends JDialog {

    private InterThreadCommunication itc;
    private final static Dimension DIALOG_DIMENSION = new Dimension(500, 600);

    // The number of recent databases to show in the recentDatabasesList before a JScrollPane appears
    private final static int RECENT_DATABASES_VISIBLE = 5;

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
        initListeners();
    }

    /**
     * Set the configuration of the Dialog
     */
    private void initDialog(){
        setTitle(Application.getApplicationTitle());
        setIconImage(Application.getIcon());
        setSize(DIALOG_DIMENSION);

        setLayout(new MigLayout());

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
        title.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 80));
        title.setHorizontalAlignment(JLabel.CENTER);

        JLabel subtitle = new JLabel();
        subtitle.setText("Password Manager");
        subtitle.setFont(new Font("Arial Bold", Font.PLAIN, 32));
        subtitle.setHorizontalAlignment(JLabel.CENTER);

        JLabel loginLabel = new JLabel();
        loginLabel.setText("Enter your master password:");

        this.masterPasswordPasswordField = new JPasswordField(20);

        this.loginButton = new JButton();
        this.loginButton.setText("Log In");

        this.recentDatabasesList = new JList<>();

        // A maximum number of visible rows are set otherwise the JList would continue endlessly, beyond the dialog size
        this.recentDatabasesList.setVisibleRowCount(RECENT_DATABASES_VISIBLE);
        this.recentDatabasesModel = new DefaultListModel<>();
        this.recentDatabasesList.setModel(this.recentDatabasesModel);

        getRecentDatabases();  // Update the model with the recent databases

        // Add a scrollbar to the JList and add it to the panel
        JScrollPane databaseScrollPane = new JScrollPane(this.recentDatabasesList);

        // Database operations panel
        this.currentDatabaseLabel = new JLabel();
        this.currentDatabaseLabel.setText("Current database: " + fileNameFromPath(getCurrentDatabase()));

        this.newDatabaseButton = new JButton();
        this.newDatabaseButton.setText("New database");

        this.changeDatabaseButton = new JButton();
        this.changeDatabaseButton.setText("Change database");

        add(title, "span, align center, wrap");
        add(subtitle, "span, align center, sg 1, wrap");
        add(loginLabel, "span, center, gaptop 25, wrap");
        add(this.masterPasswordPasswordField, "split 2, align center");
        add(this.loginButton, "wrap");
        add(databaseScrollPane, "span, gaptop 30, grow, push, sg 1, wrap");
        add(this.currentDatabaseLabel, "span, align center, wrap");
        add(this.newDatabaseButton, "split 2, align center");
        add(this.changeDatabaseButton);
    }

    private String fileNameFromPath(String path){

        Path filePath = Paths.get(path);

        return filePath.getFileName().toString();

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
        this.masterPasswordPasswordField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                char[] masterPasswordChar = masterPasswordPasswordField.getPassword();
                if (!checkMasterPassword(masterPasswordChar)) {
                    return;
                }
                sendMasterPassword(masterPasswordPasswordField.getPassword());
                dispose();  // Close the window
            }
        });

        this.loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                char[] masterPasswordChar = masterPasswordPasswordField.getPassword();
                if (!checkMasterPassword(masterPasswordChar)) {
                    return;
                }
                sendMasterPassword(masterPasswordChar);
                dispose();  // Close the window
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

        this.newDatabaseButton.addActionListener(e -> createDatabase());

        this.changeDatabaseButton.addActionListener(e -> openDatabaseChooser());
    }

    /**
     * Perform some initial check on the master password to allow only ones that comply with all the requirements
     * @param masterPassword The master password the user entered
     * @return Boolean to indicate whether the checks have been passed
     */
    public static boolean checkMasterPassword(char[] masterPassword){
        if (masterPassword.length == 0){
            return false;
        }

        return true;
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
            JOptionPane.showMessageDialog(this, "The master password entered is wrong.",
                    "Login Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    /**
     * Get the path of the database in use
     * @return Database in unse
     */
    private String getCurrentDatabase(){
        // Create the event and send it to the backend
        Event event = new Event("get-database");

        // Receive the path of the database from the backend
        Event databasePathEvent = this.itc.request(event);
        String databasePath = (String) databasePathEvent.getData().getFirst();

        return databasePath;
    }

    /**
     * Format an absolute path by showing only the drive and the last two subdirectories and the file
     */
    private String formatPath(String path){
        StringBuilder formattedPath = new StringBuilder();
        String[] splittedPath = path.split("\\\\");

        if (splittedPath.length <= 3){
            return path;
        }

        formattedPath.append(splittedPath[0]).append("\\...\\");  // Append the drive to the path

        for (int i = splittedPath.length-3; i < splittedPath.length-1; i++){
            formattedPath.append(splittedPath[i]).append("\\");
        }
        formattedPath.append(splittedPath[splittedPath.length-1]);

        return formattedPath.toString();
    }

    /**
     * Create a new database file
     */
    private void createDatabase(){
        // Create the File Chooser that opens in the desktop, and saves a .modb file
        SystemFileChooser fileChooser = new SystemFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        fileChooser.setDialogTitle("Create a new database to use");
        fileChooser.setAcceptAllFileFilterUsed(false);  // Don't accept all the types of files

        // Create the filter to save only .modb files
        SystemFileChooser.FileNameExtensionFilter filter = new SystemFileChooser.FileNameExtensionFilter(
                "Moda Password Manager Database","modb");
        fileChooser.setFileFilter(filter);

        // Open the file chooser
        if (fileChooser.showSaveDialog(getRootPane()) == SystemFileChooser.APPROVE_OPTION){
            String path = fileChooser.getSelectedFile().getAbsolutePath();  // Retrieve the path chosen

            // Check whether the database has been chosen
            if (!path.isEmpty()){
                // If the file has been saved without setting the extension, set it automatically
                if (!path.endsWith(".modb")){
                    path = path+".modb";
                }
                setDatabase(path);
            }
        }
    }

    /**
     * Open the Swing File Chooser and let the user select a database to use
     */
    private void openDatabaseChooser(){
        // Create the File Chooser that opens in the desktop view, and selects only .modb files
        SystemFileChooser fileChooser = new SystemFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        fileChooser.setDialogTitle("Choose a database to use");
        fileChooser.setAcceptAllFileFilterUsed(false);  // Don't accept all the types of files

        // Create the filter to choose only .modb files
        SystemFileChooser.FileNameExtensionFilter filter = new SystemFileChooser.FileNameExtensionFilter(
                "Moda Password Manager Database","modb");
        fileChooser.setFileFilter(filter);

        // Open the file chooser in the current dialog and check that the user has chosen a database file
        if (fileChooser.showOpenDialog((this)) == SystemFileChooser.APPROVE_OPTION){
            String path = fileChooser.getSelectedFile().getAbsolutePath();  // Retrieve the path chosen

            // Check whether the database has been chosen
            if (!path.isEmpty()){
                setDatabase(path);
            }
        }
    }

    /**
     * Change the current database in use
     * @param databasePath The path of the database to use
     */
    private void setDatabase(String databasePath){
        Event event = new Event("set-database", databasePath);
        this.itc.request(event);  // Wait for the operations to finish before setting the path in the label
        this.currentDatabaseLabel.setText(formatPath(databasePath));  // Set the new path of the database into the label
        getRecentDatabases();  // Update the recent databases list
    }

    /**
     * Get the last databases used by the user
     */
    private void getRecentDatabases(){
        Event event = new Event("get-recent-databases");
        Event response = this.itc.request(event);

        this.recentDatabases = (ArrayList<String>) response.getData().getFirst();

        this.recentDatabasesModel.clear();  // Clear the model before adding all the elements
        for (String path : this.recentDatabases){
            this.recentDatabasesModel.addElement(formatPath(path));
        }
    }
}
