package com.stocks.service;

import java.util.Date;
import java.util.List;

import com.stocks.model.Alert;
import com.stocks.model.Bse;
import com.stocks.model.BseIciciMapping;
import com.stocks.model.KeyValue;
import com.stocks.model.Nyse;
import com.stocks.model.NyseAlert;
import com.stocks.model.NysePK;
import com.stocks.model.NyseTx;
import com.stocks.model.Report;
import com.stocks.search.AlertResult;
import com.stocks.search.NyseAlertResult;

public interface StockService {
	void saveAlert(Alert alert) throws Exception;
	List<Alert> getAllBseAlerts();
	List<AlertResult> getAllAlertResultsByTrxType(final String trxType, final String isActive);

	void saveNyseAlert(NyseAlert nyseAlert) throws Exception;
	List<NyseAlert> getAllNyseAlerts();
	List<NyseAlertResult> getAllNyseAlertResultsByTrxType(final String trxType, final String isActive);

	public List<Bse> findStockByScCodeAndTradeDate(final Integer scCode, final Date tradeDate);
	public List<Bse> findStockByScCode(final Integer scCode);
	public List<Integer> getAllScCodes();
	public List<Integer> getAllScCodesWithExpectedVxC();
	
	public Nyse read(final NysePK nysePk);
	public List<Nyse> findStockBySymbol(final String symbol);
	public List<Nyse> findStockBySymbolBetweenTradeDates(final String symbol, final Date tradeStartDate, final Date tradeEndDate);
	public List<String> getAllSymbols();
	public List<String> getAllSymbolsWithExpectedVxC();
	public List<Date> getAllTradingDates();
	public List<Object[]> findUpwardMovingStocks(final Integer interval, final Double averagePercentage);

	BseIciciMapping getBseIciciMapping(String stockCode);
	void saveBseIciciMapping(BseIciciMapping bseIciciMapping) throws Exception;
	Integer getBseScCode(String stockCode) throws Exception;

	KeyValue getKeyValue(String key);
	void saveKeyValue(KeyValue keyValue) throws Exception;
	
	void saveReport(Report report) throws Exception;
	Report getReport(String reportName) throws Exception;
	
	boolean isHoliday(Date date);
	Date getPreviousBusinessDay(Date date);
	
	void saveNyseTx(NyseTx nyseTx) throws Exception;
	List<NyseTx> getAllNyseTransactions();
	List<NyseTx> findNyseTransactionsBySymbol(final String symbol);
	
}
