package com.example.fieldmapper.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BudgetItem {
    private Long id;
    private String direction;
    private String project;
    private String area;
    private String specificCost;
    private String month;
    private BigDecimal actualCumulative;
    private BigDecimal bookCumulative;
    private BigDecimal difference;
    private String remark;
    private String verifier;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
    public String getProject() { return project; }
    public void setProject(String project) { this.project = project; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public String getSpecificCost() { return specificCost; }
    public void setSpecificCost(String specificCost) { this.specificCost = specificCost; }
    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }
    public BigDecimal getActualCumulative() { return actualCumulative; }
    public void setActualCumulative(BigDecimal actualCumulative) { this.actualCumulative = actualCumulative; }
    public BigDecimal getBookCumulative() { return bookCumulative; }
    public void setBookCumulative(BigDecimal bookCumulative) { this.bookCumulative = bookCumulative; }
    public BigDecimal getDifference() { return difference; }
    public void setDifference(BigDecimal difference) { this.difference = difference; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getVerifier() { return verifier; }
    public void setVerifier(String verifier) { this.verifier = verifier; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
