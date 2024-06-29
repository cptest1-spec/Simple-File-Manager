package com.brosu.filex;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends Activity {

    private TextView currentPathTextView;
    private ListView listView;
    private ArrayList<FileItem> fileList;
    private FileListAdapter adapter;
    private String currentPath;

    private Dialog searchDialog;
    private ArrayList<String> searchResults;

    private TextView emptyDirectoryTextView; // TextView for empty directory message

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentPathTextView = findViewById(R.id.currentPathTextView);
        listView = findViewById(R.id.listView);
        emptyDirectoryTextView = findViewById(R.id.emptyDirectoryTextView); // Initialize empty directory TextView

        // Set initial path to /sdcard/
        currentPath = Environment.getExternalStorageDirectory().getPath();
        displayFiles(currentPath);

        // Setup search button click listener
        Button searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					openSearchDialog();
				}
			});

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					FileItem selectedItem = fileList.get(position);
					if (selectedItem.getName().equals("..")) {
						navigateToParentDirectory();
					} else if (selectedItem.isDirectory()) {
						if (selectedItem.getName().equals("Home")) {
							displayFiles(Environment.getExternalStorageDirectory().getPath());
						} else {
							displayFiles(selectedItem.getPath());
						}
					} else {
						// Handle file click (open file if needed)
						Toast.makeText(MainActivity.this, "Opening: " + selectedItem.getPath(), Toast.LENGTH_SHORT).show();
						// Implement your file opening logic here
					}
				}
			});
    }
	private void displayFiles(String path) {
		currentPath = path;
		currentPathTextView.setText("Current path: " + path);

		fileList = new ArrayList<>();
		File directory = new File(path);

		// Add special directory "Home" if not in the home directory
		if (!path.equals(Environment.getExternalStorageDirectory().getPath())) {
			fileList.add(new FileItem("Home", Environment.getExternalStorageDirectory().getPath(), true, R.drawable.hm, 0, 0));
			fileList.add(new FileItem("..", directory.getParent(), true, R.drawable.op, 0, 0));
		}

		File[] files = directory.listFiles();
		if (files != null) {
			for (File file : files) {
				String fileName = file.getName();
				boolean isDirectory = file.isDirectory();
				long lastModified = file.lastModified(); // Get last modified timestamp

				if (isDirectory) {
					fileList.add(new FileItem(fileName, file.getPath(), true, R.drawable.ff, lastModified, 0));
				} else {
					long fileSize = file.length(); // Get file size
					int iconResourceId = R.drawable.f; // Change this to your file icon resource ID

					// Create a new FileItem instance with correct parameters
					fileList.add(new FileItem(fileName, file.getPath(), false, iconResourceId, lastModified, fileSize));
				}
			}
		}

		if (fileList.isEmpty()) {
			showEmptyDirectoryMessage(); // Show message if directory is empty
		} else {
			hideEmptyDirectoryMessage(); // Hide message if directory has items
		}

		adapter = new FileListAdapter(this, R.layout.item_file, fileList);
		listView.setAdapter(adapter);
	}

    private void navigateToParentDirectory() {
        File file = new File(currentPath);
        String parentPath = file.getParent();
        displayFiles(parentPath);
    }

    public void openSearchDialog() {
        searchDialog = new Dialog(this);
        searchDialog.setContentView(R.layout.dialog_search);

        final EditText searchQueryEditText = searchDialog.findViewById(R.id.searchQueryEditText);
        final RadioGroup resultsRadioGroup = searchDialog.findViewById(R.id.resultsRadioGroup);
        final EditText extensionEditText = searchDialog.findViewById(R.id.extensionEditText);
        final CheckBox regexCheckBox = searchDialog.findViewById(R.id.regexCheckBox);
        final CheckBox caseSensitiveCheckBox = searchDialog.findViewById(R.id.caseSensitiveCheckBox);
        Button cancelButton = searchDialog.findViewById(R.id.cancelButton);
        Button searchStartButton = searchDialog.findViewById(R.id.searchStartButton);

        resultsRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					if (checkedId == R.id.radioFiles) {
						extensionEditText.setVisibility(View.VISIBLE);
					} else {
						extensionEditText.setVisibility(View.GONE);
					}
				}
			});

        cancelButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					searchDialog.dismiss();
				}
			});

        searchStartButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String query = searchQueryEditText.getText().toString().trim();
					if (query.isEmpty()) {
						Toast.makeText(MainActivity.this, "Cannot search nothing ¯\\_(ツ)_/¯", Toast.LENGTH_SHORT).show();
						return;
					}

					boolean searchFiles = resultsRadioGroup.getCheckedRadioButtonId() == R.id.radioFiles;
					String extension = extensionEditText.getText().toString().trim();
					boolean isRegexSearch = regexCheckBox.isChecked();
					boolean isCaseSensitive = caseSensitiveCheckBox.isChecked();

					// Start search process
					startSearch(query, searchFiles, extension, isRegexSearch, isCaseSensitive);
					searchDialog.dismiss();
				}
			});

        searchDialog.show();
    }

    private void startSearch(final String query, final boolean searchFiles, final String extension,
							 final boolean isRegexSearch, final boolean isCaseSensitive) {
		// Initialize search progress dialog
		final Dialog progressDialog = new Dialog(this);
		progressDialog.setContentView(R.layout.dialog_search_progress);
		progressDialog.setCancelable(false); // Prevent canceling

		final ProgressBar progressBar = progressDialog.findViewById(R.id.progressBar);
		final TextView progressText = progressDialog.findViewById(R.id.progressText);
		Button cancelButton = progressDialog.findViewById(R.id.cancelButton);

		cancelButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					progressDialog.dismiss();
				}
			});

		// Simulating a search process
		Thread searchThread = new Thread(new Runnable() {
				@Override
				public void run() {
					searchResults = new ArrayList<>(); // Initialize search results

					// Simulated search progress
					for (int progress = 1; progress <= 100; progress++) {
						final int currentProgress = progress;
						runOnUiThread(new Runnable() {
								@Override
								public void run() {
									progressBar.setProgress(currentProgress);
									progressText.setText("Searching...");
								}
							});

						try {
							Thread.sleep(50); // Simulate delay
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					// Simulated search is complete, show search results
					runOnUiThread(new Runnable() {
							@Override
							public void run() {
								progressDialog.dismiss();
								showSearchResults(query, searchFiles, extension, isRegexSearch, isCaseSensitive);
							}
						});
				}
			});

		progressDialog.show();
		searchThread.start();
	}

    private void showSearchResults(String query, boolean searchFiles, String extension,
								   boolean isRegexSearch, boolean isCaseSensitive) {
		// Prepare search results list based on search options
		final ArrayList<FileItem> matchedItems = new ArrayList<>();

		for (FileItem item : fileList) {
			String itemName = item.getName();
			String itemPath = item.getPath();

			boolean matchesQuery;
			if (isRegexSearch) {
				try {
					// Use regex pattern for matching
					matchesQuery = itemName.matches("(?i).*" + query + ".*"); // Case insensitive
				} catch (Exception e) {
					matchesQuery = false; // Handle invalid regex gracefully
				}
			} else {
				// Use standard contains() method for non-regex searches
				matchesQuery = isCaseSensitive ?
                    itemName.contains(query) :
                    itemName.toLowerCase().contains(query.toLowerCase());
			}

			if (matchesQuery && searchFiles) {
				if (!item.isDirectory()) {
					if (!extension.isEmpty()) {
						boolean extensionMatch = isCaseSensitive ?
                            itemPath.endsWith(extension) :
                            itemPath.toLowerCase().endsWith(extension.toLowerCase());

						if (!extensionMatch) continue;
					}
					matchedItems.add(item);
				}
			} else if (matchesQuery && !searchFiles) {
				matchedItems.add(item);
			}
		}

		// Show search results in a new dialog
		searchDialog.dismiss(); // Dismiss previous search dialog if still open

		final Dialog searchResultsDialog = new Dialog(this);
		searchResultsDialog.setContentView(R.layout.dialog_search_results);

		TextView resultsTextView = searchResultsDialog.findViewById(R.id.resultsTextView);
		ListView resultsListView = searchResultsDialog.findViewById(R.id.resultsListView);
		Button closeButton = searchResultsDialog.findViewById(R.id.closeButton);

		// Setting up adapter for results list view
		FileListAdapter resultsAdapter = new FileListAdapter(this, R.layout.item_file, matchedItems);
		resultsListView.setAdapter(resultsAdapter);

		resultsTextView.setText("Search Results for: " + query);

		// Handling item click (redirect to path)
		resultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					FileItem selectedItem = matchedItems.get(position);
					if (selectedItem.isDirectory()) {
						displayFiles(selectedItem.getPath());
					} else {
						// Handle file click (open file if needed)
						Toast.makeText(MainActivity.this, "Opening: " + selectedItem.getPath(), Toast.LENGTH_SHORT).show();
						// Implement your file opening logic here
					}
				}
			});

		// Handling long press (copy path)
		resultsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
					FileItem selectedItem = matchedItems.get(position);
					ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
					ClipData clip = ClipData.newPlainText("File Path", selectedItem.getPath());
					clipboard.setPrimaryClip(clip);
					Toast.makeText(MainActivity.this, "Copied: " + selectedItem.getPath(), Toast.LENGTH_SHORT).show();
					return true;
				}
			});

		closeButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					searchResultsDialog.dismiss();
				}
			});

		searchResultsDialog.show();
	}

	private void showEmptyDirectoryMessage() {
		emptyDirectoryTextView.setVisibility(View.VISIBLE);
		listView.setVisibility(View.GONE);
	}

	private void hideEmptyDirectoryMessage() {
		emptyDirectoryTextView.setVisibility(View.GONE);
		listView.setVisibility(View.VISIBLE);
	}
	}
