package com.stocks.command;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

import com.stocks.model.Alert;
import com.stocks.model.Bse;

public class BseAlertReportCommand extends AbstractCommand {

	public boolean execute(Context context) throws Exception {
		System.out.println( "execute() Called." );
		processBseAlerts();
		return Command.CONTINUE_PROCESSING;
	}

	private void processBseAlerts() throws Exception{
		final String STOCK_EXCHANGE = "BOM";
		PrintWriter report = new PrintWriter( getReportPath() );
		report.println( "<html><body><pre>" );
		report.println( "********************* BSE *********************" );

		List<Alert> alertList = getStockService().getAllAlerts();
		List<Double> cClose = new ArrayList<Double>();

		double ctr = 0.0;
		for( final Alert alert : alertList ){
			int index = 0;
			cClose.clear();
			List<Bse> bseList = getStockService().findStockByScCode(alert.getBseIciciMapping().getScCode());
			for(final Bse bse : bseList){
				cClose.add( bse.getClose() );
				if( alert.getEventDate().after( bse.getBsePK().getTradeDate() ) ){
					index++;
				}
			}
			
			if( index > 0 ){
				StringBuffer sb = new StringBuffer();
				sb.append( String.format("%s", alert) );
				report.println( sb );
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
				url = url.replace("~RECOMMENDED_BUY_PRICE",  alert.getEventPrice().toString() );

				report.println( "<a href=\"http://www.google.com/finance?q=" +STOCK_EXCHANGE+ ":" +alert.getBseIciciMapping().getScCode()+ "\" target=\"_new\"><img border=\"0\" src=\"" +url+ "\"></a>" );
			}
			
			getPercentCompleteReporter().setPercentComplete( (++ctr / alertList.size()) * 100.00 );
		}

		report.println( "- End of Report.</pre></body></html>" );
		report.close();
	}	
	
}
