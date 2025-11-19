package com.hospital.management.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hospital.management.R;
import com.hospital.management.adapters.DoctorAdapter;
import com.hospital.management.adapters.PatientAdapter;
import com.hospital.management.dao.DoctorDao;
import com.hospital.management.dao.PatientDao;
import com.hospital.management.model.Doctor;
import com.hospital.management.model.Patient;
import com.hospital.management.model.User;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_DOCTOR = 1;
    private static final int REQUEST_CODE_PATIENT = 2;

    private User currentUser;
    private RecyclerView rvDoctors, rvPatients;
    private TextView tvWelcome, tvEmptyDoctors, tvEmptyPatients;
    private ImageButton btnAddDoctor, btnAddPatient;

    private DoctorAdapter doctorAdapter;
    private PatientAdapter patientAdapter;
    private DoctorDao doctorDao;
    private PatientDao patientDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate started");

        try {
            setContentView(R.layout.activity_main);
            Log.d(TAG, "Layout inflated successfully");

            // Получаем пользователя из Intent
            currentUser = (User) getIntent().getSerializableExtra("USER");
            Log.d(TAG, "User received: " + (currentUser != null ? currentUser.getUsername() : "null"));

            if (currentUser == null) {
                Log.e(TAG, "User is null, finishing activity");
                Toast.makeText(this, "Ошибка: пользователь не найден", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            initializeViews();
            initializeDAOs();
            loadData();
            updateUI();
            setupButtonClickListeners();

            Log.d(TAG, "MainActivity created successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Ошибка запуска: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void initializeViews() {
        Log.d(TAG, "Initializing views");
        try {
            Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar == null) {
                Log.e(TAG, "Toolbar not found!");
                throw new RuntimeException("Toolbar not found");
            }

            // Устанавливаем Toolbar как Action Bar
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Главная - " + currentUser.getRole());
            }
            Log.d(TAG, "Toolbar initialized");

            tvWelcome = findViewById(R.id.tvWelcome);
            rvDoctors = findViewById(R.id.rvDoctors);
            rvPatients = findViewById(R.id.rvPatients);
            tvEmptyDoctors = findViewById(R.id.tvEmptyDoctors);
            tvEmptyPatients = findViewById(R.id.tvEmptyPatients);

            // Инициализация кнопок добавления
            btnAddDoctor = findViewById(R.id.add_doctor);
            btnAddPatient = findViewById(R.id.add_patient);

            // Настройка RecyclerView для врачей
            if (rvDoctors != null) {
                rvDoctors.setLayoutManager(new LinearLayoutManager(this));
                doctorAdapter = new DoctorAdapter();
                rvDoctors.setAdapter(doctorAdapter);
                Log.d(TAG, "Doctors RecyclerView initialized");
            }

            // Настройка RecyclerView для пациентов
            if (rvPatients != null) {
                rvPatients.setLayoutManager(new LinearLayoutManager(this));
                patientAdapter = new PatientAdapter();
                rvPatients.setAdapter(patientAdapter);
                Log.d(TAG, "Patients RecyclerView initialized");
            }

            // Обработчики кликов для адаптеров
            setupAdaptersClickListeners();
            Log.d(TAG, "All views initialized successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
            throw e;
        }
    }

    private void setupButtonClickListeners() {
        Log.d(TAG, "Setting up button click listeners");

        // Обработчик для кнопки добавления врача
        if (btnAddDoctor != null) {
            btnAddDoctor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ("ADMIN".equals(currentUser.getRole())) {
                        Intent intent = new Intent(MainActivity.this, DoctorManagementActivity.class);
                        intent.putExtra("CAN_EDIT", true);
                        startActivityForResult(intent, REQUEST_CODE_DOCTOR);
                    } else {
                        Toast.makeText(MainActivity.this,
                                "Недостаточно прав для добавления врачей",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        // Обработчик для кнопки добавления пациента
        if (btnAddPatient != null) {
            btnAddPatient.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ("ADMIN".equals(currentUser.getRole())) {
                        Intent intent = new Intent(MainActivity.this, PatientManagementActivity.class);
                        intent.putExtra("CAN_EDIT", true);
                        startActivityForResult(intent, REQUEST_CODE_PATIENT);
                    } else {
                        Toast.makeText(MainActivity.this,
                                "Недостаточно прав для добавления пациентов",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void initializeDAOs() {
        try {
            doctorDao = new DoctorDao(this);
            patientDao = new PatientDao(this);

            // Открываем соединения с базой данных
            doctorDao.open();
            patientDao.open();

            Log.d(TAG, "DAOs initialized and opened");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing DAOs: " + e.getMessage(), e);
            Toast.makeText(this, "Ошибка инициализации базы данных", Toast.LENGTH_LONG).show();
        }
    }

    private void loadData() {
        Log.d(TAG, "Starting data loading");

        // Создаем финальные копии для использования в лямбда-выражениях
        final DoctorDao finalDoctorDao = doctorDao;
        final PatientDao finalPatientDao = patientDao;
        final DoctorAdapter finalDoctorAdapter = doctorAdapter;
        final PatientAdapter finalPatientAdapter = patientAdapter;

        // Проверяем инициализацию DAO
        if (finalDoctorDao == null || finalPatientDao == null) {
            Log.e(TAG, "DAOs are not initialized");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this,
                            "Ошибка: база данных не инициализирована",
                            Toast.LENGTH_LONG).show();
                }
            });
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Doctor> doctors = new ArrayList<>();
                    List<Patient> patients = new ArrayList<>();

                    // Загружаем данные с проверкой на null
                    if (finalDoctorDao != null) {
                        doctors = finalDoctorDao.getAllDoctors();
                    }
                    if (finalPatientDao != null) {
                        patients = finalPatientDao.getAllPatients();
                    }

                    Log.d(TAG, "Data loaded - Doctors: " + doctors.size() + ", Patients: " + patients.size());

                    final List<Doctor> finalDoctors = doctors;
                    final List<Patient> finalPatients = patients;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (finalDoctorAdapter != null) {
                                    finalDoctorAdapter.setDoctors(finalDoctors);
                                }
                                if (finalPatientAdapter != null) {
                                    finalPatientAdapter.setPatients(finalPatients);
                                }

                                // Показать/скрыть сообщения о пустых списках
                                updateEmptyStates(finalDoctors, finalPatients);
                                Log.d(TAG, "Data displayed in UI");
                            } catch (Exception e) {
                                Log.e(TAG, "Error updating UI: " + e.getMessage(), e);
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error loading data: " + e.getMessage(), e);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this,
                                    "Ошибка загрузки данных: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }

    private void updateUI() {
        Log.d(TAG, "Updating UI for role: " + currentUser.getRole());
        try {
            String welcomeText = "Добро пожаловать, " + currentUser.getUsername() + "!";
            if (tvWelcome != null) {
                tvWelcome.setText(welcomeText);
            }

            // Настройка видимости разделов в зависимости от роли
            View layoutDoctors = findViewById(R.id.layoutDoctors);
            View layoutPatients = findViewById(R.id.layoutPatients);

            // Управление видимостью кнопок добавления
            if ("ADMIN".equals(currentUser.getRole())) {
                // Админ видит всё и кнопки добавления
                if (layoutDoctors != null) layoutDoctors.setVisibility(View.VISIBLE);
                if (layoutPatients != null) layoutPatients.setVisibility(View.VISIBLE);
                if (btnAddDoctor != null) btnAddDoctor.setVisibility(View.VISIBLE);
                if (btnAddPatient != null) btnAddPatient.setVisibility(View.VISIBLE);
                Log.d(TAG, "ADMIN view setup");
            } else if ("DOCTOR".equals(currentUser.getRole())) {
                // Врач видит пациентов, но не может добавлять
                if (layoutDoctors != null) layoutDoctors.setVisibility(View.GONE);
                if (layoutPatients != null) layoutPatients.setVisibility(View.VISIBLE);
                if (btnAddDoctor != null) btnAddDoctor.setVisibility(View.GONE);
                if (btnAddPatient != null) btnAddPatient.setVisibility(View.GONE);
                Log.d(TAG, "DOCTOR view setup");
            } else if ("PATIENT".equals(currentUser.getRole())) {
                // Пациент видит врачей, но не может добавлять
                if (layoutDoctors != null) layoutDoctors.setVisibility(View.VISIBLE);
                if (layoutPatients != null) layoutPatients.setVisibility(View.GONE);
                if (btnAddDoctor != null) btnAddDoctor.setVisibility(View.GONE);
                if (btnAddPatient != null) btnAddPatient.setVisibility(View.GONE);
                Log.d(TAG, "PATIENT view setup");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating UI: " + e.getMessage(), e);
        }
    }

    private void setupAdaptersClickListeners() {
        Log.d(TAG, "Setting up adapter click listeners");
        try {
            final User finalCurrentUser = currentUser;

            // Обработка клика на врача
            if (doctorAdapter != null) {
                doctorAdapter.setOnDoctorClickListener(new DoctorAdapter.OnDoctorClickListener() {
                    @Override
                    public void onDoctorClick(Doctor doctor) {
                        if ("ADMIN".equals(finalCurrentUser.getRole())) {
                            // Админ может редактировать врача
                            Intent intent = new Intent(MainActivity.this, DoctorManagementActivity.class);
                            intent.putExtra("DOCTOR", doctor);
                            intent.putExtra("CAN_EDIT", true);
                            startActivityForResult(intent, REQUEST_CODE_DOCTOR);
                        } else if ("PATIENT".equals(finalCurrentUser.getRole())) {
                            // Пациент может только просматривать врача
                            Intent intent = new Intent(MainActivity.this, DoctorManagementActivity.class);
                            intent.putExtra("DOCTOR", doctor);
                            intent.putExtra("CAN_EDIT", false);
                            startActivityForResult(intent, REQUEST_CODE_DOCTOR);
                        } else {
                            // Другие роли только просматривают
                            Toast.makeText(MainActivity.this,
                                    doctor.getFullName() + " - " + doctor.getSpecialization(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            // Обработка клика на пациента
            if (patientAdapter != null) {
                patientAdapter.setOnPatientClickListener(new PatientAdapter.OnPatientClickListener() {
                    @Override
                    public void onPatientClick(Patient patient) {
                        if ("ADMIN".equals(finalCurrentUser.getRole())) {
                            // Только Админ может редактировать пациента
                            Intent intent = new Intent(MainActivity.this, PatientManagementActivity.class);
                            intent.putExtra("PATIENT", patient);
                            intent.putExtra("CAN_EDIT", true);
                            startActivityForResult(intent, REQUEST_CODE_PATIENT);
                        } else if ("DOCTOR".equals(finalCurrentUser.getRole())) {
                            // Врач может только просматривать пациента
                            Intent intent = new Intent(MainActivity.this, PatientManagementActivity.class);
                            intent.putExtra("PATIENT", patient);
                            intent.putExtra("CAN_EDIT", false);
                            startActivityForResult(intent, REQUEST_CODE_PATIENT);
                        } else {
                            // Пациенты только просматривают
                            Toast.makeText(MainActivity.this,
                                    patient.getFullName(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners: " + e.getMessage(), e);
        }
    }

    private void updateEmptyStates(List<Doctor> doctors, List<Patient> patients) {
        try {
            if (tvEmptyDoctors != null && rvDoctors != null) {
                if (doctors.isEmpty()) {
                    tvEmptyDoctors.setVisibility(View.VISIBLE);
                    rvDoctors.setVisibility(View.GONE);
                } else {
                    tvEmptyDoctors.setVisibility(View.GONE);
                    rvDoctors.setVisibility(View.VISIBLE);
                }
            }

            if (tvEmptyPatients != null && rvPatients != null) {
                if (patients.isEmpty()) {
                    tvEmptyPatients.setVisibility(View.VISIBLE);
                    rvPatients.setVisibility(View.GONE);
                } else {
                    tvEmptyPatients.setVisibility(View.GONE);
                    rvPatients.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating empty states: " + e.getMessage(), e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            String operation = data.getStringExtra("OPERATION");

            if (requestCode == REQUEST_CODE_DOCTOR) {
                if ("DELETED".equals(operation)) {
                    int deletedDoctorId = data.getIntExtra("DELETED_DOCTOR_ID", -1);
                    if (deletedDoctorId != -1 && doctorAdapter != null) {
                        doctorAdapter.removeDoctor(deletedDoctorId);
                        updateEmptyStates(doctorAdapter.getDoctors(),
                                patientAdapter != null ? patientAdapter.getPatients() : new ArrayList<>());
                        Toast.makeText(this, "Врач удален", Toast.LENGTH_SHORT).show();
                    }
                } else if ("UPDATED".equals(operation) || "ADDED".equals(operation)) {
                    // Перезагружаем данные для врачей
                    loadDoctorsData();
                    String message = "UPDATED".equals(operation) ?
                            "Данные врача обновлены" : "Врач добавлен";
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == REQUEST_CODE_PATIENT) {
                if ("DELETED".equals(operation)) {
                    int deletedPatientId = data.getIntExtra("DELETED_PATIENT_ID", -1);
                    if (deletedPatientId != -1 && patientAdapter != null) {
                        patientAdapter.removePatient(deletedPatientId);
                        updateEmptyStates(doctorAdapter != null ? doctorAdapter.getDoctors() : new ArrayList<>(),
                                patientAdapter.getPatients());
                        Toast.makeText(this, "Пациент удален", Toast.LENGTH_SHORT).show();
                    }
                } else if ("UPDATED".equals(operation) || "ADDED".equals(operation)) {
                    // Перезагружаем данные для пациентов
                    loadPatientsData();
                    String message = "UPDATED".equals(operation) ?
                            "Данные пациента обновлены" : "Пациент добавлен";
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void loadDoctorsData() {
        new Thread(() -> {
            try {
                List<Doctor> doctors = doctorDao.getAllDoctors();
                runOnUiThread(() -> {
                    if (doctorAdapter != null) {
                        doctorAdapter.setDoctors(doctors);
                        updateEmptyStates(doctors,
                                patientAdapter != null ? patientAdapter.getPatients() : new ArrayList<>());
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading doctors data: " + e.getMessage(), e);
            }
        }).start();
    }

    private void loadPatientsData() {
        new Thread(() -> {
            try {
                List<Patient> patients = patientDao.getAllPatients();
                runOnUiThread(() -> {
                    if (patientAdapter != null) {
                        patientAdapter.setPatients(patients);
                        updateEmptyStates(doctorAdapter != null ? doctorAdapter.getDoctors() : new ArrayList<>(),
                                patients);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading patients data: " + e.getMessage(), e);
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            getMenuInflater().inflate(R.menu.main_menu, menu);
            menu.findItem(R.id.menu_refresh).setVisible(true);
        } catch (Exception e) {
            Log.e(TAG, "Error creating options menu: " + e.getMessage(), e);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            int id = item.getItemId();

            if (id == R.id.menu_refresh) {
                loadData();
                Toast.makeText(this, "Данные обновлены", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.menu_settings) {
                Toast.makeText(this, "Настройки", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.menu_about) {
                Toast.makeText(this, "О программе", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.menu_logout) {
                finish();
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in options item selected: " + e.getMessage(), e);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        try {
            if (doctorDao != null) {
                doctorDao.close();
            }
            if (patientDao != null) {
                patientDao.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroy: " + e.getMessage(), e);
        }
    }
}