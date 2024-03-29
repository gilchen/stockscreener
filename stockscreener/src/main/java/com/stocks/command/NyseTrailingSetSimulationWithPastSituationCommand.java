package com.stocks.command;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.josql.Query;
import org.josql.QueryResults;
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
	private static final Set<String> symbolsPending = new HashSet<String>();
	
	public static void main(String args[]) throws Exception{
		ApplicationContext context = new FileSystemXmlApplicationContext("src/main/resources/applicationContext.xml");
		NyseTrailingSetSimulationWithPastSituationCommand nyseTrailingSetSimulationWithPastSituationCommand = (NyseTrailingSetSimulationWithPastSituationCommand) context.getBean("nyseTrailingSetSimulationWithPastSituationCommand");

		System.out.println( "Executing NyseTrailingSetSimulationWithPastSituationCommand... (No Start/End Dates required.)" );
		long start = System.currentTimeMillis();
		nyseTrailingSetSimulationWithPastSituationCommand.processNyse();
		long end = System.currentTimeMillis();
		System.out.println( "Done in " +((end-start)/1000.0)+ " sec." );
		System.exit(0);
	}

	public boolean execute(Context context) throws Exception {
		Collection<String> commandNames = (Collection<String>)context.get(COMMANDS_TO_EXECUTE);

		if( commandNames.contains(this.getClass().getName()) ){
			System.out.println( "Executing NyseTrailingSetSimulationWithPastSituationCommand... (No Start/End Dates required.)" );
			processNyse();
		}
		return Command.CONTINUE_PROCESSING;
	}
	
	private void reset(){
		TOTAL_BUY = 0;
		TOTAL_SELL = 0;
		symbolsPending.clear();
	}

	private void processNyse() throws Exception{
		reset();
		
		final StringBuffer sb = new StringBuffer();
		sb.append( "<pre>\n" );
		sb.append( String.format("<B>Nyse Trailing Set Simulation with Past Situation Report (%s - %s percent Up from Bottom) - Generated on %s</B>%n", FROM_PERCENT, TO_PERCENT, new SimpleDateFormat("MMM dd, yyyy").format(new Date())));

		System.out.println( "Pulling symbols." );
		final List<String> symbolsWithExpectedVxC = getStockService().getAllSymbolsWithExpectedVxC();
		//final List<String> symbolsWithExpectedVxC = Arrays.asList(new String[]{"BAC"}); //, "HPQ", "AA", "MMM", "INDL", "ERX", "UXG", "NBG"});
		System.out.println( "Pulled symbols." );

		final int MAX_THREADS_ALLOWED = 20;
		final DoThread[] doThreadList = new DoThread[MAX_THREADS_ALLOWED];
		for( int i=0; i<symbolsWithExpectedVxC.size();  ){
			for( int j=0; j<MAX_THREADS_ALLOWED; j++ ){
				if( doThreadList[j] == null ){
					int index = i++;
					
					if( index >= symbolsWithExpectedVxC.size() ){
						break;
					}
					
					String symbol = symbolsWithExpectedVxC.get(index);
					//System.out.println( "<!-- Creating new thread for " +symbol+ " -->" );
					DoThread dt = new DoThread(symbol);
					doThreadList[j] = dt;
					dt.start();
					
					getPercentCompleteReporter().setPercentComplete( ((index+1.00) / symbolsWithExpectedVxC.size()) * 100.00 );
				}
			}
			
			try{
				Thread.sleep( 1000 );
			}
			catch(Exception e){
				e.printStackTrace();
			}
			
			for( int j=0; j<MAX_THREADS_ALLOWED; j++ ){
				if( doThreadList[j] != null ){
					if( !doThreadList[j].isAlive() ){
						sb.append( doThreadList[j].getSbHtml() );
						//System.out.println( doThreadList[j].getSymbol() +" completed." );
						doThreadList[j] = null;
					}
				}
			}
		}

		// Ensure all threads finished.
		while(true){
			boolean breakCondition = true;
			for( int j=0; j<MAX_THREADS_ALLOWED; j++ ){
				if( doThreadList[j] != null ){
					if( doThreadList[j].isAlive() ){
						breakCondition = false;
						break;
					}else{
						sb.append( doThreadList[j].getSbHtml() );
						doThreadList[j] = null;
					}
				}
			}
			
			if( breakCondition ){
				break;
			}else{
				try{
					Thread.sleep( 750 );
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
		}

		sb.append( "Total Buy: " +TOTAL_BUY ).append("\n");
		sb.append( "Total Sell: " +TOTAL_SELL ).append("\n");
		sb.append( "Total Pending: " +(TOTAL_BUY-TOTAL_SELL) ).append("\n");
		sb.append( "Pending Symbols: " +symbolsPending ).append("\n");
		
		sb.append( "- End of Report.</pre>\n" );
		final Report report = new Report( Report.ReportName.NyseTrailingSetSimulationWithPastSituationCommand.toString(), sb.toString());
		getStockService().saveReport(report);
		//System.out.println( sb );
	}
	
	class DoThread extends Thread{
		private String symbol;
		private StringBuffer sbHtml;
		
		public String getSymbol() {
			return symbol;
		}

		public StringBuffer getSbHtml() {
			return sbHtml;
		}

		public DoThread(String symbol) {
			super();
			this.symbol = symbol;
		}

		public void run(){
			try{
				processSymbol();
			}
			catch(Exception e){
				System.out.println( "Exception in processing " +this.symbol );
				e.printStackTrace();
			}
		}
		
		private void processSymbol() throws Exception{
			final List<BuySellInfo> bsiList = new ArrayList<BuySellInfo>();
			
			final int SIZE_52_WK = 52*5;
			final List<Nyse> nyseList = getStockService().findStockBySymbol(symbol);
			final QueryResults qr = Query.parseAndExec("SELECT * FROM com.stocks.model.Nyse where (close*volume) >= " +AbstractCommand.DOLLAR_VxC, nyseList);
			final List<Nyse> nyseListWithExpectedVxC = qr.getResults();

			Double lastMin = Double.MAX_VALUE;
			// Load BuyIndices
			for( final Nyse nyse : nyseListWithExpectedVxC ){
				int indexOfMatchingVxC = nyseList.indexOf( nyse );
				if( indexOfMatchingVxC >= SIZE_52_WK ){ // Matching elements must be after 52_WK
					// Find Min, Max and see if it qualifies
					final List<Nyse> nyseList52Wk = new ArrayList<Nyse>();
					nyseList52Wk.addAll( nyseList.subList( (indexOfMatchingVxC - SIZE_52_WK), indexOfMatchingVxC) );
					final QueryResults qrMinMax = Query.parseAndExec("SELECT min(close), max(close) FROM com.stocks.model.Nyse LIMIT 1,1", nyseList52Wk);
					final List<List<Double>> resultMinMax = qrMinMax.getResults();
					final Double low52wk = resultMinMax.get(0).get(0);
					final Double high52wk = resultMinMax.get(0).get(1);
					
					final Double low52w_pc  = ((nyse.getClose() - low52wk)/low52wk)*100.0; // 4.6
					final Double high52w_pc = ((nyse.getClose() - high52wk)/high52wk)*100.0; // -85
					
					if( high52w_pc <= CORRECTION_PERCENT && (low52w_pc >= FROM_PERCENT && low52w_pc <= TO_PERCENT ) && low52wk < lastMin ){
						bsiList.add( new BuySellInfo(indexOfMatchingVxC, null) );
						lastMin = low52wk;
						
						// If suggested during the past 1 week.
						if( indexOfMatchingVxC >= (nyseList.size()-5) ){
							System.out.println( symbol +"   can be considered for buying." );
						}
						
					}
				}
			}
			
			// Load SellIndices: Set SellIndex to first occurence of matching Target.
			for( final BuySellInfo bsi : bsiList ){
				Nyse nyseAtBuyIndex = nyseList.get(bsi.getBuyIndex());
				final Double targetPrice = nyseAtBuyIndex.getClose() + ( nyseAtBuyIndex.getClose() * 0.10 );
				final Query q = new Query();
				q.parse("SELECT * FROM com.stocks.model.Nyse where high >= " +targetPrice+ " and nysePK.tradeDate > :tradeDate order by nysePK.tradeDate");
				q.setVariable("tradeDate", nyseAtBuyIndex.getNysePK().getTradeDate());
				final QueryResults qrTarget = q.execute(nyseList);
				final List<Nyse> nyseListTarget = qrTarget.getResults();
				if( nyseListTarget != null && !nyseListTarget.isEmpty() ){
					int indexOfTarget = nyseList.indexOf( nyseListTarget.get(0) );
					bsi.setSellIndex( indexOfTarget );
				}
			}

			// Exit if there is no Buy/Sell Information
			if( bsiList.isEmpty() ){
				sbHtml = new StringBuffer();
				return;
			}

			// Generate HTML
//			final QueryResults qrClosePrices = Query.parseAndExec("SELECT close FROM com.stocks.model.Nyse", nyseList);
//			final List<Double> cCloseList = qrClosePrices.getResults();
//			Double[] cClose = cCloseList.toArray(new Double[]{});

			Double[] cClose = new Double[nyseList.size()];
			List<Double> cCloseList = new ArrayList<Double>();
			for(int i=0; i<nyseList.size(); i++){
				final Nyse nyse = nyseList.get(i);
				cClose[i] = nyse.getClose();
				cCloseList.add(cClose[i]);
			}
			
			String url = GOOGLE_CHART_RECOMMENDED_BUY_URL;

			url = url.replace("~NUM", String.valueOf(NUM++) );
			NUM = NUM == 10 ? 0 : NUM;

			url = url.replace("~DATA", Arrays.toString(cClose).replaceAll("[^0-9^.^,]", ""));
			url = url.replace("~MIN", Collections.min(cCloseList).toString());
			url = url.replace("~MAX", Collections.max(cCloseList).toString());

			StringBuffer buySellInfoBuffer = new StringBuffer();

			int BUY = 0, SELL=0;
			for( BuySellInfo bsi : bsiList ){
				BUY++;
				buySellInfoBuffer.append( GOOGLE_CHART_CHM_PARAM_VALUE.replace("~RECOMMENDED_BUY_PRICE", "Buy@" +cCloseList.get(bsi.getBuyIndex()) ).replace("~RECOMMENDED_BUY_INDEX", ""+bsi.getBuyIndex() ) ).append("|") ;
				if( bsi.getSellIndex() != null ){
					SELL++;
					buySellInfoBuffer.append( GOOGLE_CHART_CHM_PARAM_VALUE.replace("000000", "348017" ).replace("~RECOMMENDED_BUY_PRICE", "Sell@" +cCloseList.get(bsi.getSellIndex()) ).replace("~RECOMMENDED_BUY_INDEX", ""+bsi.getSellIndex() ) ).append("|") ;
				}else{
					synchronized(symbolsPending){
						symbolsPending.add(symbol);
					}
				}
				//System.out.println( bsi );
			}

			synchronized(symbolsPending){
				TOTAL_BUY+=BUY;
				TOTAL_SELL+=SELL;
			}
			
			sbHtml = new StringBuffer();
			sbHtml.append( String.format("Symbol: %s, Bought: %d, Sold: %d, Success pc: %f%n", symbol, BUY, SELL, ((((double)SELL-(double)BUY)/(double)BUY)*100.0)+100.0 ) );
			System.out.println( symbol+ "\t" +BUY+ "\t" +SELL );

			if( buySellInfoBuffer.length() > 0 ){
				url += buySellInfoBuffer.substring(0, buySellInfoBuffer.length()-1);
			}
			
			String postString = toHttpPostString(url, symbol);
			sbHtml.append( postString ).append("\n");
		}
	}
	
	@Override
	public String toString() {
		return this.getClass().getName();
	}
}
