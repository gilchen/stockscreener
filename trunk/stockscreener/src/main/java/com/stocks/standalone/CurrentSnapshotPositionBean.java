package com.stocks.standalone;

import java.util.Date;

import com.stocks.util.Utility;


public class CurrentSnapshotPositionBean {
	private String symbol;
	private Date buyDate;
	private Double price;
	private String qty;
	private Double realTimePrice;

	
	public CurrentSnapshotPositionBean(String symbol, Date buyDate,
			Double price, String qty, Double realTimePrice) {
		super();
		this.symbol = symbol;
		this.buyDate = buyDate;
		this.price = price;
		this.qty = qty;
		this.realTimePrice = realTimePrice;
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

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public String getQty() {
		return qty;
	}

	public void setQty(String qty) {
		this.qty = qty;
	}

	public Double getRealTimePrice() {
		return realTimePrice;
	}

	public void setRealTimePrice(Double realTimePrice) {
		this.realTimePrice = realTimePrice;
	}

	public Double getProfitPc() {
		Double profitPc = ((realTimePrice - price)/price)*100.0;
		return Utility.round(profitPc);
	}

	@Override
	public String toString() {
		
		return String.format(
				CurrentSnapshot.ROW_FORMAT_POSITIONS,
				symbol,
				Utility.getStrDate(buyDate),
				price,
				qty,
				realTimePrice,
				getProfitPc());
	}

	
}
