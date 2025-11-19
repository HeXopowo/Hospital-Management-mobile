package com.hospital.management.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.hospital.management.R;
import com.hospital.management.model.Patient;

import java.util.ArrayList;
import java.util.List;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.PatientViewHolder> {

    private List<Patient> patients = new ArrayList<>();
    private OnPatientClickListener onPatientClickListener;

    public interface OnPatientClickListener {
        void onPatientClick(Patient patient);
    }

    public void setOnPatientClickListener(OnPatientClickListener listener) {
        this.onPatientClickListener = listener;
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_patient, parent, false);
        return new PatientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        Patient patient = patients.get(position);
        holder.bind(patient);

        // Обработка клика на элемент
        holder.cardView.setOnClickListener(v -> {
            if (onPatientClickListener != null) {
                onPatientClickListener.onPatientClick(patient);
            }
        });
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    public void setPatients(List<Patient> patients) {
        this.patients = patients != null ? patients : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addPatient(Patient patient) {
        if (patient != null) {
            patients.add(patient);
            notifyItemInserted(patients.size() - 1);
        }
    }

    public void updatePatient(Patient patient) {
        if (patient != null) {
            for (int i = 0; i < patients.size(); i++) {
                if (patients.get(i).getPatientId() == patient.getPatientId()) {
                    patients.set(i, patient);
                    notifyItemChanged(i);
                    break;
                }
            }
        }
    }

    public void removePatient(int patientId) {
        for (int i = 0; i < patients.size(); i++) {
            if (patients.get(i).getPatientId() == patientId) {
                patients.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    public List<Patient> getPatients() {
        return patients;
    }

    public void clear() {
        patients.clear();
        notifyDataSetChanged();
    }

    static class PatientViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardView;
        private final TextView tvPatientName;
        private final TextView tvBirthDate;
        private final TextView tvPhone;
        private final TextView tvEmail;
        private final TextView tvSnils;
        private final TextView tvPolicy;
        private final TextView tvDistrict;
        private final TextView tvAddress;

        public PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvBirthDate = itemView.findViewById(R.id.tvBirthDate);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvSnils = itemView.findViewById(R.id.tvSnils);
            tvPolicy = itemView.findViewById(R.id.tvPolicy);
            tvDistrict = itemView.findViewById(R.id.tvDistrict);
            tvAddress = itemView.findViewById(R.id.tvAddress);
        }

        public void bind(Patient patient) {
            tvPatientName.setText(patient.getFullName());

            // Обработка опциональных полей
            if (patient.getBirthDate() != null && !patient.getBirthDate().isEmpty()) {
                tvBirthDate.setText(String.format("Дата рождения: %s", patient.getBirthDate()));
                tvBirthDate.setVisibility(View.VISIBLE);
            } else {
                tvBirthDate.setVisibility(View.GONE);
            }

            if (patient.getPhoneNumber() != null && !patient.getPhoneNumber().isEmpty()) {
                tvPhone.setText(String.format("Тел: %s", patient.getPhoneNumber()));
                tvPhone.setVisibility(View.VISIBLE);
            } else {
                tvPhone.setVisibility(View.GONE);
            }

            // Email может быть обязательным полем, поэтому всегда показываем
            if (patient.getEmail() != null && !patient.getEmail().isEmpty()) {
                tvEmail.setText(patient.getEmail());
                tvEmail.setVisibility(View.VISIBLE);
            } else {
                tvEmail.setVisibility(View.GONE);
            }

            if (patient.getSnils() != null && !patient.getSnils().isEmpty()) {
                tvSnils.setText(String.format("СНИЛС: %s", patient.getSnils()));
                tvSnils.setVisibility(View.VISIBLE);
            } else {
                tvSnils.setVisibility(View.GONE);
            }

            if (patient.getPolicyOMS() != null && !patient.getPolicyOMS().isEmpty()) {
                tvPolicy.setText(String.format("Полис: %s", patient.getPolicyOMS()));
                tvPolicy.setVisibility(View.VISIBLE);
            } else {
                tvPolicy.setVisibility(View.GONE);
            }

            // District - это int, проверяем на значение по умолчанию (0)
            if (patient.getDistrict() != 0) {
                tvDistrict.setText(String.format("Участок: %d", patient.getDistrict()));
                tvDistrict.setVisibility(View.VISIBLE);
            } else {
                tvDistrict.setVisibility(View.GONE);
            }

            if (patient.getAddress() != null && !patient.getAddress().isEmpty()) {
                tvAddress.setText(String.format("Адрес: %s", patient.getAddress()));
                tvAddress.setVisibility(View.VISIBLE);
            } else {
                tvAddress.setVisibility(View.GONE);
            }
        }
    }
}