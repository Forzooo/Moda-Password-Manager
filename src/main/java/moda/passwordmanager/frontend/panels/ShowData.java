package moda.passwordmanager.frontend.panels;

import moda.passwordmanager.backend.Data;
import moda.passwordmanager.frontend.components.CloseTab;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class ShowData extends JPanel {

    // Attribute to communicate with the backend
    private final InterThreadCommunication ITC;

    private JTabbedPane dataTabbedPane;  // The tabbed pane shows the dataList and the data the user has selected

    /**
     * The service data shown in the JList of "Show Data" panel which it's updated automatically by the timer
     */
    private ArrayList<Data> userData;  // A Data object is required as each service shown needs to be associated with its ID
    private DefaultListModel<String> userDataModel;

    // Swing components
    private JList<String> dataList;

    public ShowData(InterThreadCommunication itc){
        super();  // Initialize the Panel

        // Set the attributes given by the JFrame
        this.ITC = itc;

        // Initialize the user data ArrayList and Model
        this.userData = new ArrayList<>();
        this.userDataModel = new DefaultListModel<>();

        initPanel();
        initComponents();
        initListeners();
    }

    /**
     * Set the configuration of the JPanel
     */
    private void initPanel(){
        // A generic boxlayout can be used as it's the only component of the panel
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    }

    /**
     * Initialize the components of the panel
     */
    private void initComponents(){
        this.dataTabbedPane = new JTabbedPane();

        // Create the JList used to show all the data saved inside the database
        this.dataList = new JList<>();
        this.dataList.setFixedCellHeight(30);
        this.dataList.setModel(this.userDataModel);  // Set the model of the JList (Strings containing service data)
        this.dataList.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 20));

        this.dataList.setSelectionBackground(Color.black);
        this.dataList.setSelectionForeground(Color.white);

        JScrollPane scrollPane = new JScrollPane(this.dataList);
        scrollPane.setPreferredSize(new Dimension(1000, 750));
        scrollPane.setBorder(new EmptyBorder(10,30,10,30));

        this.dataTabbedPane.addTab("User Data", scrollPane);

        add(this.dataTabbedPane, BorderLayout.CENTER);
    }

    /**
     * Initialize all the listeners on the components
     */
    private void initListeners(){
        this.dataList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                // Only allow double clicks
                if (e.getClickCount() == 2) {
                    // Retrieve the ID selected by getting it from the userData attribute and check if a tab with that
                    // ID already exists
                    int id = userData.get(dataList.getSelectedIndex()).getID();

                    // If the tab exists, then set it to be the selected one instead of creating a new tab for it
                    if (checkDataTabExist(id)){
                        dataTabbedPane.setSelectedIndex(indexOfDataTab(id));
                        return;
                    }

                    addDataTab(userData.get(dataList.getSelectedIndex()).getID());
                }
            }
        });

        this.dataList.setCellRenderer(new DefaultListCellRenderer(){ //imposto un metodo per far renderizzare le celle della lista come mi pare
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) { //questo non so bene cosa sia, ma nell'esempio che ho spudoratamente copiato era così
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (!isSelected) {  //questo perché invece quando è selezionato sarà nero
                    if (! (index % 2 == 0)) {   //banalmente se la riga è pari avrà un colore di sfondo, mentre se è dispari un'altro
                        c.setBackground(new Color(241, 241, 241)); // pari righe
                    } else {
                        c.setBackground(new Color(255, 255, 255)); // righe dispari
                    }
                } else {
                    c.setBackground(list.getSelectionBackground());
                    c.setForeground(list.getSelectionForeground());
                }

                return c;

            }
        });
    }

    /**
     * Returns the title of the panel
     */
    public static String getPanelTitle() {
        return "Show Data";
    }

    /**
     * Check whether the service selected, thus its ID, has already a tab
     */
    private boolean checkDataTabExist(int id){
        // Iterate over all the UserData tabs and check if their ID is the same as the one given
        for (int i = 1; i < this.dataTabbedPane.getTabCount(); i++){
            Component tab = this.dataTabbedPane.getComponentAt(i);
            if (tab.getClass() == UserData.class){  // Ensure that the tab is a UserData one
                if (((UserData) tab).getID() == id){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Add a tab to the TabbedPane with the service selected by the user
     */
    private void addDataTab(int id){
        UserData userDataTab = new UserData(this.ITC, id);
        String tabName = this.userData.get(this.dataList.getSelectedIndex()).getSERVICE();  // Get the tab name from the service field
        CloseTab closeTab = new CloseTab(this.dataTabbedPane, userDataTab, tabName);
        closeTab.add();  // Add the tab to the TabbedPane
        this.dataTabbedPane.setSelectedComponent(userDataTab);  // Set the tab to be shown to be the one created
    }

    public ArrayList<Data> getUserData() {
        return this.userData;
    }

    public DefaultListModel<String> getUserDataModel() {
        return this.userDataModel;
    }

    /**
     * Return the index of the Data object of UserData that has the same ID. -1 is returned if it does not exist
     */
    public int indexOfUserData(int id){
        for (int i = 0; i < this.userData.size(); i++){
            if (this.userData.get(i).getID() == id) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Return the index of the UserData tab that has the same ID. -1 is returned if it does not exist
     */
    private int indexOfDataTab(int id){
        // Iterate over all the UserData tabs and check if their ID is the same as the one given
        for (int i = 0; i < this.dataTabbedPane.getTabCount(); i++){
            Component tab = this.dataTabbedPane.getComponentAt(i);
            if (tab.getClass() == UserData.class){  // Ensure that the tab is a UserData one
                if (((UserData) tab).getID() == id){
                    return i;
                }
            }
        }
        return -1;
    }

}
