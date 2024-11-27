package com.example.yoga;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

// Helper class for managing SQLite and Firebase database operations
public class YogaCourseDbHelper extends SQLiteOpenHelper {

    // SQLite database properties
    private static final String DATABASE_NAME = "Yoga.db"; // Database file name
    private static final int DATABASE_VERSION = 2; // Version to manage upgrades

    // Table and column definitions for the "course" table
    public static final String TABLE_COURSE = "course";
    public static final String COLUMN_COURSE_ID = "_id";
    public static final String COLUMN_COURSE_NAME = "name";
    public static final String COLUMN_COURSE_DAY_OF_WEEK = "dayOfWeek";
    public static final String COLUMN_COURSE_TIME_START = "timeStart";
    public static final String COLUMN_COURSE_CAPACITY = "capacity";
    public static final String COLUMN_COURSE_DURATION = "duration";
    public static final String COLUMN_COURSE_PRICE = "price";
    public static final String COLUMN_COURSE_CLASS_TYPE = "classType";
    public static final String COLUMN_COURSE_DESCRIPTION = "description";

    // Table and column definitions for the "class" table
    public static final String TABLE_CLASS = "class";
    public static final String COLUMN_CLASS_ID = "_id";
    public static final String COLUMN_CLASS_NAME = "name";
    public static final String COLUMN_CLASS_COURSE_ID = "courseId";
    public static final String COLUMN_CLASS_DATE = "date";
    public static final String COLUMN_CLASS_TEACHER = "teacher";
    public static final String COLUMN_CLASS_COMMENT = "comment";

    // Firebase Realtime Database references
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://yoga-485ab-default-rtdb.asia-southeast1.firebasedatabase.app");
    DatabaseReference classesRef = database.getReference("Class");
    DatabaseReference coursesRef = database.getReference("Course");

    // SQL commands for creating the tables
    private static final String TABLE_COURSE_CREATE =
            "CREATE TABLE " + TABLE_COURSE + " (" +
                    COLUMN_COURSE_ID + " TEXT PRIMARY KEY, " +
                    COLUMN_COURSE_NAME + " TEXT, " +
                    COLUMN_COURSE_DAY_OF_WEEK + " TEXT, " +
                    COLUMN_COURSE_TIME_START + " TEXT, " +
                    COLUMN_COURSE_CAPACITY + " INTEGER, " +
                    COLUMN_COURSE_DURATION + " INTEGER, " +
                    COLUMN_COURSE_PRICE + " REAL, " +
                    COLUMN_COURSE_CLASS_TYPE + " TEXT, " +
                    COLUMN_COURSE_DESCRIPTION + " TEXT);";

    private static final String TABLE_CLASS_CREATE =
            "CREATE TABLE " + TABLE_CLASS + " (" +
                    COLUMN_CLASS_ID + " TEXT PRIMARY KEY, " +
                    COLUMN_CLASS_NAME + " TEXT, " +
                    COLUMN_CLASS_COURSE_ID + " TEXT, " +
                    COLUMN_CLASS_DATE + " TEXT, " +
                    COLUMN_CLASS_TEACHER + " TEXT, " +
                    COLUMN_CLASS_COMMENT + " TEXT, " +
                    "FOREIGN KEY(" + COLUMN_CLASS_COURSE_ID + ") REFERENCES " + TABLE_COURSE + "(" + COLUMN_COURSE_ID + "));";

