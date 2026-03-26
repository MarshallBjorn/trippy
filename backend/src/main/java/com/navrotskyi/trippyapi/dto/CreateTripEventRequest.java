package com.navrotskyi.trippyapi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateTripEventRequest {
    private String name;
    private String currencyCode;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal budget;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }
}