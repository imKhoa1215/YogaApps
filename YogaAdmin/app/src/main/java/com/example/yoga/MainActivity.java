package com.example.yoga;
// I asked my friends to help me with this coursework because there's no way im able to do this on my own lol
// I am very grateful to them for their help.
// TODO: PANIC!
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // UI components for the yoga course form
    Spinner dayOfTheWeekSpinner;
    private EditText nameEditText;
    private EditText timeStartEditText;
    private EditText capacityEditText;
    private EditText durationEditText;
    private EditText priceEditText;
    private Spinner classTypeSpinner;
    private EditText descriptionEditText;
    private Button saveButton, addClassesButton, viewClassesButton;

    // Error messages for validation feedback
    private TextView errorName;
    private TextView errorDayOfTheWeek;
    private TextView errorTimeStart;
    private TextView errorCapacity;
    private TextView errorDuration;
    private TextView errorPrice;
    private TextView errorClassType;

    // Database helper instance for managing SQLite operations
    private YogaCourseDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the database helper

        dbHelper = new YogaCourseDbHelper(this);
        dbHelper.deleteAllData(); // Deletes all existing data for a fresh start
        dbHelper.syncWithFirebase(); // Syncs local data with Firebase

        // Link UI components with their corresponding views in the layout
        nameEditText = findViewById(R.id.edit_name);
        dayOfTheWeekSpinner = findViewById(R.id.spinner_day_of_the_week);
        timeStartEditText = findViewById(R.id.edit_time_start);
        capacityEditText = findViewById(R.id.edittext_capacity);
        durationEditText = findViewById(R.id.edittext_duration);
        priceEditText = findViewById(R.id.edittext_price);
        classTypeSpinner = findViewById(R.id.spinner_class_type);
        descriptionEditText = findViewById(R.id.edittext_description);

        errorName = findViewById(R.id.error_name);
        errorDayOfTheWeek = findViewById(R.id.error_day_of_the_week);
        errorTimeStart = findViewById(R.id.error_time_start);
        errorCapacity = findViewById(R.id.error_capacity);
        errorDuration = findViewById(R.id.error_duration);
        errorPrice = findViewById(R.id.error_price);
        errorClassType = findViewById(R.id.error_class_type);

        saveButton = findViewById(R.id.button_delete);
        addClassesButton = findViewById(R.id.button_add_classes);
        viewClassesButton = findViewById(R.id.button_view_classes);

        // Navigate to ViewCoursesActivity when the button is clicked
        Button viewCoursesButton = findViewById(R.id.button_view_courses);
        viewCoursesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ViewCoursesActivity.class);
                startActivity(intent);
            }
        });

        // Set up click listener for saving a yoga course
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveYogaCourse();
            }
        });

        // Navigate to AddClassActivity when the button is clicked
        addClassesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddClassActivity.class);
                startActivity(intent);
            }
        });

        // Navigate to ViewClassesActivity when the button is clicked
        viewClassesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ViewClassesActivity.class);
                startActivity(intent);
            }
        });
    }

    // Saves a yoga course to the database after validating user inputs.
    private void saveYogaCourse() {
        boolean isValid = true;

        // Retrieve values from input fields
        String name = nameEditText.getText().toString();
        String dayOfTheWeek = dayOfTheWeekSpinner.getSelectedItem().toString();
        String timeStart = timeStartEditText.getText().toString();
        String capacityStr = capacityEditText.getText().toString();
        String durationStr = durationEditText.getText().toString();
        String priceStr = priceEditText.getText().toString();
        String classType = classTypeSpinner.getSelectedItem().toString();
        String description = descriptionEditText.getText().toString();

        // Validate the user input and show errors if necessary
        if (name.equals("")) {
            errorName.setText("Please select a day of the week.");
            errorName.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            errorName.setVisibility(View.GONE);
        }

        if (dayOfTheWeek.equals("")) {
            errorDayOfTheWeek.setText("Please select a day of the week.");
            errorDayOfTheWeek.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            errorDayOfTheWeek.setVisibility(View.GONE);
        }

        if (timeStart.isEmpty()) {
            errorTimeStart.setText("Please enter the start time.");
            errorTimeStart.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            errorTimeStart.setVisibility(View.GONE);
        }

        if (capacityStr.isEmpty()) {
            errorCapacity.setText("Please enter the capacity.");
            errorCapacity.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            errorCapacity.setVisibility(View.GONE);
        }

        if (durationStr.isEmpty()) {
            errorDuration.setText("Please enter the duration.");
            errorDuration.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            errorDuration.setVisibility(View.GONE);
        }

        if (priceStr.isEmpty()) {
            errorPrice.setText("Please enter the price.");
            errorPrice.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            errorPrice.setVisibility(View.GONE);
        }

        if (classType.equals("")) {
            errorClassType.setText("Please select a class type.");
            errorClassType.setVisibility(View.VISIBLE);
            isValid = false;
        } else {
            errorClassType.setVisibility(View.GONE);
        }


        if (isValid) {

            // Convert input strings to numeric values
            int capacity = Integer.parseInt(capacityStr);
            int duration = Integer.parseInt(durationStr);
            float price = Float.parseFloat(priceStr);

            // Prepare data for insertion into the database
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(YogaCourseDbHelper.COLUMN_COURSE_NAME, name);
            values.put(YogaCourseDbHelper.COLUMN_COURSE_DAY_OF_WEEK, dayOfTheWeek);
            values.put(YogaCourseDbHelper.COLUMN_COURSE_TIME_START, timeStart);
            values.put(YogaCourseDbHelper.COLUMN_COURSE_CAPACITY, capacity);
            values.put(YogaCourseDbHelper.COLUMN_COURSE_DURATION, duration);
            values.put(YogaCourseDbHelper.COLUMN_COURSE_PRICE, price);
            values.put(YogaCourseDbHelper.COLUMN_COURSE_CLASS_TYPE, classType);
            values.put(YogaCourseDbHelper.COLUMN_COURSE_DESCRIPTION, description);

            // Save data to Firebase if applicable
            YogaCourse newCourse = new YogaCourse(name, dayOfTheWeek, timeStart, capacity, duration, price, classType, description);
            dbHelper.saveCourseFirebase(newCourse);

            // Insert data into the SQLite database
            long newRowId = db.insert(YogaCourseDbHelper.TABLE_COURSE, null, values);

            // Provide feedback to the user
            if (newRowId != -1) {
                Toast.makeText(this, "Course saved!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error saving course!", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Prompt the user to fix input errors
            Toast.makeText(this, "Invalid Inputs?! CHECK IT AGAIN!", Toast.LENGTH_SHORT).show();
        }
    }

}