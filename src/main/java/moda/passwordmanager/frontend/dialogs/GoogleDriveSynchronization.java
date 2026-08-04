package moda.passwordmanager.frontend.dialogs;

import moda.passwordmanager.Application;
import moda.passwordmanager.backend.Data;
import moda.passwordmanager.interthreadcommunication.Event;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * The Google Drive Synchronization dialog lets the user solve the conflicts related to synchronizing with Google Drive.
 */
public class GoogleDriveSynchronization extends JDialog {

    private final InterThreadCommunication ITC;
    private final ArrayList<Data> CONFLICT_DATA;  // The remote data that needs its conflicts to be solved

    // As the Data object are final, and we need to update it over time based on the user behaviour, we can use a
    // temporary HashMap to map the ID to the various fields
    // In this way we can easily create Data objects to use when the "Solve" button is clicked
    private final HashMap<Integer, LinkedHashMap<String, String>> SOLVED_DATA;

    private JPanel conflictsPanel;

    // The buttons that complete/ignores the operations
    private JButton solveButton;  // The conflicts have been solved, an ITC event will be sent with the results
    private JButton cancelButton;  // The conflicts have been ignored and the dialog will be disposed

    public GoogleDriveSynchronization(InterThreadCommunication itc, ArrayList<Data> conflictData){
        this.ITC = itc;
        this.CONFLICT_DATA = conflictData;
        this.SOLVED_DATA = new HashMap<>();

        initDialog();
        initComponents();
        addConflictedData();
        initListeners();
    }

    /**
     * Configurate the properties of the dialog
     */
    private void initDialog(){
        setTitle(Application.getApplicationTitle() + " - Google Drive Synchronization Conflicts");
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
        this.solveButton.setText("Solve");

        this.cancelButton = new JButton();
        this.cancelButton.setText("Cancel");

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
        this.solveButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                // We iterate over the HashMap, and we create a Data object for each of them
                // Then we update it in the database by using the "update-data" event
                for (int id : SOLVED_DATA.keySet()){
                    LinkedHashMap<String, String> updatedData = SOLVED_DATA.get(id);
                    Data data = new Data(id, updatedData.get("username"), updatedData.get("email_address"),
                            updatedData.get("password"), updatedData.get("service"), updatedData.get("additional_data"));

                    Event event = new Event("update-data", data);
                    ITC.request(event);
                }

                // After all the conflicts have been solved, we can update the Google Drive status to complete the
                // synchronization
                ITC.request(new Event("google-drive-synchronization-conflicts-solved"));
                // TODO. Add a notification to let the user know that the conflicts have been solved correctly
                dispose();  // The dialog is destroyed as it's not required anymore
            }
        });

        this.cancelButton.addActionListener(e -> dispose());
    }

    /**
     * Adds all the panels that contain the conflicted data to the dialog
     */
    private void addConflictedData(){
        for (Data remoteData : this.CONFLICT_DATA){
            // Decrypt in the backend the data to compare them
            Data localData = getData(remoteData.getID());  // The local data must have the same ID of the remote one
            remoteData = getData(remoteData);

            LinkedHashMap<String, String> localUserData = localData.asLinkedHashMap();
            LinkedHashMap<String, String> remoteUserData = remoteData.asLinkedHashMap();

            this.SOLVED_DATA.put(localData.getID(), localUserData);  // By default, the local one is always chosen

            // Each data has its own panel where the conflicted fields are shown
            JPanel conflictedDataPanel = new JPanel();
            conflictedDataPanel.setLayout(new MigLayout());
            conflictedDataPanel.add(new JLabel(localData.getSERVICE()), "span, wrap");

            // Iterate over each field and show only the ones that are different
            for (String key : localUserData.keySet()){
                if (!localUserData.get(key).equals(remoteUserData.get(key))){
                    // The button group allows to select only of the two radio buttons
                    ButtonGroup buttonGroup = new ButtonGroup();

                    JRadioButton localDataRadio = new JRadioButton();
                    localDataRadio.setText(localUserData.get(key));
                    localDataRadio.setSelected(true);  // By default, the local one is always chosen to avoid having to
                                                       // check whether all the conflicts have been solved: in this way
                                                       // the solve button is already enabled

                    // If the radio button is selected, then we update it in the solved data: the HashMap that will be
                    // used to update the database in the backend
                    localDataRadio.addActionListener(
                            e -> SOLVED_DATA.get(localData.getID()).put(key, localUserData.get(key))
                    );

                    JRadioButton remoteDataRadio = new JRadioButton();
                    remoteDataRadio.setText(remoteUserData.get(key));
                    remoteDataRadio.addActionListener(
                            e -> SOLVED_DATA.get(localData.getID()).put(key, remoteUserData.get(key))
                    );

                    buttonGroup.add(localDataRadio);
                    buttonGroup.add(remoteDataRadio);

                    conflictedDataPanel.add(new JLabel(key), "wrap");  // The name of the field to compare
                    conflictedDataPanel.add(localDataRadio, "wrap");
                    conflictedDataPanel.add(remoteDataRadio, "wrap");
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
        Event response = this.ITC.request(request);

        return (Data) response.getData().getFirst();
    }

    /**
     * Get the decrypted data of a record based on its ID
     */
    private Data getData(int id){
        Event request = new Event("get-data", id);
        Event response = this.ITC.request(request);

        return (Data) response.getData().getFirst();
    }

}
