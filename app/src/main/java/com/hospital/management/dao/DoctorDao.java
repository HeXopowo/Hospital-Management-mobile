package com.hospital.management.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.hospital.management.model.Doctor;

import java.util.ArrayList;
import java.util.List;

public class DoctorDao {
    private static final String TAG = "DoctorDao";

    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    // Название таблицы и колонки
    public static final String TABLE_DOCTORS = "Doctors";
    public static final String COLUMN_DOCTOR_ID = "DoctorID";
    public static final String COLUMN_FIRST_NAME = "FirstName";
    public static final String COLUMN_LAST_NAME = "LastName";
    public static final String COLUMN_SPECIALIZATION = "Specialization";
    public static final String COLUMN_ROOM_NUMBER = "RoomNumber";
    public static final String COLUMN_SCHEDULE = "Schedule";
    public static final String COLUMN_EMAIL = "Email";

    public DoctorDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // Открытие базы данных для записи
    public void open() throws SQLException {
        try {
            database = dbHelper.getWritableDatabase();
            Log.d(TAG, "Database opened successfully");
        } catch (SQLException e) {
            Log.e(TAG, "Error opening database: " + e.getMessage());
            throw e;
        }
    }

    // Закрытие базы данных
    public void close() {
        if (dbHelper != null) {
            dbHelper.close();
            Log.d(TAG, "Database closed");
        }
    }

    /**
     * Добавление нового врача
     */
    public long addDoctor(Doctor doctor) {
        try {
            if (database == null || !database.isOpen()) {
                open();
            }

            ContentValues values = new ContentValues();
            values.put(COLUMN_FIRST_NAME, doctor.getFirstName());
            values.put(COLUMN_LAST_NAME, doctor.getLastName());
            values.put(COLUMN_SPECIALIZATION, doctor.getSpecialization());
            values.put(COLUMN_ROOM_NUMBER, doctor.getRoomNumber());
            values.put(COLUMN_SCHEDULE, doctor.getSchedule());
            values.put(COLUMN_EMAIL, doctor.getEmail());

            long result = database.insert(TABLE_DOCTORS, null, values);
            Log.d(TAG, "Doctor added with ID: " + result);
            return result;
        } catch (Exception e) {
            Log.e(TAG, "Error adding doctor: " + e.getMessage(), e);
            return -1;
        }
    }

