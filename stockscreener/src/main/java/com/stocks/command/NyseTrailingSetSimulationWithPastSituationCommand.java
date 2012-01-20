package com.stocks.command;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.stocks.model.Nyse;
import com.stocks.model.Report;

public class NyseTrailingSetSimulationWithPastSituationCommand extends AbstractCommand {
	private static final double FROM_PERCENT = 9d;
	private static final double TO_PERCENT = 11d;
	private static final double CORRECTION_PERCENT = -40.0;
	private static int TOTAL_BUY = 0;
	private static int TOTAL_SELL = 0;

	public static void main(String args[]) throws Exception{
		ApplicationContext context = new FileSystemXmlApplicationContext("src/main/resources/applicationContext.xml");
		NyseTrailingSetSimulationWithPastSituationCommand nyseTrailingSetSimulationWithPastSituationCommand = (NyseTrailingSetSimulationWithPastSituationCommand) context.getBean("nyseTrailingSetSimulationWithPastSituationCommand");
		
		//Calendar cStartDate = Calendar.getInstance();
		//cStartDate.set(Calendar.YEAR, 2009);
		//cStartDate.set(Calendar.MONTH, Calendar.JANUARY);
		//cStartDate.set(Calendar.DATE, 1);
		
		//nyseTrailingSetSimulationWithPastSituationCommand.setStartDate(cStartDate.getTime());
		//nyseTrailingSetSimulationWithPastSituationCommand.setEndDate(new Date());
		long start = System.currentTimeMillis();
		nyseTrailingSetSimulationWithPastSituationCommand.processNyse();
		long end = System.currentTimeMillis();
		System.out.println( "Done in " +((end-start)/1000.0)+ " sec." );
		System.exit(0);
	}

	public boolean execute(Context context) throws Exception {
		Collection<String> commandNames = (Collection<String>)context.get(COMMANDS_TO_EXECUTE);
//		setStartDate((Date) context.get(START_DATE));
//		setEndDate((Date) context.get(END_DATE));
//
		if( commandNames.contains(this.getClass().getName()) ){
			System.out.println( "Executing NyseTrailingSetSimulationWithPastSituationCommand... (No Start/End Dates required.)" );
			processNyse();
		}
		return Command.CONTINUE_PROCESSING;
	}
	
	private void processNyse() throws Exception{
		final StringBuffer sb = new StringBuffer();
		sb.append( "<pre>\n" );
		sb.append( String.format("<B>Nyse Trailing Set Simulation with Past Situation Report (%s - %s percent Up from Bottom) - Generated on %s</B>%n", FROM_PERCENT, TO_PERCENT, new SimpleDateFormat("MMM dd, yyyy").format(new Date())));

		// This map holds buy dates for each symbol.
		final Map<String, List<Date>> mSymbolEntryDates = new HashMap<String, List<Date>>();
		final Map<String, Double> mSymbolLastMin = new HashMap<String, Double>();
		
		final List<Date> allTradingDates = getStockService().getAllTradingDates();
		
		// Load map with symbols and buy dates.
		double ctr = 0.0;
		final int SIZE_52WK = 250;
		for(int i=SIZE_52WK; i<allTradingDates.size(); i++){
		//for(int i=SIZE_52WK; i<SIZE_52WK+1; i++){
			System.out.println( "Date: " +allTradingDates.get(i) );
			loadBuyPositions( allTradingDates.get(i-SIZE_52WK), allTradingDates.get(i), mSymbolEntryDates, mSymbolLastMin );
			getPercentCompleteReporter().setPercentComplete( (++ctr / allTradingDates.size()) * 100.00 );
		}
		
		ctr = 0.0;
		// Scan thru each symbols data and put sell positions for existing buy positions found above.
		Set<String> symbols = mSymbolEntryDates.keySet();
		for(final String symbol : symbols){
			final List<Nyse> nyseList = getStockService().findStockBySymbolBetweenTradeDates(symbol, allTradingDates.get(0), allTradingDates.get(allTradingDates.size()-1));
			loadSellPositions(sb, symbol, nyseList, mSymbolEntryDates.get(symbol));
			getPercentCompleteReporter().setPercentComplete( (++ctr / symbols.size()) * 100.00 );
		}
		
		sb.append( "Total Buy: " +TOTAL_BUY ).append("\n");
		sb.append( "Total Sell: " +TOTAL_SELL ).append("\n");
		sb.append( "Total Pending: " +(TOTAL_BUY-TOTAL_SELL) ).append("\n");
		
		sb.append( "- End of Report.</pre>\n" );
		final Report report = new Report( Report.ReportName.NyseTrailingSetSimulationWithPastSituationCommand.toString(), sb.toString());
		getStockService().saveReport(report);
	}
	
