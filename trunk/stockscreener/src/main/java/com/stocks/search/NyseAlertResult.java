package com.stocks.search;

import java.io.Serializable;
import java.math.BigDecimal;

import com.stocks.model.Nyse;
import com.stocks.model.NyseAlert;

public class NyseAlertResult implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private NyseAlert nyseAlert;
	private Nyse nyse;

	public NyseAlertResult(NyseAlert nyseAlert, Nyse nyse) {
		super();
		this.nyseAlert = nyseAlert;
		this.nyse = nyse;
	}
	
	public NyseAlert getNyseAlert() {
		return nyseAlert;
	}
	public void setNyseAlert(NyseAlert nyseAlert) {
		this.nyseAlert = nyseAlert;
	}
	public Nyse getNyse() {
		return nyse;
	}
	public void setNyse(Nyse nyse) {
		this.nyse = nyse;
	}
	
	public Double getPercentChange(){
		//alertResult.nyse.close - alertResult.nyseAlert.eventPrice / (alertResult.nyseAlert.eventPrice * 100)
		double diff = nyse.getClose() - nyseAlert.getEventPrice();
		double percentChange = (diff / nyseAlert.getEventPrice()) * 100;
		BigDecimal bd = new BigDecimal( percentChange );
		bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
		return bd.doubleValue();
	}
}
