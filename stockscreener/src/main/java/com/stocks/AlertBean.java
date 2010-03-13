package com.stocks;

import java.util.Date;

import com.stocks.model.Alert;
import com.stocks.service.StockService;

public class AlertBean {
    private String stockCode;
    private String trxType;
    private Date eventDate;
    private String opportunityType;
    private Double eventPrice;
    private String targetPrice;
    private String eventType;
    private Integer qty;
    private String isActive;
    
    // Services
    private StockService stockService;

    // Getter / Setters
    public String getStockCode() {
		return stockCode;
	}
	public void setStockCode(String stockCode) {
		this.stockCode = stockCode;
	}
	public String getTrxType() {
		return trxType;
	}
	public void setTrxType(String trxType) {
		this.trxType = trxType;
	}
	public Date getEventDate() {
		return eventDate;
	}
	public void setEventDate(Date eventDate) {
		this.eventDate = eventDate;
	}
	public String getOpportunityType() {
		return opportunityType;
	}
	public void setOpportunityType(String opportunityType) {
		this.opportunityType = opportunityType;
	}
	public Double getEventPrice() {
		return eventPrice;
	}
	public void setEventPrice(Double eventPrice) {
		this.eventPrice = eventPrice;
	}
	public String getTargetPrice() {
		return targetPrice;
	}
	public void setTargetPrice(String targetPrice) {
		this.targetPrice = targetPrice;
	}
	public String getEventType() {
		return eventType;
	}
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}
	public Integer getQty() {
		return qty;
	}
	public void setQty(Integer qty) {
		this.qty = qty;
	}
	public String getIsActive() {
		return isActive;
	}
	public void setIsActive(String isActive) {
		this.isActive = isActive;
	}

	public StockService getStockService() {
		return stockService;
	}
	public void setStockService(StockService stockService) {
		this.stockService = stockService;
	}

	// Bean Actions
	public void save(){
		Alert alert = new Alert();
		alert.setEventDate(getEventDate());
		alert.setEventPrice(getEventPrice());
		alert.setEventType(getEventType());
		alert.setIsActive(getIsActive());
		alert.setOpportunityType(getOpportunityType());
		alert.setQty(getQty());
		alert.setStockCode(getStockCode());
		alert.setTargetPrice(getTargetPrice());
		alert.setTrxType(getTrxType());
		
		getStockService().saveAlert(alert);
	}
}
