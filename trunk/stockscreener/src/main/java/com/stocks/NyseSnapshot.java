package com.stocks;

import com.stocks.model.Nyse;
import com.stocks.util.Utility;

public class NyseSnapshot{
	private Nyse nyseToday;
	private Nyse nyseYesterday;
	private Double percentChange;
	
	public NyseSnapshot(Nyse nyseToday, Nyse nyseYesterday) {
		super();
		this.nyseToday = nyseToday;
		this.nyseYesterday = nyseYesterday;
	}
	public Nyse getNyseToday() {
		return nyseToday;
	}
	public void setNyseToday(Nyse nyseToday) {
		this.nyseToday = nyseToday;
	}
	public Nyse getNyseYesterday() {
		return nyseYesterday;
	}
	public void setNyseYesterday(Nyse nyseYesterday) {
		this.nyseYesterday = nyseYesterday;
	}
	
	public Double getPercentChange() {
		Double d = ( (getNyseToday().getClose() - getNyseYesterday().getClose()) / getNyseYesterday().getClose() ) * 100.00;
		return Utility.round(d);
	}
	public void setPercentChange(Double percentChange) {
		this.percentChange = percentChange;
	}

}
