package com.stocks;

import java.io.LineNumberReader;
import java.io.StringReader;

import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import com.stocks.model.Alert;
import com.stocks.model.NyseAlert;
import com.stocks.model.Report;
import com.stocks.service.StockService;

public class AlertSearchBean {
    private String trxType;
    private String stockExchange;
    private String graphHtmlContent;
    
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
    	this.setGraphHtmlContent(null);
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

	public String getGraphHtmlContent() {
		return graphHtmlContent;
	}

	public void setGraphHtmlContent(String graphHtmlContent) {
		this.graphHtmlContent = graphHtmlContent;
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

	public void viewGraph(ActionEvent ae){
		Alert alert = (Alert) getDmBseAlerts().getRowData();
		final String stockCode = alert.getBseIciciMapping().getStockCode() +",";
		try {
			Report report = getStockService().getReport(Report.ReportName.BseAlertReportCommand.toString());
			LineNumberReader reader = new LineNumberReader( new StringReader(report.getContent()) );
			String line = null;
			StringBuffer sb = new StringBuffer( "<pre>" );
			while( (line = reader.readLine()) != null ){
				if( line.contains( stockCode ) ){
					sb.append( line +"\n"+ reader.readLine()+"\n" );
					break;
				}
			}
			sb.append("</pre>");
			setGraphHtmlContent( sb.toString() );
		} catch (Exception e) {
			setGraphHtmlContent( e.getMessage() );
		}
	}
	
	public void viewNyseGraph(ActionEvent ae){
		NyseAlert nyseAlert = (NyseAlert) getDmNyseAlerts().getRowData();
		final String symbol = nyseAlert.getSymbol() +",";
		try {
			Report report = getStockService().getReport(Report.ReportName.NyseAlertReportCommand.toString());
			LineNumberReader reader = new LineNumberReader( new StringReader(report.getContent()) );
			String line = null;
			StringBuffer sb = new StringBuffer( "<pre>" );
			while( (line = reader.readLine()) != null ){
				if( line.contains( symbol ) ){
					sb.append( line +"\n"+ reader.readLine()+"\n" );
					break;
				}
			}
			sb.append("</pre>");
			setGraphHtmlContent( sb.toString() );
		} catch (Exception e) {
			setGraphHtmlContent( e.getMessage() );
		}
	}

}
