package com.brosu.filex;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileItem {
    private String name;
    private String path; // Path of the file or directory
    private boolean isDirectory;
    private int iconResourceId; // Resource ID for icon
    private Date lastChanged; // Last changed date
    private long fileSize; // File size

    // Special directory names
    private static final String PARENT_DIRECTORY = "..";
    private static final String HOME_DIRECTORY = "Home";

    public FileItem(String name, String path, boolean isDirectory) {
        this.name = name;
        this.path = path;
        this.isDirectory = isDirectory;
        this.iconResourceId = 0; // Default icon (if needed)
        this.lastChanged = new Date(); // Default to current date/time
        this.fileSize = 0; // Default to 0 for directories
    }

    public FileItem(String name, String path, boolean isDirectory, int iconResourceId) {
        this.name = name;
        this.path = path;
        this.isDirectory = isDirectory;
        this.iconResourceId = iconResourceId;
        this.lastChanged = new Date(); // Default to current date/time
        this.fileSize = 0; // Default to 0 for directories
    }

    public FileItem(String name, String path, boolean isDirectory, int iconResourceId, long lastModified, long fileSize) {
        this.name = name;
        this.path = path;
        this.isDirectory = isDirectory;
        this.iconResourceId = iconResourceId;
        this.lastChanged = new Date(lastModified);
        this.fileSize = fileSize;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public int getIconResourceId() {
        return iconResourceId;
    }

    public Date getLastChanged() {
        return lastChanged;
    }

    public String getLastChangedFormatted() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(lastChanged);
    }

    public long getFileSize() {
        return fileSize;
    }

    // Getters for special directory names
    public static String getParentDirectory() {
        return PARENT_DIRECTORY;
    }

    public static String getHomeDirectory() {
        return HOME_DIRECTORY;
    }

    @Override
    public String toString() {
        return name;
    }
}
