package com.stocks;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.faces.event.ActionEvent;

import com.stocks.service.StockService;

public class EntryTimingReportBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final String REPORT_NAME = "ENTRY_TIMING_REPORT";
	
	private String sql;
	private String overrideSymbols;
	
	private transient StockService stockService;
	
	public EntryTimingReportBean() {
		try{
			final ObjectInputStream ois = new ObjectInputStream(new FileInputStream("EntryTimingReportBean.ser"));
			final EntryTimingReportBean etrb = (EntryTimingReportBean) ois.readObject();
			this.setSql( etrb.getSql() );
			this.setOverrideSymbols( etrb.getOverrideSymbols() );
			ois.close();
		}
		catch(Exception e){
			System.out.println( "Unable to read serialized object: " +e.getMessage() );
			e.printStackTrace();
		}
	}
	
	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}
	
	public String getOverrideSymbols() {
		return overrideSymbols;
	}

	public void setOverrideSymbols(String overrideSymbols) {
		this.overrideSymbols = overrideSymbols;
	}

	public String getReportHTML() {
		try{
			return getStockService().getReport(REPORT_NAME).getContent();
		}
		catch(Exception e){
			System.out.println( "Exception in getting report." );
			e.printStackTrace();
		}
		return "";
	}

	public StockService getStockService() {
		return stockService;
	}

	public void setStockService(StockService stockService) {
		this.stockService = stockService;
	}

	public void process(ActionEvent ae){
		this.getStockService().generateEntryTimingReport(this.getSql(), this.getOverrideSymbols(), REPORT_NAME);
		try{
			final ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("EntryTimingReportBean.ser"));
			oos.writeObject(this);
			oos.close();
		}
		catch(Exception e){
			System.out.println( "Unable to write serialized object: " +e.getMessage() );
			e.printStackTrace();
		}
		
	}
}
