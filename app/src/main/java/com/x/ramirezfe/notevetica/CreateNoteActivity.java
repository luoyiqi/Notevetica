package com.x.ramirezfe.notevetica;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

import butterknife.Bind;
import butterknife.ButterKnife;

/*
    Activity used to create a new note
    Comes from MainActivity
 */

public class CreateNoteActivity extends AppCompatActivity {

    // Views
    @Bind(R.id.et_title)
    EditText etTitle;
    @Bind(R.id.et_description)
    EditText etDescription;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    // Misc. Variables
    private String etTitleText, etDescriptionText;
    private static String TAG = CreateNoteActivity.class.getSimpleName();
    // Objects
    private Note note = new Note();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);
        // ButterKnife
        ButterKnife.bind(this);
        // Toolbar
        setSupportActionBar(toolbar);
        // Open keyboard automatically
        showKeyboard();

        // Check if the note extras were passed (did the user click a note or not)
        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            String passedTitle = intent.getStringExtra(MainActivity.EXTRA_NOTE_TITLE);
            String passedDescription = intent.getStringExtra(MainActivity.EXTRA_NOTE_DESCRIPTION);
            String passedUUID = intent.getStringExtra(MainActivity.EXTRA_NOTE_UUID);
            note.setTitle(passedTitle);
            note.setDescription(passedDescription);
            note.setObjectId(passedUUID);
            etTitle.setText(passedTitle);
            etDescription.setText(passedDescription);
        }

        // Save Note FAB
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prepareForSavingNote(view);
            }

        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public void onBackPressed() {
        // Check if the user has unsaved changes
        prepareForSavingNote(null);
    }

    /*
        This method checks to see if a valid note can be created
        @param view - Your view. (It CAN be null if you don't need the snackbar, which requires a view)
     */
    public void prepareForSavingNote(View view) {
        // Get the EditText's latest values
        refreshFieldData();
        // Remove whitespaces at the beginning/end
        etTitleText = etTitleText.trim();
        etDescriptionText = etDescriptionText.trim();
        // Retrieve the notes data passed by MainActivity's intent
        Intent intent = getIntent();
        String passedTitle = intent.getStringExtra(MainActivity.EXTRA_NOTE_TITLE);
        String passedDescription = intent.getStringExtra(MainActivity.EXTRA_NOTE_DESCRIPTION);

        // If note title is empty, DO NOT save
        if (etTitleText.equals("") || etTitleText.isEmpty()) {
            // If the user clicks the back button with nothing filled out, finish();
            if (view == null) {
                finish();
            } else {
                Notify.snack(view, "Title may not be empty");
            }
            // User clicked a note they've already created and did no changes to it
        } else if (etTitleText.equals(passedTitle) && etDescriptionText.equals(passedDescription)) {
            finish();
            // The user clicked an existing note and pressed the back button
            // If the title or description is different then resave the note
        } else if (!etTitleText.equals(passedTitle) || !etDescriptionText.equals(passedDescription)) {
            if (view == null) {
                // User is leaving by pressing the back button and has unsaved changes...alert them
                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
                builder.setTitle("Would you like to save your changes?");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        saveNote(); // Resave note
                        finish();
                        dialog.cancel();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                saveNote();
                finish();
            }
        }
    }

    // Called after "prepareForSavingNote()" declares that a note meets the criteria to be saved
    public void saveNote() {
        // Get the EditText's latest values
        refreshFieldData();
        // Apply the data to the note object
        note.setTitle(etTitleText);
        note.setDescription(etDescriptionText);
        // Save to the backend
        Backendless.Persistence.save(note, new AsyncCallback<Note>() {
            public void handleResponse(Note response) {
                Notify.out("Successfully saved the following note: " + response.toString());
            }

            public void handleFault(BackendlessFault fault) {
                // An error has occurred
                Log.e(TAG, fault.getCode());
            }
        });
        finish();
    }

    public void onPause() {
        super.onPause();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            // Check if the user has unsaved changes
            prepareForSavingNote(null);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public void refreshFieldData() {
        etTitleText = etTitle.getText().toString();
        etDescriptionText = etDescription.getText().toString();
    }


}