	private void loadSellPositions(final StringBuffer sb, final String symbol, final List<Nyse> nyseList, final List<Date> entryDates){
		String url = GOOGLE_CHART_RECOMMENDED_BUY_URL;

		url = url.replace("~NUM", String.valueOf(NUM++) );
		NUM = NUM == 10 ? 0 : NUM;
		
		final List<BuySellInfo> buySellInfoList = new ArrayList<BuySellInfo>();
		
		final Double[] cClose = new Double[nyseList.size()];
		for(int i=0; i<nyseList.size(); i++){
			final Nyse nyse = nyseList.get(i);
			cClose[i] = nyse.getClose();
			
			if( entryDates.contains( nyse.getNysePK().getTradeDate() ) ){
				buySellInfoList.add( new BuySellInfo(i, null) );
			}
			
			for( final BuySellInfo bsi : buySellInfoList ){
				if( bsi.getSellIndex() == null ){ // If not already sold.
					final Double buyPrice = nyseList.get(bsi.getBuyIndex()).getClose();
					if( nyse.getClose() >= (buyPrice + (buyPrice * 0.10) ) ){
						bsi.setSellIndex(i);
					}
				}
			}
		}

		url = url.replace("~DATA", Arrays.toString(cClose).replaceAll("[^0-9^.^,]", ""));
		final List<Double> cCloseList = Arrays.asList(cClose);
		url = url.replace("~MIN", Collections.min(cCloseList).toString());
		url = url.replace("~MAX", Collections.max(cCloseList).toString());

		StringBuffer buySellInfoBuffer = new StringBuffer();

		int BUY = 0, SELL=0;
		for( BuySellInfo bsi : buySellInfoList ){
			BUY++;
			buySellInfoBuffer.append( GOOGLE_CHART_CHM_PARAM_VALUE.replace("~RECOMMENDED_BUY_PRICE", "Buy@" +cCloseList.get(bsi.getBuyIndex()) ).replace("~RECOMMENDED_BUY_INDEX", ""+bsi.getBuyIndex() ) ).append("|") ;
			if( bsi.getSellIndex() != null ){
				SELL++;
				buySellInfoBuffer.append( GOOGLE_CHART_CHM_PARAM_VALUE.replace("000000", "348017" ).replace("~RECOMMENDED_BUY_PRICE", "Sell@" +cCloseList.get(bsi.getSellIndex()) ).replace("~RECOMMENDED_BUY_INDEX", ""+bsi.getSellIndex() ) ).append("|") ;
			}
			//System.out.println( bsi );
		}

		TOTAL_BUY+=BUY;
		TOTAL_SELL+=SELL;
		sb.append( String.format("Symbol: %s, Bought: %d, Sold: %d, Success pc: %f%n", symbol, BUY, SELL, ((((double)SELL-(double)BUY)/(double)BUY)*100.0)+100.0 ) );
		System.out.println( symbol+ "\t" +BUY+ "\t" +SELL );

		if( buySellInfoBuffer.length() > 0 ){
			url += buySellInfoBuffer.substring(0, buySellInfoBuffer.length()-1);
		}
		
		String postString = toHttpPostString(url, symbol);
		sb.append( postString ).append("\n");
	}

	private void loadBuyPositions(Date startDate, Date endDate, Map<String, List<Date>> mSymbolEntryDates, Map<String, Double> mSymbolLastMin) throws Exception{
		//List<String> symbols = Arrays.asList(new String[]{"BAC"}); //getStockService().getAllSymbols();
		List<String> symbols = getStockService().getAllSymbolsAsOnGivenDate(endDate);
		System.out.println( "Processing " +symbols.size()+ " symbols." );
		List<Double> cClose = new ArrayList<Double>();

		for( final String symbol : symbols ){
			cClose.clear();
			List<Nyse> nyseList = getStockService().findStockBySymbolBetweenTradeDates(symbol, startDate, endDate);
			for( final Nyse nyse : nyseList ){
				cClose.add( nyse.getClose() );
			}

			try{
				boolean bEnterNow = checkQualificationToBuy(cClose.toArray(new Double[]{}), symbol, mSymbolLastMin);
				if( bEnterNow ){
					List<Date> entryDates = mSymbolEntryDates.get(symbol);
					if( entryDates == null ){
						entryDates = new ArrayList<Date>();
						mSymbolEntryDates.put(symbol, entryDates);
					}
					entryDates.add( endDate );
				}
			}
			catch(Exception e){
			}
		}
	}

	private boolean checkQualificationToBuy(final Double[] cClose, String symbol, Map<String, Double> mSymbolLastMin) throws Exception{
		boolean bEnterNow = false;
		
		Double lastMin = mSymbolLastMin.get(symbol);
		if( lastMin == null ){
			lastMin = Double.MAX_VALUE;
		}
		
		List<Double> list = Arrays.asList(cClose);
		final Double low52wk  = Collections.min(list); // 52wkL
		final Double high52wk = Collections.max(list); // 52wkH

		Double priceAtSubjectIndex = list.get( list.size()-1 );
		final Double low52w_pc  = ((priceAtSubjectIndex - low52wk)/low52wk)*100.0; // 4.6
		final Double high52w_pc = ((priceAtSubjectIndex - high52wk)/high52wk)*100.0; // -85
		
		if( high52w_pc <= CORRECTION_PERCENT && (low52w_pc >= FROM_PERCENT && low52w_pc <= TO_PERCENT ) && low52wk < lastMin ){
			bEnterNow = true;
			lastMin = low52wk;
		}
		
		mSymbolLastMin.put(symbol, lastMin);

		return bEnterNow;
	}

	@Override
	public String toString() {
		return this.getClass().getName();
	}
}
