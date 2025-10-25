package moda.passwordmanager.frontend.dialogs;

import moda.passwordmanager.backend.Data;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;
import moda.passwordmanager.interthreadcommunication.Event;
import moda.passwordmanager.frontend.Frontend;

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
 * A JDialog that shows the all the information related to an ID of a record of the database
 */
public class ShowDataDialog extends JDialog {

    private InterThreadCommunication itc;

    private final int ID;  // The ID associated with the data
    private List<JTextField> dataFields;  // The data field contain the user data
    private List<JButton> copyButtons;  // The copy buttons for each data field

    // Swing Components
    private JButton modifyButton;
    private JButton saveChangesButton;
    private JButton deleteButton;
    private JButton generatePasswordButton;

    public ShowDataDialog(InterThreadCommunication itc, Data userData) {
        super();  // Initialize the Panel

        this.itc = itc;
        this.ID = userData.getID();

        initDialog();
        initComponents(userData);
        initListeners();
    }

    /**
     * Set the configuration of the Dialog
     */
    private void initDialog(){
        setSize(new Dimension(600, 400));
        setResizable(false);
        setAlwaysOnTop(true);
        setLocationRelativeTo(getParent());  // The dialog is shown at the center of the window
        setIconImage(Frontend.getIcon());
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
    private void initComponents(Data userData){

        // A panel is used instead of the dialog because it is more customizable
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));  // Set its layout
        panel.setBorder(new EmptyBorder(20,20,20,20));

        // Create all the JButton
        this.modifyButton = new JButton("Modify");
        this.saveChangesButton = new JButton("Save Changes");
        this.saveChangesButton.setVisible(false);  // It's shown only when modifyButton is clicked
        this.deleteButton = new JButton("Delete");
        this.generatePasswordButton = new JButton("Generate a Password");
        this.generatePasswordButton.setVisible(false);

        // Add dynamically all the data fields
        this.dataFields = new ArrayList<>();  // All the data fields are stored here
        this.copyButtons = new ArrayList<>();  // All the copy buttons are stored here

        // Create the data row for each field
        for (String dataField : userData.getFullUserData()){
            panel.add(createDataRowPanel(dataField));
        }

        // A panel for the buttons
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.add(this.modifyButton);
        buttonsPanel.add(this.saveChangesButton);
        buttonsPanel.add(this.deleteButton);
        buttonsPanel.add(this.generatePasswordButton);

        panel.add(buttonsPanel);

        add(panel);
    }

    /**
     * Create the JPanel for each field inside the data
     * @param dataField The field which the panel will be created for
     */
    private JPanel createDataRowPanel(String dataField){
        JPanel dataRowPanel = new JPanel(new BorderLayout(5, 0));
        dataRowPanel.setBorder(new EmptyBorder(5, 0, 5, 0));  // Padding between rows

        JTextField dataTextField = new JTextField();
        dataTextField.setText(dataField);
        dataTextField.setEditable(false);  // The user cannot modify the data unless "Modify" is clicked
        this.dataFields.add(dataTextField);

        // Create the JButton to copy the data field
        JButton copyDataButton = new JButton();
        copyDataButton.setText("❏");
        copyDataButton.setPreferredSize(new Dimension(50, 50));
        copyDataButton.addActionListener(getCopyDataActionListener(dataField));
        this.copyButtons.add(copyDataButton);

        // Add the JTextField and the JButton to the panel
        dataRowPanel.add(dataTextField, BorderLayout.CENTER);
        dataRowPanel.add(copyDataButton, BorderLayout.EAST);
        return dataRowPanel;
    }

    /**
     * Initialize all the listeners on the components
     */
    private void initListeners() {
        this.modifyButton.addActionListener(e -> enableChanges());

        // Save the changes and send the event to the backend
        this.saveChangesButton.addActionListener(e -> saveChanges());

        this.deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int result = JOptionPane.showConfirmDialog(getDialog(), "Delete the data?",
                        "Moda Password Manager", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                // If the result is 0 (Yes) delete the data by sending an event to the backend
                if (result == 0) {
                    deleteData();
                    dispose();  // Destroy the dialog as the data shown has been deleted
                }
            }
        });

        this.generatePasswordButton.addActionListener(e -> generatePassword());
    }

    /**
     * Enable the JTextFields to be modified
     */
    private void enableChanges(){
        // Set the JTextFields to be editable to allow changes
        for (JTextField fields : this.dataFields){
            fields.setEditable(true);
        }

        // Set the copy buttons to not be enabled while the data is being changed
        for (JButton copyButton : this.copyButtons){
            copyButton.setEnabled(false);
        }

        this.modifyButton.setEnabled(false);  // Disable the JButton as it's already being used
        this.deleteButton.setVisible(false); // Hide the delete button
        this.saveChangesButton.setVisible(true);  // Show the JButton used to apply changes
        this.generatePasswordButton.setVisible(true);  // Show the generate password button
    }

    private void saveChanges(){
        // Store the updated strings into an array
        String[] updatedData = new String[5];

        // Iterate over the data fields to get their data and set them not to be editable
        // Set the new data to the copy button and enable it again
        for (int i = 0; i < this.dataFields.size(); i++){
            JTextField dataField = this.dataFields.get(i);
            updatedData[i] = dataField.getText();
            dataField.setEditable(false);

            // Remove the old action listener, which is always at index 0, and create a new one with the updated data
            JButton copyButton = this.copyButtons.get(i);
            copyButton.removeActionListener(copyButton.getActionListeners()[0]);
            copyButton.addActionListener(getCopyDataActionListener(dataField.getText()));
            copyButton.setEnabled(true);
        }

        // Ensure that the service field is not empty
        if (updatedData[3].isEmpty()){
            enableChanges();  // Enable to make changes again as they were disabled in the previous for loop
            return;
        }

        // Send the data to the backend
        changeData(new Data(this.ID, updatedData[0], updatedData[1], updatedData[2], updatedData[3], updatedData[4]));

        // Hide the save JButton and the generate password button, show the Delete button and enable the modify JButton again
        this.saveChangesButton.setVisible(false);
        this.deleteButton.setVisible(true);
        this.modifyButton.setEnabled(true);
        this.generatePasswordButton.setVisible(false);
    }

    /**
     * Create an Action Listener to copy a dataField used by the buttons
     * @param dataField The text which is copied to the clipboard
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
     * @param data The data modified
     */
    private void changeData(Data data){
        // Create and send the event
        Event event = new Event("change-data", data);
        this.itc.requestAndReceive(event);  // Wait for the response of the backend
    }

    /**
     * Send an event to the backend to delete this data record
     */
    private void deleteData(){
        Event event = new Event("delete-data", this.ID);
        this.itc.request(event);
    }

    /**
     * Generate a password and set the password text field to it
     */
    private void generatePassword(){
        Event event = this.itc.requestAndReceive(new Event("generate-string"));
        String password = (String) event.getData().getFirst();
        this.dataFields.get(2).setText(password);  // Set the password to the third text field (the password one)
    }
}
