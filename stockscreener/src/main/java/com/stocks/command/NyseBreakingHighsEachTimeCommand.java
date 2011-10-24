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

import com.stocks.model.Nyse;
import com.stocks.model.Report;

public class NyseBreakingHighsEachTimeCommand extends AbstractCommand {

	public static void main(String args[]) throws Exception{
		Double[] arr = new Double[]{1.00, 2.00, 3.00, 2.00, 3.00, 4.00, 5.00, 6.00, 5.00, 6.00, 7.00, 8.00, 9.00, 8.00, 9.00, 10.00, 11.00};
		checkQualificationBreakingHighs("NYSE", new StringBuffer(), "C", arr);
	}

	public boolean execute(Context context) throws Exception {
		Collection<String> commandNames = (Collection<String>)context.get(COMMANDS_TO_EXECUTE);
		setStartDate((Date) context.get(START_DATE));
		setEndDate((Date) context.get(END_DATE));
		if( commandNames.contains(this.getClass().getName()) ){
			System.out.println( "Executing NyseBreakingHighsEachTimeCommand..." );
			processNyse();
		}
		return Command.CONTINUE_PROCESSING;
	}

	private void processNyse() throws Exception{
		final String STOCK_EXCHANGE = "NYSE";
		StringBuffer sb = new StringBuffer();
		sb.append( "<pre>\n" );
		sb.append( String.format("<B>Nyse Report (Breaking Highs each time) - Generated on %s</B>%n", new SimpleDateFormat("MMM dd, yyyy").format(new Date())));

		List<String> symbols = getStockService().getAllSymbols();
		List<Double> cClose = new ArrayList<Double>();

		Calendar startDate = Calendar.getInstance();
		startDate.setTime(getEndDate());
		startDate.add(Calendar.DATE, -31);

		double ctr = 0.0;
		for( final String symbol : symbols ){
			cClose.clear();
			List<Nyse> nyseList = getStockService().findStockBySymbolBetweenTradeDates(symbol, startDate.getTime(), getEndDate());
			for( final Nyse nyse : nyseList ){
				cClose.add( nyse.getClose() );
			}

			try{
				checkQualificationBreakingHighs(STOCK_EXCHANGE, sb, symbol, cClose.toArray(new Double[]{}));
			}
			catch(Exception e){
			}
			getPercentCompleteReporter().setPercentComplete( (++ctr / symbols.size()) * 100.00 );
			//System.out.print(".");
		}
		sb.append( "- End of Report.</pre>\n" );
		final Report report = new Report( Report.ReportName.NyseBreakingHighsEachTimeCommand.toString(), sb.toString());
		getStockService().saveReport(report);
	}
	
	private static void checkQualificationBreakingHighs(final String stockExchange, final StringBuffer sb, final Object scCode, final Double[] cClose) throws Exception{
		boolean qualified = false;

		List<Double> list = Arrays.asList(cClose);
		Double previousValue = list.get( list.size()-1 );
		Double min = previousValue.doubleValue();
		boolean bUpwardMovementEncountered = false;
		int totalHighsBroken = 0;

		for( int i=list.size()-1; i>=0; i-- ){
			Double d = list.get(i);
			if( totalHighsBroken >= 3 ){
				qualified = true;
				break;
			}
			
			if( d <= min ){
				if( bUpwardMovementEncountered ){
					//System.out.println( "\t" +scCode +": Max of " +max+ " broken." );
					totalHighsBroken++;
					bUpwardMovementEncountered = false;
				}
				min = d.doubleValue();
			}else{
				bUpwardMovementEncountered = true;
			}
		}

		if( qualified ){
			//sb.append( String.format("Symbol: %s%n", scCode) );
			//sb.append( String.format("Symbol: %s, From Percent: %s, To Percent: %s%n", scCode, FROM_PERCENT, TO_PERCENT) );
			// http://chart.apis.google.com/chart?cht=lc&chs=200x100&chd=t:40,60,60,45,47,75,70,72&chxt=x,y&chxr=1,0,75
			String url = GOOGLE_CHART_URL;

			url = url.replace("~NUM", String.valueOf(NUM++) );
			NUM = NUM == 10 ? 0 : NUM;

			url = url.replace("~DATA", Arrays.toString(cClose).replaceAll("[^0-9^.^,]", ""));
			//url = url.replace("~TITLE", scCode.toString());
			final List<Double> cCloseList = Arrays.asList(cClose);
			url = url.replace("~MIN", Collections.min(cCloseList).toString());
			url = url.replace("~MAX", Collections.max(cCloseList).toString());
			sb.append( "<a href=\"http://www.google.com/finance?q=" +stockExchange+ ":" +scCode+ "\" target=\"_new\">");
			sb.append( String.format("Symbol: %s%n", scCode) );
			sb.append("<img border=\"0\" src=\"" +url+ "\"></a>\n" );
		}
	}

	@Override
	public String toString() {
		return this.getClass().getName();
	}

}
