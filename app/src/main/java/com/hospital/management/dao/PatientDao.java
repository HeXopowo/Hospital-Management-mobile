package com.hospital.management.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.hospital.management.model.Patient;

import java.util.ArrayList;
import java.util.List;

public class PatientDao {
    private static final String TAG = "PatientDao";

    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    // Название таблицы и колонки
    public static final String TABLE_PATIENTS = "Patients";
    public static final String COLUMN_PATIENT_ID = "PatientID";
    public static final String COLUMN_FIRST_NAME = "FirstName";
    public static final String COLUMN_LAST_NAME = "LastName";
    public static final String COLUMN_BIRTH_DATE = "BirthDate";
    public static final String COLUMN_PHONE_NUMBER = "PhoneNumber";
    public static final String COLUMN_EMAIL = "Email";
    public static final String COLUMN_ADDRESS = "Address";
    public static final String COLUMN_POLICY_OMS = "PolicyOMS";
    public static final String COLUMN_SNILS = "SNILS";
    public static final String COLUMN_DISTRICT = "District";

    public PatientDao(Context context) {
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
     * Добавление нового пациента
     */
    public long addPatient(Patient patient) {
        try {
            if (database == null || !database.isOpen()) {
                open();
            }

            ContentValues values = new ContentValues();
            values.put(COLUMN_FIRST_NAME, patient.getFirstName());
            values.put(COLUMN_LAST_NAME, patient.getLastName());
            values.put(COLUMN_BIRTH_DATE, patient.getBirthDate());
            values.put(COLUMN_PHONE_NUMBER, patient.getPhoneNumber());
            values.put(COLUMN_EMAIL, patient.getEmail());
            values.put(COLUMN_ADDRESS, patient.getAddress());
            values.put(COLUMN_POLICY_OMS, patient.getPolicyOMS());
            values.put(COLUMN_SNILS, patient.getSnils());
            values.put(COLUMN_DISTRICT, patient.getDistrict());

            long result = database.insert(TABLE_PATIENTS, null, values);
            Log.d(TAG, "Patient added with ID: " + result);
            return result;
        } catch (Exception e) {
            Log.e(TAG, "Error adding patient: " + e.getMessage(), e);
            return -1;
        }
    }

    /**
     * Получение пациента по ID
     */
    public Patient getPatientById(int patientId) {
        try {
            if (database == null || !database.isOpen()) {
                open();
            }

            Patient patient = null;

            String[] columns = {
                    COLUMN_PATIENT_ID,
                    COLUMN_FIRST_NAME,
                    COLUMN_LAST_NAME,
                    COLUMN_BIRTH_DATE,
                    COLUMN_PHONE_NUMBER,
                    COLUMN_EMAIL,
                    COLUMN_ADDRESS,
                    COLUMN_POLICY_OMS,
                    COLUMN_SNILS,
                    COLUMN_DISTRICT
            };

            String selection = COLUMN_PATIENT_ID + " = ?";
            String[] selectionArgs = {String.valueOf(patientId)};

            Cursor cursor = database.query(
                    TABLE_PATIENTS,
                    columns,
                    selection,
                    selectionArgs,
                    null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                patient = cursorToPatient(cursor);
                cursor.close();
            }
            return patient;
        } catch (Exception e) {
            Log.e(TAG, "Error getting patient by ID: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Получение всех пациентов
     */
    public List<Patient> getAllPatients() {
        List<Patient> patients = new ArrayList<>();
        try {
            if (database == null || !database.isOpen()) {
                open();
            }

            String[] columns = {
                    COLUMN_PATIENT_ID,
                    COLUMN_FIRST_NAME,
                    COLUMN_LAST_NAME,
                    COLUMN_BIRTH_DATE,
                    COLUMN_PHONE_NUMBER,
                    COLUMN_EMAIL,
                    COLUMN_ADDRESS,
                    COLUMN_POLICY_OMS,
                    COLUMN_SNILS,
                    COLUMN_DISTRICT
            };

            Cursor cursor = database.query(
                    TABLE_PATIENTS,
                    columns,
                    null, null, null, null,
                    COLUMN_LAST_NAME + ", " + COLUMN_FIRST_NAME
            );

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Patient patient = cursorToPatient(cursor);
                    patients.add(patient);
                }
                cursor.close();
            }
            Log.d(TAG, "Retrieved " + patients.size() + " patients");
        } catch (Exception e) {
            Log.e(TAG, "Error getting all patients: " + e.getMessage(), e);
        }
        return patients;
    }

    /**
     * Обновление данных пациента
     */
    public boolean updatePatient(Patient patient) {
        try {
            if (database == null || !database.isOpen()) {
                open();
            }

            ContentValues values = new ContentValues();
            values.put(COLUMN_FIRST_NAME, patient.getFirstName());
            values.put(COLUMN_LAST_NAME, patient.getLastName());
            values.put(COLUMN_BIRTH_DATE, patient.getBirthDate());
            values.put(COLUMN_PHONE_NUMBER, patient.getPhoneNumber());
            values.put(COLUMN_EMAIL, patient.getEmail());
            values.put(COLUMN_ADDRESS, patient.getAddress());
            values.put(COLUMN_POLICY_OMS, patient.getPolicyOMS());
            values.put(COLUMN_SNILS, patient.getSnils());
            values.put(COLUMN_DISTRICT, patient.getDistrict());

            String whereClause = COLUMN_PATIENT_ID + " = ?";
            String[] whereArgs = {String.valueOf(patient.getPatientId())};

            int rowsAffected = database.update(TABLE_PATIENTS, values, whereClause, whereArgs);
            Log.d(TAG, "Patient updated, rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error updating patient: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Удаление пациента
     */
    public boolean deletePatient(int patientId) {
        try {
            if (database == null || !database.isOpen()) {
                open();
            }

            String whereClause = COLUMN_PATIENT_ID + " = ?";
            String[] whereArgs = {String.valueOf(patientId)};

            int rowsAffected = database.delete(TABLE_PATIENTS, whereClause, whereArgs);
            Log.d(TAG, "Patient deleted, rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting patient: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Проверка наличия связанных записей
     */
    public boolean hasRelatedRecords(int patientId) {
        try {
            if (database == null || !database.isOpen()) {
                open();
            }

            // Проверяем наличие связанных записей в таблице Appointments (если существует)
            String[] tablesToCheck = {"Appointments", "MedicalRecords", "Prescriptions"};

            for (String table : tablesToCheck) {
                if (isTableExists(table)) {
                    String query = "SELECT COUNT(*) FROM " + table + " WHERE PatientID = ?";
                    Cursor cursor = database.rawQuery(query, new String[]{String.valueOf(patientId)});

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
     * Поиск пациентов по фамилии
     */
    public List<Patient> getPatientsByLastName(String lastName) {
        List<Patient> patients = new ArrayList<>();
        try {
            if (database == null || !database.isOpen()) {
                open();
            }

            String[] columns = {
                    COLUMN_PATIENT_ID,
                    COLUMN_FIRST_NAME,
                    COLUMN_LAST_NAME,
                    COLUMN_BIRTH_DATE,
                    COLUMN_PHONE_NUMBER,
                    COLUMN_EMAIL,
                    COLUMN_ADDRESS,
                    COLUMN_POLICY_OMS,
                    COLUMN_SNILS,
                    COLUMN_DISTRICT
            };

            String selection = COLUMN_LAST_NAME + " LIKE ?";
            String[] selectionArgs = {"%" + lastName + "%"};

            Cursor cursor = database.query(
                    TABLE_PATIENTS,
                    columns,
                    selection,
                    selectionArgs,
                    null, null,
                    COLUMN_LAST_NAME + ", " + COLUMN_FIRST_NAME
            );

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Patient patient = cursorToPatient(cursor);
                    patients.add(patient);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting patients by last name: " + e.getMessage(), e);
        }
        return patients;
    }

    /**
     * Проверка существования полиса ОМС
     */
    public boolean isPolicyOMSExists(String policyOMS) {
        try {
            if (database == null || !database.isOpen()) {
                open();
            }

            String[] columns = {COLUMN_PATIENT_ID};
            String selection = COLUMN_POLICY_OMS + " = ?";
            String[] selectionArgs = {policyOMS};

            Cursor cursor = database.query(
                    TABLE_PATIENTS,
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
            Log.e(TAG, "Error checking policy OMS existence: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Получение количества пациентов
     */
    public int getPatientsCount() {
        int count = 0;
        try {
            if (database == null || !database.isOpen()) {
                open();
            }

            String query = "SELECT COUNT(*) FROM " + TABLE_PATIENTS;
            Cursor cursor = database.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting patients count: " + e.getMessage(), e);
        }
        return count;
    }

    /**
     * Преобразование Cursor в объект Patient
     */
    private Patient cursorToPatient(Cursor cursor) {
        Patient patient = new Patient();
        patient.setPatientId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PATIENT_ID)));
        patient.setFirstName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIRST_NAME)));
        patient.setLastName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_NAME)));
        patient.setBirthDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BIRTH_DATE)));
        patient.setPhoneNumber(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE_NUMBER)));
        patient.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)));
        patient.setAddress(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS)));
        patient.setPolicyOMS(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_POLICY_OMS)));
        patient.setSnils(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SNILS)));
        patient.setDistrict(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DISTRICT)));
        return patient;
    }
}