package com.stocks.command;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.stocks.model.Nyse;
import com.stocks.model.Report;
import com.stocks.util.Utility;

public class NyseNPercentUpFromBottomCommand extends AbstractCommand {
	private static final double FROM_PERCENT = 9d;
	private static final double TO_PERCENT = 11d;
//	private static final double CORRECTION_MONTHS = 3d;

	public static void main(String args[]) throws Exception{
		/*
		ApplicationContext context = new FileSystemXmlApplicationContext("src/main/resources/applicationContext.xml");
		NyseNPercentUpFromBottomCommand nyseNPercentUpFromBottomCommand = (NyseNPercentUpFromBottomCommand) context.getBean("nyseNPercentUpFromBottomCommand");
		
		Calendar cStartDate = Calendar.getInstance();
		cStartDate.set(Calendar.YEAR, 2011);
		cStartDate.set(Calendar.MONDAY, Calendar.APRIL);
		cStartDate.set(Calendar.DATE, 26);
		
		nyseNPercentUpFromBottomCommand.setStartDate(cStartDate.getTime());
		nyseNPercentUpFromBottomCommand.setEndDate(new Date());
		nyseNPercentUpFromBottomCommand.processNyse();
		
		System.out.println( "Done." );
		System.exit(0);
		*/
	}

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
