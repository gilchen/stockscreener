package com.stocks.command;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.chain.impl.ContextBase;
import org.josql.Query;
import org.josql.QueryResults;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.stocks.model.Bse;
import com.stocks.model.Report;

public class BseTrailingSetSimulationWithPastSituationCommand extends AbstractCommand {
	private static final double FROM_PERCENT = 9d;
	private static final double TO_PERCENT = 11d;
	private static final double CORRECTION_PERCENT = -40.0;
	private static int TOTAL_BUY = 0;
	private static int TOTAL_SELL = 0;
	private static final Set<Integer> symbolsPending = new HashSet<Integer>();
	
	public static void main(String args[]) throws Exception{
		ApplicationContext context = new FileSystemXmlApplicationContext("src/main/resources/applicationContext.xml");
		ChainBase reportChain = (ChainBase) context.getBean("reportChain");

		//
		final List<String> commandsToExecute = new ArrayList<String>();
		commandsToExecute.add( BseTrailingSetSimulationWithPastSituationCommand.class.getName() );
		Map<String, Object> commandsMap = new HashMap<String, Object>();
		commandsMap.put(AbstractCommand.COMMANDS_TO_EXECUTE, commandsToExecute);
		
		long start = System.currentTimeMillis();
		try{
			reportChain.execute(new ContextBase( commandsMap ));
		}
		catch(Exception e){
			e.printStackTrace();
		}
		//
		long end = System.currentTimeMillis();
		System.out.println( "Done in " +((end-start)/1000.0)+ " sec." );
		System.exit(0);
	}

	public boolean execute(Context context) throws Exception {
		Collection<String> commandNames = (Collection<String>)context.get(COMMANDS_TO_EXECUTE);

		if( commandNames.contains(this.getClass().getName()) ){
			System.out.println( "Executing BseTrailingSetSimulationWithPastSituationCommand... (No Start/End Dates required.)" );
			processBse();
		}
		return Command.CONTINUE_PROCESSING;
	}
	
	private void reset(){
		TOTAL_BUY = 0;
		TOTAL_SELL = 0;
		symbolsPending.clear();
	}

