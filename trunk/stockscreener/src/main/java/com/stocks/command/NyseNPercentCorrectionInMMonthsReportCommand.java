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
import com.stocks.model.Report;

public class NyseNPercentCorrectionInMMonthsReportCommand extends AbstractCommand {
	private static final Double FROM_PERCENT = 25d;
	private static final Double TO_PERCENT = 50d;
	private static final int CORRECTION_MONTHS = 6;

	public boolean execute(Context context) throws Exception {
		System.out.println( "Executing NyseNPercentCorrectionInMMonthsReportCommand..." );
		processNyse();
		return Command.CONTINUE_PROCESSING;
	}

	private void processNyse() throws Exception{
		final String STOCK_EXCHANGE = "NYSE";
		StringBuffer sb = new StringBuffer();
		sb.append( "<pre>\n" );
		sb.append( "<B>Nyse Report (" +FROM_PERCENT+" - " +TO_PERCENT+ " percent Correction in " +CORRECTION_MONTHS+ " Months) - Generated on " +new SimpleDateFormat("MMM dd, yyyy").format(new Date())+ "</B>\n" );

		List<String> symbols = getStockService().getAllSymbols();
		List<Double> cClose = new ArrayList<Double>();

		double ctr = 0.0;
		for( final String symbol : symbols ){
			cClose.clear();
			List<Nyse> nyseList = getStockService().findStockBySymbol(symbol);
			
			for( final Nyse nyse : nyseList ){
				cClose.add( nyse.getClose() );
			}

			try{
				//checkQualification("NYSE", scCode, cClose.toArray(new Double[]{}));
				checkQualificationNPercentCorrectionInMMonths(STOCK_EXCHANGE, sb, symbol, cClose.toArray(new Double[]{}));
			}
			catch(Exception e){
			}
			getPercentCompleteReporter().setPercentComplete( (++ctr / symbols.size()) * 100.00 );
			//System.out.print(".");
		}
		sb.append( "- End of Report.</pre>\n" );
		final Report report = new Report( Report.ReportName.NyseNPercentCorrectionInMMonthsReportCommand.toString(), sb.toString());
		getStockService().saveReport(report);
	}
	
	private static void checkQualificationNPercentCorrectionInMMonths(final String stockExchange, final StringBuffer sb, final Object scCode, final Double[] cClose) throws Exception{
		boolean qualified = true;
		int i=0;
		
		double prevClosePrice = Double.MAX_VALUE;
		
		// Start: First Check - weekly avg price M Months ago
		int tradingSessionIndex = CORRECTION_MONTHS * 20;
		Double avgPriceMMonthsAgo = null;
		if( cClose.length < tradingSessionIndex ){
			List<Double> list = Arrays.asList(cClose);
			list = list.subList(tradingSessionIndex, tradingSessionIndex-5);
			Double min = Collections.min(list);
			Double max = Collections.max(list);
			avgPriceMMonthsAgo = (min + max)/2;
		}else{
			qualified = false;
		}

		// End: First Check
		
		// Start: Second Check - Last Close is FROM_PERCENT to TO_PERCENT correction of avgPriceMMonthsAgo
		if(qualified){
			Double lastClose = cClose[cClose.length-1];
			Double fromPercentPrice = FROM_PERCENT/100;
			fromPercentPrice = avgPriceMMonthsAgo - (avgPriceMMonthsAgo * fromPercentPrice);
			
			Double toPercentPrice = TO_PERCENT/100;
			toPercentPrice = avgPriceMMonthsAgo - (avgPriceMMonthsAgo * toPercentPrice);
			
			if( !(lastClose >= toPercentPrice && lastClose <= fromPercentPrice) ){
				qualified = false;
			}else{
				System.out.println( "Qualified. Avg: " +avgPriceMMonthsAgo+ ", fromPC: " +fromPercentPrice+ ", toPercent: " +toPercentPrice );
			}
		}
		// End: Second Check

		if( qualified ){
			sb.append( String.format("scCode: %s, qualified: %b, From Percent: %d, To Percent: %d, Months: %d%n", scCode, qualified, FROM_PERCENT, TO_PERCENT, CORRECTION_MONTHS) );
			// http://chart.apis.google.com/chart?cht=lc&chs=200x100&chd=t:40,60,60,45,47,75,70,72&chxt=x,y&chxr=1,0,75
			String url = GOOGLE_CHART_URL;

			url = url.replace("~NUM", String.valueOf(NUM++) );
			NUM = NUM == 10 ? 0 : NUM;

			url = url.replace("~DATA", Arrays.toString(cClose).replaceAll("[^0-9^.^,]", ""));
			//url = url.replace("~TITLE", scCode.toString());
			final List<Double> cCloseList = Arrays.asList(cClose);
			url = url.replace("~MIN", Collections.min(cCloseList).toString());
			url = url.replace("~MAX", Collections.max(cCloseList).toString());
			sb.append( "<a href=\"http://www.google.com/finance?q=" +stockExchange+ ":" +scCode+ "\" target=\"_new\"><img border=\"0\" src=\"" +url+ "\"></a>\n" );
		}
	}

}
