package com.stocks.command;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

import com.stocks.model.Nyse;
import com.stocks.model.Report;
import com.stocks.util.Utility;

public class NyseNPercentUpFromBottomScanningSimulationCommand extends AbstractCommand {
	private static final double FROM_PERCENT = 9d;
	private static final double TO_PERCENT = 11d;
	
	int TOTAL_STOCKS = 0;
	int TOTAL_UNQUALIFIED = 0;
	int TOTAL_TARGET_HITS = 0;
	int TOTAL_STOPLOSS_HITS = 0;
	int TOTAL_NO_EVENT = 0;

//	public static void main(String args[]) throws Exception{
//		Double[] arr = new Double[]{39.00, 38.90, 38.80, 38.70, 38.60, 38.50, 38.40, 38.30, 38.20, 38.10, 38.00, 37.90, 37.80, 37.70, 37.60, 37.50, 37.40, 37.30, 37.20, 37.10, 37.00, 36.90, 36.80, 36.70, 36.60, 36.50, 36.40, 36.30, 36.20, 36.10, 36.00, 35.90, 35.80, 35.70, 35.60, 35.50, 35.40, 35.30, 35.20};
//		checkQualificationNPercentCorrectionInMMonths("NYSE", new StringBuffer(), "C", arr);
//	}

	public boolean execute(Context context) throws Exception {
		Collection<String> commandNames = (Collection<String>)context.get(COMMANDS_TO_EXECUTE);
		if( commandNames.contains(this.getClass().getName()) ){
			System.out.println( "Executing NyseNPercentUpFromBottomScanningSimulationCommand..." );
			
			TOTAL_STOCKS = 0;
			TOTAL_UNQUALIFIED = 0;
			TOTAL_TARGET_HITS = 0;
			TOTAL_STOPLOSS_HITS = 0;

			processNyse();
			System.out.println( "TOTAL_STOCKS: " +TOTAL_STOCKS );
			System.out.println( "TOTAL_UNQUALIFIED: " +TOTAL_UNQUALIFIED );
			System.out.println( "TOTAL_TARGET_HITS: " +TOTAL_TARGET_HITS );
			System.out.println( "TOTAL_STOPLOSS_HITS: " +TOTAL_STOPLOSS_HITS );
			System.out.println( "TOTAL_NO_EVENT: " +TOTAL_NO_EVENT );
		}
		return Command.CONTINUE_PROCESSING;
	}

	private void processNyse() throws Exception{
		final String STOCK_EXCHANGE = "NYSE";
		StringBuffer sb = new StringBuffer();
		
		sb.append( "<div id='googleChartDiv' style='position:fixed;left:22%;top:15%'>" );
		sb.append( "<img id='googleChart' border='1' onclick='this.style.display=\"none\"' />" );
		sb.append( "</div>" );
		sb.append( "<pre>\n" );
		sb.append( String.format("<B>Nyse Report (%s - %s percent Up from Bottom Scanning Simulation) - Generated on %s</B>%n", FROM_PERCENT, TO_PERCENT, new SimpleDateFormat("MMM dd, yyyy").format(new Date())));

		// Start: Start-End Dates
		Calendar dt = Calendar.getInstance();
		final Date tradeEndDate = (Date) dt.getTime().clone();
		
		dt.add( Calendar.DAY_OF_YEAR, -30*12 ); // 12 months ago
		final Date tradeStartDate = dt.getTime();
		// End: Start-End Dates
		
		
		List<String> symbols = getStockService().getAllSymbols();
		TOTAL_STOCKS = symbols.size();
		
		List<Double> cClose = new ArrayList<Double>();

		double ctr = 0.0;
		for( final String symbol : symbols ){
			cClose.clear();
			List<Nyse> nyseList = getStockService().findStockBySymbolBetweenTradeDates(symbol, tradeStartDate, tradeEndDate);
			for( final Nyse nyse : nyseList ){
				cClose.add( nyse.getClose() );
			}

			try{
				checkQualificationNPercentUpFromBottomScanningSimulation(STOCK_EXCHANGE, sb, symbol, cClose.toArray(new Double[]{}));
			}
			catch(Exception e){
			}
			getPercentCompleteReporter().setPercentComplete( (++ctr / symbols.size()) * 100.00 );
			//System.out.print(".");
		}
		sb.append( "<BR>TOTAL_STOCKS: " +TOTAL_STOCKS );
		sb.append( "<BR>TOTAL_UNQUALIFIED: " +TOTAL_UNQUALIFIED );
		sb.append( "<BR>TOTAL_TARGET_HITS (May happen more than once for a Stock): " +TOTAL_TARGET_HITS );
		sb.append( "<BR>TOTAL_STOPLOSS_HITS (May happen more than once for a Stock): " +TOTAL_STOPLOSS_HITS );
		sb.append( "<BR>TOTAL_NO_EVENT (Waiting Stop Loss or Target): " +TOTAL_NO_EVENT );

		sb.append( "<BR>- End of Report.</pre>\n" );
		final Report report = new Report( Report.ReportName.NyseNPercentUpFromBottomScanningSimulationCommand.toString(), sb.toString());
		getStockService().saveReport(report);
	}
	
