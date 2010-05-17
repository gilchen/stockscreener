package com.stocks;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import org.springframework.beans.factory.annotation.Required;

import com.stocks.model.Nyse;
import com.stocks.service.StockService;

public class NyseSnapshotBean{    
    // Services
    private StockService stockService;
    private Date snapshotDate;
    private String symbols;
    private DataModel dmNyseSnapshot;
    
    public NyseSnapshotBean() {
		this.setSymbols("DJI.IDX, MMM, AA, AXP, T, BAC, BA, CAT, CVX, CSCO, KO, DD, XOM, GE, HPQ, HD, INTC, IBM, JNJ, JPM, KFT, MCD, MRK, MSFT, PFE, PG, TRV, UTX, VZ, WMT, DIS");

		Calendar calendar = Calendar.getInstance();
    	this.setSnapshotDate( (Date) calendar.getTime().clone() );
	}
    
    // Getter / Setters
	public StockService getStockService() {
		return stockService;
	}

	@Required
	public void setStockService(StockService stockService) {
		this.stockService = stockService;
	}

	public Date getSnapshotDate() {
		return snapshotDate;
	}

	public void setSnapshotDate(Date snapshotDate) {
		this.snapshotDate = snapshotDate;
	}

	public DataModel getDmNyseSnapshot() {
		return dmNyseSnapshot;
	}

	public void setDmNyseSnapshot(DataModel dmNyseSnapshot) {
		this.dmNyseSnapshot = dmNyseSnapshot;
	}

	public String getSymbols() {
		return symbols;
	}

	public void setSymbols(String symbols) {
		this.symbols = symbols;
	}

	// Bean Actions
	public void showNyseSnapshot(final ActionEvent ae){
		List<NyseSnapshot> list = new ArrayList<NyseSnapshot>();
		String[] symbolList = this.getSymbols().split("[,]");
		for( String symbol : symbolList ){
			symbol = symbol.trim();

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(this.getSnapshotDate());
			calendar.add(Calendar.DATE, -5);
			final Date tradeStartDate = calendar.getTime();
			final Date tradeEndDate = this.getSnapshotDate();
			List<Nyse> nyseList = this.getStockService().findStockBySymbolBetweenTradeDates(symbol, tradeStartDate, tradeEndDate);
			if( nyseList.size() >= 2 ){
				list.add( new NyseSnapshot(nyseList.get(nyseList.size()-1), nyseList.get(nyseList.size()-2)) );
			}
		}
		
		Comparator comparator = new Comparator(){
			public int compare(Object o1, Object o2) {
				NyseSnapshot ns1 = (NyseSnapshot) o1;
				NyseSnapshot ns2 = (NyseSnapshot) o2;
				return ns1.getPercentChange().compareTo( ns2.getPercentChange() );
			}
		};
		
		Collections.sort(list, comparator);

		this.setDmNyseSnapshot(new ListDataModel(list));
	}
	
}
