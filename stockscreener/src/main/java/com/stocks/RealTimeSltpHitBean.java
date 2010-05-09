package com.stocks;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import com.stocks.model.Nyse;
import com.stocks.service.StockService;
import com.stocks.util.Utility;

public class RealTimeSltpHitBean {
	private StockService stockService;
	
	private DataModel dmNyse;
	private Date lastMarketCloseDate;
    private String symbols;
	
	public RealTimeSltpHitBean() {
    	clear();
	}

    public void clear(){
    	this.setDmNyse(null);
    	this.setSymbols("MMM, AA, AXP, T, BAC, BA, CAT, CVX, CSCO, KO, DD, XOM, GE, HPQ, HD, INTC, IBM, JNJ, JPM, KFT, MCD, MRK, MSFT, PFE, PG, TRV, UTX, VZ, WMT, DIS");

    	Calendar calendar = Calendar.getInstance();
		if( calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY ){
			calendar.set(Calendar.DATE, calendar.get(Calendar.DATE)-3);
		}else{
			calendar.set(Calendar.DATE, calendar.get(Calendar.DATE)-1);
		}
		this.setLastMarketCloseDate(calendar.getTime());
    }
    
    public StockService getStockService() {
		return stockService;
	}

	public void setStockService(StockService stockService) {
		this.stockService = stockService;
	}

	public DataModel getDmNyse() {
		return dmNyse;
	}

	public void setDmNyse(DataModel dmNyse) {
		this.dmNyse = dmNyse;
	}

	public Date getLastMarketCloseDate() {
		return lastMarketCloseDate;
	}

	public void setLastMarketCloseDate(Date lastMarketCloseDate) {
		this.lastMarketCloseDate = lastMarketCloseDate;
	}

	public String getSymbols() {
		return symbols;
	}

	public void setSymbols(String symbols) {
		this.symbols = symbols;
	}

	public void getSltpHitStocks(ActionEvent ae){
		final String baseUrl = "http://www.google.com/finance?q=";
		final String patternToFind = "<span class=\"goog-inline-block key\">Range</span><span class=\"goog-inline-block val\">";
		String djia[] = this.getSymbols().split("[,]");
		List<Nyse> stopLossHitList = new ArrayList<Nyse>();
		for( String symbol : djia ){
			symbol = symbol.trim();
			List<Nyse> nyseList = getStockService().findStockBySymbolAndTradeDate(symbol, this.getLastMarketCloseDate());
			Nyse nyseBuy = null;
			if( nyseList != null && !nyseList.isEmpty() ){
				nyseBuy = nyseList.get(0);
			}
			System.out.println( "(" +symbol+ "): nyseBuy: " +nyseBuy );
			try{
				final String content = Utility.getContent( baseUrl +symbol );
//				System.out.println( "content: " +content );
				int startIndex = content.indexOf(patternToFind);
				if( startIndex != -1 ){
					startIndex += patternToFind.length();
					String lowHigh = content.substring( startIndex, content.indexOf( "</span>", startIndex ) );
					String arrLowHigh[] = lowHigh.split("-");
					Double low = new Double(arrLowHigh[0].trim());
					Double high = new Double(arrLowHigh[1].trim());
					double sltpPrice = nyseBuy.getClose() - (nyseBuy.getClose() * (4.00/100.0));
					System.out.println( "Low: " +low+ ", High: " +high+ ", Sltp: " +sltpPrice  );
					if( (sltpPrice > low && sltpPrice < high) || sltpPrice > high ){
						// Stop Loss Hit
						stopLossHitList.add(nyseBuy);
						System.out.println( "\t" +symbol );
					}
				}else{
					System.out.println( "Unable to find required pattern for " +symbol );
				}
			}
			catch(Exception e){
				System.out.println( "Exception in pulling data for " +symbol );
			}
		}
		this.setDmNyse( new ListDataModel(stopLossHitList) );
    }
}
