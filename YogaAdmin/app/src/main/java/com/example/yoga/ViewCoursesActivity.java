package com.example.yoga;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import androidx.appcompat.app.AppCompatActivity;

public class ViewCoursesActivity extends AppCompatActivity {

    // Database helper instance for managing SQLite database
    private YogaCourseDbHelper dbHelper;
    // ListView to display the courses
    private ListView listViewCourses;
    // Adapter to bind data from the database to the ListView
    private SimpleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_courses);

        // Initialize the database helper
        dbHelper = new YogaCourseDbHelper(this);

        // Find the ListView in the layout
        listViewCourses = findViewById(R.id.listview_courses);

        // Set a click listener for items in the ListView
        listViewCourses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Launch the EditCourseActivity when an item is clicked, passing the selected course's details
                Intent intent = new Intent(ViewCoursesActivity.this, EditCourseActivity.class);
                intent.putExtra("COURSE_ID", id);

                // Retrieve the selected course's data from the cursor
                Cursor cursor = (Cursor) adapter.getItem(position);
                intent.putExtra("NAME", cursor.getString(cursor.getColumnIndexOrThrow(YogaCourseDbHelper.COLUMN_COURSE_NAME)));
                intent.putExtra("DAY_OF_WEEK", cursor.getString(cursor.getColumnIndexOrThrow(YogaCourseDbHelper.COLUMN_COURSE_DAY_OF_WEEK)));
                intent.putExtra("TIME_START", cursor.getString(cursor.getColumnIndexOrThrow(YogaCourseDbHelper.COLUMN_COURSE_TIME_START)));
                intent.putExtra("CAPACITY", cursor.getInt(cursor.getColumnIndexOrThrow(YogaCourseDbHelper.COLUMN_COURSE_CAPACITY)));
                intent.putExtra("DURATION", cursor.getInt(cursor.getColumnIndexOrThrow(YogaCourseDbHelper.COLUMN_COURSE_DURATION)));
                intent.putExtra("PRICE", cursor.getFloat(cursor.getColumnIndexOrThrow(YogaCourseDbHelper.COLUMN_COURSE_PRICE)));
                intent.putExtra("CLASS_TYPE", cursor.getString(cursor.getColumnIndexOrThrow(YogaCourseDbHelper.COLUMN_COURSE_CLASS_TYPE)));
                intent.putExtra("DESCRIPTION", cursor.getString(cursor.getColumnIndexOrThrow(YogaCourseDbHelper.COLUMN_COURSE_DESCRIPTION)));

                startActivityForResult(intent, 1); // Request code 1 for edit activity
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Display the list of courses when the activity starts
        displayCourses();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the course list when the activity resumes
        displayCourses();
    }

    // Method to display the list of courses in the ListView
    private void displayCourses() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Define the columns to retrieve from the database
        String[] projection = {
                YogaCourseDbHelper.COLUMN_COURSE_ID,
                YogaCourseDbHelper.COLUMN_COURSE_NAME,
                YogaCourseDbHelper.COLUMN_COURSE_DAY_OF_WEEK,
                YogaCourseDbHelper.COLUMN_COURSE_TIME_START,
                YogaCourseDbHelper.COLUMN_COURSE_CAPACITY,
                YogaCourseDbHelper.COLUMN_COURSE_DURATION,
                YogaCourseDbHelper.COLUMN_COURSE_PRICE,
                YogaCourseDbHelper.COLUMN_COURSE_CLASS_TYPE,
                YogaCourseDbHelper.COLUMN_COURSE_DESCRIPTION
        };

        // Query the database for all courses
        Cursor cursor = db.query(
                YogaCourseDbHelper.TABLE_COURSE,
                projection,
                null,
                null,
                null,
                null,
                null);

        // Map database columns to the views in the ListView layout
        String[] fromColumns = {
                YogaCourseDbHelper.COLUMN_COURSE_DAY_OF_WEEK,
                YogaCourseDbHelper.COLUMN_COURSE_NAME,
                YogaCourseDbHelper.COLUMN_COURSE_TIME_START,
                YogaCourseDbHelper.COLUMN_COURSE_CAPACITY,
                YogaCourseDbHelper.COLUMN_COURSE_DURATION,
                YogaCourseDbHelper.COLUMN_COURSE_PRICE,
                YogaCourseDbHelper.COLUMN_COURSE_CLASS_TYPE,
                YogaCourseDbHelper.COLUMN_COURSE_DESCRIPTION
        };

        int[] toViews = {
                // Maps to the course name TextView
                R.id.text_name,
                // Maps to the day of week TextView
                R.id.text_day_of_week,
                // Maps to the price TextView
                R.id.text_price,
                // Maps to the class type TextView
                R.id.text_class_type,
        };

        // Create an adapter to bind the database data to the ListView
        adapter = new SimpleCursorAdapter(
                this,
                // Layout for individual items
                R.layout.list_item_course,
                // Cursor containing the data
                cursor,
                // Columns to bind
                fromColumns,
                // Views to bind the columns to
                toViews,
                // No additional flags
                0);

        // Set the adapter on the ListView
        listViewCourses.setAdapter(adapter);
    }
}
