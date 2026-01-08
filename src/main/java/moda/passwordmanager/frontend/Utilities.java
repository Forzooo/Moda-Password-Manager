package moda.passwordmanager.frontend;

import com.formdev.flatlaf.util.SystemFileChooser;

import javax.swing.filechooser.FileSystemView;

/**
 * The Utilities class provides general use APIs that can be used by any Frontend class.
 */
public class Utilities {

    /**
     * Perform some initial check on the master password to allow only ones that comply with all the requirements
     * @param masterPassword The master password the user entered
     * @return Boolean to indicate whether the checks have been passed
     */
    public static boolean checkMasterPassword(char[] masterPassword){
        return masterPassword.length != 0;
    }

    /**
    * Opens the FlatLaf File Chooser and let the user select a database to use
    * @return Returns the absolute path of the database if one is selected, otherwise an empty string
    */
    public static String openDatabaseFileChooser(){
        // Create the File Chooser that opens in the desktop view, and selects only .modb files
        SystemFileChooser fileChooser = new SystemFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        fileChooser.setDialogTitle("Choose a database to use");
        fileChooser.setAcceptAllFileFilterUsed(false);  // Don't accept all the types of files
        fileChooser.addChoosableFileFilter(new SystemFileChooser.FileNameExtensionFilter(
                "Moda Password Manager Database","modb"));

        // Open the file chooser in the current dialogue and check that the user has chosen a database file
        if (fileChooser.showOpenDialog(null) == SystemFileChooser.APPROVE_OPTION){
            String path = fileChooser.getSelectedFile().getAbsolutePath();  // Retrieve the path chosen

            // Check whether the database has been chosen
            if (!path.isBlank()){
                return path;
            }
        }
        return "";
    }

    /**
    * Opens the Swing File Chooser and let the user create a database to use
    * @return Returns the absolute path of the database if one is created, otherwise an empty string
    */
    public static String newDatabaseFileChooser(){
        // Create the File Chooser that opens in the desktop view, and saves a .modb file
        SystemFileChooser fileChooser = new SystemFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        fileChooser.setDialogTitle("Create a new database to use");
        fileChooser.setAcceptAllFileFilterUsed(false);  // Don't accept all the types of files
        fileChooser.addChoosableFileFilter(new SystemFileChooser.FileNameExtensionFilter(
                "Moda Password Manager Database","modb"));

        // Open the file chooser
        if (fileChooser.showSaveDialog(null) == SystemFileChooser.APPROVE_OPTION){
            String path = fileChooser.getSelectedFile().getAbsolutePath();  // Retrieve the path chosen

            // Check whether the database has been chosen
            if (!path.isEmpty()){
                // If the file has been saved without setting the extension, set it automatically
                if (!path.endsWith(".modb")){
                    path = path+".modb";
                }
                return path;
            }
        }
        return "";
    }

}
