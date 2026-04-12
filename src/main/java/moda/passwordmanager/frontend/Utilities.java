package moda.passwordmanager.frontend;

import com.formdev.flatlaf.util.SystemFileChooser;
import moda.passwordmanager.Application;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

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

    /**
     * Get the stack trace of the exception raised
     * @param throwable The exception raised
     * @return The stack trace formatted as a string
     */
    public static String getStackTrace(Throwable throwable){
        // StringWriter and PrintWriter are used to get the stack trace of the exception into the string format
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);

        return stringWriter.toString();
    }

    /**
     * Get the last n rows of a stack trace
     * @param stackTrace The stack trace
     */
    public static String getStackTraceRows(String stackTrace, int rows){
        String[] stackTraceArray = stackTrace.split("\n");  // Split the string by the \n character
        StringBuilder newStackTrace = new StringBuilder();

        // We need to include the "Caused by" text in the stack so we need to increment by 1 the stack rows
        for (int i = 0; i < rows+1; i++){
            // If the length of stackTrace is less than the number of rows, break the for loop
            if (i == stackTraceArray.length){
                break;
            }
            newStackTrace.append(stackTraceArray[i]).append("\n");
        }

        // If the stack trace is longer than the number of rows, we show triple dots to indicate that there are more
        // lines than displayed
        if (rows + 1 < stackTraceArray.length){
            newStackTrace.append("... (").append(stackTraceArray.length - rows).append(" more line");

            // Add the "s" to line if there are multiple lines hidden
            if (stackTraceArray.length - rows - 1 > 1){
                newStackTrace.append("s");
            }
            newStackTrace.append(" hidden)");
        }

        return newStackTrace.toString();
    }

    /**
     * Returns an icon based on its name
     * @param name The name of the file (ex. icon.png)
     */
    public static ImageIcon getIcon(String name){
        return new ImageIcon(Utilities.class.getResource("/icons/"+name));
    }

    /**
     * Applies all the default properties to a button that is used as a trailing component
     */
    public static void applyTrailingButtonProperties(JButton button){
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(50, 70));
        button.setMaximumSize(new Dimension(50, 70));
    }

    /**
     * Get the filename from a path
     */
    public static String getFilenameFromPath(String path){
        Path filePath = Paths.get(path);

        return filePath.getFileName().toString();
    }

    /**
     * Get a string from the locale file for the current language used
     */
    public static String getLocaleString(String key){
        // Create the resource bundle using the locale defined in the Application class. As the files are in the locale
        // directory, which is a subdirectory of resources, we need to specify it in the baseName of the getBundle method
        ResourceBundle resourceBundle = ResourceBundle.getBundle("locales/locale", Application.getApplicationLocale());
        return resourceBundle.getString(key);
    }

}
