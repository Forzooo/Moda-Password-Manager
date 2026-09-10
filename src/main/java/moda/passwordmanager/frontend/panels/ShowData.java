package moda.passwordmanager.frontend.panels;

import moda.passwordmanager.backend.Data;
import moda.passwordmanager.frontend.Utilities;
import moda.passwordmanager.frontend.components.*;
import moda.passwordmanager.interthreadcommunication.InterThreadCommunication;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.function.IntConsumer;

public class ShowData extends JPanel {

    // Attribute to communicate with the backend
    private final InterThreadCommunication itc;

    private JTabbedPane dataTabbedPane;  // The tabbed pane shows the dataList and the data the user has selected

    private ModaTextField searchBar;

    /**
     * The service fields shown in the JList, which are updated by the FrontendEventListener
     */
    private final ArrayList<Data> userData;  // A Data object is required as each service shown needs to be associated with its ID
    private final ArrayList<Data> userFilteredData;  // The Data objects that match the selection IDs of the USER_DATA_MODEL
                                                       // because using the USER_DATA one would not match the indexes
    private final DefaultListModel<String> userDataModel;

    // Swing components
    private JList<String> dataList;

    public ShowData(InterThreadCommunication itc){
        super();  // Initialize the Panel

        // Set the attributes given by the JFrame
        this.itc = itc;

        // Initialize the user data ArrayList and Model
        this.userData = new ArrayList<>();
        this.userFilteredData = new ArrayList<>();
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
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    /**
     * Initialize the components of the panel
     */
    private void initComponents(){
        JPanel userDataPanel = new JPanel();  // In the userDataPanel the scrollPane and the search bar are added
        userDataPanel.setLayout(new MigLayout("insets 10 10 10 10, fill"));

        this.dataTabbedPane = new JTabbedPane();

        // Create the JList used to show all the data saved inside the database
        this.dataList = new JList<>();
        this.dataList.setModel(this.userDataModel);
        this.dataList.setFont(new Font(UIManager.getString("Moda.GeneralUseFontFamily"), Font.PLAIN, 20));
        this.dataList.setFixedCellHeight(30);
        this.dataList.setBackground(null);

        ModaScrollPane scrollPane = new ModaScrollPane(this.dataList);
        scrollPane.setBorder(new EmptyBorder(10,10,10,10));
        scrollPane.setBackground(Color.WHITE);

        int searchBarSize = 50;  // The size of the search bar
        this.searchBar = new ModaTextField(searchBarSize);

        JLabel searchIconLabel = new JLabel();
        ImageIcon searchIcon = Utilities.getIcon("search_icon.png");
        searchIcon.setImage(searchIcon.getImage().getScaledInstance(searchBarSize, searchBarSize, Image.SCALE_SMOOTH));
        searchIconLabel.setIcon(searchIcon);

        this.searchBar.putClientProperty("JTextField.trailingComponent", searchIconLabel);
        this.searchBar.putClientProperty("JTextField.placeholderText", Utilities.getLocaleString("Moda.ShowData.searchBarPlaceholder"));

        // Add the scroll pane and the search bar into the userDataPanel to make them both in the same tab of the dataTabbedPane
        userDataPanel.add(scrollPane, "grow, push, wrap");
        userDataPanel.add(this.searchBar, "span, growx, pushx");

        this.dataTabbedPane.addTab(getPanelTitle(), userDataPanel);
        add(this.dataTabbedPane);
    }

    /**
     * Initialize all the listeners on the components
     */
    private void initListeners(){
        this.dataList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                // Only allow double clicks or more
                if (e.getClickCount() >= 2) {
                    // Retrieve the ID selected by getting it from the USER_DATA attribute if the search bar has not
                    // been used as the USER_FILTERED_DATA is empty in that case, otherwise get it from the USER_FILTERED_DATA
                    // because otherwise the selectedIndex would not match the ID of USER_DATA
                    int id;
                    if (searchBar.getText().trim().isBlank()){
                        id = userData.get(dataList.getSelectedIndex()).getId();
                    }else{
                        id = userFilteredData.get(dataList.getSelectedIndex()).getId();
                    }

                    // If the tab exists, then set it to be the selected one instead of creating a new tab for it
                    if (checkDataTabExist(id)){
                        dataTabbedPane.setSelectedIndex(indexOfDataTab(id));
                        return;
                    }

                    addDataTab(id);
                }
            }
        });

        this.dataList.setCellRenderer(new DefaultListCellRenderer(){
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (isSelected){  // The selected row has black background
                    c.setBackground(UIManager.getColor("Moda.ShowData.DataList.selectionBackground"));
                } else if (index % 2 == 0) {  // The other rows have two different colors based on the index
                        c.setBackground(UIManager.getColor("Moda.ShowData.DataList.evenBackground"));
                } else {
                    c.setBackground(UIManager.getColor("Moda.ShowData.DataList.oddBackground"));
                }
                return c;
            }
        });

        this.searchBar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filter();
            }

            /**
             * Filter the data model to show only the services that start with the input of the user
             */
            private void filter(){
                String searchText = searchBar.getText().toLowerCase().trim();  // Get the input of the user

                userDataModel.clear();  // Clear the model to add only services that start with the input of the user
                userFilteredData.clear();

                // If the search text is empty, add all the services to the model
                if (searchText.isEmpty()) {
                    for (Data data : userData) {
                        userDataModel.addElement(data.getService());
                        userFilteredData.add(data);
                    }
                    return;
                }

                // Iterate over all the data and add the services that starts with the input of the user
                for (Data data : userData) {
                    if (data.getService().toLowerCase().trim().startsWith(searchText)) {
                        userDataModel.addElement(data.getService());
                        userFilteredData.add(data);
                    }
                }
            }
        });
    }

    /**
     * Returns the title of the panel
     */
    public static String getPanelTitle() {
        return Utilities.getLocaleString("Moda.ShowData.panelTitle");
    }

    /**
     * Check whether the service selected, thus its ID, has already a tab
     */
    private boolean checkDataTabExist(int id){
        // Iterate over all the UserData tabs and check if their ID is the same as the one given
        for (int i = 1; i < this.dataTabbedPane.getTabCount(); i++){
            Component tab = this.dataTabbedPane.getComponentAt(i);
            if (tab.getClass() == UserData.class && ((UserData) tab).getId() == id){  // Ensure that the tab is a UserData one
                    return true;
            }
        }
        return false;
    }

    /**
     * Add a tab to the TabbedPane with the service selected by the user
     */
    private void addDataTab(int id){
        UserData userDataTab = new UserData(this.itc, id);
        userDataTab.putClientProperty("JTabbedPane.tabClosable", true);  // Set only the tab to be closeable, not the
                                                                         // entire JTabbedPane
        userDataTab.putClientProperty("JTabbedPane.tabCloseCallback",
                (IntConsumer) tabIndex -> this.dataTabbedPane.remove(tabIndex));

        String tabName = userDataTab.getTitle();  // Get the tab name from the object itself
        this.dataTabbedPane.add(tabName, userDataTab);
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
            if (this.userData.get(i).getId() == id) {
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
                if (((UserData) tab).getId() == id){
                    return i;
                }
            }
        }
        return -1;
    }
}