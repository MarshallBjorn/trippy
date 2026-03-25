package com.navrotskyi.trippyapi.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CreateTripNodeRequest {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String name;
    private String note;
    private BigDecimal price;
    private boolean isSeparate;

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public boolean isSeparate() { return isSeparate; }
    public void setSeparate(boolean separate) { isSeparate = separate; }
}