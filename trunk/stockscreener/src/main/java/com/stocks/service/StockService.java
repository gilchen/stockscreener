package com.stocks.service;

import java.util.Date;
import java.util.List;

import com.stocks.model.Alert;
import com.stocks.model.Bse;
import com.stocks.model.BseIciciMapping;
import com.stocks.model.KeyValue;
import com.stocks.model.Nyse;

public interface StockService {
	void saveAlert(Alert alert) throws Exception;
	List<Alert> getAllAlerts();
	
	public List<Bse> findStockByScCodeAndTradeDate(final Integer scCode, final Date tradeDate);
	public List<Bse> findStockByScCode(final Integer scCode);
	public List<Integer> getAllScCodes();
	
	public List<Nyse> findStockBySymbolAndTradeDate(final String symbol, final Date tradeDate);
	public List<String> getAllSymbols();

	BseIciciMapping getBseIciciMapping(String stockCode);
	void saveBseIciciMapping(BseIciciMapping bseIciciMapping) throws Exception;
	Integer getBseScCode(String stockCode) throws Exception;

	KeyValue getKeyValue(String key);
	void saveKeyValue(KeyValue keyValue) throws Exception;
}