	private void checkQualificationNPercentUpFromBottomScanningSimulation(final String stockExchange, final StringBuffer sb, final Object scCode, final Double[] cClose) throws Exception{
		boolean qualified = false;

		// Step 1: Get Lowest Price
		List<Double> list = Arrays.asList(cClose);
		
		StringBuffer sbChm = new StringBuffer();
		
		// Start: 6 Months Scanning
		final int TRADING_DAYS_IN_6_MONTHS = 125;
		boolean bStartScanning = false;
		Double targetPrice = 0.0, sltp = 0.0;
		
		// The sbChmSet will ensure single entry of each element e.g. LowestPrice.
		final Set<String> sbChmSet = new HashSet<String>();
		final Stack<Integer> identifierStack = new Stack<Integer>();
		for( int i=9; i>0; i-- ){
			identifierStack.push(i);
		}
		
		Integer identifier = null;
		for( int i=TRADING_DAYS_IN_6_MONTHS; i<list.size(); i++ ){
			List<Double> subList = list.subList(i-TRADING_DAYS_IN_6_MONTHS, i);

			Double lowestPrice = Collections.min(subList);
			
			//Double lastClose = cClose[cClose.length-1];
			Double fromPrice = Utility.round(lowestPrice + (lowestPrice * (FROM_PERCENT/100.0)));
			Double toPrice   = Utility.round(lowestPrice + (lowestPrice * (TO_PERCENT/100.0)));

			Double priceAtCurrentIndex = subList.get(subList.size()-1);
			if( !bStartScanning && (priceAtCurrentIndex >= fromPrice && priceAtCurrentIndex <= toPrice) ){
				qualified = true;
				identifier = identifierStack.pop();
				
				int indexOfLowestPrice = list.lastIndexOf( lowestPrice );
				String str = GOOGLE_CHART_CHM_PARAM_VALUE;
				str = str.replace("~RECOMMENDED_BUY_INDEX",  String.valueOf(indexOfLowestPrice));
				str = str.replace("~RECOMMENDED_BUY_PRICE",  identifier +" Low@" +list.get(indexOfLowestPrice));
				sbChmSet.add(str);

				str = GOOGLE_CHART_CHM_PARAM_VALUE;
				str = str.replace("~RECOMMENDED_BUY_INDEX",  String.valueOf(i-1));
				str = str.replace("~RECOMMENDED_BUY_PRICE",  identifier +" Buy@" +priceAtCurrentIndex.toString());
				sbChmSet.add(str);
				
				bStartScanning = true;
				targetPrice = priceAtCurrentIndex + (priceAtCurrentIndex * (2.0/100.0)); // Target: 2%
				sltp = priceAtCurrentIndex - (priceAtCurrentIndex * (5.0/100.0)); // Stop Loss: 5%
			}
			
			if( bStartScanning ){
				if( priceAtCurrentIndex <= sltp ){
					bStartScanning = false;
					TOTAL_STOPLOSS_HITS++;

					String str = GOOGLE_CHART_CHM_PARAM_VALUE;
					str = str.replace("~RECOMMENDED_BUY_INDEX",  String.valueOf(i-1));
					str = str.replace("~RECOMMENDED_BUY_PRICE",  identifier +" SL@" +priceAtCurrentIndex);
					str = str.replace("000000", "FF0000");
					sbChmSet.add(str);
				}
				
				if( priceAtCurrentIndex >= targetPrice ){
					bStartScanning = false;
					TOTAL_TARGET_HITS++;

					String str = GOOGLE_CHART_CHM_PARAM_VALUE;
					str = str.replace("~RECOMMENDED_BUY_INDEX",  String.valueOf(i-1));
					str = str.replace("~RECOMMENDED_BUY_PRICE",  identifier +" TGT@" +priceAtCurrentIndex);
					str = str.replace("000000", "00AA00");
					sbChmSet.add(str);
				}
			}
		}
		
		for(String s : sbChmSet){
			sbChm.append( s ).append("|");
		}
		
		if( bStartScanning ){
			TOTAL_NO_EVENT++;
		}
		// End: 6 Months Scanning

		if( qualified ){
			sb.append( "<span>" );
			
			// http://chart.apis.google.com/chart?cht=lc&chs=200x100&chd=t:40,60,60,45,47,75,70,72&chxt=x,y&chxr=1,0,75
			String url = GOOGLE_CHART_RECOMMENDED_BUY_URL;
			url = url.replace("~NUM", String.valueOf(NUM++) );
			NUM = NUM == 10 ? 0 : NUM;

			url = url.replace("~DATA", Arrays.toString(cClose).replaceAll("[^0-9^.^,]", ""));
			//url = url.replace("~TITLE", scCode.toString());
			url = url.replace("~MIN", Collections.min(list).toString());
			url = url.replace("~MAX", Collections.max(list).toString());
			
			if( sbChm.length() > 0 ){
				url += sbChm.substring(0, sbChm.length()-1);
			}

			String str = ( "<a href=\"http://www.google.com/finance?q=" +stockExchange+ ":" +scCode+ "\" target=\"_new\" onmouseover=\"javascript:showGoogleChart(document.getElementById('googleChart'), '" +url+ "')\">" +scCode+ "</a>" );
			sb.append( String.format("Symbol: %s%n", str) );
			sb.append( "</span>" );
			
			System.out.println( String.format("wget --post-data=\"%s\" http://0.chart.apis.google.com/chart -O %s.png", url.substring( url.indexOf("?")+1 ), scCode) );
			
		}else{
			TOTAL_UNQUALIFIED++;
		}
	}

