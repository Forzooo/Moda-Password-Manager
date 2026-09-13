package moda.passwordmanager.frontend.panels;

import moda.passwordmanager.Application;
import moda.passwordmanager.backend.Data;
import moda.passwordmanager.frontend.Utilities;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;
import moda.passwordmanager.interthreadcommunication.Event;
import net.miginfocom.swing.MigLayout;
import raven.modal.Toast;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A JPanel that shows the all the information related to an ID of a record of the database
 */
public class UserData extends JPanel {

    private final InterThreadCommunication itc;
    private final int id;  // The ID associated with the data
    private String title;  // The title of the tab

    // Swing Components
    private EnumMap<Data.Fields, UserDataField> userDataFields;  // The fields of the data

    // The fields before the editing state is enabled, thus added only inside "enableEditing"
    // They are used when "Discard Changes" button is clicked to restore the previous values, and it will clear the array
    private EnumMap<Data.Fields, String> rollbackData;

    private JButton modifyButton;
    private JButton deleteButton;
    private JButton saveChangesButton;
    private JButton discardChangesButton;

    public UserData(InterThreadCommunication itc, int id) {
        super();  // Initialize the Panel

        this.itc = itc;
        this.id = id;

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
        Data data = getData();  // We get the data associated with the ID given in the constructor
        this.title = data.getService();  // The title of the tab could be retrieved from the userData array itself
                                         // or from the userDataFields array but if in future it may occur that the index
                                         // of the service field changes, then the title would leak sensitive information
                                         // of the user, thus we retrieve it from the getter method of Data

        LinkedHashMap<Data.Fields, String> userData = data.asLinkedHashMap();

        this.userDataFields = new EnumMap<>(Data.Fields.class);
        this.rollbackData = new EnumMap<>(Data.Fields.class);

        // Iterate over the fields of the data to create a JPanel for each field
        for (Map.Entry<Data.Fields, String> field : userData.entrySet()){
            UserDataField userDataField;

            // The ID has to be skipped as it does not require a panel
            if (field.getKey().equals(Data.Fields.ID)){
                continue;
            }else if (field.getKey().equals(Data.Fields.PASSWORD)){  // The password panel has its own class
                userDataField = new UserPasswordField(field.getValue(), this.itc);
            }else{
                userDataField = new UserDataField(field.getValue());
            }

            this.rollbackData.put(field.getKey(), field.getValue());  // We add the field to the rollback map

            this.userDataFields.put(field.getKey(), userDataField);
            add(userDataField, "span, align center, wrap");  // Add the panel to the GUI
        }

        // Create all the buttons to handle the data
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

        this.deleteButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(getPanel(), Utilities.getLocaleString("Moda.UserData.deleteButtonConfirmDialog"),
                    Application.getApplicationTitle(), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            // If the result is 0 (Yes) delete the data by sending an event to the backend
            if (result == 0) {
                deleteData();
                closeTab();
            }
        });

        this.saveChangesButton.addActionListener(e -> saveChanges());
        this.discardChangesButton.addActionListener(e -> discardChanges());
    }

    /**
     * Retrieve the data associated with the ID given in the constructor
     */
    private Data getData(){
        Event getSingleDataCompleted = this.itc.request(new Event("get-data", this.id));

        return (Data) getSingleDataCompleted.getData().getFirst();  // Get the user data
    }

    /**
     * Get the ID associated with this UserData tab
     */
    public int getId() {
        return this.id;
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
        for (UserDataField userDataField : this.userDataFields.values()){
            userDataField.enableEditing();
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
        // Set the DataFields to not be editable to disable changes
        for (UserDataField dataField : this.userDataFields.values()){
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
        EnumMap<Data.Fields, String> updatedData = new EnumMap<>(Data.Fields.class);

        // Iterate over the data fields to get their data
        for (Map.Entry<Data.Fields, UserDataField> entry : this.userDataFields.entrySet()){
            updatedData.put(entry.getKey(), entry.getValue().getData());
        }

        // Ensure that the service field is not empty, otherwise we cannot allow the data to be saved
        if (updatedData.get(Data.Fields.SERVICE).isBlank()){
            return;
        }

        // Updates the rollback map to the new values
        this.rollbackData.replaceAll((k, v) -> updatedData.get(k));

        // Disable the editing and send the data to the backend
        disableEditing();

        updatedData.put(Data.Fields.ID, String.valueOf(this.id));  // We put the ID inside the updated Data to use a
                                                                   // a cleaner Data constructor
        updateData(new Data(updatedData));
    }

    /**
     * Discard the current changes made and disable editing mode
     */
    private void discardChanges(){
        // Rollback each TextField to its previous value, where their clipboard is automatically updated when editing
        // mode is disabled
        for (Map.Entry<Data.Fields, String> entry : this.rollbackData.entrySet()){
            this.userDataFields.get(entry.getKey()).setText(entry.getValue());
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
        this.itc.request(event);  // Wait for the response of the backend
    }

    /**
     * Send an event to the backend to delete this data record
     */
    private void deleteData(){
        Event event = new Event("delete-data", this.id);
        this.itc.request(event);
        Utilities.showToast(getParent(), Toast.Type.SUCCESS, Utilities.getLocaleString("Moda.Toast.deletedDataSuccessful"));
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
