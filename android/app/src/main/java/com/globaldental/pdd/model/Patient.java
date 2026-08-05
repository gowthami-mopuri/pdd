package com.globaldental.pdd.model;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class Patient {
    @SerializedName("id")
    private String id;

    @SerializedName("patient_id")
    private String patientId;

    @SerializedName("name")
    private String name;

    @SerializedName("age")
    private Integer age;

    @SerializedName("gender")
    private String gender;

    @SerializedName("height")
    private Integer height;

    @SerializedName("weight")
    private Integer weight;

    @SerializedName("last_visit")
    private String lastVisit;

    @SerializedName("status")
    private String status;

    @SerializedName("risk")
    private String risk;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("clinical_data")
    private Map<String, Object> clinicalData;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Integer getHeight() { return height; }
    public void setHeight(Integer height) { this.height = height; }

    public Integer getWeight() { return weight; }
    public void setWeight(Integer weight) { this.weight = weight; }

    public String getLastVisit() { return lastVisit; }
    public void setLastVisit(String lastVisit) { this.lastVisit = lastVisit; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRisk() { return risk; }
    public void setRisk(String risk) { this.risk = risk; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public Map<String, Object> getClinicalData() { return clinicalData; }
    public void setClinicalData(Map<String, Object> clinicalData) { this.clinicalData = clinicalData; }

    public String getMedicalHistory() {
        if (clinicalData != null && clinicalData.get("medical_history") != null) {
            return String.valueOf(clinicalData.get("medical_history"));
        }
        return "None reported";
    }
}
