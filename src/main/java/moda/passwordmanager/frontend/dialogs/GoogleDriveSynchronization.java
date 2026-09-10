package moda.passwordmanager.frontend.dialogs;

import moda.passwordmanager.Application;
import moda.passwordmanager.backend.Data;
import moda.passwordmanager.frontend.Utilities;
import moda.passwordmanager.interthreadcommunication.Event;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;
import net.miginfocom.swing.MigLayout;
import raven.modal.Toast;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * The Google Drive Synchronization dialog lets the user solve the conflicts related to synchronizing with Google Drive.
 */
public class GoogleDriveSynchronization extends JDialog {

    private final InterThreadCommunication itc;
    private final ArrayList<Data> conflictData;  // The remote data that needs its conflicts to be solved

    // As the Data object are final, and we need to update it over time based on the user behaviour, we can use a
    // temporary HashMap to map the ID to the various fields
    // In this way we can easily create Data objects to use when the "Solve" button is clicked
    private final HashMap<Integer, LinkedHashMap<String, String>> solvedData;

    private JPanel conflictsPanel;

    // The buttons that complete/ignores the operations
    private JButton solveButton;  // The conflicts have been solved, an ITC event will be sent with the results
    private JButton cancelButton;  // The conflicts have been ignored and the dialog will be disposed

    public GoogleDriveSynchronization(Frame owner, InterThreadCommunication itc, List<Data> conflictData){
        super(owner);  // We have to set the owner of the frame to use notifications inside the frame and not in dialog

        this.itc = itc;
        this.conflictData = (ArrayList<Data>) conflictData;
        this.solvedData = new HashMap<>();

        initDialog();
        initComponents();
        addConflictedData();
        initListeners();
    }

    /**
     * Configurate the properties of the dialog
     */
    private void initDialog(){
        setTitle(Application.getApplicationTitle() + " - " + Utilities.getLocaleString("Moda.GoogleDriveSynchronizationConflicts.title"));
        setIconImage(Application.getIcon());
        setSize(new Dimension(800, 700));
        setModal(true);
        setResizable(false);
        setLayout(new MigLayout("debug"));
    }

    /**
     * Initialize the components
     */
    private void initComponents(){
        this.conflictsPanel = new JPanel();
        this.conflictsPanel.setLayout(new MigLayout("debug"));
//        ModaScrollPane scrollPane = new ModaScrollPane(conflictsPanel);

        JPanel dialogOperations = new JPanel();
        this.solveButton = new JButton();
        this.solveButton.setText(Utilities.getLocaleString("Moda.GoogleDriveSynchronizationConflicts.solveButton"));

        this.cancelButton = new JButton();
        this.cancelButton.setText(Utilities.getLocaleString("Moda.GoogleDriveSynchronizationConflicts.cancelButton"));

        dialogOperations.add(this.solveButton);
        dialogOperations.add(this.cancelButton);

//        add(scrollPane, "span, grow, wrap");
        add(this.conflictsPanel, "span, align right");
        add(dialogOperations, "span, align right");
    }

    /**
     * Initialize the listeners of the components
     */
    private void initListeners(){
        this.solveButton.addActionListener(e -> {
            // We iterate over the HashMap (ID->Data fields), and we create a Data object for each of them
            for (Map.Entry<Integer, LinkedHashMap<String, String>> dataEntry : solvedData.entrySet()){
                // It is set to null only when the record has already been deleted on the remote, and the user has
                // chosen to delete it locally too
                if (dataEntry.getValue() == null){
                    itc.request(new Event("delete-data", dataEntry.getKey()));
                }else{
                    LinkedHashMap<String, String> updatedData = solvedData.get(dataEntry.getKey());
                    Data data = new Data(dataEntry.getKey(), updatedData.get("username"), updatedData.get("email_address"),
                        updatedData.get("password"), updatedData.get("service"), updatedData.get("additional_data"));

                    // We add the record in the local database has it has been added only to the remote one yet
                    if (solvedData.get(dataEntry.getKey()).containsKey("addedRemote")){
                        itc.request(new Event("add-data", data));  // TODO. Check whether ID are kept even
                                                                            // TODO. if added without setting it
                                                                            // TODO. from the remote one
                    }else{  // We update the record in the database
                        itc.request(new Event("update-data", data));
                    }
                }
            }

            // After all the conflicts have been solved, we can update the Google Drive status to complete the
            // synchronization
            itc.request(new Event("google-drive-synchronization-conflicts-solved"));
            Utilities.showToast(getOwner(), Toast.Type.SUCCESS, Utilities.getLocaleString("Moda.Toast.solvedGoogleDriveConflicts"));
            dispose();  // The dialog is destroyed as it's not required anymore
        });

        this.cancelButton.addActionListener(e -> dispose());
    }

