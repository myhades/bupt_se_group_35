package org.group35.util;

import javafx.stage.FileChooser;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.List;

/**
 * Utility for showing native file/directory pickers.
 * All methods must be called on the JavaFX Application Thread.
 */
public final class FileUtils {

    private FileUtils() { /* no instantiation */ }

    /**
     * Show an “Open File” dialog.
     *
     * @param owner    the owner Window (e.g. your primary Stage or scene.getWindow())
     * @param title    the dialog title
     * @param initialDirectory optional starting directory (may be null)
     * @param extensionFilters optional list of FileChooser.ExtensionFilter (may be null or empty)
     * @return the selected File, or null if the user canceled
     */
    public static File chooseFile(Window owner,
                                  String title,
                                  File initialDirectory,
                                  List<FileChooser.ExtensionFilter> extensionFilters)
    {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        if (initialDirectory != null && initialDirectory.isDirectory()) {
            chooser.setInitialDirectory(initialDirectory);
        }
        if (extensionFilters != null) {
            chooser.getExtensionFilters().setAll(extensionFilters);
        }
        return chooser.showOpenDialog(owner);
    }

    /**
     * Show an “Open Multiple Files” dialog.
     *
     * @param owner
     * @param title
     * @param initialDirectory
     * @param extensionFilters
     * @return list of selected Files (possibly empty), or null if canceled
     */
    public static List<File> chooseFiles(Window owner,
                                         String title,
                                         File initialDirectory,
                                         List<FileChooser.ExtensionFilter> extensionFilters)
    {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        if (initialDirectory != null && initialDirectory.isDirectory()) {
            chooser.setInitialDirectory(initialDirectory);
        }
        if (extensionFilters != null) {
            chooser.getExtensionFilters().setAll(extensionFilters);
        }
        return chooser.showOpenMultipleDialog(owner);
    }

    /**
     * Show a “Save File” dialog.
     *
     * @param owner
     * @param title
     * @param initialDirectory
     * @param extensionFilters
     * @return the File to save to, or null if canceled
     */
    public static File chooseSaveFile(Window owner,
                                      String title,
                                      File initialDirectory,
                                      List<FileChooser.ExtensionFilter> extensionFilters)
    {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        if (initialDirectory != null && initialDirectory.isDirectory()) {
            chooser.setInitialDirectory(initialDirectory);
        }
        if (extensionFilters != null) {
            chooser.getExtensionFilters().setAll(extensionFilters);
        }
        return chooser.showSaveDialog(owner);
    }

    /**
     * Show a “Select Directory” dialog.
     *
     * @param owner
     * @param title
     * @param initialDirectory
     * @return the chosen directory, or null if canceled
     */
    public static File chooseDirectory(Window owner,
                                       String title,
                                       File initialDirectory)
    {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(title);
        if (initialDirectory != null && initialDirectory.isDirectory()) {
            chooser.setInitialDirectory(initialDirectory);
        }
        return chooser.showDialog(owner);
    }
}
