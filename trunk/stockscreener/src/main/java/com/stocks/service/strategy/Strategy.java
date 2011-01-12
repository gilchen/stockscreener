package com.stocks.service.strategy;

import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;

public class Strategy {
	private String symbol;
	private Date buyDate;
	private Double expectedGainPercent;
	private Date sellDate;
	
	public Strategy() {
	}
	
	public Strategy(String symbol, Date buyDate, Double expectedGainPercent,
			Date sellDate) {
		super();
		this.symbol = symbol;
		this.buyDate = buyDate;
		this.expectedGainPercent = expectedGainPercent;
		this.sellDate = sellDate;
	}

	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public Date getBuyDate() {
		return buyDate;
	}
	public void setBuyDate(Date buyDate) {
		this.buyDate = buyDate;
	}
	public Double getExpectedGainPercent() {
		return expectedGainPercent;
	}
	public void setExpectedGainPercent(Double expectedGainPercent) {
		this.expectedGainPercent = expectedGainPercent;
	}
	public Date getSellDate() {
		return sellDate;
	}
	public void setSellDate(Date sellDate) {
		this.sellDate = sellDate;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