	@Override
	public String toString() {
		return this.getClass().getName();
	}

	public static void main(String... args) throws Exception{
		final String strClose = "11.23,11.21,11.04,11.02,11.17,11.71,11.56,11.52,11.58,11.59,11.66,11.35,10.66,10.73,10.64,10.6,10.95,10.93,10.66,10.57,10.37,10.06,10.11,10.01,10.32,10.25,10.22,10.52,10.83,10.88,10.86,11.07,11.23,11.17,11.52,11.49,11.43,11.26,11.17,11.38,11.17,11.7,11.74,12.2,12.07,12.22,12.08,12.11,12.23,11.92,12.14,12.37,12.2,12.89,12.93,13.2,13.37,13.13,13.13,13.14,12.67,12.95,12.78,12.72,12.88,12.87,12.7,12.65,13.14,13.05,13.24,13.14,13.6,14.0,13.96,13.75,13.88,13.81,13.49,13.4,13.03,12.94,13.38,13.38,13.29,13.13,13.31,13.17,13.29,13.13,13.57,14.09,14.23,14.24,14.15,14.14,14.15,14.25,14.36,14.2,13.96,14.46,14.56,14.77,14.89,15.14,15.34,15.23,15.25,15.13,15.21,15.39,15.8,16.52,16.56,16.36,16.42,16.49,16.33,16.24,15.75,15.97,16.27,16.06,15.98,15.79,16.43,16.24,16.6,16.47,16.13,16.57,17.32,17.21,17.21,17.14,17.32,17.4,17.16,17.12,17.37,17.59,17.4,17.59,17.52,17.28,16.54,16.44,16.55,16.68,16.85,16.23,16.18,16.63,16.58,16.25,16.49,16.3,15.8,16.03,16.12,16.04,15.66,16.01,16.11,16.55,16.45,16.95,17.11,17.09,17.24,17.49,17.64,17.66,17.47,17.56,18.05,18.13,18.12,17.92,17.77,16.7,16.55,16.55,16.52,16.13,16.44,16.64,16.97,16.89,17.03,17.18,17.09,17.0,17.22,17.67,17.47,17.01,17.15,17.53,17.52,17.05,17.15,17.1,16.92,16.45,16.69,16.67,16.26,15.98,16.11,16.33,16.37,16.48,16.81,16.09,16.2,15.92,15.61,15.69,15.41,15.5,15.28,15.1,15.41,14.96,14.79,14.72,14.78,15.37,15.29,15.28,15.23,15.28,15.65,15.82,15.86,16.31,16.39,16.24,16.49,16.38,15.91,15.71,15.85,15.46,15.48";
		final List<Double> list = new ArrayList<Double>();
		for( String close : strClose.split(",") ){
			list.add( new Double(close) );
		}
		
		NyseNPercentUpFromBottomScanningSimulationCommand n = new NyseNPercentUpFromBottomScanningSimulationCommand();
		StringBuffer sb = new StringBuffer();
		n.checkQualificationNPercentUpFromBottomScanningSimulation("NYSE", sb, "TEST", list.toArray(new Double[]{}));
		System.out.println( sb );
	}
	
}
