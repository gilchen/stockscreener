package com.stocks.command;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

import com.stocks.model.Nyse;
import com.stocks.model.NyseAlert;
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

		double ctr = 0.0;
		for( final NyseAlert nyseAlert : nyseAlertList ){
			int index = 0;
			cClose.clear();
			List<Nyse> nyseList = getStockService().findStockBySymbol(nyseAlert.getSymbol());
			for(final Nyse nyse : nyseList){
				cClose.add( nyse.getClose() );
				if( nyseAlert.getEventDate().after( nyse.getNysePK().getTradeDate() ) ){
					index++;
				}
			}
			
			if( index > 0 ){
				sb.append( String.format("%s%n", nyseAlert) );
				// http://chart.apis.google.com/chart?cht=lc&chs=200x100&chd=t:40,60,60,45,47,75,70,72&chxt=x,y&chxr=1,0,75
				String url = GOOGLE_CHART_RECOMMENDED_BUY_URL;

				url = url.replace("~NUM", String.valueOf(NUM++) );
				NUM = NUM == 10 ? 0 : NUM;

				url = url.replace("~DATA", Arrays.toString(cClose.toArray(new Double[]{})).replaceAll("[^0-9^.^,]", ""));
				//url = url.replace("~TITLE", scCode.toString());
				final List<Double> cCloseList = Arrays.asList(cClose.toArray(new Double[]{}));
				url = url.replace("~MIN", Collections.min(cCloseList).toString());
				url = url.replace("~MAX", Collections.max(cCloseList).toString());
				url = url.replace("~RECOMMENDED_BUY_INDEX",  String.valueOf(index) );
				url = url.replace("~RECOMMENDED_BUY_PRICE",  nyseAlert.getEventPrice().toString() );

				sb.append( "<a href=\"http://www.google.com/finance?q=" +STOCK_EXCHANGE+ ":" +nyseAlert.getSymbol()+ "\" target=\"_new\"><img border=\"0\" src=\"" +url+ "\"></a>\n" );
			}
			
			getPercentCompleteReporter().setPercentComplete( (++ctr / nyseAlertList.size()) * 100.00 );
		}

		sb.append( "- End of Report.</pre>\n" );
		final Report report = new Report( Report.ReportName.NyseAlertReportCommand.toString(), sb.toString());
		getStockService().saveReport(report);
	}
	
}
