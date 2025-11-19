package com.hospital.management.model;

import java.io.Serializable;

public class Patient implements Serializable {
    private int patientId;
    private String firstName;
    private String lastName;
    private String birthDate;
    private String phoneNumber;
    private String email;
    private String address;
    private String policyOMS;
    private String snils;
    private int district;

    // Конструкторы
    public Patient() {}

    public Patient(String firstName, String lastName, String birthDate,
                   String phoneNumber, String email, String address,
                   String policyOMS, String snils, int district) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
        this.policyOMS = policyOMS;
        this.snils = snils;
        this.district = district;
    }

    // Геттеры и сеттеры
    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFullName() { return firstName + " " + lastName; }

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPolicyOMS() { return policyOMS; }
    public void setPolicyOMS(String policyOMS) { this.policyOMS = policyOMS; }

    public String getSnils() { return snils; }
    public void setSnils(String snils) { this.snils = snils; }

    public int getDistrict() { return district; }
    public void setDistrict(int district) { this.district = district; }
}