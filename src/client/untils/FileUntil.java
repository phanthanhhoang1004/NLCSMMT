package src.client.untils;

import java.sql.Timestamp;

public class FileUntil {
    public static String readableFileSize(long size) {
        if (size <= 0)
            return "0 B";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    public static String formatUploadTime(Timestamp timestamp) {
        if (timestamp == null)
            return "N/A";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        return sdf.format(timestamp);
    }
}
