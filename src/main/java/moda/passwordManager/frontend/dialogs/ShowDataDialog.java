package moda.passwordManager.frontend.dialogs;

import moda.passwordManager.backend.Data;
import moda.passwordManager.communicationHandler.CommunicationHandler;
import moda.passwordManager.communicationHandler.Event;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * A JDialog that shows the all the information related to an ID selected by the user.
 */
public class ShowDataDialog extends JDialog {

    // Attribute to communicate with the backend
    private CommunicationHandler communicationHandler;

    private Data data;  // The user data retried from the backend
    private List<JTextField> dataFields;  // The data field contain the user data
    private List<JButton> copyButtons;  // The copy buttons for each data field

    // Swing Components
    private JButton modifyButton;
    private JButton saveChangesButton;
    private JButton deleteButton;

    public ShowDataDialog(CommunicationHandler communicationHandler, Data data) {
        super();  // Initialize the Panel

        this.communicationHandler = communicationHandler;
        this.data = data;

        initDialog();
        initComponents();
        initListeners();
    }

    /**
     * Set the configuration of the Dialog
     */
    private void initDialog(){
        setSize(new Dimension(600, 400));
        setResizable(false);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
    }

    /**
     * Get the panel of this class as some action listener require it
     * @return JPanel of the class
     */
    private JDialog getDialog(){
        return this;
    }

    /**
     * Initialize the components of the panel
     */
    private void initComponents(){

        // A panel is used instead of the dialog because it is more customizable
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));  // Set its layout
        panel.setBorder(new EmptyBorder(20,20,20,20));

        // Create all the JButton
        this.modifyButton = new JButton("Modify");
        this.saveChangesButton = new JButton("Save Changes");
        this.saveChangesButton.setVisible(false);  // It's shown only when modifyButton is clicked
        this.deleteButton = new JButton("Delete");

        // Add dynamically all the data fields
        this.dataFields = new ArrayList<>();  // All the data fields are stored here
        this.copyButtons = new ArrayList<>();  // All the copy buttons are stored here

        for (String dataField : this.data.getFullUserData()){
            // Create the JTextField for each field
            JTextField dataTextField = new JTextField();
            dataTextField.setText(dataField);
            dataTextField.setEditable(false);  // The user cannot modify the data unless "Modify" is clicked
            this.dataFields.add(dataTextField);

            // Create the JButton to copy the data field
            JButton copyDataButton = new JButton();
            copyDataButton.setText("❏");
            copyDataButton.setPreferredSize(new Dimension(50, 50));
            copyDataButton.setMaximumSize(new Dimension(50, 50));
            copyDataButton.setMinimumSize(new Dimension(50, 50));
            copyDataButton.addActionListener(getCopyDataActionListener(dataField));
            this.copyButtons.add(copyDataButton);

            // Add the JTextField and the JButton to the panel
            JPanel rowPanel = new JPanel(new BorderLayout(5, 0));
            rowPanel.setBorder(new EmptyBorder(5, 0, 5, 0));  // Padding between rows
            rowPanel.add(dataTextField, BorderLayout.CENTER);
            rowPanel.add(copyDataButton, BorderLayout.EAST);
            panel.add(rowPanel);
        }

        // A panel for the buttons
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.add(this.modifyButton);
        buttonsPanel.add(this.saveChangesButton);
        buttonsPanel.add(this.deleteButton);

        panel.add(buttonsPanel);

        add(panel);
    }

    /**
     * Initialize all the listeners on the components
     */
    private void initListeners(){
        this.modifyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Set the JTextFields to be editable to allow changes
                for (JTextField fields : dataFields){
                    fields.setEditable(true);
                }

                // Set the copy buttons to not be enabled while the data is being changed
                for (JButton copyButton : copyButtons){
                    copyButton.setEnabled(false);
                }

                modifyButton.setEnabled(false);  // Disable the JButton as it's already being used
                deleteButton.setVisible(false); // HIde the delete button
                saveChangesButton.setVisible(true);  // Show the JButton used to apply changes
            }
        });

        // Save the changes and send the event to the backend
        this.saveChangesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Store the updated strings into an array
                String[] updatedData = new String[5];

                // Iterate over the data fields to get their data and set them not to be editable
                // Set the new data to the copy button and enable it again
                for (int i = 0; i < dataFields.size(); i++){
                    JTextField dataField = dataFields.get(i);
                    updatedData[i] = dataField.getText();
                    dataField.setEditable(false);

                    JButton copyButton = copyButtons.get(i);

                    // Remove the old action listener and create a new one with the updated data
                    copyButton.removeActionListener(copyButton.getActionListeners()[0]);
                    copyButton.addActionListener(getCopyDataActionListener(dataField.getText()));
                    copyButton.setEnabled(true);
                }

                // Send the data to the backend
                changeData(updatedData[0], updatedData[1], updatedData[2], updatedData[3], updatedData[4]);

                // Hide the save JButton, show the Delete button and enable the modify JButton again
                saveChangesButton.setVisible(false);
                deleteButton.setVisible(true);
                modifyButton.setEnabled(true);
            }
        });

        this.deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int result = JOptionPane.showConfirmDialog(getDialog(), "Delete the data?");

                // If the result is 0 (Yes) delete the data by sending an event to the backend
                if (result == 0) {
                    deleteData();
                    dispose();  // Destroy the dialog as the data shown has been deleted
                }
            }
        });
    }

    /**
     * Create an Action Listener to copy a dataField used by the buttons
     * @param dataField
     * @return ActionListener with actionPerformed method
     */
    private ActionListener getCopyDataActionListener(String dataField){
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Get the clipboard from the system
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection dataToCopy = new StringSelection(dataField);  // Create a Transferable
                clipboard.setContents(dataToCopy, dataToCopy);  // Copy the transferable
                JOptionPane.showMessageDialog(getDialog(), "Copied the data to the clipboard.");
            }
        };
    }

    /**
     * Update a record of the database
     * @param username
     * @param emailAddress
     * @param password
     * @param service
     * @param additional
     */
    private void changeData(String username, String emailAddress, String password, String service, String additional){
        // Create the data to send with the event
        Data data = new Data(this.data.getID(), username, emailAddress, password, service, additional);

        // Create and send the event
        Event event = new Event("change-data", data);
        this.communicationHandler.send(event);
        this.communicationHandler.receive();
//        notifyUser();  // Example method to show the user a messagebox with the operation status
    }

    /**
     * Send an event to the backend to delete this data record
     */
    private void deleteData(){
        // Create the event to send
        Event event = new Event("delete-data", this.data.getID());
        this.communicationHandler.send(event);
        this.communicationHandler.receive();
//        notifyUser();  // Example method to show the user a messagebox with the operation status
    }
}