    /**
     * Adds all the panels that contain the conflicted data to the dialog
     */
    private void addConflictedData(){
        for (Data remoteData : this.conflictData){
            Data localData = getData(remoteData.getId());  // The local data must have the same ID of the remote one

            // Each data has its own panel where the conflicted fields are shown
            JPanel conflictedDataPanel = new JPanel();
            conflictedDataPanel.setLayout(new MigLayout());

            // If the service is set to null then it means that the data has been deleted on the remote
            if (remoteData.getService() == null){
                conflictedDataPanel.add(new JLabel(localData.getService() + " (" +
                        Utilities.getLocaleString("Moda.GoogleDriveSynchronizationConflicts.deletedOnRemote") + ")"),
                "span, wrap");
                // The button group allows to select only of the two radio buttons
                ButtonGroup buttonGroup = new ButtonGroup();

                JRadioButton restoreRadio = new JRadioButton();
                restoreRadio.setText(Utilities.getLocaleString("Moda.GoogleDriveSynchronizationConflicts.restoreRadio"));
                restoreRadio.setSelected(true);  // By default, the data is restored
                restoreRadio.addActionListener(e -> this.solvedData.put(localData.getId(),
                        localData.asLinkedHashMap()));

                JRadioButton removeRadio = new JRadioButton();
                removeRadio.setText(Utilities.getLocaleString("Moda.GoogleDriveSynchronizationConflicts.removeRadio"));

                // If the user chooses Remove then in the solved data hash map we set its linked hash map as null
                // to let solve button listener know that it needs to be deleted
                removeRadio.addActionListener(e -> this.solvedData.put(localData.getId(), null));

                buttonGroup.add(restoreRadio);
                buttonGroup.add(removeRadio);

                conflictedDataPanel.add(restoreRadio, "span, wrap");
                conflictedDataPanel.add(removeRadio, "span, wrap");

            }else if (localData.getService() == null){  // If the service is set to null it means that the data has
                                                        // been added on the remote
                // Decrypt the remote data to add it automatically to the database once the user has clicked
                // the solve button
                remoteData = getData(remoteData);
                LinkedHashMap<String, String> remoteUserData = remoteData.asLinkedHashMap();
                remoteUserData.put("addedRemote", "");  // We need to have a flag set on the linked hash map to know
                                                        // that the data has been added on remote to call the event
                                                        // add-data instead of update-data
                this.solvedData.put(remoteData.getId(), remoteUserData);

            }else{  // The default case where the data has been modified on local/remote
                conflictedDataPanel.add(new JLabel(localData.getService()), "span, wrap");
                remoteData = getData(remoteData);  // Decrypt in the backend the data to compare them
                LinkedHashMap<String, String> localUserData = localData.asLinkedHashMap();
                LinkedHashMap<String, String> remoteUserData = remoteData.asLinkedHashMap();

                this.solvedData.put(localData.getId(), localUserData);  // By default, the local one is always chosen

                // Iterate over each field and show only the ones that are different
                for (Map.Entry<String, String> localDataEntry : localUserData.entrySet()){
                    if (!localDataEntry.getValue().equals(remoteUserData.get(localDataEntry.getKey()))){
                        // The button group allows to select only of the two radio buttons
                        ButtonGroup buttonGroup = new ButtonGroup();

                        JRadioButton localDataRadio = new JRadioButton();
                            localDataRadio.setText(localDataEntry.getValue() + " (" +
                                    Utilities.getLocaleString("Moda.GoogleDriveSynchronizationConflicts.localDataRadio")
                                    + ")");
                        localDataRadio.setSelected(true);  // By default, the local one is always chosen to avoid having to
                                                           // check whether all the conflicts have been solved: in this way
                                                           // the solve button is already enabled

                        // If the radio button is selected, then we update it in the solved data: the HashMap that will be
                        // used to update the database in the backend
                        localDataRadio.addActionListener(
                                e -> solvedData.get(localData.getId()).put(localDataEntry.getKey(), localDataEntry.getValue())
                        );

                        JRadioButton remoteDataRadio = new JRadioButton();
                            remoteDataRadio.setText(remoteUserData.get(localDataEntry.getKey()) + " (" +
                                    Utilities.getLocaleString("Moda.GoogleDriveSynchronizationConflicts.remoteDataRadio")
                                    + ")");
                        remoteDataRadio.addActionListener(
                                e -> solvedData.get(localData.getId()).put(localDataEntry.getKey(), remoteUserData.get(localDataEntry.getKey()))
                        );

                        buttonGroup.add(localDataRadio);
                        buttonGroup.add(remoteDataRadio);

                        conflictedDataPanel.add(new JLabel(localDataEntry.getKey()), "wrap");  // The name of the field to compare
                        conflictedDataPanel.add(localDataRadio, "wrap");
                        conflictedDataPanel.add(remoteDataRadio, "wrap");
                    }
                }
            }
            this.conflictsPanel.add(conflictedDataPanel, "wrap");
            this.conflictsPanel.add(new JSeparator(SwingConstants.HORIZONTAL), "span, wrap");
        }
    }

    /**
     * Get the decrypted data of one that's encrypted
     */
    private Data getData(Data data){
        Event request = new Event("decrypt-data", data);
        Event response = this.itc.request(request);

        return (Data) response.getData().getFirst();
    }

    /**
     * Get the decrypted data of a record based on its ID
     */
    private Data getData(int id){
        Event request = new Event("get-data", id);
        Event response = this.itc.request(request);

        return (Data) response.getData().getFirst();
    }

}
