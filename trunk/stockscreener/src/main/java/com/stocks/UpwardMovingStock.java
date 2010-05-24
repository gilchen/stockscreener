package com.stocks;


public class UpwardMovingStock{
	private String symbol;
	private Double close;
	private Double average;

	public UpwardMovingStock(String symbol, Double close, Double average) {
		super();
		this.symbol = symbol;
		this.close = close;
		this.average = average;
	}
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public Double getClose() {
		return close;
	}
	public void setClose(Double close) {
		this.close = close;
	}
	public Double getAverage() {
		return average;
	}
	public void setAverage(Double average) {
		this.average = average;
	}
	

}
