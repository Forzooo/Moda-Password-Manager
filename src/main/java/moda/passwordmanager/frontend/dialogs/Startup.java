package moda.passwordmanager.frontend.dialogs;

import moda.passwordmanager.Application;
import moda.passwordmanager.interthreadcommunication.Event;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Startup extends JDialog {

    private InterThreadCommunication itc;
    private final static Dimension DIALOG_DIMENSION = new Dimension(500, 600);

    private JPasswordField masterPasswordPasswordField;
    private JButton loginButton;
    private JLabel currentDatabaseLabel;
    private JButton newDatabaseButton;
    private JButton changeDatabaseButton;

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

        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        setModal(true);  // Enable modality to block input to other password manager windows
        setLocationRelativeTo(getRootPane());
//        setAlwaysOnTop(true);
    }

    /**
     * Initialize and add all the Swing components of the Dialog
     */
    private void initComponents(){
        int width = (int) DIALOG_DIMENSION.getWidth();
        int height = (int) DIALOG_DIMENSION.getHeight();
        JPanel rootPanel = new JPanel();  // We use a root panel as it has a better layout than the JDialog itself

        // Title section
        JPanel titlePanel = new JPanel();
        titlePanel.setPreferredSize(new Dimension(width, height/4));

        JLabel title = new JLabel();  // Create the JLabel that displays the name of the Password Manager
        title.setText("MODA");
        title.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 80));

        JLabel subtitle = new JLabel();
        subtitle.setText("Password Manager");
        subtitle.setFont(new Font("Arial Bold", Font.PLAIN, 32));

        titlePanel.add(title);
        titlePanel.add(subtitle);

        // The master password panel
        JPanel masterPasswordPanel = new JPanel();
        masterPasswordPanel.setLayout(new BoxLayout(masterPasswordPanel, BoxLayout.Y_AXIS));
        masterPasswordPanel.setPreferredSize(new Dimension(width, height/2));

        JLabel loginLabel = new JLabel();
        loginLabel.setText("Enter your master password:");

        JPanel masterPasswordFieldPanel = new JPanel();

        this.masterPasswordPasswordField = new JPasswordField();
        this.masterPasswordPasswordField.setPreferredSize(new Dimension(200, 25));
        this.masterPasswordPasswordField.setMaximumSize(new Dimension(200, 25));

        this.loginButton = new JButton();
        this.loginButton.setText("Log In");

        masterPasswordFieldPanel.add(this.masterPasswordPasswordField);
        masterPasswordFieldPanel.add(this.loginButton);

        masterPasswordPanel.add(Box.createRigidArea(new Dimension(0, 50)));
        masterPasswordPanel.add(loginLabel);
        masterPasswordPanel.add(masterPasswordFieldPanel);

        // Database section
        JPanel databaseInfoPanel = new JPanel();
        databaseInfoPanel.setPreferredSize(new Dimension(width, height/4));

        this.currentDatabaseLabel = new JLabel();
        this.currentDatabaseLabel.setText("Current database: " + formatPath(getCurrentDatabase()));

        // Database operations panel
        JPanel databaseOperationsPanel = new JPanel();
        this.newDatabaseButton = new JButton();
        this.newDatabaseButton.setText("New database");

        this.changeDatabaseButton = new JButton();
        this.changeDatabaseButton.setText("Change database");

        databaseOperationsPanel.add(newDatabaseButton);
        databaseOperationsPanel.add(changeDatabaseButton);

        databaseInfoPanel.add(this.currentDatabaseLabel);
        databaseInfoPanel.add(databaseOperationsPanel);

        // Add all the sections to the root panel
        rootPanel.add(titlePanel);
        rootPanel.add(masterPasswordPanel);
        rootPanel.add(databaseInfoPanel);

        add(rootPanel);
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
        Event confirmEvent = this.itc.requestAndReceive(setMasterPassword);
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
        Event databasePathEvent = this.itc.requestAndReceive(event);
        String databasePath = (String) databasePathEvent.getData().getFirst();

        return databasePath;
    }

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
        JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        fileChooser.setDialogTitle("Create a new database to use");
        fileChooser.setAcceptAllFileFilterUsed(false);  // Don't accept all the types of files

        // Create the filter to save only .modb files
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Moda Password Manager Database (.modb)",
                ".modb");
        fileChooser.setFileFilter(filter);

        // Open the file chooser
        if (fileChooser.showSaveDialog(getRootPane()) == JFileChooser.APPROVE_OPTION){
            String path = fileChooser.getSelectedFile().getAbsolutePath();  // Retrieve the path chosen

            // Check whether the database has been chosen
            if (!path.isEmpty()){
                // If the file has been saved without setting the extension, set it automatically
                if (!path.endsWith(".modb")){
                    path = path+".modb";
                }
                changeDatabase(path);
            }
        }
    }

    /**
     * Open the Swing File Chooser and let the user select a database to use
     */
    private void openDatabaseChooser(){
        // Create the File Chooser that opens in the desktop view, and selects only .modb files
        JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        fileChooser.setDialogTitle("Choose a database to use");
        fileChooser.setAcceptAllFileFilterUsed(false);  // Don't accept all the types of files

        // Create the filter to choose only .modb files
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Moda Password Manager Database (.modb)",
                "modb");
        fileChooser.setFileFilter(filter);

        // Open the file chooser in the current dialog and check that the user has chosen a database file
        if (fileChooser.showOpenDialog((this)) == JFileChooser.APPROVE_OPTION){
            String path = fileChooser.getSelectedFile().getAbsolutePath();  // Retrieve the path chosen

            // Check whether the database has been chosen
            if (!path.isEmpty()){
                changeDatabase(path);
            }
        }
    }

    /**
     * Change the current database in use
     * @param databasePath The path of the database to use
     */
    private void changeDatabase(String databasePath){
        Event event = new Event("set-database", databasePath);
        this.itc.requestAndReceive(event);
        this.currentDatabaseLabel.setText(formatPath(databasePath));  // Set the new path of the database into the label
    }
}
