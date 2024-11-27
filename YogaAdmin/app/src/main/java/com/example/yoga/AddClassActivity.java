package com.example.yoga;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class AddClassActivity extends AppCompatActivity {

    // UI components
    private EditText editName, editDate, editTeacher, editComment;
    private TextView errorName, errorDate, errorTeacher;
    private Spinner spinnerCourse;

    // Database helper and data variables
    private YogaCourseDbHelper dbHelper;
    private String courseId;
    private ArrayList<String> courseNames;
    private ArrayList<Long> courseIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_class);

        // Initialize database helper
        dbHelper = new YogaCourseDbHelper(this);

        // Bind UI components
        editName = findViewById(R.id.edit_name);
        errorName = findViewById(R.id.error_name);
        spinnerCourse = findViewById(R.id.spinner_course);
        editDate = findViewById(R.id.edit_date);
        editTeacher = findViewById(R.id.edit_teacher);
        editComment = findViewById(R.id.edit_comment);
        errorDate = findViewById(R.id.error_date);
        errorTeacher = findViewById(R.id.error_teacher);
        Button buttonSaveClass = findViewById(R.id.button_save_class);

        // Initialize lists for course data
        courseNames = new ArrayList<>();
        courseIds = new ArrayList<>();

        // Load courses from the database into the spinner
        loadCourses();

        // Pre-select the spinner position based on courseId if applicable
        int spinnerPosition = courseIds.indexOf(courseId);
        spinnerCourse.setSelection(spinnerPosition);

        // Set click listener for the Save button
        buttonSaveClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs()) { // Validate inputs before saving
                    saveClass(); // Save the class to the database
                }
            }
        });

        // Set listener for course selection in the spinner
        spinnerCourse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Update courseId based on selected spinner item
                courseId = courseIds.get(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing if no item is selected
            }
        });
    }

    // Loads the list of courses from the database and populates the spinner
    private void loadCourses() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Query the course table for IDs and names
        Cursor cursor = db.query(YogaCourseDbHelper.TABLE_COURSE,
                new String[]{YogaCourseDbHelper.COLUMN_COURSE_ID, YogaCourseDbHelper.COLUMN_COURSE_NAME},
                null, null, null, null, null);

        // Iterate through the result set and populate the course lists
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(YogaCourseDbHelper.COLUMN_COURSE_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(YogaCourseDbHelper.COLUMN_COURSE_NAME));

            courseIds.add(id); // Add course ID
            courseNames.add(name); // Add course name
        }
        cursor.close(); // Close the cursor to release resources

        // Set up the spinner adapter with the course names
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courseNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourse.setAdapter(adapter); // Attach the adapter to the spinner
    }

    // Validates the input fields and displays error messages if needed
    private boolean validateInputs() {
        boolean isValid = true;

        // Validate the class name input
        if (editName.getText().toString().trim().isEmpty()) {
            errorName.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            errorName.setVisibility(View.GONE);
        }

        // Validate the date input
        if (editDate.getText().toString().trim().isEmpty()) {
            errorDate.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            errorDate.setVisibility(View.GONE);
        }

        // Validate the teacher input
        if (editTeacher.getText().toString().trim().isEmpty()) {
            errorTeacher.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            errorTeacher.setVisibility(View.GONE);
        }

        return isValid;
    }

    // Saves the new class to the database and Firebase
    private void saveClass() {

        // Retrieve user input
        String date = editDate.getText().toString();
        String name = editName.getText().toString();
        String teacher = editTeacher.getText().toString();
        String comment = editComment.getText().toString();

        // Prepare the values to insert into the database
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(YogaCourseDbHelper.COLUMN_CLASS_COURSE_ID, courseId);
        values.put(YogaCourseDbHelper.COLUMN_CLASS_DATE, date);
        values.put(YogaCourseDbHelper.COLUMN_CLASS_NAME, name);
        values.put(YogaCourseDbHelper.COLUMN_CLASS_TEACHER, teacher);
        values.put(YogaCourseDbHelper.COLUMN_CLASS_COMMENT, comment);

        // Create a new YogaClass object for Firebase
        YogaClass newClass = new YogaClass(name, courseId, date, teacher ,comment);

        // Insert the new class into the database
        db.insert(YogaCourseDbHelper.TABLE_CLASS, null, values);

        // Save the class to Firebase
        dbHelper.saveClassFirebase(newClass);

        // Finish the activity and return to the previous screen
        finish();
    }
}