package com.stocks.command;

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
	public static final String START_DATE = "START_DATE";
	public static final String END_DATE = "END_DATE";
	public static final Long DOLLAR_VxC = 100000000L;
	
/*
  Following table is used to determine value for RupeeVxC (Snapshot as of 2/8/2012)
	VxC    #OfQualifyingStocks  TotalBuy  TotalSell TotalPending  Success%
	======================================================================
	500cr      43                   1         1         0           100%
	250cr     136                   1         1         0           100%
	100cr     318                   9         7         2            78%
	 50cr     516                  17        13         4            76%
	 25cr     756                  54        41        13            76%
	 10cr    1123                 182       139        43            76%
	  5cr    1457                 313       231        82            74%
*/
	public static final Long RUPEE_VxC  = 10000000L * 10; // 15cr

	protected static final String GOOGLE_CHART_URL = "http://~NUM.chart.apis.google.com/chart?cht=lc&chs=700x200&chd=t:~DATA&chg=0,2,1,1&chds=~MIN,~MAX"; //&chtt=~TITLE
	protected static final String GOOGLE_CHART_RECOMMENDED_BUY_URL = "http://~NUM.chart.apis.google.com/chart?cht=lc&chs=700x200&chd=t:~DATA&chg=0,2,1,1&chds=~MIN,~MAX&chm=";
	protected static final String GOOGLE_CHART_CHM_PARAM_VALUE = "A~RECOMMENDED_BUY_PRICE,000000,0,~RECOMMENDED_BUY_INDEX,9";
	protected static final String GOOGLE_CHART_CHM_SLTP = "|h,FF0000,0,~VAL_BETWEEN_ZERO_AND_ONE,1";

//	protected static Date tradeStartDateParam = null;
//	protected static Date tradeEndDateParam = null;

	private Date startDate;
	private Date endDate;
	
	protected static int NUM = 0;
//	static{
//		Calendar dt = Calendar.getInstance();
//		tradeEndDateParam = (Date) dt.getTime().clone();
//		
//		dt.add( Calendar.DAY_OF_YEAR, -30*6 ); // 6 months ago
//		tradeStartDateParam = dt.getTime();
//	}
	
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

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
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
	
	protected String toHttpPostString(String get, String scCode){
		StringBuffer sb = new StringBuffer();
		// Step 1: Create Image tag
		sb.append( String.format("<a href='javascript:void(0)' onclick='process%s()'><img id='img_%s' width='1000' height='300' src='images/spacer.jpg' border='1'></a>\n", scCode, scCode) );
		sb.append( String.format("<script language='javascript'>") ).append("\n");
		sb.append( String.format("function process%s(){", scCode) ).append("\n");
		sb.append( String.format("\tstr%s = '%s';", scCode, get.substring(get.indexOf("?")+1)) ).append("\n");
		sb.append( String.format("\tvar oXHR%s = new XMLHttpRequest();", scCode) ).append("\n");
		sb.append( String.format("\toXHR%s.open('POST', 'https://chart.googleapis.com/chart');", scCode) ).append("\n");
		sb.append( String.format("\toXHR%s.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');", scCode) ).append("\n");
		sb.append( String.format("\toXHR%s.responseType = 'arraybuffer';", scCode) ).append("\n");
		sb.append( String.format("\toXHR%s.onload = function (oEvent) {", scCode) ).append("\n");
		sb.append( String.format("\t\tvar arrayBuffer%s = oXHR%s.response;", scCode, scCode) ).append("\n");
		sb.append( String.format("\t\tsetChart( base64ArrayBuffer( arrayBuffer%s ), 'img_%s' );", scCode, scCode) ).append("\n");
		sb.append( String.format("\t}") ).append("\n");
		sb.append( String.format("\toXHR%s.send(str%s);", scCode, scCode) ).append("\n");
		sb.append( String.format("}") ).append("\n");
		sb.append( String.format("</script>") ).append("\n");

		return sb.toString();
	}
}
