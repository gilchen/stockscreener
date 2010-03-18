package com.stocks.command;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Required;

import com.stocks.model.Alert;
import com.stocks.model.Bse;
import com.stocks.model.Nyse;
import com.stocks.service.StockService;


public class StockBoxCommand implements Command{

	private static final Double BOX_SIZE = 1.0; // n.0 percent
	private static final int NUM_BOXES = 1;
	private static final int MIN_TRADING_SESSIONS_EXPECTED = 3;
	private static final int MAX_TRADING_SESSIONS_EXPECTED = 4; //Integer.MAX_VALUE;
	
	private static final String GOOGLE_CHART_URL = "http://~NUM.chart.apis.google.com/chart?cht=lc&chs=700x200&chd=t:~DATA&chg=0,2,1,0&chds=~MIN,~MAX"; //&chtt=~TITLE
	private static final String GOOGLE_CHART_RECOMMENDED_BUY_URL = "http://~NUM.chart.apis.google.com/chart?cht=lc&chs=700x200&chd=t:~DATA&chg=0,2,1,0&chds=~MIN,~MAX&chm=A~RECOMMENDED_BUY_PRICE,,0,~RECOMMENDED_BUY_INDEX,9";
	private static double percentComplete = 0.0;

	private static Date tradeDateParam = null;
	private static int NUM = 0;
	static{
		Calendar dt = Calendar.getInstance();
		dt.add( Calendar.DAY_OF_YEAR, -30*6 ); // 6 months ago
		tradeDateParam = dt.getTime();
	}
	
	private StockService stockService;
	private String reportFolder;
	
	public StockService getStockService() {
		return stockService;
	}

	@Required
	public void setStockService(StockService stockService) {
		this.stockService = stockService;
	}

	public String getReportFolder() {
		return reportFolder;
	}

	@Required
	public void setReportFolder(String reportFolder) {
		this.reportFolder = reportFolder;
	}

	public boolean execute(Context arg0) throws Exception {
		Timer timer = new Timer("Test");
		timer.scheduleAtFixedRate(new StockBoxCommand().new PercentCompleteReporter(), new Date(), 15*1000);

		processBse();
		processNyse();
		processWatchBse();
		
		timer.cancel();
		System.out.println( "Command Finished." );
		return true;
	}
	
	public static void main(String args[]) throws Exception{
		//final Double d[] = new Double[]{96.50, 96.10, 97.50, 97.90, 96.40, 98.00, 98.10, 99.40, 98.50, 99.00, 100.00};
		//final Double d[] = new Double[]{98.00, 99.00, 100.00, 101.00, 103.00, 102.00, 101.00, 99.40, 98.50, 99.00, 100.00};
		//checkQualification("TEST", d);
		Timer timer = new Timer("Test");
		timer.scheduleAtFixedRate(new StockBoxCommand().new PercentCompleteReporter(), new Date(), 15*1000);

//		processBse();
//		processNyse();
//		processWatchBse();
		timer.cancel();
		
		System.out.println( "Done" );
	}
	
	private void processBse() throws Exception{
		final String STOCK_EXCHANGE = "BOM";
		PrintWriter report = new PrintWriter( String.format("%sreport%s.html", getReportFolder(), STOCK_EXCHANGE) );
		report.println( "<html><body><pre>" );
		report.println( "********************* BSE *********************" );
		//final PreparedStatement ps = connection.prepareStatement( String.format("SELECT SC_CODE, CLOSE FROM BSE WHERE SC_CODE=? AND TRADE_DATE > '%s' ORDER BY TRADE_DATE ASC", tradeDateParam));

		List<Integer> scCodes = getStockService().getAllScCodes();
		List<Double> cClose = new ArrayList<Double>();
		percentComplete = 0.0;
		double ctr = 0.0;
		for( final Integer scCode : scCodes ){
			cClose.clear();
			List<Bse> bseList = getStockService().findStockByScCodeAndTradeDate(scCode, tradeDateParam);
			
			for( final Bse bse : bseList ){
				cClose.add( bse.getClose() );
			}

			try{
				//checkQualification("BOM", scCode, cClose.toArray(new Double[]{}));
				checkQualificationConstantUpwardMovement(STOCK_EXCHANGE, report, scCode, cClose.toArray(new Double[]{}), false);
			}
			catch(Exception e){
			}
			percentComplete = (++ctr / scCodes.size()) * 100.00;
			//System.out.print(".");
		}
		report.println( "- End of Report.</pre></body></html>" );
		report.close();
	}

