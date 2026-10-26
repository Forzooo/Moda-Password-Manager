package moda.passwordmanager.frontend.dialogs;

import moda.passwordmanager.Application;
import moda.passwordmanager.backend.Data;
import moda.passwordmanager.frontend.Utilities;
import moda.passwordmanager.frontend.components.ModernScrollPane;
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
    private final HashMap<Integer, LinkedHashMap<Data.Fields, String>> solvedData;

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
        setTitle(Application.getApplicationTitle());
        setIconImage(Application.getIcon());
        setSize(new Dimension(700, 600));
        setModal(true);
        setResizable(false);
        setLocationRelativeTo(getOwner());
        setLayout(new MigLayout(""));
    }

    /**
     * Initialize the components
     */
    private void initComponents(){
        JLabel titleLabel = new JLabel();
        titleLabel.setText(Utilities.getLocaleString("Moda.GoogleDriveSynchronizationConflicts.title"));
        titleLabel.setFont(new Font(UIManager.getString("Moda.GeneralUseFontFamily"), Font.BOLD, 32));

        this.conflictsPanel = new JPanel();

        // The layout of the conflicts is: local data panel, vertical separator, remote data panel and a gap of 20 between
        // two data
        this.conflictsPanel.setLayout(new MigLayout("align center", "[]20[]20[]", "[]20[]"));
        ModernScrollPane scrollPane = new ModernScrollPane(this.conflictsPanel);

        // The two labels are used to let the user know whether the data is local or remote
        JLabel localDataSectionLabel = new JLabel();
        localDataSectionLabel.setText(Utilities.getLocaleString("Moda.GoogleDriveSynchronizationConflicts.localDataSectionLabel"));
        localDataSectionLabel.setFont(new Font(UIManager.getString("Moda.GeneralUseFontFamily"), Font.BOLD, 24));

        JLabel remoteDataSectionLabel = new JLabel();
        remoteDataSectionLabel.setText(Utilities.getLocaleString("Moda.GoogleDriveSynchronizationConflicts.remoteDataSectionLabel"));
        remoteDataSectionLabel.setFont(new Font(UIManager.getString("Moda.GeneralUseFontFamily"), Font.BOLD, 24));

        this.conflictsPanel.add(localDataSectionLabel, "align center");
        this.conflictsPanel.add(remoteDataSectionLabel, "align center, skip, wrap");

        this.solveButton = new JButton();
        this.solveButton.setText(Utilities.getLocaleString("Moda.GoogleDriveSynchronizationConflicts.solveButton"));

        this.cancelButton = new JButton();
        this.cancelButton.setText(Utilities.getLocaleString("Moda.GoogleDriveSynchronizationConflicts.cancelButton"));

        add(titleLabel, "align center, span, wrap");
        add(scrollPane, "align center, span, push, grow, wrap");

        // TODO. Check if it's feasible to anchor the two buttons to the south-est of the conflictsPanel
        add(this.cancelButton, "align right, split 2");
        add(this.solveButton);
    }

    /**
     * Initialize the listeners of the components
     */
    private void initListeners(){
        this.solveButton.addActionListener(e -> {
            // We iterate over the HashMap (ID->Data fields), and we create a Data object for each of them
            for (Map.Entry<Integer, LinkedHashMap<Data.Fields, String>> dataEntry : solvedData.entrySet()){
                // It is set to null only when the record has already been deleted on the remote, and the user has
                // chosen to delete it locally too
                if (dataEntry.getValue() == null){
                    this.itc.request(new Event("delete-data", dataEntry.getKey()));
                }else{
                    // To create the Daa object we set the ID in the updatedData to use a cleaner Data constructor
                    LinkedHashMap<Data.Fields, String> updatedData = solvedData.get(dataEntry.getKey());
                    updatedData.put(Data.Fields.ID, String.valueOf(dataEntry.getKey()));
                    Data data = new Data(updatedData);

                    // We add the record in the local database has it has been added only to the remote one yet
                    if (this.solvedData.get(dataEntry.getKey()).containsKey(Data.Fields.ADDED_ON_REMOTE)){
                        this.itc.request(new Event("add-data", data));  // TODO. Check whether ID are kept even
                                                                            // TODO. if added without setting it
                                                                            // TODO. from the remote one
                    }else{  // We update the record in the database
                        this.itc.request(new Event("update-data", data));
                    }
                }
            }

            // After all the conflicts have been solved, we can update the Google Drive status to complete the
            // synchronization
            this.itc.request(new Event("google-drive-synchronization-conflicts-solved"));
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
            remoteData = decryptData(remoteData);  // We decrypt the data here as it can be used for the service section
            Data localData = getData(remoteData.getId());  // The local data must have the same ID of the remote one

            // The service section label is used to let the user know what data he is solving the issues for
            // It is shown in each case unless is they have two different service values and both are not null
            JLabel serviceSectionLabel = new JLabel();
            serviceSectionLabel.setFont(new Font(UIManager.getString("Moda.GeneralUseFontFamily"), Font.BOLD, 18));
            serviceSectionLabel.setText(localData.getService());

            // If the service of remote one is null, we have to add to the service label that it has been deleted on
            // the remote side
            if (remoteData.getService() == null){
                serviceSectionLabel.setText(serviceSectionLabel.getText() + " (" +
                        Utilities.getLocaleString("Moda.GoogleDriveSynchronizationConflicts.deletedOnRemote") + ")");
            }else if (localData.getService() == null){  // Otherwise, we have to set a completely new text as we need to
                                                        // use the remote service instead of the empty local one
                serviceSectionLabel.setText(remoteData.getService());
            }

            // We ensure that service section label is added in each case unless the services both exists and are not
            // equal
            if (localData.getService() == null || remoteData.getService() == null || localData.getService().equals(remoteData.getService())){
                this.conflictsPanel.add(serviceSectionLabel, "align center, span, wrap");
            }

            // Each data has its own panel where the conflicted fields are shown
            // We have one panel for the local data and one for the remote one to divide them
            JPanel localDataPanel = new JPanel();
            localDataPanel.setLayout(new MigLayout("", "[]"));

            JPanel remoteDataPanel = new JPanel();
            remoteDataPanel.setLayout(new MigLayout("", "[]"));

            // If the service is set to null then it means that the data has been deleted on the remote
            if (remoteData.getService() == null){
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

                localDataPanel.add(restoreRadio, "span, wrap");
                remoteDataPanel.add(removeRadio, "span, wrap");

            }else if (localData.getService() == null){  // If the local service is null it means that the data has
                                                        // been added on the remote
                // Add the remote data automatically to the database once the user has clicked the solve button
                LinkedHashMap<Data.Fields, String> remoteUserData = remoteData.asLinkedHashMap();
                remoteUserData.put(Data.Fields.ADDED_ON_REMOTE, "");  // We need to have a flag set on the linked hash map to know
                                                        // that the data has been added on remote to call the event
                                                        // add-data instead of update-data
                this.solvedData.put(remoteData.getId(), remoteUserData);

            }else{  // The default case where the data has been modified on local/remote sides
                LinkedHashMap<Data.Fields, String> localUserData = localData.asLinkedHashMap();
                LinkedHashMap<Data.Fields, String> remoteUserData = remoteData.asLinkedHashMap();

                this.solvedData.put(localData.getId(), localUserData);  // By default, the local one is always chosen

                // Iterate over each field and show only the ones that are different
                for (Map.Entry<Data.Fields, String> localDataEntry : localUserData.entrySet()){
                    if (!localDataEntry.getValue().equals(remoteUserData.get(localDataEntry.getKey()))){
                        // The button group allows to select only one of the two radio buttons
                        ButtonGroup buttonGroup = new ButtonGroup();

                        JRadioButton localDataRadio = new JRadioButton();
                        localDataRadio.setText(formatText(localDataEntry.getValue()) + " (" + localDataEntry.getKey().toString() +  ")");
                        localDataRadio.setSelected(true);  // By default, the local one is always chosen to avoid having to
                                                           // check whether all the conflicts have been solved: in this way
                                                           // the solve button is already enabled

                        // If the radio button is selected, then we update it in the solved data: the HashMap that will be
                        // used to update the database in the backend
                        localDataRadio.addActionListener(
                                e -> solvedData.get(localData.getId()).put(localDataEntry.getKey(), localDataEntry.getValue())
                        );

                        JRadioButton remoteDataRadio = new JRadioButton();
                        remoteDataRadio.setText(formatText(remoteUserData.get(localDataEntry.getKey())) +
                                " (" + localDataEntry.getKey().toString() +  ")");
                        remoteDataRadio.addActionListener(
                                e -> solvedData.get(localData.getId()).put(localDataEntry.getKey(), remoteUserData.get(localDataEntry.getKey()))
                        );

                        buttonGroup.add(localDataRadio);
                        buttonGroup.add(remoteDataRadio);

                        localDataPanel.add(localDataRadio, "wrap");
                        remoteDataPanel.add(remoteDataRadio, "wrap");
                    }
                }
            }

            // After we have added the components related to each case, we can add the panels divided by a separator
            this.conflictsPanel.add(localDataPanel, "grow");
            this.conflictsPanel.add(new JSeparator(SwingConstants.VERTICAL), "growy, width 4!, align center");
            this.conflictsPanel.add(remoteDataPanel, "grow, wrap");

            // Lastly, we add a horizontal separator to divide the different services
            this.conflictsPanel.add(new JSeparator(SwingConstants.HORIZONTAL), "span, growx, wrap");
        }
    }

    /**
     * Get the decrypted data of one that's encrypted
     */
    private Data decryptData(Data data){
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

    /**
     * A text formatter that strips the text of some of its character to adhere to the maximum length of 32
     */
    private String formatText(String text){
        if (text.length() >= 32){
            return text.substring(0, 13) + "[...]" + text.substring(text.length() - 13);
        }

        return text;
    }
}
