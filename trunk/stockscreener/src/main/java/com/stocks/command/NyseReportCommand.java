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

public class NyseReportCommand extends AbstractCommand {
	private static final int MIN_TRADING_SESSIONS_EXPECTED = 3;
	private static final int MAX_TRADING_SESSIONS_EXPECTED = 4; //Integer.MAX_VALUE;

	public boolean execute(Context context) throws Exception {
		Collection<String> commandNames = (Collection<String>)context.get(COMMANDS_TO_EXECUTE);
		if( commandNames.contains(this.getClass().getName()) ){
			System.out.println( "Executing NyseReportCommand..." );
			processNyse();
		}
		return Command.CONTINUE_PROCESSING;
	}

	private void processNyse() throws Exception{
		final String STOCK_EXCHANGE = "NYSE";
		StringBuffer sb = new StringBuffer();
		sb.append( "<pre>\n" );
		sb.append( "<B>Nyse Report (Constant Upward Movement) - Generated on " +new SimpleDateFormat("MMM dd, yyyy").format(new Date())+ "</B>\n" );

		List<String> symbols = getStockService().getAllSymbols();
		List<Double> cClose = new ArrayList<Double>();

		double ctr = 0.0;
		for( final String symbol : symbols ){
			cClose.clear();
			List<Nyse> nyseList = getStockService().findStockBySymbolBetweenTradeDates(symbol, tradeStartDateParam, tradeEndDateParam);
			
			for( final Nyse nyse : nyseList ){
				cClose.add( nyse.getClose() );
			}

			try{
				//checkQualification("NYSE", scCode, cClose.toArray(new Double[]{}));
				checkQualificationConstantUpwardMovement(STOCK_EXCHANGE, sb, symbol, cClose.toArray(new Double[]{}), true);
			}
			catch(Exception e){
			}
			getPercentCompleteReporter().setPercentComplete( (++ctr / symbols.size()) * 100.00 );
			//System.out.print(".");
		}
		sb.append( "- End of Report.</pre>\n" );
		final Report report = new Report( Report.ReportName.NyseReportCommand.toString(), sb.toString());
		getStockService().saveReport(report);
	}
	
	private static void checkQualificationConstantUpwardMovement(final String stockExchange, final StringBuffer sb, final Object scCode, final Double[] cClose, final boolean filterOutPennyStocks) throws Exception{
		boolean qualified = true;
		int i=0;
		
		double prevClosePrice = Double.MAX_VALUE;
		int numberOfTradingSessions = 0;
		
		// Start: First Check - Validate constant upward movement
		for( i=cClose.length-1; i>=0; i--, numberOfTradingSessions++ ){
			if( filterOutPennyStocks && cClose[i] < 1.0D ){
				qualified = false;
				break;
			}
			
			if(cClose[i] > prevClosePrice){
				if( numberOfTradingSessions < MIN_TRADING_SESSIONS_EXPECTED ){
					qualified = false;
				}
				break;
			}
			
			if(numberOfTradingSessions > MAX_TRADING_SESSIONS_EXPECTED){
				qualified = false;
				break;
			}

			prevClosePrice = cClose[i];
		}
		// End: First Check
		
		// Start: Second Check - Average of prior 3rd & 4th week is greater than 5% of Last Close
		if(qualified){
			final List<Double> mainList = Arrays.asList( cClose );
			try{
				final List<Double> subList = mainList.subList(mainList.size()-30, mainList.size()-20);
				double sum = 0.0;
				for(final Double d : subList){
					sum += d;
				}
				double avg = sum / subList.size();
				
				double lastClose = cClose[cClose.length-1];
				if( avg < lastClose+(lastClose*0.05) ){
					qualified = false;
				}
				
			}
			catch(ArrayIndexOutOfBoundsException e){
				System.out.println( e.getMessage() );
			}
		}
		// End: Second Check

		if( qualified && numberOfTradingSessions >= MIN_TRADING_SESSIONS_EXPECTED && numberOfTradingSessions <= MAX_TRADING_SESSIONS_EXPECTED ){
			sb.append( String.format("scCode: %s, qualified: %b, Trading Sessions:\t%d%n", scCode, qualified, numberOfTradingSessions) );
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

	@Override
	public String toString() {
		return this.getClass().getName();
	}

}