	private void processNyse() throws Exception{
		final String STOCK_EXCHANGE = "NYSE";
		PrintWriter report = new PrintWriter( String.format("%sreport%s.html", getReportFolder(), STOCK_EXCHANGE) );
		report.println( "<html><body><pre>" );
		report.println( "********************* NYSE *********************" );

		List<String> symbols = getStockService().getAllSymbols();
		List<Double> cClose = new ArrayList<Double>();
		percentComplete = 0.0;
		double ctr = 0.0;
		for( final String symbol : symbols ){
			cClose.clear();
			List<Nyse> nyseList = getStockService().findStockBySymbolAndTradeDate(symbol, tradeDateParam);
			
			for( final Nyse nyse : nyseList ){
				cClose.add( nyse.getClose() );
			}

			try{
				//checkQualification("NYSE", scCode, cClose.toArray(new Double[]{}));
				checkQualificationConstantUpwardMovement(STOCK_EXCHANGE, report, symbol, cClose.toArray(new Double[]{}), true);
			}
			catch(Exception e){
			}
			percentComplete = (++ctr / symbols.size()) * 100.00;
			//System.out.print(".");
		}
		report.println( "- End of Report.</pre></body></html>" );
		report.close();
	}
	
	private void processWatchBse() throws Exception{
		final String STOCK_EXCHANGE = "BOM";
		PrintWriter report = new PrintWriter( String.format("%sreportWatch%s.html", getReportFolder(), STOCK_EXCHANGE) );
		report.println( "<html><body><pre>" );
		report.println( "********************* BSE *********************" );

		List<Alert> alertList = getStockService().getAllAlerts();
		List<Double> cClose = new ArrayList<Double>();
		percentComplete = 0.0;
		double ctr = 0.0;
		for( final Alert alert : alertList ){
			int index = 0;
			cClose.clear();
			List<Bse> bseList = getStockService().findStockByScCode(alert.getBseIciciMapping().getScCode());
			for(final Bse bse : bseList){
				cClose.add( bse.getClose() );
				if( alert.getEventDate().after( bse.getBsePK().getTradeDate() ) ){
					index++;
				}
			}
			
			if( index > 0 ){
				StringBuffer sb = new StringBuffer();
				sb.append( String.format("%s", alert) );
				report.println( sb );
				// http://chart.apis.google.com/chart?cht=lc&chs=200x100&chd=t:40,60,60,45,47,75,70,72&chxt=x,y&chxr=1,0,75
				String url = GOOGLE_CHART_RECOMMENDED_BUY_URL;

				url = url.replace("~NUM", String.valueOf(NUM++) );
				NUM = NUM == 10 ? 0 : NUM;

				url = url.replace("~DATA", Arrays.toString(cClose.toArray(new Double[]{})).replaceAll("[^0-9^.^,]", ""));
				//url = url.replace("~TITLE", scCode.toString());
				final List<Double> cCloseList = Arrays.asList(cClose.toArray(new Double[]{}));
				url = url.replace("~MIN", Collections.min(cCloseList).toString());
				url = url.replace("~MAX", Collections.max(cCloseList).toString());
				url = url.replace("~RECOMMENDED_BUY_INDEX",  String.valueOf(index) );
				url = url.replace("~RECOMMENDED_BUY_PRICE",  alert.getEventPrice().toString() );

				report.println( "<a href=\"http://www.google.com/finance?q=" +STOCK_EXCHANGE+ ":" +alert.getBseIciciMapping().getScCode()+ "\" target=\"_new\"><img border=\"0\" src=\"" +url+ "\"></a>" );
			}
			
			percentComplete = (++ctr / alertList.size()) * 100.00;
		}

		report.println( "- End of Report.</pre></body></html>" );
		report.close();
	}	
	