    /**
     * Получение врача по ID
     */
    public Doctor getDoctorById(int doctorId) {
        try {
            if (database == null || !database.isOpen()) {
                open();
            }

            Doctor doctor = null;

            String[] columns = {
                    COLUMN_DOCTOR_ID,
                    COLUMN_FIRST_NAME,
                    COLUMN_LAST_NAME,
                    COLUMN_SPECIALIZATION,
                    COLUMN_ROOM_NUMBER,
                    COLUMN_SCHEDULE,
                    COLUMN_EMAIL
            };

            String selection = COLUMN_DOCTOR_ID + " = ?";
            String[] selectionArgs = {String.valueOf(doctorId)};

            Cursor cursor = database.query(
                    TABLE_DOCTORS,
                    columns,
                    selection,
                    selectionArgs,
                    null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                doctor = cursorToDoctor(cursor);
                cursor.close();
            }
            return doctor;
        } catch (Exception e) {
            Log.e(TAG, "Error getting doctor by ID: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Получение всех врачей
     */
    public List<Doctor> getAllDoctors() {
        List<Doctor> doctors = new ArrayList<>();
        try {
            if (database == null || !database.isOpen()) {
                open();
            }

            String[] columns = {
                    COLUMN_DOCTOR_ID,
                    COLUMN_FIRST_NAME,
                    COLUMN_LAST_NAME,
                    COLUMN_SPECIALIZATION,
                    COLUMN_ROOM_NUMBER,
                    COLUMN_SCHEDULE,
                    COLUMN_EMAIL
            };

            Cursor cursor = database.query(
                    TABLE_DOCTORS,
                    columns,
                    null, null, null, null,
                    COLUMN_LAST_NAME + ", " + COLUMN_FIRST_NAME
            );

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Doctor doctor = cursorToDoctor(cursor);
                    doctors.add(doctor);
                }
                cursor.close();
            }
            Log.d(TAG, "Retrieved " + doctors.size() + " doctors");
        } catch (Exception e) {
            Log.e(TAG, "Error getting all doctors: " + e.getMessage(), e);
        }
        return doctors;
    }

    /**
     * Обновление данных врача
     */
    public boolean updateDoctor(Doctor doctor) {
        try {
            if (database == null || !database.isOpen()) {
                open();
            }

            ContentValues values = new ContentValues();
            values.put(COLUMN_FIRST_NAME, doctor.getFirstName());
            values.put(COLUMN_LAST_NAME, doctor.getLastName());
            values.put(COLUMN_SPECIALIZATION, doctor.getSpecialization());
            values.put(COLUMN_ROOM_NUMBER, doctor.getRoomNumber());
            values.put(COLUMN_SCHEDULE, doctor.getSchedule());
            values.put(COLUMN_EMAIL, doctor.getEmail());

            String whereClause = COLUMN_DOCTOR_ID + " = ?";
            String[] whereArgs = {String.valueOf(doctor.getDoctorId())};

            int rowsAffected = database.update(TABLE_DOCTORS, values, whereClause, whereArgs);
            Log.d(TAG, "Doctor updated, rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error updating doctor: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Удаление врача
     */
    public boolean deleteDoctor(int doctorId) {
        try {
            if (database == null || !database.isOpen()) {
                open();
            }

            String whereClause = COLUMN_DOCTOR_ID + " = ?";
            String[] whereArgs = {String.valueOf(doctorId)};

            int rowsAffected = database.delete(TABLE_DOCTORS, whereClause, whereArgs);
            Log.d(TAG, "Doctor deleted, rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting doctor: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Проверка наличия связанных записей
     */
    public boolean hasRelatedRecords(int doctorId) {
        try {
            if (database == null || !database.isOpen()) {
                open();
            }

            // Проверяем наличие связанных записей в таблице Appointments (если существует)
            String[] tablesToCheck = {"Appointments", "DoctorSchedules", "MedicalRecords"};

            for (String table : tablesToCheck) {
                if (isTableExists(table)) {
                    String query = "SELECT COUNT(*) FROM " + table + " WHERE DoctorID = ?";
                    Cursor cursor = database.rawQuery(query, new String[]{String.valueOf(doctorId)});

                    if (cursor != null && cursor.moveToFirst()) {
                        int count = cursor.getInt(0);
                        cursor.close();
                        if (count > 0) {
                            Log.d(TAG, "Found " + count + " related records in table " + table);
                            return true;
                        }
                    }
                }
            }

            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error checking related records: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Проверка существования таблицы
     */
    private boolean isTableExists(String tableName) {
        try {
            Cursor cursor = database.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                    new String[]{tableName}
            );
            boolean exists = cursor.getCount() > 0;
            cursor.close();
            return exists;
        } catch (Exception e) {
            Log.e(TAG, "Error checking table existence: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Поиск врачей по специализации
     */
    public List<Doctor> getDoctorsBySpecialization(String specialization) {
        List<Doctor> doctors = new ArrayList<>();
        try {
            if (database == null || !database.isOpen()) {
                open();
            }

            String[] columns = {
                    COLUMN_DOCTOR_ID,
                    COLUMN_FIRST_NAME,
                    COLUMN_LAST_NAME,
                    COLUMN_SPECIALIZATION,
                    COLUMN_ROOM_NUMBER,
                    COLUMN_SCHEDULE,
                    COLUMN_EMAIL
            };

            String selection = COLUMN_SPECIALIZATION + " LIKE ?";
            String[] selectionArgs = {"%" + specialization + "%"};

            Cursor cursor = database.query(
                    TABLE_DOCTORS,
                    columns,
                    selection,
                    selectionArgs,
                    null, null,
                    COLUMN_LAST_NAME + ", " + COLUMN_FIRST_NAME
            );

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Doctor doctor = cursorToDoctor(cursor);
                    doctors.add(doctor);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting doctors by specialization: " + e.getMessage(), e);
        }
        return doctors;
    }

    /**
     * Проверка существования email
     */
    public boolean isEmailExists(String email) {
        try {
            if (database == null || !database.isOpen()) {
                open();
            }

            String[] columns = {COLUMN_DOCTOR_ID};
            String selection = COLUMN_EMAIL + " = ?";
            String[] selectionArgs = {email};

            Cursor cursor = database.query(
                    TABLE_DOCTORS,
                    columns,
                    selection,
                    selectionArgs,
                    null, null, null
            );

            boolean exists = cursor != null && cursor.getCount() > 0;
            if (cursor != null) {
                cursor.close();
            }
            return exists;
        } catch (Exception e) {
            Log.e(TAG, "Error checking email existence: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Получение количества врачей
     */
    public int getDoctorsCount() {
        int count = 0;
        try {
            if (database == null || !database.isOpen()) {
                open();
            }

            String query = "SELECT COUNT(*) FROM " + TABLE_DOCTORS;
            Cursor cursor = database.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting doctors count: " + e.getMessage(), e);
        }
        return count;
    }

    /**
     * Преобразование Cursor в объект Doctor
     */
    private Doctor cursorToDoctor(Cursor cursor) {
        Doctor doctor = new Doctor();
        doctor.setDoctorId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DOCTOR_ID)));
        doctor.setFirstName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIRST_NAME)));
        doctor.setLastName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_NAME)));
        doctor.setSpecialization(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SPECIALIZATION)));
        doctor.setRoomNumber(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROOM_NUMBER)));
        doctor.setSchedule(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SCHEDULE)));
        doctor.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)));
        return doctor;
    }
}