package moda.passwordmanager.frontend.panels;

import moda.passwordmanager.backend.Data;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;
import moda.passwordmanager.interthreadcommunication.Event;

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
 * A JPanel that shows the all the information related to an ID of a record of the database
 */
public class UserData extends JPanel {

    private InterThreadCommunication itc;

    private final int ID;  // The ID associated with the data
    private List<JTextField> dataFields;  // The data field contain the user data
    private List<JButton> copyButtons;  // The copy buttons for each data field

    // Swing Components
    private JButton modifyButton;
    private JButton saveChangesButton;
    private JButton deleteButton;
    private JButton generatePasswordButton;

    public UserData(InterThreadCommunication itc, int id, int width, int height) {
        super();  // Initialize the Panel

        this.itc = itc;
        this.ID = id;

        initPanel(width, height);
        initComponents();
        initListeners();
    }

    /**
     * Set the configuration of the Dialog
     */
    private void initPanel(int width, int height){
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));  // Set its layout
        setBorder(new EmptyBorder(20,20,20,20));

        // Set the preferred size
        setPreferredSize(new Dimension(width, height));

    }

    /**
     * Get the panel of this class as some action listener require it
     * @return JPanel of the class
     */
    private JPanel getPanel(){
        return this;
    }

    /**
     * Retrieve the data associated with the ID given in the constructor
     */
    private Data getData(){
        // Create the event to send to the backend
        Event getData = new Event("get-data", this.ID);

        // Wait for the response
        Event getSingleDataCompleted = this.itc.requestAndReceive(getData);

        Data userData = (Data) getSingleDataCompleted.getData().getFirst();  // Get the user data
        return userData;
    }

    /**
     * Initialize the components of the panel
     */
    private void initComponents(){
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
        for (String dataField : getData().getFullUserData()){
            add(createDataFieldPanel(dataField));
        }

        // A panel for the buttons
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.add(this.modifyButton);
        buttonsPanel.add(this.saveChangesButton);
        buttonsPanel.add(this.deleteButton);
        buttonsPanel.add(this.generatePasswordButton);

        add(buttonsPanel);
    }

    /**
     * Create the JPanel for each field inside the data
     * @param dataField The data field which the panel will be created for
     */
    private JPanel createDataFieldPanel(String dataField){
        JPanel dataFieldPanel = new JPanel(new BorderLayout(5, 0));
        dataFieldPanel.setBorder(new EmptyBorder(5, 0, 5, 0));  // Padding between rows

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
        dataFieldPanel.add(dataTextField, BorderLayout.CENTER);
        dataFieldPanel.add(copyDataButton, BorderLayout.EAST);
        return dataFieldPanel;
    }

    /**
     * Initialize all the listeners on the components
     */
    private void initListeners() {
        this.modifyButton.addActionListener(e -> enableEditing());

        // Save the changes and send the event to the backend
        this.saveChangesButton.addActionListener(e -> saveChanges());

        this.deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int result = JOptionPane.showConfirmDialog(getPanel(), "Delete the data?",
                        "Moda Password Manager", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                // If the result is 0 (Yes) delete the data by sending an event to the backend
                if (result == 0) {
                    deleteData();
                    closeTab();
                }
            }
        });

        this.generatePasswordButton.addActionListener(e -> generatePassword());
    }

    /**
     * Set the data to be edited
     */
    private void enableEditing(){
        // Set the JTextFields to be editable to allow changes
        for (JTextField dataField : this.dataFields){
            dataField.setEditable(true);
        }

        // Set the copy buttons to not be enabled while the data is being changed
        for (JButton copyButton : this.copyButtons){
            copyButton.setEnabled(false);
        }

        // Disable and hide the buttons that cannot be used while in Modify state
        this.modifyButton.setEnabled(false);
        this.modifyButton.setVisible(false);
        this.deleteButton.setEnabled(false);
        this.deleteButton.setVisible(false);

        // Enable and show the buttons that are related to Modify state
        this.saveChangesButton.setEnabled(true);
        this.saveChangesButton.setVisible(true);
        this.generatePasswordButton.setEnabled(true);
        this.generatePasswordButton.setVisible(true);
    }

    /**
     * Set the data to not be edited
     */
    private void disableEditing(){
        // Set the JTextFields to not be editable to disable changes
        for (JTextField dataField : this.dataFields){
            dataField.setEditable(false);
        }

        // Set the copy buttons to be enabled again
        for (JButton copyButton : this.copyButtons){
            copyButton.setEnabled(false);
        }

        // Disable and hide the buttons that cannot be used while in Read-only state
        this.saveChangesButton.setEnabled(false);
        this.saveChangesButton.setVisible(false);
        this.generatePasswordButton.setEnabled(false);
        this.generatePasswordButton.setVisible(false);

        // Enable and show the buttons that are related to Read-only state
        this.modifyButton.setEnabled(true);
        this.modifyButton.setVisible(true);
        this.deleteButton.setEnabled(true);
        this.deleteButton.setVisible(true);
    }

    private void saveChanges(){
        // The updatedData is used to update the backend with the new data
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
            enableEditing();  // Enable to make changes again as they were disabled in the previous for loop
            return;
        }

        // Send the data to the backend
        changeData(new Data(this.ID, updatedData[0], updatedData[1], updatedData[2], updatedData[3], updatedData[4]));
        disableEditing();
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
                JOptionPane.showMessageDialog(getPanel(), "Copied the data to the clipboard.");
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
        this.dataFields.get(2).setText(password);  // Set the password to the third text field: the password one
    }

    /**
     * Remove the tab from the ShowData tabbed pane
     */
    private void closeTab(){
        // The parent of the UserData panel is the TabbedPane where the panel is added to, so we can retrieve it
        // and remove this panel from it
        JTabbedPane tabbedPane = (JTabbedPane) getParent();
        tabbedPane.remove(this);
    }

}
