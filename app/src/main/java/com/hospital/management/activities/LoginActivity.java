package com.hospital.management.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.hospital.management.R;
import com.hospital.management.dao.DatabaseHelper;
import com.hospital.management.dao.UserDao;
import com.hospital.management.model.User;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private EditText etUsername, etPassword;
    private Button btnLogin, btnRegister;
    private UserDao userDao;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate started");

        try {
            setContentView(R.layout.activity_login);
            Log.d(TAG, "Layout inflated successfully");

            initializeViews();
            setupClickListeners();
            initializeDatabase();

            // Автовход для тестирования
            autoLoginForTesting();

            Log.d(TAG, "LoginActivity created successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Ошибка инициализации приложения", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void initializeViews() {
        Log.d(TAG, "Initializing views");
        try {
            etUsername = findViewById(R.id.etUsername);
            etPassword = findViewById(R.id.etPassword);
            btnLogin = findViewById(R.id.btnLogin);
            btnRegister = findViewById(R.id.btnRegister);

            // Проверяем что все View найдены
            if (etUsername == null) Log.e(TAG, "etUsername not found");
            if (etPassword == null) Log.e(TAG, "etPassword not found");
            if (btnLogin == null) Log.e(TAG, "btnLogin not found");
            if (btnRegister == null) Log.e(TAG, "btnRegister not found");

            Log.d(TAG, "All views initialized");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
            throw e;
        }
    }

    private void initializeDatabase() {
        Log.d(TAG, "Initializing database");
        try {
            databaseHelper = new DatabaseHelper(this);
            userDao = new UserDao(this);

            // Проверяем доступность базы данных и наличие пользователей
            int userCount = userDao.getUsersCount();
            Log.d(TAG, "Total users in database: " + userCount);

            // Проверяем конкретно наличие пользователя admin
            User adminUser = userDao.getUserByUsername("admin");
            if (adminUser != null) {
                Log.d(TAG, "Admin user found: " + adminUser.getUsername());
            } else {
                Log.e(TAG, "Admin user NOT found in database!");
            }

            Log.d(TAG, "Database initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing database: " + e.getMessage(), e);
            Toast.makeText(this, "Ошибка инициализации базы данных", Toast.LENGTH_LONG).show();
        }
    }

    private void setupClickListeners() {
        Log.d(TAG, "Setting up click listeners");
        try {
            btnLogin.setOnClickListener(v -> {
                Log.d(TAG, "Login button clicked");
                handleLogin();
            });

            btnRegister.setOnClickListener(v -> {
                Log.d(TAG, "Register button clicked");
                handleRegister();
            });
            Log.d(TAG, "Click listeners setup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners: " + e.getMessage(), e);
        }
    }

    private void handleLogin() {
        Log.d(TAG, "handleLogin started");

        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        Log.d(TAG, "Username: " + username + ", Password: " + (password.isEmpty() ? "empty" : "provided"));

        // Валидация
        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Введите имя пользователя");
            Log.w(TAG, "Username validation failed");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Введите пароль");
            Log.w(TAG, "Password validation failed");
            return;
        }

        showProgress(true);
        Log.d(TAG, "Starting authentication process");

        new Thread(() -> {
            try {
                Log.d(TAG, "Authentication thread started");
                User user = userDao.authenticate(username, password);
                Log.d(TAG, "Authentication result: " + (user != null ? "SUCCESS" : "FAILED"));

                runOnUiThread(() -> {
                    showProgress(false);

                    if (user != null) {
                        Log.d(TAG, "User authenticated: " + user.getUsername() + ", role: " + user.getRole());
                        // Успешная аутентификация
                        try {
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("USER", user);
                            Log.d(TAG, "Starting MainActivity");
                            startActivity(intent);
                            finish();
                            Log.d(TAG, "LoginActivity finished");
                        } catch (Exception e) {
                            Log.e(TAG, "Error starting MainActivity: " + e.getMessage(), e);
                            Toast.makeText(LoginActivity.this,
                                    "Ошибка запуска приложения", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.w(TAG, "Authentication failed for user: " + username);
                        Toast.makeText(LoginActivity.this,
                                "Неверный логин или пароль", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error during authentication: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(LoginActivity.this,
                            "Ошибка при аутентификации: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void handleRegister() {
        Log.d(TAG, "handleRegister called");
        Toast.makeText(this,
                "Для регистрации обратитесь к администратору",
                Toast.LENGTH_LONG).show();
    }

    private void showProgress(boolean show) {
        Log.d(TAG, "Show progress: " + show);
        try {
            if (show) {
                btnLogin.setEnabled(false);
                btnRegister.setEnabled(false);
                btnLogin.setText("Вход...");
            } else {
                btnLogin.setEnabled(true);
                btnRegister.setEnabled(true);
                btnLogin.setText("Вход");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in showProgress: " + e.getMessage(), e);
        }
    }

    private void autoLoginForTesting() {
        Log.d(TAG, "Setting up auto-login for testing");
        try {
            // Автозаполнение для тестирования
            if (etUsername != null && etPassword != null) {
                etUsername.setText("admin");
                etPassword.setText("admin123");
                Log.d(TAG, "Auto-login credentials set");

                // Автоматический вход (раскомментировать для отладки)
                // handler.postDelayed(() -> handleLogin(), 1000);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in autoLoginForTesting: " + e.getMessage(), e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        try {
            if (userDao != null) {
                userDao.close();
            }
            if (databaseHelper != null) {
                databaseHelper.close();
            }
            Log.d(TAG, "Resources cleaned up");
        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroy: " + e.getMessage(), e);
        }
    }
}