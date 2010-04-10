package com.stocks.command;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.chain.Command;
import org.springframework.beans.factory.annotation.Required;

import com.stocks.model.Alert;
import com.stocks.model.NyseAlert;
import com.stocks.service.StockService;
import com.stocks.util.PercentCompleteReporter;

public abstract class AbstractCommand implements Command{
	public static final String COMMANDS_TO_EXECUTE = "COMMANDS_TO_EXECUTE";
	protected static final String GOOGLE_CHART_URL = "http://~NUM.chart.apis.google.com/chart?cht=lc&chs=700x200&chd=t:~DATA&chg=0,2,1,1&chds=~MIN,~MAX"; //&chtt=~TITLE
	protected static final String GOOGLE_CHART_RECOMMENDED_BUY_URL = "http://~NUM.chart.apis.google.com/chart?cht=lc&chs=700x200&chd=t:~DATA&chg=0,2,1,1&chds=~MIN,~MAX&chm=";
	protected static final String GOOGLE_CHART_CHM_PARAM_VALUE = "A~RECOMMENDED_BUY_PRICE,,0,~RECOMMENDED_BUY_INDEX,9";
	protected static final String GOOGLE_CHART_CHM_SLTP = "|h,FF0000,0,~VAL_BETWEEN_ZERO_AND_ONE,1";

	protected static Date tradeDateParam = null;
	protected static int NUM = 0;
	static{
		Calendar dt = Calendar.getInstance();
		dt.add( Calendar.DAY_OF_YEAR, -30*6 ); // 6 months ago
		tradeDateParam = dt.getTime();
	}
	
	private StockService stockService;
	private PercentCompleteReporter percentCompleteReporter;
	
	public StockService getStockService() {
		return stockService;
	}

	@Required
	public void setStockService(StockService stockService) {
		this.stockService = stockService;
	}

	public PercentCompleteReporter getPercentCompleteReporter() {
		return percentCompleteReporter;
	}

	@Required
	public void setPercentCompleteReporter(
			PercentCompleteReporter percentCompleteReporter) {
		this.percentCompleteReporter = percentCompleteReporter;
	}

	protected String nyseAlertsToString(List<NyseAlert> nyseAlerts){
		StringBuffer sb = new StringBuffer();
		for( final NyseAlert nyseAlert : nyseAlerts ){
			sb.append( nyseAlert ).append("<BR>");
		}
		return sb.substring(0, sb.length()-4);
	}
	
	protected String bseAlertsToString(List<Alert> alerts){
		StringBuffer sb = new StringBuffer();
		for( final Alert alert : alerts ){
			sb.append( alert ).append("<BR>");
		}
		return sb.substring(0, sb.length()-4);
	}
}
