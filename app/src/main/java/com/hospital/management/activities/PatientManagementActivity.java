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
import com.hospital.management.dao.PatientDao;
import com.hospital.management.model.Patient;

public class PatientManagementActivity extends AppCompatActivity {
    private EditText etFirstName, etLastName, etBirthDate, etPhoneNumber,
            etEmail, etSnils, etPolicyOMS, etDistrict, etAddress;
    private Button btnSave, btnDelete, btnCancel;

    private PatientDao patientDao;
    private Patient currentPatient;
    private boolean isEditMode = false;
    private boolean canEdit = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_management);

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
        etBirthDate = findViewById(R.id.etBirthDate);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etEmail = findViewById(R.id.etEmail);
        etSnils = findViewById(R.id.etSnils);
        etPolicyOMS = findViewById(R.id.etPolicyOMS);
        etDistrict = findViewById(R.id.etDistrict);
        etAddress = findViewById(R.id.etAddress);

        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void initializeDAO() {
        patientDao = new PatientDao(this);
        patientDao.open();
    }

    private void checkEditMode() {
        currentPatient = (Patient) getIntent().getSerializableExtra("PATIENT");
        if (currentPatient != null) {
            isEditMode = true;
            populateData();
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Редактирование пациента");
            }
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            isEditMode = false;
            btnDelete.setVisibility(View.GONE);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Добавление пациента");
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
                getSupportActionBar().setTitle("Просмотр пациента");
            }
            btnCancel.setText("Назад");
        }
    }

    private void setFieldsEnabled(boolean enabled) {
        etFirstName.setEnabled(enabled);
        etLastName.setEnabled(enabled);
        etBirthDate.setEnabled(enabled);
        etPhoneNumber.setEnabled(enabled);
        etEmail.setEnabled(enabled);
        etSnils.setEnabled(enabled);
        etPolicyOMS.setEnabled(enabled);
        etDistrict.setEnabled(enabled);
        etAddress.setEnabled(enabled);
    }

    private void populateData() {
        if (currentPatient != null) {
            etFirstName.setText(currentPatient.getFirstName());
            etLastName.setText(currentPatient.getLastName());
            etBirthDate.setText(currentPatient.getBirthDate());
            etPhoneNumber.setText(currentPatient.getPhoneNumber());
            etEmail.setText(currentPatient.getEmail());
            etSnils.setText(currentPatient.getSnils());
            etPolicyOMS.setText(currentPatient.getPolicyOMS());
            etDistrict.setText(String.valueOf(currentPatient.getDistrict()));
            etAddress.setText(currentPatient.getAddress());
        }
    }

    private void setupClickListeners() {
        btnSave.setOnClickListener(v -> handleSave());
        btnDelete.setOnClickListener(v -> handleDelete());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void handleSave() {
        if (!canEdit) {
            Toast.makeText(this, "Недостаточно прав для редактирования данных пациентов", Toast.LENGTH_LONG).show();
            return;
        }

        if (!validateForm()) {
            return;
        }

        Patient patient = isEditMode ? currentPatient : new Patient();

        try {
            patient.setFirstName(etFirstName.getText().toString().trim());
            patient.setLastName(etLastName.getText().toString().trim());
            patient.setBirthDate(etBirthDate.getText().toString().trim());
            patient.setPhoneNumber(etPhoneNumber.getText().toString().trim());
            patient.setEmail(etEmail.getText().toString().trim());
            patient.setSnils(etSnils.getText().toString().trim());
            patient.setPolicyOMS(etPolicyOMS.getText().toString().trim());

            String districtText = etDistrict.getText().toString().trim();
            int district = districtText.isEmpty() ? 0 : Integer.parseInt(districtText);
            patient.setDistrict(district);

            patient.setAddress(etAddress.getText().toString().trim());

            showProgress(true);

            new Thread(() -> {
                try {
                    boolean success;
                    if (isEditMode) {
                        success = patientDao.updatePatient(patient);
                    } else {
                        long result = patientDao.addPatient(patient);
                        success = result != -1;
                    }

                    runOnUiThread(() -> {
                        showProgress(false);

                        if (success) {
                            String message = isEditMode ?
                                    "Данные пациента обновлены" : "Пациент добавлен";
                            Toast.makeText(PatientManagementActivity.this,
                                    message, Toast.LENGTH_LONG).show();

                            // Возвращаем результат с информацией об операции
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("OPERATION", isEditMode ? "UPDATED" : "ADDED");
                            if (isEditMode) {
                                resultIntent.putExtra("PATIENT", patient);
                            }
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        } else {
                            Toast.makeText(PatientManagementActivity.this,
                                    "Ошибка сохранения", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        showProgress(false);
                        Toast.makeText(PatientManagementActivity.this,
                                "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }).start();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Ошибка: участок должен быть числом",
                    Toast.LENGTH_LONG).show();
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, "Ошибка валидации: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void handleDelete() {
        if (!canEdit) {
            Toast.makeText(this, "Недостаточно прав для удаления пациентов", Toast.LENGTH_LONG).show();
            return;
        }

        if (currentPatient == null) return;

        // Проверяем наличие связанных записей перед удалением
        new Thread(() -> {
            try {
                boolean hasRelatedRecords = patientDao.hasRelatedRecords(currentPatient.getPatientId());

                runOnUiThread(() -> {
                    if (hasRelatedRecords) {
                        new android.app.AlertDialog.Builder(this)
                                .setTitle("Невозможно удалить")
                                .setMessage("Невозможно удалить пациента " + currentPatient.getFullName() +
                                        ", так как есть связанные записи (приемы, назначения и т.д.).")
                                .setPositiveButton("OK", null)
                                .show();
                    } else {
                        showDeleteConfirmationDialog();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(PatientManagementActivity.this,
                            "Ошибка проверки связанных записей: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void showDeleteConfirmationDialog() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Подтверждение удаления")
                .setMessage("Вы действительно хотите удалить пациента " +
                        currentPatient.getFullName() + "?")
                .setPositiveButton("Удалить", (dialog, which) -> deletePatient())
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void deletePatient() {
        showProgress(true);

        new Thread(() -> {
            try {
                boolean success = patientDao.deletePatient(currentPatient.getPatientId());

                runOnUiThread(() -> {
                    showProgress(false);

                    if (success) {
                        Toast.makeText(PatientManagementActivity.this,
                                "Пациент удален", Toast.LENGTH_LONG).show();

                        // Возвращаем результат с информацией об удалении
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("OPERATION", "DELETED");
                        resultIntent.putExtra("DELETED_PATIENT_ID", currentPatient.getPatientId());
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    } else {
                        Toast.makeText(PatientManagementActivity.this,
                                "Ошибка удаления", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(PatientManagementActivity.this,
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

        String phone = etPhoneNumber.getText().toString().trim();
        if (!TextUtils.isEmpty(phone)) {
            if (!phone.matches("^\\+?[0-9\\-\\s()]{7,20}$")) {
                etPhoneNumber.setError("Неверный формат телефона");
                isValid = false;
            }
        }

        String snils = etSnils.getText().toString().trim();
        if (!TextUtils.isEmpty(snils)) {
            if (!snils.matches("^\\d{3}-\\d{3}-\\d{3} \\d{2}$")) {
                etSnils.setError("Формат: XXX-XXX-XXX XX");
                isValid = false;
            }
        }

        String policy = etPolicyOMS.getText().toString().trim();
        if (!TextUtils.isEmpty(policy)) {
            if (!policy.matches("^\\d{16}$")) {
                etPolicyOMS.setError("Должно быть 16 цифр");
                isValid = false;
            }
        }

        String birthDate = etBirthDate.getText().toString().trim();
        if (!TextUtils.isEmpty(birthDate)) {
            if (!birthDate.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                etBirthDate.setError("Формат: ГГГГ-ММ-ДД");
                isValid = false;
            }
        }

        String district = etDistrict.getText().toString().trim();
        if (!TextUtils.isEmpty(district)) {
            try {
                Integer.parseInt(district);
            } catch (NumberFormatException e) {
                etDistrict.setError("Участок должен быть числом");
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
        if (patientDao != null) {
            patientDao.close();
        }
    }
}