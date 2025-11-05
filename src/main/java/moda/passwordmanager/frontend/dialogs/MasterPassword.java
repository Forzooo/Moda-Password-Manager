package moda.passwordmanager.frontend.dialogs;

import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;
import moda.passwordmanager.interthreadcommunication.Event;
import moda.passwordmanager.frontend.Frontend;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * A JDialog used to retrieve the parameters of the generation of the password
 */
public class MasterPassword extends JDialog {

    private InterThreadCommunication itc;

    // Dialog components
    private JPasswordField masterPassword;
    private JButton sendButton;
    private JLabel currentDatabaseLabel;
    private JButton changeDatabaseButton;

    public MasterPassword(InterThreadCommunication itc){
        super();

        this.itc = itc;
        initDialog();
        initLoginComponents();
        initDatabaseInformationComponents();
        initListeners();
    }

    /**
     * Initialize the Master Password Dialog with a fixed database: it cannot be changed
     * @param databaseToUse Path of the database to use
     */
    public MasterPassword(InterThreadCommunication itc, String databaseToUse){
        super();

        this.itc = itc;
        initDialog();
        initLoginComponents();
        initDatabaseInformationComponents(databaseToUse);
        initListeners();
    }

    /**
     * Get the layout used for the Dialog
     *
     * @return BoxLayout
     */
    private BoxLayout getDialogLayout() {
        // The target is the Dialog so to use it we retrieve it from the content pane otherwise we would use the
        // entire container including other JPanels
        return new BoxLayout(getContentPane(), BoxLayout.Y_AXIS);
    }

    /**
     * Set the configuration of the Dialog
     */
    private void initDialog(){
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);  // We handle on our own how the dialog closes
        setLayout(getDialogLayout());  // Set its layout

        setTitle("MODA - Password Manager");
        setModal(true);

        // Set the preferred size
        setSize(new Dimension(700, 150));
        setLocationRelativeTo(getParent());  // The dialog is shown at the center of the window
        setAlwaysOnTop(true);

        setIconImage(Frontend.getIcon());
    }

    /**
     * Initialize and add all the Swing components of the Dialog
     */
    private void initLoginComponents() {
        // The panel that contains the master password login
        JPanel masterPasswordPanel = new JPanel();
        masterPasswordPanel.setLayout(new FlowLayout());

        this.masterPassword = new JPasswordField();
        this.masterPassword.setPreferredSize(new Dimension(200, 25));

        this.sendButton = new JButton();
        this.sendButton.setText("Log In");

        masterPasswordPanel.add(this.masterPassword);
        masterPasswordPanel.add(this.sendButton);

        add(masterPasswordPanel);
    }

    /**
     * Initialize the database information components
     */
    private void initDatabaseInformationComponents(){
        // The panel that contains the information about the database
        JPanel databaseInformationPanel = new JPanel();
        databaseInformationPanel.setLayout(new FlowLayout());

        this.currentDatabaseLabel = new JLabel();
        this.currentDatabaseLabel.setText("Current database: " + getCurrentDatabase());

        this.changeDatabaseButton = new JButton();
        this.changeDatabaseButton.setText("Change database");

        databaseInformationPanel.add(this.currentDatabaseLabel);
        databaseInformationPanel.add(this.changeDatabaseButton);
        add(databaseInformationPanel);
    }

    /**
     * Initialize the database information components using a fixed database
     * @param databaseToUse The path of the database
     */
    private void initDatabaseInformationComponents(String databaseToUse){
        // The panel that contains the information about the database
        JPanel databaseInformationPanel = new JPanel();
        databaseInformationPanel.setLayout(new FlowLayout());

        this.currentDatabaseLabel = new JLabel();
        this.currentDatabaseLabel.setText("Current database: " + databaseToUse);

        // The button is only initialized, without any attribute set, only for consistency
        this.changeDatabaseButton = new JButton();

        databaseInformationPanel.add(this.currentDatabaseLabel);
        add(databaseInformationPanel);
    }

    /**
     * Initialize all the listeners on the components
     */
    private void initListeners(){
        // If the user closes the dialog himself, then close the application
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);  // Stop the process
            }
        });

        // Action Listener for the 'Enter' key pressed
        this.masterPassword.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                char[] masterPasswordChar = masterPassword.getPassword();
                if (!checkMasterPassword(masterPasswordChar)){
                    return;
                }
                sendMasterPassword(masterPassword.getPassword());
                dispose();  // Close the window
            }
        });

        this.sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                char[] masterPasswordChar = masterPassword.getPassword();
                if (!checkMasterPassword(masterPasswordChar)){
                    return;
                }
                sendMasterPassword(masterPasswordChar);
                dispose();  // Close the window
            }
        });

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
        this.currentDatabaseLabel.setText(databasePath);  // Set the new path of the database into the label
    }

}
