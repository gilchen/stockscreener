package com.stocks;

import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import com.stocks.service.StockService;

public class AlertSearchBean {
    private String trxType;
    private String stockExchange;

    private DataModel dmBseAlerts;
    private DataModel dmNyseAlerts;

    // Services
    private StockService stockService;
    
    public AlertSearchBean() {
    	clear();
	}

    public void clear(){
    	this.setTrxType(null);
    	this.setStockExchange(null);
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
