package com.stocks;

import java.util.ArrayList;
import java.util.List;

import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import com.stocks.service.StockService;
import com.stocks.util.Utility;

public class UpwardMovingStockFinderBean {
    private Integer interval;
    private Double averagePercentage;

    // Other properties
    private StockService stockService;
    private DataModel dmUpwardMovingStocks;
    
    public UpwardMovingStockFinderBean() {
    	this.setAveragePercentage( 0.05 );
    	this.setInterval( 31 );
	}

	public Integer getInterval() {
		return interval;
	}
	
	public void setInterval(Integer interval) {
		this.interval = interval;
	}

	public Double getAveragePercentage() {
		return averagePercentage;
	}

	public void setAveragePercentage(Double averagePercentage) {
		this.averagePercentage = averagePercentage;
	}

	public StockService getStockService() {
		return stockService;
	}

	public void setStockService(StockService stockService) {
		this.stockService = stockService;
	}

	public DataModel getDmUpwardMovingStocks() {
		return dmUpwardMovingStocks;
	}

	public void setDmUpwardMovingStocks(DataModel dmUpwardMovingStocks) {
		this.dmUpwardMovingStocks = dmUpwardMovingStocks;
	}

	public void getUpwardMovingStocks(ActionEvent ae){
		List<UpwardMovingStock> list = new ArrayList<UpwardMovingStock>();
		List<Object[]> results = this.getStockService().findUpwardMovingStocks(this.getInterval(), this.getAveragePercentage());
		for( final Object[] result : results ){
			String symbol = result[0].toString();
			Double close = new Double(result[1].toString());
			Double average = Utility.round(new Double(result[2].toString()));

			list.add( new UpwardMovingStock(symbol, close, average) );
		}
		this.setDmUpwardMovingStocks( new ListDataModel( list ) );
	}
}
