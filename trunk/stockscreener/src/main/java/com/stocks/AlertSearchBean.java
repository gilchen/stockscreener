package com.stocks;

import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import com.stocks.model.Alert;
import com.stocks.model.NyseAlert;
import com.stocks.service.StockService;

public class AlertSearchBean {
    private String trxType;
    private String stockExchange;

    private AlertBean alertBean;
    private DataModel dmBseAlerts;
    private DataModel dmNyseAlerts;

    // Services
    private StockService stockService;
    
    public AlertSearchBean() {
    	clear(null);
	}

    public void clear(ActionEvent ae){
    	this.setTrxType(null);
    	this.setStockExchange(null);
    	this.setDmBseAlerts(null);
    	this.setDmNyseAlerts(null);
    }
    
    // Getter / Setters
	public String getTrxType() {
		return trxType;
	}
	public void setTrxType(String trxType) {
		this.trxType = trxType;
	}

	public String getStockExchange() {
		return stockExchange;
	}

	public void setStockExchange(String stockExchange) {
		this.stockExchange = stockExchange;
	}

	public AlertBean getAlertBean() {
		return alertBean;
	}

	public void setAlertBean(AlertBean alertBean) {
		this.alertBean = alertBean;
	}

	public DataModel getDmBseAlerts() {
		return dmBseAlerts;
	}

	public void setDmBseAlerts(DataModel dmBseAlerts) {
		this.dmBseAlerts = dmBseAlerts;
	}

	public DataModel getDmNyseAlerts() {
		return dmNyseAlerts;
	}

	public void setDmNyseAlerts(DataModel dmNyseAlerts) {
		this.dmNyseAlerts = dmNyseAlerts;
	}

	public StockService getStockService() {
		return stockService;
	}
	public void setStockService(StockService stockService) {
		this.stockService = stockService;
	}
	
	public void search(ActionEvent ae){
		if( getStockExchange().equals("BSE") ){
			this.setDmBseAlerts( new ListDataModel( getStockService().getAllBseAlertsByTrxType( getTrxType()) ) );
		}else if( getStockExchange().equals("NYSE") ){
			this.setDmNyseAlerts( new ListDataModel(getStockService().getAllNyseAlertsByTrxType( getTrxType()) ) );
		}
	}
	
	public void editAlert(ActionEvent ae){
		AlertBean ab = this.getAlertBean();
		Alert alert = (Alert) getDmBseAlerts().getRowData();
		ab.setAlertId(alert.getAlertId());
		ab.setStockCode(alert.getBseIciciMapping().getStockCode());
		ab.setTrxType(alert.getTrxType());
		ab.setEventDate(alert.getEventDate());
		ab.setOpportunityType(alert.getOpportunityType());
		ab.setEventPrice(alert.getEventPrice());
		ab.setTargetPrice(alert.getTargetPrice());
		ab.setSltp(alert.getSltp());
		ab.setEventType(alert.getEventType());
		ab.setQty(alert.getQty());
		ab.setIsActive(alert.getIsActive());
		ab.setAlertFor("BSE");
	}

	public void editNyseAlert(ActionEvent ae){
		AlertBean ab = this.getAlertBean();
		NyseAlert alert = (NyseAlert) getDmNyseAlerts().getRowData();
		ab.setAlertId(alert.getAlertId());
		ab.setStockCode(alert.getSymbol());
		ab.setTrxType(alert.getTrxType());
		ab.setEventDate(alert.getEventDate());
		ab.setOpportunityType(alert.getOpportunityType());
		ab.setEventPrice(alert.getEventPrice());
		ab.setTargetPrice(alert.getTargetPrice());
		ab.setSltp(alert.getSltp());
		ab.setEventType(alert.getEventType());
		ab.setQty(alert.getQty());
		ab.setIsActive(alert.getIsActive());
		ab.setAlertFor("NYSE");
	}
	
//	public void deactivateBseAlert(ActionEvent ae){
//		Alert alert = (Alert) getDmBseAlerts().getRowData();
//		alert.setIsActive("N");
//		try {
//			getStockService().saveAlert(alert);
//			loadAllAlerts(null);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	public void deactivateNyseAlert(ActionEvent ae){
//		NyseAlert nyseAlert = (NyseAlert) getDmNyseAlerts().getRowData();
//		nyseAlert.setIsActive("N");
//		try {
//			getStockService().saveNyseAlert(nyseAlert);
//			loadAllAlerts(null);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

}
