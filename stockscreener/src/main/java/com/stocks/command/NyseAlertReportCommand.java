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

import com.stocks.model.Nyse;
import com.stocks.model.NyseAlert;
import com.stocks.model.NysePK;
import com.stocks.model.Report;

public class NyseAlertReportCommand extends AbstractCommand {

	public boolean execute(Context context) throws Exception {
		System.out.println( "Executing NyseAlertReportCommand..." );
		processNyseAlerts();
		return Command.CONTINUE_PROCESSING;
	}

	private void processNyseAlerts() throws Exception{
		final String STOCK_EXCHANGE = "NYSE";
		StringBuffer sb = new StringBuffer();
		sb.append( "<pre>\n" );
		sb.append( "<B>Nyse Alerts Report - Generated on " +new SimpleDateFormat("MMM dd, yyyy").format(new Date())+ "</B>\n" );

		List<NyseAlert> nyseAlertList = getStockService().getAllNyseAlerts();
		List<Double> cClose = new ArrayList<Double>();

		// Group Alerts by Symbol and add to Map
		Map<String, List<NyseAlert>> mSymbolNyseAlert = new HashMap<String, List<NyseAlert>>();
		for( final NyseAlert nyseAlert : nyseAlertList ){
			List<NyseAlert> nyseAlerts = mSymbolNyseAlert.get( nyseAlert.getSymbol() );
			if( nyseAlerts == null ){
				nyseAlerts = new ArrayList<NyseAlert>();
			}
			nyseAlerts.add(nyseAlert);
			mSymbolNyseAlert.put(nyseAlert.getSymbol(), nyseAlerts);
		}
		// End

		double ctr = 0.0;
		for( final List<NyseAlert> nyseAlerts : mSymbolNyseAlert.values() ){
			cClose.clear();
			List<Nyse> nyseList = getStockService().findStockBySymbol(nyseAlerts.get(0).getSymbol());
			for(final Nyse nyse : nyseList){
				cClose.add( nyse.getClose() );
			}
			
			// In case there is no data for this symbol e.g. symbol is a NASDAQ symbol or misentered.
			if( cClose.size() == 0 ){
				continue;
			}
			
			StringBuffer sbChm = new StringBuffer();
			for( final NyseAlert nyseAlert : nyseAlerts ){
				Nyse nyse = new Nyse();
				NysePK nysePK = new NysePK();
				nysePK.setSymbol( nyseAlert.getSymbol() );
				nysePK.setTradeDate( nyseAlert.getEventDate() );
				nyse.setNysePK(nysePK);
				
				// Attempt to match dates 5 times as there can be long weekends.
				for(int i=0; i<5; i++){
					int index = nyseList.indexOf( nyse );
					if( index != -1 ){
						String str = GOOGLE_CHART_CHM_PARAM_VALUE;
						str = str.replace("~RECOMMENDED_BUY_INDEX",  String.valueOf(index-1));
						str = str.replace("~RECOMMENDED_BUY_PRICE",  nyseAlert.getEventPrice().toString());
						sbChm.append( str ).append("|");
						break;
					}else{
						Date tradeDate = nyse.getNysePK().getTradeDate();
						tradeDate.setDate( tradeDate.getDate()-1 );
						nyse.getNysePK().setTradeDate( tradeDate );
					}
				}
			}

			sb.append( String.format("%s%n", nyseAlerts.get(0)) );
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

			sb.append( "<a href=\"http://www.google.com/finance?q=" +STOCK_EXCHANGE+ ":" +nyseAlerts.get(0).getSymbol()+ "\" target=\"_new\"><img border=\"0\" src=\"" +url+ "\"></a>\n" );

			ctr += nyseAlerts.size();
			getPercentCompleteReporter().setPercentComplete( (ctr / nyseAlertList.size()) * 100.00 );
		}

		sb.append( "- End of Report.</pre>\n" );
		final Report report = new Report( Report.ReportName.NyseAlertReportCommand.toString(), sb.toString());
		getStockService().saveReport(report);
	}
	
}
