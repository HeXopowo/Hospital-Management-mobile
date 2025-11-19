package com.hospital.management.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.hospital.management.R;
import com.hospital.management.dao.DoctorDao;
import com.hospital.management.model.Doctor;

public class DoctorManagementActivity extends AppCompatActivity {
    private EditText etFirstName, etLastName, etSpecialization,
            etRoomNumber, etSchedule, etEmail;
    private Button btnSave, btnDelete, btnCancel;

    private DoctorDao doctorDao;
    private Doctor currentDoctor;
    private boolean isEditMode = false;
    private boolean canEdit = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_management);

        initializeViews();
        initializeDAO();
        checkEditMode();
        checkPermissions();
        setupClickListeners();
        updateUIForPermissions();
    }

    private void initializeViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etSpecialization = findViewById(R.id.etSpecialization);
        etRoomNumber = findViewById(R.id.etRoomNumber);
        etSchedule = findViewById(R.id.etSchedule);
        etEmail = findViewById(R.id.etEmail);

        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void initializeDAO() {
        doctorDao = new DoctorDao(this);
        doctorDao.open();
    }

    private void checkEditMode() {
        currentDoctor = (Doctor) getIntent().getSerializableExtra("DOCTOR");
        if (currentDoctor != null) {
            isEditMode = true;
            populateData();
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Редактирование врача");
            }
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            isEditMode = false;
            btnDelete.setVisibility(View.GONE);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Добавление врача");
            }
        }
    }

    private void checkPermissions() {
        canEdit = getIntent().getBooleanExtra("CAN_EDIT", true);
    }

    private void updateUIForPermissions() {
        if (!canEdit) {
            setFieldsEnabled(false);
            btnSave.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Просмотр врача");
            }
            btnCancel.setText("Назад");
        }
    }

    private void setFieldsEnabled(boolean enabled) {
        etFirstName.setEnabled(enabled);
        etLastName.setEnabled(enabled);
        etSpecialization.setEnabled(enabled);
        etRoomNumber.setEnabled(enabled);
        etSchedule.setEnabled(enabled);
        etEmail.setEnabled(enabled);
    }

    private void populateData() {
        if (currentDoctor != null) {
            etFirstName.setText(currentDoctor.getFirstName());
            etLastName.setText(currentDoctor.getLastName());
            etSpecialization.setText(currentDoctor.getSpecialization());
            etRoomNumber.setText(currentDoctor.getRoomNumber());
            etSchedule.setText(currentDoctor.getSchedule());
            etEmail.setText(currentDoctor.getEmail());
        }
    }

    private void setupClickListeners() {
        btnSave.setOnClickListener(v -> handleSave());
        btnDelete.setOnClickListener(v -> handleDelete());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void handleSave() {
        if (!canEdit) {
            Toast.makeText(this, "Недостаточно прав для редактирования данных врачей", Toast.LENGTH_LONG).show();
            return;
        }

        if (!validateForm()) {
            return;
        }

        Doctor doctor = isEditMode ? currentDoctor : new Doctor();

        try {
            doctor.setFirstName(etFirstName.getText().toString().trim());
            doctor.setLastName(etLastName.getText().toString().trim());
            doctor.setSpecialization(etSpecialization.getText().toString().trim());
            doctor.setRoomNumber(etRoomNumber.getText().toString().trim());
            doctor.setSchedule(etSchedule.getText().toString().trim());
            doctor.setEmail(etEmail.getText().toString().trim());

            showProgress(true);

            new Thread(() -> {
                try {
                    boolean success;
                    if (isEditMode) {
                        success = doctorDao.updateDoctor(doctor);
                    } else {
                        long result = doctorDao.addDoctor(doctor);
                        success = result != -1;
                    }

                    runOnUiThread(() -> {
                        showProgress(false);

                        if (success) {
                            String message = isEditMode ?
                                    "Данные врача обновлены" : "Врач добавлен";
                            Toast.makeText(DoctorManagementActivity.this,
                                    message, Toast.LENGTH_LONG).show();

                            // Возвращаем результат с информацией об операции
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("OPERATION", isEditMode ? "UPDATED" : "ADDED");
                            if (isEditMode) {
                                resultIntent.putExtra("DOCTOR", doctor);
                            }
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        } else {
                            Toast.makeText(DoctorManagementActivity.this,
                                    "Ошибка сохранения", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        showProgress(false);
                        Toast.makeText(DoctorManagementActivity.this,
                                "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }).start();

        } catch (IllegalArgumentException e) {
            Toast.makeText(this, "Ошибка валидации: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void handleDelete() {
        if (!canEdit) {
            Toast.makeText(this, "Недостаточно прав для удаления врачей", Toast.LENGTH_LONG).show();
            return;
        }

        if (currentDoctor == null) return;

        // Проверяем наличие связанных записей перед удалением
        new Thread(() -> {
            try {
                boolean hasRelatedRecords = doctorDao.hasRelatedRecords(currentDoctor.getDoctorId());

                runOnUiThread(() -> {
                    if (hasRelatedRecords) {
                        new android.app.AlertDialog.Builder(this)
                                .setTitle("Невозможно удалить")
                                .setMessage("Невозможно удалить врача " + currentDoctor.getFullName() +
                                        ", так как есть связанные записи (приемы, назначения и т.д.).")
                                .setPositiveButton("OK", null)
                                .show();
                    } else {
                        showDeleteConfirmationDialog();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(DoctorManagementActivity.this,
                            "Ошибка проверки связанных записей: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void showDeleteConfirmationDialog() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Подтверждение удаления")
                .setMessage("Вы действительно хотите удалить врача " +
                        currentDoctor.getFullName() + "?")
                .setPositiveButton("Удалить", (dialog, which) -> deleteDoctor())
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void deleteDoctor() {
        showProgress(true);

        new Thread(() -> {
            try {
                boolean success = doctorDao.deleteDoctor(currentDoctor.getDoctorId());

                runOnUiThread(() -> {
                    showProgress(false);

                    if (success) {
                        Toast.makeText(DoctorManagementActivity.this,
                                "Врач удален", Toast.LENGTH_LONG).show();

                        // Возвращаем результат с информацией об удалении
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("OPERATION", "DELETED");
                        resultIntent.putExtra("DELETED_DOCTOR_ID", currentDoctor.getDoctorId());
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    } else {
                        Toast.makeText(DoctorManagementActivity.this,
                                "Ошибка удаления", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(DoctorManagementActivity.this,
                            "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private boolean validateForm() {
        boolean isValid = true;

        if (TextUtils.isEmpty(etFirstName.getText().toString().trim())) {
            etFirstName.setError("Введите имя");
            isValid = false;
        }

        if (TextUtils.isEmpty(etLastName.getText().toString().trim())) {
            etLastName.setError("Введите фамилию");
            isValid = false;
        }

        if (TextUtils.isEmpty(etSpecialization.getText().toString().trim())) {
            etSpecialization.setError("Введите специализацию");
            isValid = false;
        }

        if (TextUtils.isEmpty(etRoomNumber.getText().toString().trim())) {
            etRoomNumber.setError("Введите номер кабинета");
            isValid = false;
        }

        if (TextUtils.isEmpty(etSchedule.getText().toString().trim())) {
            etSchedule.setError("Введите график работы");
            isValid = false;
        }

        if (TextUtils.isEmpty(etEmail.getText().toString().trim())) {
            etEmail.setError("Введите email");
            isValid = false;
        } else {
            String email = etEmail.getText().toString().trim();
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Неверный формат email");
                isValid = false;
            }
        }

        return isValid;
    }

    private void showProgress(boolean show) {
        if (show) {
            btnSave.setEnabled(false);
            btnDelete.setEnabled(false);
            btnCancel.setEnabled(false);
            btnSave.setText("Сохранение...");
        } else {
            btnSave.setEnabled(true);
            btnDelete.setEnabled(true);
            btnCancel.setEnabled(true);
            btnSave.setText(isEditMode ? "Обновить" : "Сохранить");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        setResult(RESULT_CANCELED);
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (doctorDao != null) {
            doctorDao.close();
        }
    }
}