    // Constructor to initialize the SQLiteOpenHelper
    public YogaCourseDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create tables when the database is first initialized
        db.execSQL(TABLE_COURSE_CREATE);
        db.execSQL(TABLE_CLASS_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrades by dropping old tables and recreating them
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COURSE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLASS);
        onCreate(db);
    }

    // Synchronize SQLite database with Firebase Realtime Database
    public void syncWithFirebase() {
        // Set up SQLite Database
        SQLiteDatabase db = getWritableDatabase(); // Get writable SQLite database

        // Sync class data from Firebase to SQLite
        classesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot courseSnapshot : dataSnapshot.getChildren()) {
                    YogaClass yogaClass = courseSnapshot.getValue(YogaClass.class);
                    ContentValues values = new ContentValues();

                    // Map class fields to SQLite columns
                    values.put(YogaCourseDbHelper.COLUMN_CLASS_NAME, yogaClass.getName());
                    values.put(YogaCourseDbHelper.COLUMN_CLASS_COURSE_ID, yogaClass.getCourseId());
                    values.put(YogaCourseDbHelper.COLUMN_CLASS_TEACHER, yogaClass.getTeacher());
                    values.put(YogaCourseDbHelper.COLUMN_CLASS_DATE, yogaClass.getDate());
                    values.put(YogaCourseDbHelper.COLUMN_CLASS_COMMENT, yogaClass.getComment());

                    db.insert(YogaCourseDbHelper.TABLE_CLASS, null, values); // Insert into SQLite database
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Log or handle database error
            }
        });

        // Sync course data from Firebase to SQLite
        coursesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot courseSnapshot : dataSnapshot.getChildren()) {
                    YogaCourse course = courseSnapshot.getValue(YogaCourse.class);
                    ContentValues values = new ContentValues();

                    // Map course fields to SQLite columns
                    values.put(YogaCourseDbHelper.COLUMN_COURSE_ID, course.getId());
                    values.put(YogaCourseDbHelper.COLUMN_COURSE_NAME, course.getName());
                    values.put(YogaCourseDbHelper.COLUMN_COURSE_DAY_OF_WEEK, course.getDayOfWeek());
                    values.put(YogaCourseDbHelper.COLUMN_COURSE_TIME_START, course.getTimeStart());
                    values.put(YogaCourseDbHelper.COLUMN_COURSE_CAPACITY, course.getCapacity());
                    values.put(YogaCourseDbHelper.COLUMN_COURSE_DURATION, course.getDuration());
                    values.put(YogaCourseDbHelper.COLUMN_COURSE_PRICE, course.getPrice());
                    values.put(YogaCourseDbHelper.COLUMN_COURSE_CLASS_TYPE, course.getClassType());
                    values.put(YogaCourseDbHelper.COLUMN_COURSE_DESCRIPTION, course.getDescription());


                    Log.d("COURSE", course.getName());
                    // Insert into SQLite database
                    db.insert(YogaCourseDbHelper.TABLE_COURSE, null, values);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Log or handle database error
            }
        });
    }

    // Delete a course from Firebase by ID
    public void deleteCourseFirebase(String id) {
        DatabaseReference courseRef = coursesRef.child(String.valueOf(id));
        courseRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("FirebaseDelete", "Course deleted successfully");
            } else {
                Log.e("FirebaseDelete", "Error deleting course", task.getException());
            }
        });
    }

    // Save a course to Firebase with a provided ID
    public void saveCourseFirebase(String courseId, YogaCourse course) {
        coursesRef.child(courseId).setValue(course).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("FirebaseAdd", "Course added successfully");
            } else {
                Log.e("FirebaseAdd", "Error adding course", task.getException());
            }
        });
    }

    // Save a course to Firebase and auto-generate an ID
    public void saveCourseFirebase(YogaCourse course) {
        String courseId = classesRef.push().getKey(); // Generate unique ID

        course.setId(courseId);

        coursesRef.child(courseId).setValue(course).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("FirebaseAdd", "Course added successfully");
            } else {
                Log.e("FirebaseAdd", "Error adding course", task.getException());
            }
        });
    }

    // Save a class to Firebase with a provided ID
    public void saveClassFirebase(String classId, YogaClass yogaClass) {
        classesRef.child(String.valueOf(classId)).setValue(yogaClass).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("FirebaseAdd", "Class added successfully");
            } else {
                Log.e("FirebaseAdd", "Error adding class", task.getException());
            }
        });
    }

    // Save a class to Firebase and auto-generate an ID
    public void saveClassFirebase(YogaClass yogaClass) {
        String classId = classesRef.push().getKey();

        classesRef.child(String.valueOf(classId)).setValue(yogaClass).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("FirebaseAdd", "Class added successfully");
            } else {
                Log.e("FirebaseAdd", "Error adding class", task.getException());
            }
        });
    }

    // Delete a class from Firebase by ID
    public void deleteClassFirebase(String id) {
        DatabaseReference classRef = classesRef.child(String.valueOf(id));
        classRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("FirebaseDelete", "Course deleted successfully");
            } else {
                Log.e("FirebaseDelete", "Error deleting course", task.getException());
            }
        });
    }

    // Delete all data from both tables in SQLite
    public void deleteAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM course");
        db.execSQL("DELETE FROM class");
    }
}