	private static void checkQualification(final String stockExchange, final PrintWriter report, final Object scCode, final Double[] cClose) throws Exception{
		boolean qualified = true;
		int boxIndex = 0;

		StringBuffer sb = new StringBuffer();
		int i=0;
		
		final List<Double> lClosePrice = new ArrayList<Double>();
		double max, min, percentValue, boundary=Double.MAX_VALUE;
		for( i=cClose.length-1; (i>=0) && (boxIndex <=NUM_BOXES); i-- ){
			lClosePrice.add(cClose[i]);
			
			max = Collections.max(lClosePrice);
			min = Collections.min(lClosePrice);
			
			if( max > boundary ){
				if( boxIndex < NUM_BOXES ){
					qualified = false;
					throw new Exception(
							String.format("Re-entering previous box [min: %f] [max: %f] [boundary: %f] [value: %f]", min, max, boundary, cClose[i])
					);
				}else{
					break;
				}
			}
			
			percentValue = (max*(BOX_SIZE/100));
			
			if( min < (max-percentValue) ){
				if( min < (max-(percentValue*2)) ){
					if( boxIndex < NUM_BOXES ){
						qualified = false;
						throw new Exception(
								String.format("Up/Down by more than one box [min: %f] [max: %f] [boundary: %f] [value: %f]", min, max, boundary, cClose[i])
						);
					}else{
						break;
					}
				}else{
					boxIndex++;
					lClosePrice.clear();
					boundary = max-percentValue;
					lClosePrice.add( boundary ); // New Max
				}
			}
		}
		
		int numberOfTradingSessions = ((cClose.length-1)-i);
		if(boxIndex >= NUM_BOXES && numberOfTradingSessions >= MIN_TRADING_SESSIONS_EXPECTED && numberOfTradingSessions <= MAX_TRADING_SESSIONS_EXPECTED ){
			sb.append( String.format("scCode: %s, qualified: %b, boxIndex: %d, Trading Sessions:\t%d", scCode, qualified, boxIndex, numberOfTradingSessions) );
			report.println( sb );
			// http://chart.apis.google.com/chart?cht=lc&chs=200x100&chd=t:40,60,60,45,47,75,70,72&chxt=x,y&chxr=1,0,75
			String url = GOOGLE_CHART_URL;
			
			url = url.replace("~NUM", String.valueOf(NUM++) );
			NUM = NUM == 10 ? 0 : NUM;
			
			url = url.replace("~DATA", Arrays.toString(cClose).replaceAll("[^0-9^.^,]", ""));
			//url = url.replace("~TITLE", scCode.toString());
			final List<Double> cCloseList = Arrays.asList(cClose);
			url = url.replace("~MIN", Collections.min(cCloseList).toString());
			url = url.replace("~MAX", Collections.max(cCloseList).toString());
			report.println( "<a href=\"http://www.google.com/finance?q=" +stockExchange+ ":" +scCode+ "\" target=\"_new\"><img border=\"0\" src=\"" +url+ "\"></a>" );
		}
		//System.out.println( sb );
		//System.out.println( String.format("qualified: %b, boxIndex: %d, Trading Sessions:\t%d", qualified, boxIndex, numberOfTradingSessions) );
	}

	private static void checkQualificationConstantUpwardMovement(final String stockExchange, final PrintWriter report, final Object scCode, final Double[] cClose, final boolean filterOutPennyStocks) throws Exception{
		boolean qualified = true;

		StringBuffer sb = new StringBuffer();
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
			sb.append( String.format("scCode: %s, qualified: %b, Trading Sessions:\t%d", scCode, qualified, numberOfTradingSessions) );
			report.println( sb );
			// http://chart.apis.google.com/chart?cht=lc&chs=200x100&chd=t:40,60,60,45,47,75,70,72&chxt=x,y&chxr=1,0,75
			String url = GOOGLE_CHART_URL;

			url = url.replace("~NUM", String.valueOf(NUM++) );
			NUM = NUM == 10 ? 0 : NUM;

			url = url.replace("~DATA", Arrays.toString(cClose).replaceAll("[^0-9^.^,]", ""));
			//url = url.replace("~TITLE", scCode.toString());
			final List<Double> cCloseList = Arrays.asList(cClose);
			url = url.replace("~MIN", Collections.min(cCloseList).toString());
			url = url.replace("~MAX", Collections.max(cCloseList).toString());
			report.println( "<a href=\"http://www.google.com/finance?q=" +stockExchange+ ":" +scCode+ "\" target=\"_new\"><img border=\"0\" src=\"" +url+ "\"></a>" );
		}
		//System.out.println( sb );
		//System.out.println( String.format("qualified: %b, boxIndex: %d, Trading Sessions:\t%d", qualified, boxIndex, numberOfTradingSessions) );
	}
	
	private class PercentCompleteReporter extends TimerTask{
		@Override
		public void run() {
			System.out.println( Math.round(percentComplete) +"% completed." );
		}
	}
	
}