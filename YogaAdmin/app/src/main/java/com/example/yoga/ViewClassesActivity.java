package com.example.yoga;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ViewClassesActivity extends AppCompatActivity {

    // Database helper for accessing SQLite
    private YogaCourseDbHelper dbHelper;

    // UI components
    private ListView listViewClasses;
    private SearchView searchView; // Search bar for filtering classes

    // Adapter and data structures for displaying class details
    private ArrayAdapter<String> adapter;
    private ArrayList<String> classIds;
    private ArrayList<String> classDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_classes);

        // Initialize database helper and UI components
        dbHelper = new YogaCourseDbHelper(this);
        listViewClasses = findViewById(R.id.list_view_classes);
        searchView = findViewById(R.id.search_view);

        // Initialize data lists
        classIds = new ArrayList<>();
        classDetails = new ArrayList<>();

        // Set up the adapter for the list view
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, classDetails);
        listViewClasses.setAdapter(adapter);

        // Set up a listener for search input changes
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Load classes based on the submitted query
                loadClasses(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Load classes as the user types
                loadClasses(newText);
                return false;
            }
        });

        // Set a listener for list view item clicks
        listViewClasses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Get the class ID corresponding to the clicked item
                String classId = classIds.get(position).toString();

                // Launch the EditClassActivity and pass the class ID
                Intent intent = new Intent(ViewClassesActivity.this, EditClassActivity.class);
                intent.putExtra("CLASS_ID", classId);
                startActivity(intent);
            }
        });
    }

    // Load classes when the activity starts
    @Override
    protected void onStart() {
        super.onStart();
        loadClasses(null);
    }

    // Reload classes when the activity resumes (e.g., after editing)
    @Override
    protected void onResume() {
        super.onResume();
        loadClasses(null);
    }

    // Method to load classes from the database
    private void loadClasses(String filter) {
        // Clear existing data
        classIds.clear();
        classDetails.clear();

        // Get a readable database instance
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Query to retrieve class details with course information
        String query = "SELECT " + YogaCourseDbHelper.TABLE_CLASS + "." + YogaCourseDbHelper.COLUMN_CLASS_ID + ", " +
                "CLASS." + YogaCourseDbHelper.COLUMN_CLASS_NAME + ", " +
                YogaCourseDbHelper.COLUMN_CLASS_DATE + ", " +
                YogaCourseDbHelper.COLUMN_CLASS_TEACHER + ", " +
                YogaCourseDbHelper.COLUMN_CLASS_COMMENT + ", " +
                YogaCourseDbHelper.COLUMN_COURSE_DESCRIPTION +
                " FROM " + YogaCourseDbHelper.TABLE_CLASS +
                " INNER JOIN " + YogaCourseDbHelper.TABLE_COURSE +
                " ON " + YogaCourseDbHelper.TABLE_CLASS + "." + YogaCourseDbHelper.COLUMN_CLASS_COURSE_ID +
                " = " + YogaCourseDbHelper.TABLE_COURSE + "." + YogaCourseDbHelper.COLUMN_COURSE_ID;

        // Add a filter to the query if provided
        if (!TextUtils.isEmpty(filter)) {
            query += " WHERE " + YogaCourseDbHelper.COLUMN_CLASS_TEACHER + " LIKE ? OR " +
                    YogaCourseDbHelper.COLUMN_CLASS_DATE + " LIKE ?";
        }

        // Execute the query
        Cursor cursor;
        if (!TextUtils.isEmpty(filter)) {
            cursor = db.rawQuery(query, new String[]{"%" + filter + "%", "%" + filter + "%"});
        } else {
            cursor = db.rawQuery(query, null);
        }

        // Iterate through the result set and populate the data lists
        while (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndexOrThrow(YogaCourseDbHelper.COLUMN_CLASS_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(YogaCourseDbHelper.COLUMN_CLASS_NAME));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(YogaCourseDbHelper.COLUMN_CLASS_DATE));
            String teacher = cursor.getString(cursor.getColumnIndexOrThrow(YogaCourseDbHelper.COLUMN_CLASS_TEACHER));
            String courseName = cursor.getString(cursor.getColumnIndexOrThrow(YogaCourseDbHelper.COLUMN_COURSE_NAME));

            // Add the class ID and formatted details to their respective lists
            classIds.add(id);
            classDetails.add("Name: " + name + "\nDate: " + date + "\nTeacher: " + teacher + "\nCourse: " + courseName);
        }
        cursor.close(); // Close the cursor to free up resources

        // Notify the adapter of the data change to update the list view
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}
