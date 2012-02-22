package com.stocks.dao;

import java.util.Date;
import java.util.List;

import com.stocks.model.Nyse;
import com.stocks.model.NysePK;

public interface NyseDao extends Dao<Nyse, NysePK> {
	public List<Nyse> findStockBySymbol(final String symbol);
	public List<Nyse> findStockBySymbolBetweenTradeDates(final String symbol, final Date tradeStartDate, final Date tradeEndDate);
	public List<String> getAllSymbols();
	public List<String> getAllSymbolsWithExpectedVxC();
	public List<Date> getAllTradingDates();
	public List<Object[]> findUpwardMovingStocks(final Integer interval, final Double averagePercentage);
	
	public void attachPrevious(List<Nyse> nyseList);
}