	private void processBse() throws Exception{
		reset();
		
		final StringBuffer sb = new StringBuffer();
		sb.append( "<pre>\n" );
		sb.append( String.format("<B>Bse Trailing Set Simulation with Past Situation Report (%s - %s percent Up from Bottom) - Generated on %s</B>%n", FROM_PERCENT, TO_PERCENT, new SimpleDateFormat("MMM dd, yyyy").format(new Date())));

		System.out.println( "Pulling symbols." );
		final List<Integer> symbolsWithExpectedVxC = getStockService().getAllScCodesWithExpectedVxC();
		System.out.println( symbolsWithExpectedVxC.size() +" scCodes pulled" );
		//final List<Integer> symbolsWithExpectedVxC = Arrays.asList(new Integer[]{532977, 532454, 500103, 500087, 533278, 532868, 500010, 500180, 500182, 500440, 500696, 532174, 500209, 500875, 532532, 532286, 500510, 500520, 532500, 532541, 500312, 500325, 500112, 500900, 524715, 532540, 500570, 500400, 500470, 507685}); //, "HPQ", "AA", "MMM", "INDL", "ERX", "UXG", "NBG"});
		/*
		final List<Integer> symbolsWithExpectedVxC = new ArrayList<Integer>();
		URL url = new URL("file:///c:/temp/stk/SC_CODE_LIST.txt");
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			symbolsWithExpectedVxC.add( new Integer(inputLine) );
		}
		in.close();
		System.out.println( "Pulled symbols." );
		*/

		final int MAX_THREADS_ALLOWED = 20;
		final DoThread[] doThreadList = new DoThread[MAX_THREADS_ALLOWED];
		for( int i=0; i<symbolsWithExpectedVxC.size();  ){
			for( int j=0; j<MAX_THREADS_ALLOWED; j++ ){
				if( doThreadList[j] == null ){
					int index = i++;
					
					if( index >= symbolsWithExpectedVxC.size() ){
						break;
					}
					
					Integer symbol = symbolsWithExpectedVxC.get(index);
					//System.out.println( "\t<!-- Creating new thread for " +symbol+ " -->" );
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
						//System.out.println( "\t" +doThreadList[j].getSymbol() +" completed." );
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

		//
		System.out.println( "Total Buy: " +TOTAL_BUY );
		System.out.println( "Total Sell: " +TOTAL_SELL );
		System.out.println( "Total Pending: " +(TOTAL_BUY-TOTAL_SELL) );
		System.out.println( "Pending Symbols: " +symbolsPending );
		//
		
		sb.append( "- End of Report.</pre>\n" );
		final Report report = new Report( Report.ReportName.BseTrailingSetSimulationWithPastSituationCommand.toString(), sb.toString());
		getStockService().saveReport(report);
		//System.out.println( sb );
	}
	
	class DoThread extends Thread{
		private Integer symbol;
		private StringBuffer sbHtml;
		
		public Integer getSymbol() {
			return symbol;
		}

		public StringBuffer getSbHtml() {
			return sbHtml;
		}

		public DoThread(Integer symbol) {
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
			final List<Bse> bseList = getStockService().findStockByScCode(symbol);
			final QueryResults qr = Query.parseAndExec("SELECT * FROM com.stocks.model.Bse where (close*noOfShares) >= " +AbstractCommand.RUPEE_VxC, bseList);
			final List<Bse> bseListWithExpectedVxC = qr.getResults();

			Double lastMin = Double.MAX_VALUE;
			// Load BuyIndices
			for( final Bse bse : bseListWithExpectedVxC ){
				int indexOfMatchingVxC = bseList.indexOf( bse );
				if( indexOfMatchingVxC >= SIZE_52_WK ){ // Matching elements must be after 52_WK
					// Find Min, Max and see if it qualifies
					final List<Bse> bseList52Wk = new ArrayList<Bse>();
					bseList52Wk.addAll( bseList.subList( (indexOfMatchingVxC - SIZE_52_WK), indexOfMatchingVxC) );
					final QueryResults qrMinMax = Query.parseAndExec("SELECT min(close), max(close) FROM com.stocks.model.Bse LIMIT 1,1", bseList52Wk);
					final List<List<Double>> resultMinMax = qrMinMax.getResults();
					final Double low52wk = resultMinMax.get(0).get(0);
					final Double high52wk = resultMinMax.get(0).get(1);
					
					final Double low52w_pc  = ((bse.getClose() - low52wk)/low52wk)*100.0; // 4.6
					final Double high52w_pc = ((bse.getClose() - high52wk)/high52wk)*100.0; // -85
					
					if( high52w_pc <= CORRECTION_PERCENT && (low52w_pc >= FROM_PERCENT && low52w_pc <= TO_PERCENT ) && low52wk < lastMin ){
						bsiList.add( new BuySellInfo(indexOfMatchingVxC, null) );
						lastMin = low52wk;
						
						// If suggested during the past 1 week.
						if( indexOfMatchingVxC >= (bseList.size()-5) ){
							System.out.println( symbol +"   can be considered for buying." );
						}
						
					}
				}
			}
			
			// Load SellIndices: Set SellIndex to first occurence of matching Target.
			for( final BuySellInfo bsi : bsiList ){
				Bse bseAtBuyIndex = bseList.get(bsi.getBuyIndex());
				final Double targetPrice = bseAtBuyIndex.getClose() + ( bseAtBuyIndex.getClose() * 0.10 );
				final Query q = new Query();
				q.parse("SELECT * FROM com.stocks.model.Bse where high >= " +targetPrice+ " and bsePK.tradeDate > :tradeDate order by bsePK.tradeDate");
				q.setVariable("tradeDate", bseAtBuyIndex.getBsePK().getTradeDate());
				final QueryResults qrTarget = q.execute(bseList);
				final List<Bse> bseListTarget = qrTarget.getResults();
				if( bseListTarget != null && !bseListTarget.isEmpty() ){
					int indexOfTarget = bseList.indexOf( bseListTarget.get(0) );
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

			Double[] cClose = new Double[bseList.size()];
			List<Double> cCloseList = new ArrayList<Double>();
			for(int i=0; i<bseList.size(); i++){
				final Bse bse = bseList.get(i);
				cClose[i] = bse.getClose();
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
			
			String postString = toHttpPostString(url, symbol.toString());
			sbHtml.append( postString ).append("\n");
		}
	}
	
	@Override
	public String toString() {
		return this.getClass().getName();
	}
}
