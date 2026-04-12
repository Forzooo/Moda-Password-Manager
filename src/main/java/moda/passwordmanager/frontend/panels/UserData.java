package moda.passwordmanager.frontend.panels;

import moda.passwordmanager.Application;
import moda.passwordmanager.backend.Data;
import moda.passwordmanager.frontend.Utilities;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;
import moda.passwordmanager.interthreadcommunication.Event;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

/**
 * A JPanel that shows the all the information related to an ID of a record of the database
 */
public class UserData extends JPanel {

    private final InterThreadCommunication ITC;
    private final int ID;  // The ID associated with the data
    private String title;  // The title of the tab

    // Swing Components
    private UserDataField[] userDataFields;  // The fields of the data

    // The fields before the editing state is enabled, thus added only inside "enableEditing"
    // They are used when "Discard Changes" button is clicked to restore the previous values, and it will clear the array
    private String[] rollbackDataFields;

    private JButton modifyButton;
    private JButton deleteButton;
    private JButton saveChangesButton;
    private JButton discardChangesButton;

    public UserData(InterThreadCommunication itc, int id) {
        super();  // Initialize the Panel

        this.ITC = itc;
        this.ID = id;

        initPanel();
        initComponents();
        initListeners();
    }

    /**
     * Set the configuration of the Panel
     */
    private void initPanel(){
        setLayout(new MigLayout("fill"));
        setBorder(new EmptyBorder(20,20,20,20));
    }

    /**
     * Get the panel of this class as some action listener require it
     */
    private UserData getPanel(){
        return this;
    }

    /**
     * Initialize the components of the panel
     */
    private void initComponents(){
        // Create all the JButton
        this.modifyButton = new JButton();
        this.modifyButton.setText(Utilities.getLocaleString("Moda.UserData.modifyButton"));

        this.deleteButton = new JButton();
        this.deleteButton.setText(Utilities.getLocaleString("Moda.UserData.deleteButton"));

        this.saveChangesButton = new JButton();
        this.saveChangesButton.setText(Utilities.getLocaleString("Moda.UserData.saveChangesButton"));
        this.saveChangesButton.setEnabled(false);
        this.saveChangesButton.setVisible(false);  // It's shown only in the editing state

        this.discardChangesButton = new JButton();
        this.discardChangesButton.setText(Utilities.getLocaleString("Moda.UserData.discardChangesButton"));
        this.discardChangesButton.setEnabled(false);
        this.discardChangesButton.setVisible(false);  // It's shown only in the editing state

        // Create the panel for each field of the data
        Data data = getData();
        this.title = data.getSERVICE();  // The title of the tab could be retrieved from the userData array itself
                                         // or from the userDataFields array but if in future it may occur that the index
                                         // of the service field changes, then the title would leak sensitive information
                                         // of the user, thus we retrieve it from the getter method of Data

        String[] userData = data.getFullUserData();  // Retrieve the data of the user to know its length
        this.userDataFields = new UserDataField[userData.length];  // Set the size based on the data
        this.rollbackDataFields = new String[userData.length];  // Create the rollback array based on the data length

        for (int i = 0; i < this.userDataFields.length; i++){
            // As the password field requires its own panel we need to check each time the value of i to know
            // the field we are creating
            UserDataField userDataField;
            if (i != 2){
                userDataField = new UserDataField(userData[i]);
            }else{
                userDataField = new UserPasswordField(userData[i], this.ITC);
            }
            this.userDataFields[i] = userDataField;  // Set the panel to the array
            add(userDataField, "span, align center, wrap");  // Add the panel to the GUI
        }

        add(this.modifyButton, "split 2, align center");
        add(this.deleteButton, "wrap");
        add(this.saveChangesButton, "split 2, align center");
        add(this.discardChangesButton);
    }

