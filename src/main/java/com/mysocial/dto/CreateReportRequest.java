package com.mysocial.dto;

public class CreateReportRequest {
    private String type; // "POST" hoặc "COMMENT"
    private Long targetId;
    private String reason;
    // getter/setter
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
} 