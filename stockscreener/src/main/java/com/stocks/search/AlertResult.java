package com.stocks.search;

import java.io.Serializable;
import java.math.BigDecimal;

import com.stocks.model.Alert;
import com.stocks.model.Bse;

public class AlertResult implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private Alert alert;
	private Bse bse;

	public AlertResult(Alert alert, Bse bse) {
		super();
		this.alert = alert;
		this.bse = bse;
	}

	public Alert getAlert() {
		return alert;
	}

	public void setAlert(Alert alert) {
		this.alert = alert;
	}

	public Bse getBse() {
		return bse;
	}

	public void setBse(Bse bse) {
		this.bse = bse;
	}

	public Double getPercentChange(){
		double diff = bse.getClose() - alert.getEventPrice();
		double percentChange = (diff / alert.getEventPrice()) * 100;
		BigDecimal bd = new BigDecimal( percentChange );
		bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
		return bd.doubleValue();
	}
}
