package com.hospital.management.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.hospital.management.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserDao {
    private static final String TAG = "UserDao";

    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    // Название таблицы и колонки
    public static final String TABLE_USERS = "Users";
    public static final String COLUMN_USER_ID = "UserID";
    public static final String COLUMN_USERNAME = "Username";
    public static final String COLUMN_PASSWORD = "Password";
    public static final String COLUMN_ROLE = "Role";
    public static final String COLUMN_ROLE_ID = "RoleID";

    public UserDao(Context context) {
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
     * Аутентификация пользователя
     */
    public User authenticate(String username, String password) {
        try {
            if (database == null || !database.isOpen()) {
                open();
            }

            User user = null;

            String[] columns = {
                    COLUMN_USER_ID,
                    COLUMN_USERNAME,
                    COLUMN_PASSWORD,
                    COLUMN_ROLE,
                    COLUMN_ROLE_ID
            };

            String selection = COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?";
            String[] selectionArgs = {username, password};

            Log.d(TAG, "Authenticating user: " + username);

            Cursor cursor = database.query(
                    TABLE_USERS,
                    columns,
                    selection,
                    selectionArgs,
                    null, null, null
            );

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    user = cursorToUser(cursor);
                    Log.d(TAG, "User found: " + user.getUsername() + ", role: " + user.getRole());
                } else {
                    Log.d(TAG, "No user found with credentials: " + username);
                }
                cursor.close();
            } else {
                Log.e(TAG, "Cursor is null for authentication query");
            }

            return user;
        } catch (Exception e) {
            Log.e(TAG, "Error during authentication: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Добавление нового пользователя
     */
    public long addUser(User user) {
        try {
            if (database == null || !database.isOpen()) {
                open();
            }

            ContentValues values = new ContentValues();
            values.put(COLUMN_USERNAME, user.getUsername());
            values.put(COLUMN_PASSWORD, user.getPassword());
            values.put(COLUMN_ROLE, user.getRole());
            values.put(COLUMN_ROLE_ID, user.getRoleId());

            long result = database.insert(TABLE_USERS, null, values);
            Log.d(TAG, "User added with ID: " + result);
            return result;
        } catch (Exception e) {
            Log.e(TAG, "Error adding user: " + e.getMessage(), e);
            return -1;
        }
    }

    /**
     * Получение пользователя по ID
     */
    public User getUserById(int userId) {
        try {
            if (database == null || !database.isOpen()) {
                open();
            }

            User user = null;

            String[] columns = {
                    COLUMN_USER_ID,
                    COLUMN_USERNAME,
                    COLUMN_PASSWORD,
                    COLUMN_ROLE,
                    COLUMN_ROLE_ID
            };

            String selection = COLUMN_USER_ID + " = ?";
            String[] selectionArgs = {String.valueOf(userId)};

            Cursor cursor = database.query(
                    TABLE_USERS,
                    columns,
                    selection,
                    selectionArgs,
                    null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                user = cursorToUser(cursor);
                cursor.close();
            }
            return user;
        } catch (Exception e) {
            Log.e(TAG, "Error getting user by ID: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Получение пользователя по имени
     */
    public User getUserByUsername(String username) {
        try {
            if (database == null || !database.isOpen()) {
                open();
            }

            User user = null;

            String[] columns = {
                    COLUMN_USER_ID,
                    COLUMN_USERNAME,
                    COLUMN_PASSWORD,
                    COLUMN_ROLE,
                    COLUMN_ROLE_ID
            };

            String selection = COLUMN_USERNAME + " = ?";
            String[] selectionArgs = {username};

            Cursor cursor = database.query(
                    TABLE_USERS,
                    columns,
                    selection,
                    selectionArgs,
                    null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                user = cursorToUser(cursor);
                cursor.close();
            }
            return user;
        } catch (Exception e) {
            Log.e(TAG, "Error getting user by username: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Получение всех пользователей
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        try {
            if (database == null || !database.isOpen()) {
                open();
            }

            String[] columns = {
                    COLUMN_USER_ID,
                    COLUMN_USERNAME,
                    COLUMN_PASSWORD,
                    COLUMN_ROLE,
                    COLUMN_ROLE_ID
            };

            Cursor cursor = database.query(
                    TABLE_USERS,
                    columns,
                    null, null, null, null,
                    COLUMN_ROLE + ", " + COLUMN_USERNAME
            );

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    User user = cursorToUser(cursor);
                    users.add(user);
                }
                cursor.close();
            }
            Log.d(TAG, "Retrieved " + users.size() + " users");
        } catch (Exception e) {
            Log.e(TAG, "Error getting all users: " + e.getMessage(), e);
        }
        return users;
    }

    /**
     * Получение пользователей по роли
     */
    public List<User> getUsersByRole(String role) {
        List<User> users = new ArrayList<>();
        try {
            if (database == null || !database.isOpen()) {
                open();
            }

            String[] columns = {
                    COLUMN_USER_ID,
                    COLUMN_USERNAME,
                    COLUMN_PASSWORD,
                    COLUMN_ROLE,
                    COLUMN_ROLE_ID
            };

            String selection = COLUMN_ROLE + " = ?";
            String[] selectionArgs = {role};

            Cursor cursor = database.query(
                    TABLE_USERS,
                    columns,
                    selection,
                    selectionArgs,
                    null, null,
                    COLUMN_USERNAME
            );

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    User user = cursorToUser(cursor);
                    users.add(user);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting users by role: " + e.getMessage(), e);
        }
        return users;
    }

    /**
     * Обновление данных пользователя
     */
    public boolean updateUser(User user) {
        try {
            if (database == null || !database.isOpen()) {
                open();
            }

            ContentValues values = new ContentValues();
            values.put(COLUMN_USERNAME, user.getUsername());
            values.put(COLUMN_PASSWORD, user.getPassword());
            values.put(COLUMN_ROLE, user.getRole());
            values.put(COLUMN_ROLE_ID, user.getRoleId());

            String whereClause = COLUMN_USER_ID + " = ?";
            String[] whereArgs = {String.valueOf(user.getUserId())};

            int rowsAffected = database.update(TABLE_USERS, values, whereClause, whereArgs);
            Log.d(TAG, "User updated, rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error updating user: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Обновление пароля пользователя
     */
    public boolean updatePassword(int userId, String newPassword) {
        try {
            if (database == null || !database.isOpen()) {
                open();
            }

            ContentValues values = new ContentValues();
            values.put(COLUMN_PASSWORD, newPassword);

            String whereClause = COLUMN_USER_ID + " = ?";
            String[] whereArgs = {String.valueOf(userId)};

            int rowsAffected = database.update(TABLE_USERS, values, whereClause, whereArgs);
            Log.d(TAG, "Password updated, rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error updating password: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Удаление пользователя
     */
    public boolean deleteUser(int userId) {
        try {
            if (database == null || !database.isOpen()) {
                open();
            }

            String whereClause = COLUMN_USER_ID + " = ?";
            String[] whereArgs = {String.valueOf(userId)};

            int rowsAffected = database.delete(TABLE_USERS, whereClause, whereArgs);
            Log.d(TAG, "User deleted, rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting user: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Проверка существования имени пользователя
     */
    public boolean isUsernameExists(String username) {
        try {
            if (database == null || !database.isOpen()) {
                open();
            }

            String[] columns = {COLUMN_USER_ID};
            String selection = COLUMN_USERNAME + " = ?";
            String[] selectionArgs = {username};

            Cursor cursor = database.query(
                    TABLE_USERS,
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
            Log.e(TAG, "Error checking username existence: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Получение количества пользователей
     */
    public int getUsersCount() {
        int count = 0;
        try {
            if (database == null || !database.isOpen()) {
                open();
            }

            String query = "SELECT COUNT(*) FROM " + TABLE_USERS;
            Cursor cursor = database.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting users count: " + e.getMessage(), e);
        }
        return count;
    }

    /**
     * Получение количества пользователей по роли
     */
    public int getUsersCountByRole(String role) {
        int count = 0;
        try {
            if (database == null || !database.isOpen()) {
                open();
            }

            String query = "SELECT COUNT(*) FROM " + TABLE_USERS + " WHERE " + COLUMN_ROLE + " = ?";
            Cursor cursor = database.rawQuery(query, new String[]{role});

            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting users count by role: " + e.getMessage(), e);
        }
        return count;
    }

    /**
     * Создание пользователя для врача
     */
    public boolean createDoctorUser(String username, String password, int doctorId) {
        try {
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            user.setRole("DOCTOR");
            user.setRoleId(doctorId);

            return addUser(user) != -1;
        } catch (Exception e) {
            Log.e(TAG, "Error creating doctor user: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Создание пользователя для пациента
     */
    public boolean createPatientUser(String username, String password, int patientId) {
        try {
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            user.setRole("PATIENT");
            user.setRoleId(patientId);

            return addUser(user) != -1;
        } catch (Exception e) {
            Log.e(TAG, "Error creating patient user: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Преобразование Cursor в объект User
     */
    private User cursorToUser(Cursor cursor) {
        User user = new User();
        user.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)));
        user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)));
        user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)));
        user.setRole(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE)));
        user.setRoleId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ROLE_ID)));
        return user;
    }
}