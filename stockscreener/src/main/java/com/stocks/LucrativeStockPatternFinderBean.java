package com.stocks;

import java.util.ArrayList;
import java.util.List;

import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import com.stocks.util.Utility;

public class LucrativeStockPatternFinderBean {
    private String stockCode;
    private Double stopLossPercent;
    private Double expectedGainPercent;
    private int totalSltpHitTx;
    private int totalTargetHitTx;
    private Double lossPerTx;
    private Double profitPerTx;
    private Double totalProfit;

    // Other properties
    private String symbols;
    private AlertSearchBean alertSearchBean;
    private DataModel dmLucrativeStockPatternFinder;
    
    public LucrativeStockPatternFinderBean() {
	}
    
    public LucrativeStockPatternFinderBean(String stockCode,
			Double stopLossPercent, Double expectedGainPercent,
			int totalSltpHitTx, int totalTargetHitTx, Double lossPerTx,
			Double profitPerTx, Double totalProfit) {
		super();
		this.stockCode = stockCode;
		this.stopLossPercent = stopLossPercent;
		this.expectedGainPercent = expectedGainPercent;
		this.totalSltpHitTx = totalSltpHitTx;
		this.totalTargetHitTx = totalTargetHitTx;
		this.lossPerTx = lossPerTx;
		this.profitPerTx = profitPerTx;
		this.totalProfit = totalProfit;
	}
    
	public String getSymbols() {
		return symbols;
	}

	public void setSymbols(String symbols) {
		this.symbols = symbols;
	}

	public DataModel getDmLucrativeStockPatternFinder() {
		return dmLucrativeStockPatternFinder;
	}

	public void setDmLucrativeStockPatternFinder(
			DataModel dmLucrativeStockPatternFinder) {
		this.dmLucrativeStockPatternFinder = dmLucrativeStockPatternFinder;
	}

	public AlertSearchBean getAlertSearchBean() {
		return alertSearchBean;
	}

	public void setAlertSearchBean(AlertSearchBean alertSearchBean) {
		this.alertSearchBean = alertSearchBean;
	}

	public String getStockCode() {
		return stockCode;
	}
	public void setStockCode(String stockCode) {
		this.stockCode = stockCode;
	}
	public Double getStopLossPercent() {
		return stopLossPercent;
	}
	public void setStopLossPercent(Double stopLossPercent) {
		this.stopLossPercent = stopLossPercent;
	}
	public Double getExpectedGainPercent() {
		return expectedGainPercent;
	}
	public void setExpectedGainPercent(Double expectedGainPercent) {
		this.expectedGainPercent = expectedGainPercent;
	}
	public int getTotalSltpHitTx() {
		return totalSltpHitTx;
	}
	public void setTotalSltpHitTx(int totalSltpHitTx) {
		this.totalSltpHitTx = totalSltpHitTx;
	}
	public int getTotalTargetHitTx() {
		return totalTargetHitTx;
	}
	public void setTotalTargetHitTx(int totalTargetHitTx) {
		this.totalTargetHitTx = totalTargetHitTx;
	}
	public Double getLossPerTx() {
		return lossPerTx;
	}
	public void setLossPerTx(Double lossPerTx) {
		this.lossPerTx = lossPerTx;
	}
	public Double getProfitPerTx() {
		return profitPerTx;
	}
	public void setProfitPerTx(Double profitPerTx) {
		this.profitPerTx = profitPerTx;
	}
	public Double getTotalProfit() {
		return totalProfit;
	}
	public void setTotalProfit(Double totalProfit) {
		this.totalProfit = totalProfit;
	}

	public void generateLucrativeStockPattern(ActionEvent ae){
		List<LucrativeStockPatternFinderBean> list = new ArrayList<LucrativeStockPatternFinderBean>();
		String[] symbolList = this.getSymbols().split("[,]");
		
		for( String symbol : symbolList ){
			for( double i=1.0; i<=4.0; i++ ){
				for( double j=1.0; j<=7.0; j++ ){
					this.getAlertSearchBean().clear(ae);
					this.getAlertSearchBean().setSymbols(symbol);
			    	this.getAlertSearchBean().setSimulationSltpPercent( i );
			    	this.getAlertSearchBean().setSimulationExpectedGainPercent( j );
			    	this.getAlertSearchBean().simulateTransactions(ae);

			    	Double lossPerTransaction = Utility.round( (this.getAlertSearchBean().getInvestmentAmount() * (i/100)) +AlertSearchBean.BROKERAGE*2.0 );
			    	Double profitPerTransaction = Utility.round( (this.getAlertSearchBean().getInvestmentAmount() * (j/100)) -AlertSearchBean.BROKERAGE*2.0 );
			    	LucrativeStockPatternFinderBean bean = new LucrativeStockPatternFinderBean(
		    			symbol,
		    			i,
		    			j,
		    			this.getAlertSearchBean().getTotalSltpHit(),
		    			this.getAlertSearchBean().getTotalTargetHit(),
		    			lossPerTransaction,
		    			profitPerTransaction,
		    			Utility.round( (profitPerTransaction * this.getAlertSearchBean().getTotalTargetHit()) - (lossPerTransaction * this.getAlertSearchBean().getTotalSltpHit()) )
			    	);
			    	list.add(bean);
				}
			}
		}
		this.setDmLucrativeStockPatternFinder( new ListDataModel( list ) );
	}
}
