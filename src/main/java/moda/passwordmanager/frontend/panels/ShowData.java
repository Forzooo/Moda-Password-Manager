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
    private final InterThreadCommunication ITC;

    private JTabbedPane dataTabbedPane;  // The tabbed pane shows the dataList and the data the user has selected

    private ModaTextField searchBar;

    /**
     * The service fields shown in the JList, which are updated by the FrontendEventListener
     */
    private final ArrayList<Data> USER_DATA;  // A Data object is required as each service shown needs to be associated with its ID
    private final DefaultListModel<String> USER_DATA_MODEL;

    // Swing components
    private ModaList dataList;

    public ShowData(InterThreadCommunication itc){
        super();  // Initialize the Panel

        // Set the attributes given by the JFrame
        this.ITC = itc;

        // Initialize the user data ArrayList and Model
        this.USER_DATA = new ArrayList<>();
        this.USER_DATA_MODEL = new DefaultListModel<>();

        initPanel();
        initComponents();
        initListeners();
    }

    /**
     * Set the configuration of the JPanel
     */
    private void initPanel(){
        // A generic boxlayout can be used as it's the only component of the panel
        setLayout(new MigLayout("debug, insets 30 30 30 30, fill"));
    }

    /**
     * Initialize the components of the panel
     */
    private void initComponents(){
        this.dataTabbedPane = new JTabbedPane();

        // Create the JList used to show all the data saved inside the database
        this.dataList = new ModaList(this.USER_DATA_MODEL, 20, 30);

        this.dataList.setBackground(null);
        this.dataList.setSelectionBackground(Color.black);

        ModaScrollPane scrollPane = new ModaScrollPane(this.dataList);
        ModaScrollBarUI modaScrollBarUI = new ModaScrollBarUI();

        scrollPane.getVerticalScrollBar().setUI(modaScrollBarUI);

        scrollPane.setBorder(new EmptyBorder(10,10,10,10));
        scrollPane.setBackground(Color.WHITE);

        this.dataTabbedPane.addTab("User Data", scrollPane);  // Add the default tab which cannot be closed

        int searchBarSize = 50;  // The size of the search bar
        this.searchBar = new ModaTextField(ModaTextField.TextFieldStyle.CLASSIC, searchBarSize);

        JLabel searchIconLabel = new JLabel();
        ImageIcon searchIcon = Utilities.getIcon("search_icon.png");
        searchIcon.setImage(searchIcon.getImage().getScaledInstance(searchBarSize, searchBarSize, Image.SCALE_SMOOTH));
        searchIconLabel.setIcon(searchIcon);

        this.searchBar.putClientProperty("JTextField.trailingComponent", searchIconLabel);

        add(this.dataTabbedPane, "grow, push, wrap");
        add(this.searchBar, "span, growx, pushx, split 2");
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
                    int id = USER_DATA.get(dataList.getSelectedIndex()).getID();

                    // If the tab exists, then set it to be the selected one instead of creating a new tab for it
                    if (checkDataTabExist(id)){
                        dataTabbedPane.setSelectedIndex(indexOfDataTab(id));
                        return;
                    }

                    addDataTab(USER_DATA.get(dataList.getSelectedIndex()).getID());
                }
            }
        });

//        this.dataList.setCellRenderer(new DefaultListCellRenderer(){
//            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
//                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
//
//                if (isSelected){  // The selected row has black background
//                    c.setBackground(Color.BLACK);
//                } else {  // The other rows have two different colors
//                    if (index % 2 == 0){
//                        c.setBackground(new Color(255, 255, 255));
//                    } else {
//                        c.setBackground(new Color(241, 241, 241));
//                    }
//                }
//                return c;
//            }
//        });

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

                USER_DATA_MODEL.clear();  // Clear the model to add only services that start with the input of the user

                // If the search text is empty, add all the services to the model
                if (searchText.isEmpty()) {
                    for (Data d : USER_DATA) {
                        USER_DATA_MODEL.addElement(d.getSERVICE());
                    }
                    return;
                }

                // Iterate over all the data and add the services that starts with the input of the user
                for (Data data : USER_DATA) {
                    if (data.getSERVICE().toLowerCase().trim().startsWith(searchText)) {
                        USER_DATA_MODEL.addElement(data.getSERVICE());
                    }
                }
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
        userDataTab.putClientProperty("JTabbedPane.tabClosable", true);  // Set only the tab to be closeable, not the
                                                                         // entire JTabbedPane
        userDataTab.putClientProperty("JTabbedPane.tabCloseCallback",
                (IntConsumer) tabIndex -> this.dataTabbedPane.remove(tabIndex));

        String tabName = this.USER_DATA.get(this.dataList.getSelectedIndex()).getSERVICE();  // Get the tab name from the service field
        this.dataTabbedPane.add(tabName, userDataTab);
        this.dataTabbedPane.setSelectedComponent(userDataTab);  // Set the tab to be shown to be the one created
    }

    public ArrayList<Data> getUSER_DATA() {
        return this.USER_DATA;
    }

    public DefaultListModel<String> getUSER_DATA_MODEL() {
        return this.USER_DATA_MODEL;
    }

    /**
     * Return the index of the Data object of UserData that has the same ID. -1 is returned if it does not exist
     */
    public int indexOfUserData(int id){
        for (int i = 0; i < this.USER_DATA.size(); i++){
            if (this.USER_DATA.get(i).getID() == id) {
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
