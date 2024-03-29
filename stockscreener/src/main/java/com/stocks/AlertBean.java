package com.stocks;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import com.stocks.model.Alert;
import com.stocks.model.NyseAlert;
import com.stocks.service.StockService;

public class AlertBean {
	private Long alertId;
    private String stockCode;
    private String trxType;
    private Date eventDate;
    private String opportunityType;
    private Double eventPrice;
    private String targetPrice;
    private Double sltp;
    private String eventType;
    private Integer qty;
    private String isActive;
    
    //
    private String alertFor;

    // Services
    private StockService stockService;
    
    public AlertBean() {
    	clear();
	}

    public void clear(){
    	this.setAlertId(null);
    	this.setStockCode(null);
    	this.setTrxType(null);
    	this.setEventDate(new Date());
    	this.setOpportunityType(null);
    	this.setEventPrice(null);
    	this.setTargetPrice(null);
    	this.setSltp(null);
    	this.setEventType(null);
    	this.setQty(1);
    	this.setIsActive("Y");
    }
    
    // Getter / Setters
	public Long getAlertId() {
		return alertId;
	}
	public void setAlertId(Long alertId) {
		this.alertId = alertId;
	}
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
	
	public Double getSltp() {
		return sltp;
	}

	public void setSltp(Double sltp) {
		this.sltp = sltp;
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

	public String getAlertFor() {
		return alertFor;
	}
	public void setAlertFor(String alertFor) {
		this.alertFor = alertFor;
	}

	public StockService getStockService() {
		return stockService;
	}
	public void setStockService(StockService stockService) {
		this.stockService = stockService;
	}
	
	public List<SelectItem> getEventTypes(){
		List<SelectItem> selectItemList = new ArrayList<SelectItem>();
		final String items[] = new String[]{"Reversal of prior trend", "Continuation of prior trend", "Ascending Continuation Triangle", "Bottom Triangle", "Continuation Diamond (Bullish)", "Continuation Wedge (Bullish)", "Diamond Bottom", "Double Bottom", "Flag (Bullish)", "Head and Shoulders Bottom", "Megaphone Bottom", "Pennant (Bullish)", "Rounded Bottom", "Symmetrical Continuation Triangle (Bullish)", "Triple Bottom", "Upside Breakout", "SmarTrend", "Shailesh Own", "Neelam Own"};
		for( final String item : items ){
			selectItemList.add( new SelectItem( item, item ) );
		}
		return selectItemList;
	}

	// Bean Actions
	public void save(){
		Alert alert = null;
		NyseAlert nyseAlert = null;
		if( getAlertFor().equals("BSE") ){
			alert = new Alert();
			alert.setAlertId(getAlertId());
			alert.setEventDate(getEventDate());
			alert.setEventPrice(getEventPrice());
			alert.setEventType(getEventType());
			alert.setIsActive(getIsActive());
			alert.setOpportunityType(getOpportunityType());
			alert.setQty(getQty());
			alert.setBseIciciMapping( getStockService().getBseIciciMapping(getStockCode()) );
			alert.setTargetPrice(getTargetPrice());
			alert.setSltp(getSltp());
			alert.setTrxType(getTrxType());
		}else if( getAlertFor().equals("NYSE") ){
			nyseAlert = new NyseAlert();
			nyseAlert.setAlertId(getAlertId());
			nyseAlert.setEventDate(getEventDate());
			nyseAlert.setEventPrice(getEventPrice());
			nyseAlert.setEventType(getEventType());
			nyseAlert.setIsActive(getIsActive());
			nyseAlert.setOpportunityType(getOpportunityType());
			nyseAlert.setQty(getQty());
			nyseAlert.setSymbol( getStockCode() );
			nyseAlert.setTargetPrice(getTargetPrice());
			nyseAlert.setSltp(getSltp());
			nyseAlert.setTrxType(getTrxType());
		}
		
		try {
			if( getAlertFor().equals("BSE") ){
				getStockService().saveAlert(alert);
				final String msg = "Alert Saved for " +getStockCode();
				FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg));
			}else if( getAlertFor().equals("NYSE") ){
				getStockService().saveNyseAlert(nyseAlert);
				final String msg = "Nyse Alert Saved for " +getStockCode();
				FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg));
			}
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.getMessage()));
		}
	}
	
	public void setAlertForBse(ActionEvent ae){
		this.setAlertFor("BSE");
		clear();
	}
	
	public void setAlertForNyse(ActionEvent ae){
		this.setAlertFor("NYSE");
		clear();
	}
}
