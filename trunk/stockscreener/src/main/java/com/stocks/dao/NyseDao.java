package com.stocks.dao;

import java.util.Date;
import java.util.List;

import com.stocks.model.Nyse;
import com.stocks.model.NysePK;

public interface NyseDao extends Dao<Nyse, NysePK> {
	public List<Nyse> findStockBySymbol(final String symbol);
	public List<Nyse> findStockBySymbolAndTradeDate(final String symbol, final Date tradeDate);
	public List<String> getAllSymbols();

}
