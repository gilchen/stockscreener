package com.stocks.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.stocks.dao.AlertDao;
import com.stocks.dao.BseIciciMappingDao;
import com.stocks.dao.KeyValueDao;
import com.stocks.model.Alert;
import com.stocks.model.BseIciciMapping;
import com.stocks.model.KeyValue;
import com.stocks.service.StockService;
import com.stocks.util.Utility;

@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class StockServiceImpl implements StockService {
	public static final String ICICI_GET_QUOTE_URL = "http://getquote.icicidirect.com/trading/equity/trading_stock_quote.asp?Symbol=";
	
	@Autowired(required = true)
    private PlatformTransactionManager transactionManager;

    private AlertDao alertDao;
    private BseIciciMappingDao bseIciciMappingDao;
    private KeyValueDao keyValueDao;
    
	public AlertDao getAlertDao() {
		return alertDao;
	}

	@Required
	public void setAlertDao(AlertDao alertDao) {
		this.alertDao = alertDao;
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

	public List<Alert> getAllAlerts() {
		return getAlertDao().findAll();
	}

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void saveAlert(Alert alert) throws Exception {
		getAlertDao().save(alert);
	}
    
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void saveBseIciciMapping(BseIciciMapping bseIciciMapping) throws Exception {
    	getBseIciciMappingDao().save(bseIciciMapping);
	}

    public KeyValue getKeyValue(String key) {
    	return getKeyValueDao().read(key);
    }
    
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void saveKeyValue(KeyValue keyValue) throws Exception {
    	getKeyValueDao().save(keyValue);
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
    

}