    /**
     * Initialize all the listeners on the components
     */
    private void initListeners() {
        this.modifyButton.addActionListener(e -> enableEditing());

        this.deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int result = JOptionPane.showConfirmDialog(getPanel(), Utilities.getLocaleString("Moda.UserData.deleteButtonConfirmDialog"),
                        Application.getApplicationTitle(), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                // If the result is 0 (Yes) delete the data by sending an event to the backend
                if (result == 0) {
                    deleteData();
                    closeTab();
                }
            }
        });

        this.saveChangesButton.addActionListener(e -> saveChanges());
        this.discardChangesButton.addActionListener(e -> discardChanges());
    }

    /**
     * Retrieve the data associated with the ID given in the constructor
     */
    private Data getData(){
        // Create the event to send to the backend
        Event getData = new Event("get-data", this.ID);

        // Wait for the response
        Event getSingleDataCompleted = this.ITC.request(getData);

        Data userData = (Data) getSingleDataCompleted.getData().getFirst();  // Get the user data
        return userData;
    }

    /**
     * Get the ID associated with this UserData tab
     */
    public int getID() {
        return this.ID;
    }

    /**
     * Get the title of the tab: the service field
     */
    public String getTitle(){
        return this.title;
    }

    /**
     * Set the data to be edited
     */
    private void enableEditing(){
        // Set the DataFields to be editable to allow changes
        for (int i = 0; i < this.userDataFields.length; i++){
            this.rollbackDataFields[i] = this.userDataFields[i].getData();  // Save the data for rollback purposes
            this.userDataFields[i].enableEditing();
        }

        // Disable and hide the buttons that cannot be used while in editing state
        this.modifyButton.setEnabled(false);
        this.modifyButton.setVisible(false);
        this.deleteButton.setEnabled(false);
        this.deleteButton.setVisible(false);

        // Enable and show the buttons that are related to the editing state
        this.saveChangesButton.setEnabled(true);
        this.saveChangesButton.setVisible(true);
        this.discardChangesButton.setEnabled(true);
        this.discardChangesButton.setVisible(true);
    }

    /**
     * Set the data to not be edited
     */
    private void disableEditing(){
        Arrays.fill(this.rollbackDataFields, "");  // Remove the previous data as it is not required anymore

        // Set the DataFields to not be editable to disable changes
        for (UserDataField dataField : this.userDataFields){
            dataField.disableEditing();
        }

        // Disable and hide the buttons that cannot be used while in Read-only state
        this.saveChangesButton.setEnabled(false);
        this.saveChangesButton.setVisible(false);
        this.discardChangesButton.setEnabled(false);
        this.discardChangesButton.setVisible(false);

        // Enable and show the buttons that are related to Read-only state
        this.modifyButton.setEnabled(true);
        this.modifyButton.setVisible(true);
        this.deleteButton.setEnabled(true);
        this.deleteButton.setVisible(true);
    }

    /**
     * Save the changes made into a Data object that is sent to the backend
     */
    private void saveChanges(){
        // The updatedData is used to update the backend with the new data
        String[] updatedData = new String[5];

        // Iterate over the data fields to get their data and set them not to be editable
        // Set the new data to the copy button and enable it again
        for (int i = 0; i < this.userDataFields.length; i++){
            updatedData[i] = this.userDataFields[i].getData();
        }

        // Ensure that the service field is not empty
        if (updatedData[3].isEmpty()){
            return;
        }

        // Disable the editing and send the data to the backend
        disableEditing();
        updateData(new Data(this.ID, updatedData[0], updatedData[1], updatedData[2], updatedData[3], updatedData[4]));
    }

    /**
     * Discard the current changes made and disable editing mode
     */
    private void discardChanges(){
        // Rollback each TextField to its previous value, where their clipboard is automatically updated when editing
        // mode is disabled
        for (int i = 0; i < this.userDataFields.length; i++){
            this.userDataFields[i].setText(this.rollbackDataFields[i]);
        }
        disableEditing();  // Disable editing, which also clears the rollback array
    }

    /**
     * Update a record of the database
     * @param data The data modified
     */
    private void updateData(Data data){
        // Create and send the event
        Event event = new Event("update-data", data);
        this.ITC.request(event);  // Wait for the response of the backend
    }

    /**
     * Send an event to the backend to delete this data record
     */
    private void deleteData(){
        Event event = new Event("delete-data", this.ID);
        this.ITC.request(event);
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
