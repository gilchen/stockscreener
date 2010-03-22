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
	private static final double FROM_PERCENT = 5d;
	private static final double TO_PERCENT = 10d;
	private static final double CORRECTION_MONTHS = 1d;

//	public static void main(String args[]) throws Exception{
//		Double[] arr = new Double[]{39.00, 38.90, 38.80, 38.70, 38.60, 38.50, 38.40, 38.30, 38.20, 38.10, 38.00, 37.90, 37.80, 37.70, 37.60, 37.50, 37.40, 37.30, 37.20, 37.10, 37.00, 36.90, 36.80, 36.70, 36.60, 36.50, 36.40, 36.30, 36.20, 36.10, 36.00, 35.90, 35.80, 35.70, 35.60, 35.50, 35.40, 35.30, 35.20};
//		checkQualificationNPercentCorrectionInMMonths("NYSE", new StringBuffer(), "C", arr);
//	}

	public boolean execute(Context context) throws Exception {
		System.out.println( "Executing NyseNPercentCorrectionInMMonthsReportCommand..." );
		processNyse();
		return Command.CONTINUE_PROCESSING;
	}

	private void processNyse() throws Exception{
		final String STOCK_EXCHANGE = "NYSE";
		StringBuffer sb = new StringBuffer();
		sb.append( "<pre>\n" );
		sb.append( String.format("<B>Nyse Report (%s - %s percent Correction in %s Months) - Generated on %s</B>%n", FROM_PERCENT, TO_PERCENT, CORRECTION_MONTHS, new SimpleDateFormat("MMM dd, yyyy").format(new Date())));

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

		// Start: First Check - weekly avg price M Months ago
		int tradingSessionIndex = (int) Math.round(CORRECTION_MONTHS * 20.0);
		Double avgPriceMMonthsAgo = null;
		if( tradingSessionIndex < cClose.length ){
			List<Double> list = Arrays.asList(cClose);
			int startIndex = cClose.length - tradingSessionIndex - 5;
			int endIndex = cClose.length - tradingSessionIndex;
			if( startIndex >= 0 ){
				list = list.subList(startIndex, endIndex);
				Double min = Collections.min(list);
				Double max = Collections.max(list);
				avgPriceMMonthsAgo = (min + max)/2;
			}else{
				qualified = false;
			}
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
			sb.append( String.format("scCode: %s, qualified: %b, From Percent: %s, To Percent: %s, Months: %s%n", scCode, qualified, FROM_PERCENT, TO_PERCENT, CORRECTION_MONTHS) );
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
