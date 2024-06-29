package com.brosu.filex;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

class FileListAdapter extends ArrayAdapter<FileItem> {

    private Context mContext;
    private int mResource;
    private ArrayList<FileItem> mItems;
    private SimpleDateFormat mDateFormat;

    public FileListAdapter(Context context, int resource, ArrayList<FileItem> items) {
        super(context, resource, items);
        this.mContext = context;
        this.mResource = resource;
        this.mItems = items;
        this.mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(mResource, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.iconImageView = convertView.findViewById(R.id.iconImageView);
            viewHolder.nameTextView = convertView.findViewById(R.id.nameTextView);
            viewHolder.lastChangedTextView = convertView.findViewById(R.id.lastChangedTextView);
            viewHolder.itemsTextView = convertView.findViewById(R.id.itemsTextView);
            viewHolder.sizeTextView = convertView.findViewById(R.id.sizeTextView); // TextView for file size

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        FileItem item = mItems.get(position);

        // Set icon
        viewHolder.iconImageView.setImageResource(item.getIconResourceId());

        // Set file name
        viewHolder.nameTextView.setText(item.getName());

        // Special handling for special items (Home and ..)
        if (item.getName().equals("Home") || item.getName().equals("..")) {
            viewHolder.lastChangedTextView.setVisibility(View.GONE);
            viewHolder.itemsTextView.setVisibility(View.GONE);
            viewHolder.sizeTextView.setVisibility(View.GONE); // Hide size TextView for special items
        } else {
            // Set last changed date
            if (item.getLastChanged() != null) {
                String formattedDate = mDateFormat.format(item.getLastChanged());
                viewHolder.lastChangedTextView.setText("Last Changed: " + formattedDate);
                viewHolder.lastChangedTextView.setVisibility(View.VISIBLE);
            } else {
                viewHolder.lastChangedTextView.setText("Last Changed: Not available");
                viewHolder.lastChangedTextView.setVisibility(View.VISIBLE);
            }

            // Show item count for directories
            if (item.isDirectory()) {
                File directory = new File(item.getPath());
                File[] files = directory.listFiles();
                int itemCount = files != null ? files.length : 0;
                viewHolder.itemsTextView.setText("Items: " + itemCount);
                viewHolder.itemsTextView.setVisibility(View.VISIBLE);
            } else {
                viewHolder.itemsTextView.setVisibility(View.GONE);
            }

            // Display file size if it's a file
            if (!item.isDirectory()) {
                viewHolder.sizeTextView.setVisibility(View.VISIBLE);
                viewHolder.sizeTextView.setText(formatFileSize(item.getFileSize())); // Implement formatFileSize method
            } else {
                viewHolder.sizeTextView.setVisibility(View.GONE); // Hide size TextView for directories
            }
        }

        return convertView;
    }

    static class ViewHolder {
        ImageView iconImageView;
        TextView nameTextView;
        TextView lastChangedTextView;
        TextView itemsTextView;
        TextView sizeTextView;
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "0 KB"; // Handle zero and negative sizes
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
