package com.hospital.management.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "HospitalManagement.db";

    // SQL для создания таблицы Users
    private static final String SQL_CREATE_USERS_TABLE =
            "CREATE TABLE " + UserDao.TABLE_USERS + " (" +
                    UserDao.COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    UserDao.COLUMN_USERNAME + " TEXT NOT NULL UNIQUE," +
                    UserDao.COLUMN_PASSWORD + " TEXT NOT NULL," +
                    UserDao.COLUMN_ROLE + " TEXT NOT NULL," +
                    UserDao.COLUMN_ROLE_ID + " INTEGER DEFAULT 0" +
                    ")";

    // SQL для создания таблицы Patients
    private static final String SQL_CREATE_PATIENTS_TABLE =
            "CREATE TABLE " + PatientDao.TABLE_PATIENTS + " (" +
                    PatientDao.COLUMN_PATIENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    PatientDao.COLUMN_FIRST_NAME + " TEXT NOT NULL," +
                    PatientDao.COLUMN_LAST_NAME + " TEXT NOT NULL," +
                    PatientDao.COLUMN_BIRTH_DATE + " TEXT," +
                    PatientDao.COLUMN_PHONE_NUMBER + " TEXT," +
                    PatientDao.COLUMN_EMAIL + " TEXT," +
                    PatientDao.COLUMN_ADDRESS + " TEXT," +
                    PatientDao.COLUMN_POLICY_OMS + " TEXT," +
                    PatientDao.COLUMN_SNILS + " TEXT," +
                    PatientDao.COLUMN_DISTRICT + " INTEGER DEFAULT 0" +
                    ")";

    // SQL для создания таблицы Doctors
    private static final String SQL_CREATE_DOCTORS_TABLE =
            "CREATE TABLE " + DoctorDao.TABLE_DOCTORS + " (" +
                    DoctorDao.COLUMN_DOCTOR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DoctorDao.COLUMN_FIRST_NAME + " TEXT NOT NULL," +
                    DoctorDao.COLUMN_LAST_NAME + " TEXT NOT NULL," +
                    DoctorDao.COLUMN_SPECIALIZATION + " TEXT," +
                    DoctorDao.COLUMN_ROOM_NUMBER + " TEXT," +
                    DoctorDao.COLUMN_SCHEDULE + " TEXT," +
                    DoctorDao.COLUMN_EMAIL + " TEXT" +
                    ")";

    // SQL для удаления таблиц
    private static final String SQL_DELETE_USERS_TABLE =
            "DROP TABLE IF EXISTS " + UserDao.TABLE_USERS;

    private static final String SQL_DELETE_PATIENTS_TABLE =
            "DROP TABLE IF EXISTS " + PatientDao.TABLE_PATIENTS;

    private static final String SQL_DELETE_DOCTORS_TABLE =
            "DROP TABLE IF EXISTS " + DoctorDao.TABLE_DOCTORS;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database tables");

        // Создаем таблицы
        db.execSQL(SQL_CREATE_USERS_TABLE);
        db.execSQL(SQL_CREATE_PATIENTS_TABLE);
        db.execSQL(SQL_CREATE_DOCTORS_TABLE);

        // Добавляем начальные данные
        insertInitialData(db);

        Log.d(TAG, "Database tables created successfully");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

        // При обновлении удаляем старые таблицы и создаем новые
        db.execSQL(SQL_DELETE_USERS_TABLE);
        db.execSQL(SQL_DELETE_PATIENTS_TABLE);
        db.execSQL(SQL_DELETE_DOCTORS_TABLE);
        onCreate(db);

        Log.d(TAG, "Database upgrade completed");
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    /**
     * Добавление начальных данных в базу
     */
    private void insertInitialData(SQLiteDatabase db) {
        Log.d(TAG, "Inserting initial data");

        try {
            // Добавляем пользователя администратора
            ContentValues adminUser = new ContentValues();
            adminUser.put(UserDao.COLUMN_USERNAME, "admin");
            adminUser.put(UserDao.COLUMN_PASSWORD, "admin123");
            adminUser.put(UserDao.COLUMN_ROLE, "ADMIN");
            adminUser.put(UserDao.COLUMN_ROLE_ID, 0);

            long adminId = db.insert(UserDao.TABLE_USERS, null, adminUser);
            Log.d(TAG, "Admin user inserted with ID: " + adminId);

            // Добавляем тестового врача
            ContentValues doctor = new ContentValues();
            doctor.put(DoctorDao.COLUMN_FIRST_NAME, "Иван");
            doctor.put(DoctorDao.COLUMN_LAST_NAME, "Петров");
            doctor.put(DoctorDao.COLUMN_SPECIALIZATION, "Терапевт");
            doctor.put(DoctorDao.COLUMN_ROOM_NUMBER, "101");
            doctor.put(DoctorDao.COLUMN_SCHEDULE, "Пн-Пт 9:00-18:00");
            doctor.put(DoctorDao.COLUMN_EMAIL, "i.petrov@hospital.ru");

            long doctorId = db.insert(DoctorDao.TABLE_DOCTORS, null, doctor);
            Log.d(TAG, "Test doctor inserted with ID: " + doctorId);

            // Добавляем пользователя для врача
            ContentValues doctorUser = new ContentValues();
            doctorUser.put(UserDao.COLUMN_USERNAME, "doctor");
            doctorUser.put(UserDao.COLUMN_PASSWORD, "doctor123");
            doctorUser.put(UserDao.COLUMN_ROLE, "DOCTOR");
            doctorUser.put(UserDao.COLUMN_ROLE_ID, (int) doctorId);

            long doctorUserId = db.insert(UserDao.TABLE_USERS, null, doctorUser);
            Log.d(TAG, "Doctor user inserted with ID: " + doctorUserId);

            // Добавляем тестового пациента
            ContentValues patient = new ContentValues();
            patient.put(PatientDao.COLUMN_FIRST_NAME, "Мария");
            patient.put(PatientDao.COLUMN_LAST_NAME, "Сидорова");
            patient.put(PatientDao.COLUMN_BIRTH_DATE, "1985-05-15");
            patient.put(PatientDao.COLUMN_PHONE_NUMBER, "+79161234567");
            patient.put(PatientDao.COLUMN_EMAIL, "m.sidorova@mail.ru");
            patient.put(PatientDao.COLUMN_ADDRESS, "ул. Ленина, д. 10, кв. 5");
            patient.put(PatientDao.COLUMN_POLICY_OMS, "1234567890123456");
            patient.put(PatientDao.COLUMN_SNILS, "123-456-789 01");
            patient.put(PatientDao.COLUMN_DISTRICT, 1);

            long patientId = db.insert(PatientDao.TABLE_PATIENTS, null, patient);
            Log.d(TAG, "Test patient inserted with ID: " + patientId);

            // Добавляем пользователя для пациента
            ContentValues patientUser = new ContentValues();
            patientUser.put(UserDao.COLUMN_USERNAME, "patient");
            patientUser.put(UserDao.COLUMN_PASSWORD, "patient123");
            patientUser.put(UserDao.COLUMN_ROLE, "PATIENT");
            patientUser.put(UserDao.COLUMN_ROLE_ID, (int) patientId);

            long patientUserId = db.insert(UserDao.TABLE_USERS, null, patientUser);
            Log.d(TAG, "Patient user inserted with ID: " + patientUserId);

            Log.d(TAG, "Initial data inserted successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error inserting initial data: " + e.getMessage(), e);
        }
    }
}