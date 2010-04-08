package com.stocks.command;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

import com.stocks.model.Alert;
import com.stocks.model.Bse;
import com.stocks.model.BsePK;
import com.stocks.model.Report;

public class BseAlertReportCommand extends AbstractCommand {

	public boolean execute(Context context) throws Exception {
		System.out.println( "Executing BseAlertReportCommand..." );
		processBseAlerts();
		return Command.CONTINUE_PROCESSING;
	}

	private void processBseAlerts() throws Exception{
		final String STOCK_EXCHANGE = "BOM";
		StringBuffer sb = new StringBuffer();
		sb.append( "<pre>\n" );
		sb.append( "<B>Bse Alerts Report - Generated on " +new SimpleDateFormat("MMM dd, yyyy").format(new Date())+ "</B>\n" );

		List<Alert> alertList = getStockService().getAllBseAlerts();
		List<Double> cClose = new ArrayList<Double>();

		// Group Alerts by StockCode and add to Map
		Map<String, List<Alert>> mSymbolAlert = new HashMap<String, List<Alert>>();
		for( final Alert alert : alertList ){
			List<Alert> alerts = mSymbolAlert.get( alert.getBseIciciMapping().getStockCode() );
			if( alerts == null ){
				alerts = new ArrayList<Alert>();
			}
			alerts.add(alert);
			mSymbolAlert.put(alert.getBseIciciMapping().getStockCode(), alerts);
		}
		// End

		double ctr = 0.0;
		
		for( final List<Alert> alerts : mSymbolAlert.values() ){
			cClose.clear();
			List<Bse> bseList = getStockService().findStockByScCodeAndTradeDate(alerts.get(0).getBseIciciMapping().getScCode(), tradeDateParam);
			for(final Bse bse : bseList){
				cClose.add( bse.getClose() );
			}
			
			StringBuffer sbChm = new StringBuffer();
			StringBuffer sbChmSltp = new StringBuffer();
			Double min = Collections.min(cClose);
			Double max = Collections.max(cClose);
			Double diff = max - min;
			for( final Alert alert : alerts ){
				Bse bse = new Bse();
				BsePK bsePK = new BsePK();
				bsePK.setScCode( alert.getBseIciciMapping().getScCode() );
				bsePK.setTradeDate( alert.getEventDate() );
				bse.setBsePK(bsePK);
				
				// Attempt to match dates 5 times as there can be long weekends.
				for(int i=0; i<5; i++){
					int index = bseList.indexOf( bse );
					if( index != -1 ){
						String str = GOOGLE_CHART_CHM_PARAM_VALUE;
						str = str.replace("~RECOMMENDED_BUY_INDEX",  String.valueOf(index-1));
						str = str.replace("~RECOMMENDED_BUY_PRICE",  alert.getEventPrice().toString());
						sbChm.append( str ).append("|");
						break;
					}else{
						Date tradeDate = bse.getBsePK().getTradeDate();
						tradeDate.setDate( tradeDate.getDate()-1 );
						bse.getBsePK().setTradeDate( tradeDate );
					}
				}

				if( alert.getSltp() != null ){
					/*
					Steps for getting sltp value between zero and 1.
					1	Find Min		57.9
					2	Find Max		79.27
					3	Pull Max - Min		21.37
					4	Take a SLTP		75.32
					5	Pull SLTP - Min		17.42
					6	Divide 5) by 3)		0.815161441
					7	Convert to 0 if 6) is less than zero (when SLTP is below min).
					*/
					Double sltpLine = (alert.getSltp() - min) / diff;
					if( sltpLine < 0.0 ){
						sltpLine = 0.0;
					}
					String chmHLine = GOOGLE_CHART_CHM_SLTP;
					chmHLine = chmHLine.replace("~VAL_BETWEEN_ZERO_AND_ONE", sltpLine.toString());
					sbChmSltp.append( chmHLine );
				}
			}

			sb.append( String.format("%s%n", bseAlertsToString(alerts)) );
			// http://chart.apis.google.com/chart?cht=lc&chs=200x100&chd=t:40,60,60,45,47,75,70,72&chxt=x,y&chxr=1,0,75
			String url = GOOGLE_CHART_RECOMMENDED_BUY_URL;

			url = url.replace("~NUM", String.valueOf(NUM++) );
			NUM = NUM == 10 ? 0 : NUM;

			url = url.replace("~DATA", Arrays.toString(cClose.toArray(new Double[]{})).replaceAll("[^0-9^.^,]", ""));
			//url = url.replace("~TITLE", scCode.toString());
			final List<Double> cCloseList = Arrays.asList(cClose.toArray(new Double[]{}));
			url = url.replace("~MIN", Collections.min(cCloseList).toString());
			url = url.replace("~MAX", Collections.max(cCloseList).toString());

			// sbChm can be blank if insufficient data is available for the stock.
			if( sbChm.length() > 0 ){
				url += sbChm.substring(0, sbChm.length()-1);
			}
			if( sbChmSltp.length() > 0 ){
				url += sbChmSltp.toString();
			}

			sb.append( "<a href=\"http://www.google.com/finance?q=" +STOCK_EXCHANGE+ ":" +alerts.get(0).getBseIciciMapping().getScCode()+ "\" target=\"_new\"><img border=\"0\" src=\"" +url+ "\"></a>\n" );

			ctr += alerts.size();
			getPercentCompleteReporter().setPercentComplete( (ctr / alertList.size()) * 100.00 );
		}

		sb.append( "- End of Report.</pre>\n" );
		final Report report = new Report( Report.ReportName.BseAlertReportCommand.toString(), sb.toString());
		getStockService().saveReport(report);
	}
	
}
