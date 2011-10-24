package com.stocks.command;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

import com.stocks.model.Nyse;
import com.stocks.model.Report;
import com.stocks.util.Utility;

public class NyseNPercentUpFromBottomCommand extends AbstractCommand {
	private static final double FROM_PERCENT = 9d;
	private static final double TO_PERCENT = 11d;
//	private static final double CORRECTION_MONTHS = 3d;

//	public static void main(String args[]) throws Exception{
//		Double[] arr = new Double[]{39.00, 38.90, 38.80, 38.70, 38.60, 38.50, 38.40, 38.30, 38.20, 38.10, 38.00, 37.90, 37.80, 37.70, 37.60, 37.50, 37.40, 37.30, 37.20, 37.10, 37.00, 36.90, 36.80, 36.70, 36.60, 36.50, 36.40, 36.30, 36.20, 36.10, 36.00, 35.90, 35.80, 35.70, 35.60, 35.50, 35.40, 35.30, 35.20};
//		checkQualificationNPercentCorrectionInMMonths("NYSE", new StringBuffer(), "C", arr);
//	}

	public boolean execute(Context context) throws Exception {
		Collection<String> commandNames = (Collection<String>)context.get(COMMANDS_TO_EXECUTE);
		setStartDate((Date) context.get(START_DATE));
		setEndDate((Date) context.get(END_DATE));

		if( commandNames.contains(this.getClass().getName()) ){
			System.out.println( "Executing NyseNPercentUpFromBottomCommand..." );
			System.out.println( "*** It keeps printing symbols along that are found to be in recent uptrend." );
			processNyse();
		}
		return Command.CONTINUE_PROCESSING;
	}

	private void processNyse() throws Exception{
		final String STOCK_EXCHANGE = "NYSE";
		StringBuffer sb = new StringBuffer();
		sb.append( "<pre>\n" );
		sb.append( String.format("<B>Nyse Report (%s - %s percent Up from Bottom) - Generated on %s</B>%n", FROM_PERCENT, TO_PERCENT, new SimpleDateFormat("MMM dd, yyyy").format(new Date())));

		List<String> symbols = getStockService().getAllSymbols();

		List<Double> cClose = new ArrayList<Double>();

		double ctr = 0.0;
		for( final String symbol : symbols ){
			cClose.clear();
			List<Nyse> nyseList = getStockService().findStockBySymbolBetweenTradeDates(symbol, getStartDate(), getEndDate());
			for( final Nyse nyse : nyseList ){
				cClose.add( nyse.getClose() );
			}

			try{
				checkQualificationNPercentUpFromBottom(STOCK_EXCHANGE, sb, symbol, cClose.toArray(new Double[]{}));
			}
			catch(Exception e){
			}
			getPercentCompleteReporter().setPercentComplete( (++ctr / symbols.size()) * 100.00 );
			//System.out.print(".");
		}
		sb.append( "- End of Report.</pre>\n" );
		final Report report = new Report( Report.ReportName.NyseNPercentUpFromBottomCommand.toString(), sb.toString());
		getStockService().saveReport(report);
	}
	
	private static void checkQualificationNPercentUpFromBottom(final String stockExchange, final StringBuffer sb, final Object scCode, final Double[] cClose) throws Exception{
		boolean qualified = true;

		// Step 1: Get Lowest Price
		List<Double> list = Arrays.asList(cClose);
		Double lowestPrice = Collections.min(list);
		
		Double lastClose = cClose[cClose.length-1];
		Double fromPrice = Utility.round(lowestPrice + (lowestPrice * (FROM_PERCENT/100.0)));
		Double toPrice   = Utility.round(lowestPrice + (lowestPrice * (TO_PERCENT/100.0)));
		if( lastClose >= fromPrice && lastClose <= toPrice ){
			qualified = true;
		}else{
			qualified = false;
		}

		if( qualified ){
			sb.append( String.format("Symbol: %s, Lowest: %s, [%s (%s) %s]%n", scCode, lowestPrice, fromPrice, lastClose, toPrice) );
			//sb.append( String.format("Symbol: %s, From Percent: %s, To Percent: %s%n", scCode, FROM_PERCENT, TO_PERCENT) );
			// http://chart.apis.google.com/chart?cht=lc&chs=200x100&chd=t:40,60,60,45,47,75,70,72&chxt=x,y&chxr=1,0,75
			String url = GOOGLE_CHART_RECOMMENDED_BUY_URL;

			url = url.replace("~NUM", String.valueOf(NUM++) );
			NUM = NUM == 10 ? 0 : NUM;

			url = url.replace("~DATA", Arrays.toString(cClose).replaceAll("[^0-9^.^,]", ""));
			//url = url.replace("~TITLE", scCode.toString());
			final List<Double> cCloseList = Arrays.asList(cClose);
			url = url.replace("~MIN", Collections.min(cCloseList).toString());
			url = url.replace("~MAX", Collections.max(cCloseList).toString());
			
			url += GOOGLE_CHART_CHM_PARAM_VALUE.replace("~RECOMMENDED_BUY_PRICE", "Low@" +lowestPrice ).replace("~RECOMMENDED_BUY_INDEX", ""+(list.lastIndexOf(lowestPrice)) ) ;
			sb.append( "<a href=\"http://www.google.com/finance?q=" +stockExchange+ ":" +scCode+ "\" target=\"_new\"><img border=\"0\" src=\"" +url+ "\"></a>\n" );
			
			// Start: Uptrend Check
			List<Double> subList = list.subList(list.size()-10, list.size());
			// Step 1: Get avg
			Double avg = 0.0;
			for(Double d : subList){
				avg += d;
			}
			avg /= subList.size();
			
			// Step 2: Qualifies if lastClose > avg
			if( list.get( list.size()-1 ) >= avg ){
				System.out.println( String.format("%-5s -> wget --post-data=\"%s\" http://0.chart.apis.google.com/chart -O %s.png", scCode, url.substring( url.indexOf("?")+1 ), scCode) );
			}
			// End: Uptrend Check
		}
	}

	@Override
	public String toString() {
		return this.getClass().getName();
	}

}
