package moda.passwordManager.frontend.dialogs;

import moda.passwordManager.communicationHandler.CommunicationHandler;
import moda.passwordManager.communicationHandler.Event;
import moda.passwordManager.frontend.components.Placeholder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * A JDialog used to retrieve the parameters of the generation of the password
 */
public class MasterPasswordDialog extends JDialog {

    private CommunicationHandler communicationHandler;

    // Dialog components
    private JPasswordField masterPassword;
    private JButton sendButton;

    public MasterPasswordDialog(CommunicationHandler communicationHandler){
        super();

        this.communicationHandler = communicationHandler;

        initDialog();
        initComponents();
        initActionListener();
    }

    /**
     * Get the layout used for the Dialog
     *
     * @return FlowLayout
     */
    private FlowLayout getDialogLayout() {
        // The target is the Dialog so to use it we retrieve it from the content pane otherwise we would use the
        // entire container including other JPanels
        return new FlowLayout();
    }

    /**
     * Set the configuration of the Dialog
     */
    private void initDialog(){
        setLayout(getDialogLayout());  // Set its layout

        setTitle("Enter the Master Password");
        setModal(true);

        // Set the preferred size
        setSize(new Dimension(300, 100));
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);

        setBackground(Color.WHITE);

        setIcon();
    }

    /**
     * Set the icon of the dialog
     */
    private void setIcon(){
        ImageIcon imageIcon = new ImageIcon(getClass().getResource("/icon.png"));  // Get the image from the resources
        setIconImage(imageIcon.getImage());  // Get the image from the ImageIcon and set it to the application
    }

    /**
     * Initialize and add all the Swing components of the Dialog
     */
    private void initComponents(){
        this.masterPassword = new JPasswordField();
        this.masterPassword.setPreferredSize(new Dimension(200, 25));

        this.sendButton = new JButton();
        this.sendButton.setText("Log In");

        add(this.masterPassword);
        add(this.sendButton);
    }

    /**
     * Initialize all the Action Listeners of the components
     */
    private void initActionListener(){
        // Action Listener for the 'Enter' key pressed
        this.masterPassword.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMasterPassword(masterPassword.getPassword());
                dispose();  // Close the window
            }
        });

        this.sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMasterPassword(masterPassword.getPassword());
                dispose();  // Close the window
            }
        });
    }

    /**
     * Send the master password the user has entered to the backend thread
     * @param masterPassword
     */
    private void sendMasterPassword(char[] masterPassword){
        // Create and send the event to the backend telling to set the master password
        ArrayList dataToSend = new ArrayList();  // The communication requires using an ArrayList for the data
        dataToSend.add(new String(masterPassword));  // Convert the char array to a string
        Event setMasterPassword = new Event("set-master-password", dataToSend);

        this.communicationHandler.send(setMasterPassword);

        // Wait for the confirm event and notify the user about it
        Event confirmEvent = this.communicationHandler.receive();
//        notifyUser();  // Example method to show the user a messagebox with the operation status
    }

}
