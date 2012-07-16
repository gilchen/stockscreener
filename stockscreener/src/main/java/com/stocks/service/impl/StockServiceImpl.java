package com.stocks.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.stocks.dao.AlertDao;
import com.stocks.dao.BseDao;
import com.stocks.dao.BseIciciMappingDao;
import com.stocks.dao.HolidayDao;
import com.stocks.dao.KeyValueDao;
import com.stocks.dao.NyseAlertDao;
import com.stocks.dao.NyseDao;
import com.stocks.dao.NyseTxDao;
import com.stocks.dao.ReportDao;
import com.stocks.dao.Summary52WkBseDao;
import com.stocks.dao.Summary52WkNyseDao;
import com.stocks.dao.Summary52WkNyseDao.DURATION;
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
import com.stocks.service.StockService;
import com.stocks.util.Utility;

@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class StockServiceImpl implements StockService {
	public static final String ICICI_GET_QUOTE_URL = "http://getquote.icicidirect.com/trading/equity/trading_stock_quote.asp?Symbol=";

	@Autowired(required = true)
    private PlatformTransactionManager transactionManager;

    private AlertDao alertDao;
    private NyseAlertDao nyseAlertDao;
    private BseDao bseDao;
    private NyseDao nyseDao;
    private BseIciciMappingDao bseIciciMappingDao;
    private KeyValueDao keyValueDao;
    private ReportDao reportDao;
    private HolidayDao holidayDao;
    private NyseTxDao nyseTxDao;
    private Summary52WkNyseDao summary52WkNyseDao;
    private Summary52WkBseDao summary52WkBseDao;

	public AlertDao getAlertDao() {
		return alertDao;
	}

	@Required
	public void setAlertDao(AlertDao alertDao) {
		this.alertDao = alertDao;
	}

	public NyseAlertDao getNyseAlertDao() {
		return nyseAlertDao;
	}

	@Required
	public void setNyseAlertDao(NyseAlertDao nyseAlertDao) {
		this.nyseAlertDao = nyseAlertDao;
	}

	public BseDao getBseDao() {
		return bseDao;
	}

	@Required
	public void setBseDao(BseDao bseDao) {
		this.bseDao = bseDao;
	}

	public NyseDao getNyseDao() {
		return nyseDao;
	}

	@Required
	public void setNyseDao(NyseDao nyseDao) {
		this.nyseDao = nyseDao;
	}

	public BseIciciMappingDao getBseIciciMappingDao() {
		return bseIciciMappingDao;
	}

	@Required
	public void setBseIciciMappingDao(BseIciciMappingDao bseIciciMappingDao) {
		this.bseIciciMappingDao = bseIciciMappingDao;
	}

	public KeyValueDao getKeyValueDao() {
		return keyValueDao;
	}

	@Required
	public void setKeyValueDao(KeyValueDao keyValueDao) {
		this.keyValueDao = keyValueDao;
	}
	
	public ReportDao getReportDao() {
		return reportDao;
	}

	@Required
	public void setReportDao(ReportDao reportDao) {
		this.reportDao = reportDao;
	}

	public HolidayDao getHolidayDao() {
		return holidayDao;
	}

	@Required
	public void setHolidayDao(HolidayDao holidayDao) {
		this.holidayDao = holidayDao;
	}

	public NyseTxDao getNyseTxDao() {
		return nyseTxDao;
	}

	@Required
	public void setNyseTxDao(NyseTxDao nyseTxDao) {
		this.nyseTxDao = nyseTxDao;
	}

	public Summary52WkNyseDao getSummary52WkNyseDao() {
		return summary52WkNyseDao;
	}

	@Required
	public void setSummary52WkNyseDao(Summary52WkNyseDao summary52WkNyseDao) {
		this.summary52WkNyseDao = summary52WkNyseDao;
	}

	public Summary52WkBseDao getSummary52WkBseDao() {
		return summary52WkBseDao;
	}

	@Required
	public void setSummary52WkBseDao(Summary52WkBseDao summary52WkBseDao) {
		this.summary52WkBseDao = summary52WkBseDao;
	}

	public List<Alert> getAllBseAlerts() {
		return getAlertDao().findAll();
	}
	
	public List<AlertResult> getAllAlertResultsByTrxType(final String trxType, final String isActive){
		return getAlertDao().findAlertResultsByTrxType(trxType, isActive);
	}

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void saveAlert(Alert alert) throws Exception {
		getAlertDao().save(alert);
	}

	public List<NyseAlert> getAllNyseAlerts() {
		return getNyseAlertDao().findAll();
	}
	
	public List<NyseAlertResult> getAllNyseAlertResultsByTrxType(final String trxType, final String isActive) {
		return getNyseAlertDao().findAlertResultsByTrxType(trxType, isActive);
	}

	public void saveNyseTx(NyseTx nyseTx) throws Exception {
		this.getNyseTxDao().save(nyseTx);
	}
	
	public List<NyseTx> getAllNyseTransactions() {
		return this.getNyseTxDao().findAll();
	}
	
	public List<NyseTx> findNyseTransactionsBySymbol(String symbol) {
		return this.getNyseTxDao().findNyseTransactionsBySymbol(symbol);
	}
	
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void saveNyseAlert(NyseAlert nyseAlert) throws Exception {
		getNyseAlertDao().save(nyseAlert);
	}

    public List<Bse> findStockByScCodeAndTradeDate(Integer scCode, Date tradeDate) {
    	return getBseDao().findStockByScCodeAndTradeDate(scCode, tradeDate);
    }
    
    public List<Bse> findStockByScCode(Integer scCode) {
    	return getBseDao().findStockByScCode(scCode);
    }
    
    public List<Integer> getAllScCodes(){
    	return getBseDao().getAllScCodes();
    }
    
    public Nyse read(NysePK nysePk) {
    	return getNyseDao().read(nysePk);
    }
    
    public List<Nyse> findStockBySymbol(String symbol) {
    	return getNyseDao().findStockBySymbol(symbol);
    }

    public List<Nyse> findStockBySymbolBetweenTradeDates(final String symbol, final Date tradeStartDate, final Date tradeEndDate) {
    	return getNyseDao().findStockBySymbolBetweenTradeDates(symbol, tradeStartDate, tradeEndDate);
    }
    
    public List<Object[]> findUpwardMovingStocks(final Integer interval, final Double averagePercentage){
    	return getNyseDao().findUpwardMovingStocks(interval, averagePercentage);
    }
    
    public List<String> getAllSymbols() {
    	return getNyseDao().getAllSymbols();
    }

    public List<String> getAllSymbolsWithExpectedVxC() {
    	return getNyseDao().getAllSymbolsWithExpectedVxC();
    }
    
    public List<Integer> getAllScCodesWithExpectedVxC(){
    	return getBseDao().getAllScCodesWithExpectedVxC();
    }
    
    public List<Date> getAllTradingDates(){
    	return getNyseDao().getAllTradingDates();
    }

    
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void saveBseIciciMapping(BseIciciMapping bseIciciMapping) throws Exception {
    	getBseIciciMappingDao().save(bseIciciMapping);
	}
    
    public BseIciciMapping getBseIciciMapping(String stockCode) {
    	return getBseIciciMappingDao().read(stockCode);
    }

    public KeyValue getKeyValue(String key) {
    	return getKeyValueDao().read(key);
    }
    
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void saveKeyValue(KeyValue keyValue) throws Exception {
    	getKeyValueDao().save(keyValue);
    }
    
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void saveReport(Report report) throws Exception {
    	getReportDao().save(report);
    }
    
    public Report getReport(String reportName) throws Exception {
    	return getReportDao().read(reportName);
    }
    
    
    public Integer getBseScCode(final String stockCode) throws Exception {
		String url = ICICI_GET_QUOTE_URL +stockCode;
		String content = null;
		try {
			content = Utility.getContent(url);
		} catch (Exception e) {
			throw new Exception("Unable to pull data from icici for " +stockCode+ ". Check Antivirus Firewall Settings.");
		}

		String pattern = "<td width=\"37%\" class=\"content\" align=\"left\" colspan=\"4\">";
		int index = content.indexOf(pattern);
		if( index != -1 ){
			int endPatternIndex = content.indexOf("</td>", index+pattern.length());
			String stockName = content.substring(index+pattern.length(), endPatternIndex);

			String stkName = stockName.replaceAll(" ", "-").replace(".", "").replace("LIMITED", "LTD");
			final String rediffURL = "http://money.rediff.com/companies/" +stkName.toLowerCase();
			try {
				content = Utility.getContent(rediffURL);
				pattern = "<div style=\"padding-top:5px;\"><span style=\"float:left; color:#666666;\">";
			
				index = content.indexOf(pattern);
				if( index != -1 ){
					endPatternIndex = content.indexOf("&nbsp;", index+pattern.length());
					Integer bseScCode = Integer.valueOf(content.substring(index+pattern.length(), endPatternIndex));
					return bseScCode;
				}else{
					throw new Exception( String.format("Index -1 for [%s: %s]. Check Antivirus Firewall Settings.", stockCode, stockName ) );
				}
			} catch (Exception e) {
				throw new Exception( "Exception in getting data from rediff.com for "+ stockCode+ ". Check Antivirus Firewall Settings." );
			}
		}else{
			throw new Exception("Index -1 while accessing data from icici for " +stockCode+ ". Check Antivirus Firewall Settings.");
		}
    }
    
	public boolean isHoliday(Date date) {
		return this.getHolidayDao().read(date) != null;
	}
	
	public Date getPreviousBusinessDay(Date date) {
		return this.getHolidayDao().getPreviousBusinessDay(date);
	}
	
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void syncUpSummary52WkNyse() {
		this.getSummary52WkNyseDao().syncUp(DURATION._52_);
		this.getSummary52WkNyseDao().syncUp(DURATION._104_);
		this.getSummary52WkNyseDao().syncUp(DURATION._156_);
	}

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void syncUpSummary52WkBse() {
		this.getSummary52WkBseDao().syncUp();
	}
}
