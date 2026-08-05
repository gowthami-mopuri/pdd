package com.globaldental.pdd.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Report {
    @SerializedName("id")
    private String id;

    @SerializedName("patientId")
    private String patientId;

    @SerializedName("date")
    private String date;

    @SerializedName("riskLevel")
    private String riskLevel;

    @SerializedName("survivalProbability")
    private Integer survivalProbability;

    @SerializedName("actionItems")
    private List<String> actionItems;

    @SerializedName("patientName")
    private String patientName;

    @SerializedName("failureRisk")
    private Integer failureRisk;

    @SerializedName("confidence")
    private Integer confidence;

    @SerializedName("riskFactors")
    private List<String> riskFactors;

    @SerializedName("narrative")
    private List<String> narrative;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public Integer getSurvivalProbability() { return survivalProbability; }
    public void setSurvivalProbability(Integer survivalProbability) { this.survivalProbability = survivalProbability; }

    public Integer getFailureRisk() { return failureRisk; }
    public void setFailureRisk(Integer failureRisk) { this.failureRisk = failureRisk; }

    public Integer getConfidence() { return confidence; }
    public void setConfidence(Integer confidence) { this.confidence = confidence; }

    public List<String> getRiskFactors() { return riskFactors; }
    public void setRiskFactors(List<String> riskFactors) { this.riskFactors = riskFactors; }

    public List<String> getActionItems() { return actionItems; }
    public void setActionItems(List<String> actionItems) { this.actionItems = actionItems; }

    public List<String> getNarrative() { return narrative; }
    public void setNarrative(List<String> narrative) { this.narrative = narrative; }
}
