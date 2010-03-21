package com.stocks;

import javax.faces.event.ActionEvent;

import org.apache.commons.chain.Chain;
import org.apache.commons.chain.impl.ContextBase;

import com.stocks.model.Report;
import com.stocks.service.StockService;

public class ReportBean{    
	private StockService stockService;
	private String content;
    private Chain reportChain;

	public Chain getReportChain() {
		return reportChain;
	}

	public void setReportChain(Chain reportChain) {
		this.reportChain = reportChain;
	}

	public StockService getStockService() {
		return stockService;
	}

	public void setStockService(StockService stockService) {
		this.stockService = stockService;
	}
	
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		if(content != null && !content.trim().equals("")){
			this.content = content;
		}else{
			this.content = "No Data";
		}
	}

	public void executeReportChain(ActionEvent ae){
		try{
			reportChain.execute(new ContextBase());
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public void getBseAlertReport(ActionEvent ae){
		try {
			Report report = getStockService().getReport(Report.ReportName.BseAlertReportCommand.toString());
			setContent( report.getContent() );
		} catch (Exception e) {
			setContent( e.getMessage() );
		}
	}

	public void getBseReport(ActionEvent ae){
		try {
			Report report = getStockService().getReport(Report.ReportName.BseReportCommand.toString());
			setContent( report.getContent() );
		} catch (Exception e) {
			setContent( e.getMessage() );
		}
	}

	public void getNyseReport(ActionEvent ae){
		try {
			Report report = getStockService().getReport(Report.ReportName.NyseReportCommand.toString());
			setContent( report.getContent() );
		} catch (Exception e) {
			setContent( e.getMessage() );
		}
	}
}
