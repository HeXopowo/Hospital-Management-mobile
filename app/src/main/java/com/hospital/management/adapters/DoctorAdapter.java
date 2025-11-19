package com.hospital.management.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.hospital.management.R;
import com.hospital.management.model.Doctor;

import java.util.ArrayList;
import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder> {

    private List<Doctor> doctors = new ArrayList<>();
    private OnDoctorClickListener onDoctorClickListener;

    public interface OnDoctorClickListener {
        void onDoctorClick(Doctor doctor);
    }

    public void setOnDoctorClickListener(OnDoctorClickListener listener) {
        this.onDoctorClickListener = listener;
    }

    @NonNull
    @Override
    public DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_doctor, parent, false);
        return new DoctorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorViewHolder holder, int position) {
        Doctor doctor = doctors.get(position);
        holder.bind(doctor);

        // Обработка клика на элемент
        holder.cardView.setOnClickListener(v -> {
            if (onDoctorClickListener != null) {
                onDoctorClickListener.onDoctorClick(doctor);
            }
        });
    }

    @Override
    public int getItemCount() {
        return doctors.size();
    }

    public void setDoctors(List<Doctor> doctors) {
        this.doctors = doctors != null ? doctors : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addDoctor(Doctor doctor) {
        if (doctor != null) {
            doctors.add(doctor);
            notifyItemInserted(doctors.size() - 1);
        }
    }

    public void updateDoctor(Doctor doctor) {
        if (doctor != null) {
            for (int i = 0; i < doctors.size(); i++) {
                if (doctors.get(i).getDoctorId() == doctor.getDoctorId()) {
                    doctors.set(i, doctor);
                    notifyItemChanged(i);
                    break;
                }
            }
        }
    }

    public void removeDoctor(int doctorId) {
        for (int i = 0; i < doctors.size(); i++) {
            if (doctors.get(i).getDoctorId() == doctorId) {
                doctors.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    public List<Doctor> getDoctors() {
        return doctors;
    }

    public void clear() {
        doctors.clear();
        notifyDataSetChanged();
    }

    static class DoctorViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardView;
        private final TextView tvDoctorName;
        private final TextView tvSpecialization;
        private final TextView tvRoom;
        private final TextView tvSchedule;
        private final TextView tvEmail;

        public DoctorViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvDoctorName = itemView.findViewById(R.id.tvDoctorName);
            tvSpecialization = itemView.findViewById(R.id.tvSpecialization);
            tvRoom = itemView.findViewById(R.id.tvRoom);
            tvSchedule = itemView.findViewById(R.id.tvSchedule);
            tvEmail = itemView.findViewById(R.id.tvEmail);
        }

        public void bind(Doctor doctor) {
            tvDoctorName.setText(doctor.getFullName());
            tvSpecialization.setText(doctor.getSpecialization());
            tvRoom.setText(String.format("Каб. %s", doctor.getRoomNumber()));
            tvSchedule.setText(doctor.getSchedule());
            tvEmail.setText(doctor.getEmail());
        }
    }
}