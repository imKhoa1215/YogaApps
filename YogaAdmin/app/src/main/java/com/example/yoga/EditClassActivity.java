package com.example.yoga;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class EditClassActivity extends AppCompatActivity {

    // Database helper for managing SQLite database operations
    private YogaCourseDbHelper dbHelper;

    // UI components
    private Spinner spinnerCourses;
    private EditText editName, editDate, editTeacher, editComment;
    private TextView errorName, errorDate, errorTeacher;
    private Button buttonSave, buttonDelete;

    // Data-related variables
    private String classId;
    private String courseId;
    private ArrayList<Long> courseIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_class);

        // Initialize database helper
        dbHelper = new YogaCourseDbHelper(this);

        // Link UI components to their respective views
        editName = findViewById(R.id.edit_name);
        errorName = findViewById(R.id.error_name);
        spinnerCourses = findViewById(R.id.spinner_courses);
        editDate = findViewById(R.id.edit_date);
        editTeacher = findViewById(R.id.edit_teacher);
        editComment = findViewById(R.id.edit_comment);
        errorDate = findViewById(R.id.error_date);
        errorTeacher = findViewById(R.id.error_teacher);
        buttonSave = findViewById(R.id.button_save);
        buttonDelete = findViewById(R.id.button_delete);

        // Get the class ID from the Intent
        Intent intent = getIntent();
        classId = intent.getStringExtra("CLASS_ID");

        // Load available courses and the details of the class being edited
        loadCourses();
        loadClassDetails(classId);

        // Handle spinner item selection to update courseId
        spinnerCourses.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                courseId = courseIds.get(position).toString(); // Update courseId based on selection
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Handle Save button click
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveClass();
            }
        });

        // Handle Delete button click
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDelete(); // Prompt user for confirmation before deletion
            }
        });
    }

    // Load available courses from the database and populate the spinner
    private void loadCourses() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(YogaCourseDbHelper.TABLE_COURSE, null, null, null, null, null, null);

        ArrayList<String> courseDescriptions = new ArrayList<>();
        courseIds = new ArrayList<>();

        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(YogaCourseDbHelper.COLUMN_COURSE_ID));
            String courseName = cursor.getString(cursor.getColumnIndexOrThrow(YogaCourseDbHelper.COLUMN_COURSE_NAME));
            courseIds.add(id);
            courseDescriptions.add(courseName); // Collect course names for display
        }
        cursor.close();

        // Set up the spinner adapter with course names
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courseDescriptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourses.setAdapter(adapter);
    }

    // Load details of the class being edited
    private void loadClassDetails(String classId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(YogaCourseDbHelper.TABLE_CLASS, null, YogaCourseDbHelper.COLUMN_CLASS_ID + "=?", new String[]{String.valueOf(classId)}, null, null, null);

        if (cursor.moveToFirst()) {
            // Retrieve class details from the database
            String name = cursor.getString(cursor.getColumnIndexOrThrow(YogaCourseDbHelper.COLUMN_CLASS_NAME));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(YogaCourseDbHelper.COLUMN_CLASS_DATE));
            String teacher = cursor.getString(cursor.getColumnIndexOrThrow(YogaCourseDbHelper.COLUMN_CLASS_TEACHER));
            String comment = cursor.getString(cursor.getColumnIndexOrThrow(YogaCourseDbHelper.COLUMN_CLASS_COMMENT));
            long courseId = cursor.getLong(cursor.getColumnIndexOrThrow(YogaCourseDbHelper.COLUMN_CLASS_COURSE_ID));

            // Set the UI fields with the retrieved details
            editName.setText(name);
            editDate.setText(date);
            editTeacher.setText(teacher);
            editComment.setText(comment);

            // Set the spinner selection based on the class's course ID
            int coursePosition = courseIds.indexOf(courseId);
            if (coursePosition != -1) {
                spinnerCourses.setSelection(coursePosition);
            }
        }
        cursor.close();
    }

    // Save the updated class details to the database
    private void saveClass() {
        // Retrieve user input
        String name = editName.getText().toString();
        String date = editDate.getText().toString();
        String teacher = editTeacher.getText().toString();
        String comment = editComment.getText().toString();

        boolean isValid = true;

        // Validate inputs
        if (TextUtils.isEmpty(name)) {
            errorName.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            errorName.setVisibility(View.GONE);
        }

        if (TextUtils.isEmpty(date)) {
            errorDate.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            errorDate.setVisibility(View.GONE);
        }

        if (TextUtils.isEmpty(teacher)) {
            errorTeacher.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            errorTeacher.setVisibility(View.GONE);
        }

        if (!isValid) {
            return; // Stop execution if validation fails
        }

        // Prepare updated values
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(YogaCourseDbHelper.COLUMN_CLASS_NAME, name);
        values.put(YogaCourseDbHelper.COLUMN_CLASS_COURSE_ID, courseId);
        values.put(YogaCourseDbHelper.COLUMN_CLASS_DATE, date);
        values.put(YogaCourseDbHelper.COLUMN_CLASS_TEACHER, teacher);
        values.put(YogaCourseDbHelper.COLUMN_CLASS_COMMENT, comment);
        YogaClass newClass = new YogaClass(name, courseId, date, teacher, comment);

        // Update the class record in the database
        db.update(YogaCourseDbHelper.TABLE_CLASS, values, YogaCourseDbHelper.COLUMN_CLASS_ID + "=?", new String[]{String.valueOf(classId)});
        dbHelper.saveClassFirebase(classId, newClass);
        finish(); // Close the activity after saving
    }

    // Show confirmation dialog for class deletion
    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Class")
                .setMessage("Are you sure you want to delete this class?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteClass();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    // Delete the class from the database
    private void deleteClass() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(YogaCourseDbHelper.TABLE_CLASS, YogaCourseDbHelper.COLUMN_CLASS_ID + "=?", new String[]{String.valueOf(classId)});
        dbHelper.deleteClassFirebase(classId);
        Toast.makeText(this, "Class deleted", Toast.LENGTH_SHORT).show();
        finish(); // Close the activity after deletion

    }
}
