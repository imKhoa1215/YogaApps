package com.example.yoga;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

public class EditCourseActivity extends AppCompatActivity {

    // Declare UI components and helper class for database operations
    private EditText editName, editTimeStart, editCapacity, editDuration, editPrice, editDescription;
    private Spinner spinnerDayOfWeek, spinnerClassType;

    // Helper class to manage database interactions
    private YogaCourseDbHelper dbHelper;
    // Stores the ID of the course being edited
    private String courseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_course);

        // Initialize the database helper
        dbHelper = new YogaCourseDbHelper(this);

        // Bind UI elements to their respective views
        editName = findViewById(R.id.edit_name);
        spinnerDayOfWeek = findViewById(R.id.spinner_day_of_week);
        editTimeStart = findViewById(R.id.edit_time_start);
        editCapacity = findViewById(R.id.edit_capacity);
        editDuration = findViewById(R.id.edit_duration);
        editPrice = findViewById(R.id.edit_price);
        spinnerClassType = findViewById(R.id.spinner_class_type);
        editDescription = findViewById(R.id.edit_description);
        Button buttonSave = findViewById(R.id.button_save);
        Button buttonDelete = findViewById(R.id.button_delete);

        // Populate spinners with predefined values from resources
        ArrayAdapter<CharSequence> dayOfWeekAdapter = ArrayAdapter.createFromResource(this,
                R.array.days_of_week, android.R.layout.simple_spinner_item);
        dayOfWeekAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDayOfWeek.setAdapter(dayOfWeekAdapter);

        ArrayAdapter<CharSequence> classTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.class_types, android.R.layout.simple_spinner_item);
        classTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClassType.setAdapter(classTypeAdapter);

        // Retrieve the course ID passed from the previous activity
        courseId = getIntent().getStringExtra("COURSE_ID");

        // Populate the fields with the course details
        populateFields();

        // Set an event listener for the Save button
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCourse();
            } // Save changes to the database
        });

        // Set an event listener for the Delete button
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show confirmation before deleting
                showDeleteConfirmationDialog(courseId);
            }
        });
    }

    // Populate the UI fields with the course data passed from the previous activity
    private void populateFields() {
        String name = getIntent().getStringExtra("NAME");
        String dayOfWeek = getIntent().getStringExtra("DAY_OF_WEEK");
        String timeStart = getIntent().getStringExtra("TIME_START");
        int capacity = getIntent().getIntExtra("CAPACITY", 0);
        int duration = getIntent().getIntExtra("DURATION", 0);
        float price = getIntent().getFloatExtra("PRICE", 0);
        String classType = getIntent().getStringExtra("CLASS_TYPE");
        String description = getIntent().getStringExtra("DESCRIPTION");

        // Set values for text fields and spinners
        editName.setText(name);
        spinnerDayOfWeek.setSelection(((ArrayAdapter) spinnerDayOfWeek.getAdapter()).getPosition(dayOfWeek));
        editTimeStart.setText(timeStart);
        editCapacity.setText(String.valueOf(capacity));
        editDuration.setText(String.valueOf(duration));
        editPrice.setText(String.valueOf(price));
        spinnerClassType.setSelection(((ArrayAdapter) spinnerClassType.getAdapter()).getPosition(classType));
        editDescription.setText(description);
    }

    // Save the updated course details to the database
    private void saveCourse() {
        // Retrieve updated values from the input fields
        String name = editName.getText().toString();
        String dayOfWeek = spinnerDayOfWeek.getSelectedItem().toString();
        String timeStart = editTimeStart.getText().toString();
        int capacity = Integer.parseInt(editCapacity.getText().toString());
        int duration = Integer.parseInt(editDuration.getText().toString());
        float price = Float.parseFloat(editPrice.getText().toString());
        String classType = spinnerClassType.getSelectedItem().toString();
        String description = editDescription.getText().toString();

        // Prepare the values for updating the database
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(YogaCourseDbHelper.COLUMN_COURSE_NAME, name);
        values.put(YogaCourseDbHelper.COLUMN_COURSE_DAY_OF_WEEK, dayOfWeek);
        values.put(YogaCourseDbHelper.COLUMN_COURSE_TIME_START, timeStart);
        values.put(YogaCourseDbHelper.COLUMN_COURSE_CAPACITY, capacity);
        values.put(YogaCourseDbHelper.COLUMN_COURSE_DURATION, duration);
        values.put(YogaCourseDbHelper.COLUMN_COURSE_PRICE, price);
        values.put(YogaCourseDbHelper.COLUMN_COURSE_CLASS_TYPE, classType);
        values.put(YogaCourseDbHelper.COLUMN_COURSE_DESCRIPTION, description);

        YogaCourse newCourse = new YogaCourse(courseId, name, dayOfWeek, timeStart, capacity, duration, price, classType, description);

        // Update the database record
        db.update(YogaCourseDbHelper.TABLE_COURSE, values,
                YogaCourseDbHelper.COLUMN_COURSE_ID + "=?", new String[]{String.valueOf(courseId)});

        // Synchronize with Firebase
        dbHelper.saveCourseFirebase(courseId, newCourse);

        // Close the activity after saving
        finish();
    }

    // Show a confirmation dialog before deleting a course
    private void showDeleteConfirmationDialog(final String id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this course?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int dialogId) {
                deleteCourse(id); // Delete the course if confirmed
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int dialogId) {
                if (dialog != null) {
                    dialog.dismiss(); // Close the dialog if canceled
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show(); // Display the dialog
    }

    // Delete the course from the database and redirect to the course list view
    private void deleteCourse(String id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(YogaCourseDbHelper.TABLE_COURSE, YogaCourseDbHelper.COLUMN_COURSE_ID + "=?", new String[]{String.valueOf(id)});
        dbHelper.deleteCourseFirebase(id); // Synchronize deletion with Firebase

        // Redirect to the ViewCoursesActivity
        Intent intent = new Intent(EditCourseActivity.this, ViewCoursesActivity.class);
        startActivity(intent);
    }
}
