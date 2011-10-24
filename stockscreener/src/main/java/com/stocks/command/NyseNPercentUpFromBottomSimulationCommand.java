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

public class NyseNPercentUpFromBottomSimulationCommand extends AbstractCommand {
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
		setStartDate((Date) context.get(START_DATE));
		setEndDate((Date) context.get(END_DATE));

		if( commandNames.contains(this.getClass().getName()) ){
			System.out.println( "Executing NyseNPercentUpFromBottomSimulationCommand..." );
			
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
		sb.append( String.format("<B>Nyse Report (%s - %s percent Up from Bottom Simulation) - Generated on %s</B>%n", FROM_PERCENT, TO_PERCENT, new SimpleDateFormat("MMM dd, yyyy").format(new Date())));

		List<String> symbols = getStockService().getAllSymbols();
		TOTAL_STOCKS = symbols.size();
		
		List<Double> cClose = new ArrayList<Double>();

		double ctr = 0.0;
		for( final String symbol : symbols ){
			cClose.clear();
			List<Nyse> nyseList = getStockService().findStockBySymbolBetweenTradeDates(symbol, getStartDate(), getEndDate());
			for( final Nyse nyse : nyseList ){
				cClose.add( nyse.getClose() );
			}

			try{
				checkQualificationNPercentUpFromBottomSimulation(STOCK_EXCHANGE, sb, symbol, cClose.toArray(new Double[]{}));
			}
			catch(Exception e){
			}
			getPercentCompleteReporter().setPercentComplete( (++ctr / symbols.size()) * 100.00 );
			//System.out.print(".");
		}
		sb.append( "<BR>TOTAL_STOCKS: " +TOTAL_STOCKS );
		sb.append( "<BR>TOTAL_UNQUALIFIED: " +TOTAL_UNQUALIFIED );
		sb.append( "<BR>TOTAL_TARGET_HITS: " +TOTAL_TARGET_HITS );
		sb.append( "<BR>TOTAL_STOPLOSS_HITS: " +TOTAL_STOPLOSS_HITS );
		sb.append( "<BR>TOTAL_NO_EVENT (Waiting Stop Loss or Target): " +TOTAL_NO_EVENT );

		sb.append( "<BR>- End of Report.</pre>\n" );
		final Report report = new Report( Report.ReportName.NyseNPercentUpFromBottomSimulationCommand.toString(), sb.toString());
		getStockService().saveReport(report);
	}
	
	private void checkQualificationNPercentUpFromBottomSimulation(final String stockExchange, final StringBuffer sb, final Object scCode, final Double[] cClose) throws Exception{
		boolean qualified = false;

		// Step 1: Get Lowest Price
		List<Double> list = Arrays.asList(cClose);
		Double lowestPrice = Collections.min(list);
		
		//Double lastClose = cClose[cClose.length-1];
		Double fromPrice = Utility.round(lowestPrice + (lowestPrice * (FROM_PERCENT/100.0)));
		Double toPrice   = Utility.round(lowestPrice + (lowestPrice * (TO_PERCENT/100.0)));
		boolean bStartScanning = false;
		StringBuffer sbChm = new StringBuffer();
		Double targetPrice = 0.0, sltp = 0.0;
		boolean bTargetHit = false, bStopLossHit = false;
		for( int i=0; i<cClose.length; i++ ){
			if( !bStartScanning && cClose[i] == lowestPrice ){
				bStartScanning = true;
				String str = GOOGLE_CHART_CHM_PARAM_VALUE;
				str = str.replace("~RECOMMENDED_BUY_INDEX",  String.valueOf(i));
				str = str.replace("~RECOMMENDED_BUY_PRICE",  cClose[i].toString());
				sbChm.append( str ).append("|");;
			}
			
			if( bStartScanning && !qualified && (cClose[i] >= fromPrice && cClose[i] <= toPrice) ){
				qualified = true;
				String str = GOOGLE_CHART_CHM_PARAM_VALUE;
				str = str.replace("~RECOMMENDED_BUY_INDEX",  String.valueOf(i));
				str = str.replace("~RECOMMENDED_BUY_PRICE",  cClose[i].toString());
				sbChm.append( str );
				//break;
				
				targetPrice = cClose[i] + (cClose[i] * (2.0/100.0)); // Target: 2%
				sltp = cClose[i] - (cClose[i] * (5.0/100.0)); // Stop Loss: 5%
			}
			
			if( qualified ){
				// If Closing Price is >= 2% of BoughtPrice
				if( cClose[i] <= sltp ){
					TOTAL_STOPLOSS_HITS++;
					bStopLossHit = true;
					break;
				}
				if( cClose[i] >= targetPrice ){
					TOTAL_TARGET_HITS++;
					bTargetHit = true;
					break;
				}
			}
		}

		if( qualified ){
			sb.append( "<span " );
			if( bTargetHit ){
				sb.append( "class='success'" );
			}else if( bStopLossHit ){
				sb.append( "class='error'" );
			}else{
				TOTAL_NO_EVENT++;
			}
			sb.append( ">" );
			
			// http://chart.apis.google.com/chart?cht=lc&chs=200x100&chd=t:40,60,60,45,47,75,70,72&chxt=x,y&chxr=1,0,75
			String url = GOOGLE_CHART_RECOMMENDED_BUY_URL;
			url = url.replace("~NUM", String.valueOf(NUM++) );
			NUM = NUM == 10 ? 0 : NUM;

			url = url.replace("~DATA", Arrays.toString(cClose).replaceAll("[^0-9^.^,]", ""));
			//url = url.replace("~TITLE", scCode.toString());
			final List<Double> cCloseList = Arrays.asList(cClose);
			url = url.replace("~MIN", Collections.min(cCloseList).toString());
			url = url.replace("~MAX", Collections.max(cCloseList).toString());
			
			if( sbChm.length() > 0 ){
				url += sbChm.substring(0, sbChm.length());
			}
			
			String str = ( "<a href=\"http://www.google.com/finance?q=" +stockExchange+ ":" +scCode+ "\" target=\"_new\" onmouseover=\"javascript:showGoogleChart(document.getElementById('googleChart'), '" +url+ "')\">" +scCode+ "</a>" );
			sb.append( String.format("Symbol: %s, Lowest: %s, Enter Between [%s - %s]%n", str, lowestPrice, fromPrice, toPrice) );
			sb.append( "</span>" );
		}else{
			TOTAL_UNQUALIFIED++;
		}
	}

	@Override
	public String toString() {
		return this.getClass().getName();
	}

}
