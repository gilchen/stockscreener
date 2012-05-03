package com.stocks.command;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.chain.impl.ContextBase;
import org.josql.Query;
import org.josql.QueryResults;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.stocks.model.Bse;
import com.stocks.model.Report;
import com.stocks.util.Utility;
import com.sun.org.apache.xalan.internal.xsltc.compiler.sym;

public class BseBounceFrom52WkReportCommand extends AbstractCommand {
	private static final double RANGE_LOWER_PERCENT  = (9.0/100.0);
	private static final double RANGE_HIGHER_PERCENT = (12.0/100.0);
	
	public static void main(String args[]) throws Exception{
		ApplicationContext context = new FileSystemXmlApplicationContext("src/main/resources/applicationContext.xml");
		ChainBase reportChain = (ChainBase) context.getBean("reportChain");

		//
		final List<String> commandsToExecute = new ArrayList<String>();
		commandsToExecute.add( BseBounceFrom52WkReportCommand.class.getName() );
		Map<String, Object> commandsMap = new HashMap<String, Object>();
		commandsMap.put(AbstractCommand.COMMANDS_TO_EXECUTE, commandsToExecute);
		
		long start = System.currentTimeMillis();
		try{
			final Calendar cStartDate = Calendar.getInstance();
			cStartDate.set(Calendar.YEAR, cStartDate.get(Calendar.YEAR)-1);
			
			ContextBase cb = new ContextBase( commandsMap );
			cb.put(AbstractCommand.START_DATE, cStartDate.getTime());
			reportChain.execute( cb );
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
		setStartDate((Date) context.get(START_DATE));
		setEndDate((Date) context.get(END_DATE));
		if( commandNames.contains(this.getClass().getName()) ){
			System.out.println( "Executing BseBounceFrom52WkReportCommand..." );
			processBse();
		}
		return Command.CONTINUE_PROCESSING;
	}
	
	private void processBse() throws Exception{
		final String STOCK_EXCHANGE = "BOM";
		final StringBuffer sb = new StringBuffer();
		sb.append( "<pre>\n" );
		sb.append( "<B>Bse Report (Bounce From 52Wk) - Generated on " +new SimpleDateFormat("MMM dd, yyyy").format(new Date())+ "</B>\n" );

		System.out.println( "Pulling symbols." );
		final List<Integer> symbolsWithExpectedVxC = getStockService().getAllScCodesWithExpectedVxC();
		System.out.println( symbolsWithExpectedVxC.size() +" scCodes pulled" );

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
					DoThread dt = new DoThread(symbol, getStartDate());
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

		sb.append( "- End of Report.</pre>\n" );
		System.out.println( sb );
		final Report report = new Report( Report.ReportName.BseBounceFrom52WkReportCommand.toString(), sb.toString());
		getStockService().saveReport(report);
	}
	
	class DoThread extends Thread{
		private Integer symbol;
		private StringBuffer sbHtml;
		private Date startDate;
		
		public Integer getSymbol() {
			return symbol;
		}

		public StringBuffer getSbHtml() {
			return sbHtml;
		}

		public Date getStartDate() {
			return startDate;
		}

		public DoThread(Integer symbol, Date startDate) {
			super();
			this.symbol = symbol;
			this.startDate = startDate;
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
			sbHtml = new StringBuffer();
			final int SIZE_52_WK = 52*5;
			final List<Bse> bseList = getStockService().findStockByScCodeAndTradeDate(symbol, getStartDate());

			final QueryResults qrMinMax = Query.parseAndExec("SELECT min(close), max(close) FROM com.stocks.model.Bse LIMIT 1,1", bseList);
			final List<List<Double>> resultMinMax = qrMinMax.getResults();
			Double min = null;
			Double max = null;
			
			try{
				min = resultMinMax.get(0).get(0);
				max = resultMinMax.get(0).get(1);
			}
			catch(Exception e){
				System.out.println( "Exception " +e.getMessage()+ " in " +this.getSymbol() );
				return;
			}
			
			Bse lastTrade = bseList.get(bseList.size()-1);
			final double lastClose = lastTrade.getClose();
			final boolean enoughTrading = (lastClose * lastTrade.getNoOfShares() >= 10000000); //Trading >= 1 Crore
			
			double lowerRange = min + (min * RANGE_LOWER_PERCENT);
			double higherRange = min + (min * RANGE_HIGHER_PERCENT);
			
			if( enoughTrading && (lastClose >= lowerRange && lastClose <= higherRange) ){
				String range52w_pc = "";
				Double low52w_pc = 0.0;
				Double high52w_pc = 0.0;
				low52w_pc = ((lastClose - min)/min)*100.0;
				high52w_pc = ((lastClose - max)/max)*100.0;
				
				range52w_pc = Utility.round(low52w_pc) +"% / "+ Utility.round(high52w_pc)+"%";
				
				sbHtml.append( String.format("SC Code: %s, 52Wk Min-Max: %f - %f, Last Close: %f, 52Wk Min-Max(pc): %s%n", symbol, min, max, lastClose, range52w_pc) );
			}
		}
	}
	
	@Override
	public String toString() {
		return this.getClass().getName();
	}
}
