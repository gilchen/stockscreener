package com.stocks.util;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.stocks.enums.Movement;
import com.stocks.standalone.IntraDayStructure;

public class Utility {
	final static DecimalFormat df = new DecimalFormat("###,###.##");
	final static DecimalFormat dfInteger = new DecimalFormat("###,###");
	
	public static final Long getDaysDifference(Date d1, Date d2){
		Long diff = d2.getTime() - d1.getTime();
		Long diffDays = diff / (24 * 60 * 60 * 1000);
		return diffDays;
	}
	
	public static String getContent(String sUrl) throws Exception {
		StringBuffer content = new StringBuffer();

		URL url = new URL(sUrl);
		final URLConnection urlConnection = url.openConnection();
		urlConnection.setUseCaches(false);
		BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			content.append(inputLine).append("\n");
		}
		in.close();
		return content.toString();
	}
	
	public static void saveContent(String path, String content) throws Exception{
		FileWriter writer = new FileWriter(path);
		writer.append(content);
		writer.close();
	}

	public static Double round(Double d){
		BigDecimal bd = new BigDecimal( d );
		bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
		return bd.doubleValue();
	}

	public static boolean areDatesEqual(final Date d1, final Date d2){
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		final String s1 = df.format(d1);
		final String s2 = df.format(d2);
		return s1.equals(s2);
	}

	public static Date getDateFor(String str, String format) throws Exception {
		return new SimpleDateFormat(format).parse( str );
	}
	
	public static Date getDate(String str) throws Exception {
		return new SimpleDateFormat("MM/dd/yyyy").parse( str );
	}

	public static String getStrDate(Date dt, String format){
		return new SimpleDateFormat(format).format( dt );
	}
	
	public static String getStrDate(Date dt){
		return new SimpleDateFormat("MM/dd/yyyy").format( dt );
	}
	
	public static String getStrTime(Date dt){
		return new SimpleDateFormat("HH:mm:ss").format( dt );
	}
	
	public static long getDaysDiffBetween(Date olderDate, Date newerDate){
		long diff = newerDate.getTime() - olderDate.getTime();
		return Math.abs(diff / (1000 * 60 * 60 * 24));
	}
	
	public static String getFormattedNumber(Number number){
		return number != null ? df.format(number) : null;
	}

	public static String getFormattedInteger(Number number){
		return number != null ? dfInteger.format(number) : null;
	}
	
	/**
	 * Converts 1K to 1000, 1.2K to 1200 for K, M and B (thousands, millions and billions)
	 * @param strNum e.g 1K
	 * @return
	 */
	public static String convertFinancials(String strNum){
		String k = "000";
		String m = k + k;
		String b = m + k;
		String t = b + k;
		
		String result = strNum.toUpperCase().trim();

		if( result.endsWith("K") ){
			if( result.indexOf(".") != -1 ){
				int filler = (result.length()-2) - result.indexOf(".");
				k = k.substring(filler);
			}
			result = result.replace("K", k).replace(".", "");
		}else if( result.endsWith("M") ){
			if( result.indexOf(".") != -1 ){
				int filler = (result.length()-2) - result.indexOf(".");
				m = m.substring(filler);
			}
			result = result.replace("M", m).replace(".", "");
		}else if( result.endsWith("B") ){
			if( result.indexOf(".") != -1 ){
				int filler = (result.length()-2) - result.indexOf(".");
				b = b.substring(filler);
			}
			result = result.replace("B", b).replace(".", "");
		}else if( result.endsWith("T") ){
			if( result.indexOf(".") != -1 ){
				int filler = (result.length()-2) - result.indexOf(".");
				t = t.substring(filler);
			}
			result = result.replace("T", t).replace(".", "");
		} 
		
		//System.out.println( strNum +" --> "+result );
		return result;
	}
	
	public static Movement getCandleStickType(final IntraDayStructure ids){
		// Step1: Is Hammer-Like
		if( ids.getClose() > ids.getOpen() ){
			// Split OHLC into 3 parts
			final Double diffHighToClose = ids.getHigh() - ids.getClose();
			final Double body = ids.getClose() - ids.getOpen();
			final Double diffOpenToLow = ids.getOpen() - ids.getLow();
			if( body > diffHighToClose && body > diffOpenToLow ){ // If Body is largest
				// Buy
				return Movement.PUMP;
			}else if( diffHighToClose > diffOpenToLow ){ // If HighToClose is largest
				// Sell
				return Movement.DUMP;
			}else if( diffOpenToLow > diffHighToClose ){
				// Buy
				return Movement.PUMP;
			}else{
				return Movement.NONE;
			}
		}else if( ids.getClose() < ids.getOpen() ) { // Step2: Is Gravestone-Like
			// Split OHLC into 3 parts
			final Double diffHighToOpen = ids.getHigh() - ids.getOpen();
			final Double body = ids.getOpen() - ids.getClose();
			final Double diffCloseToLow = ids.getClose() - ids.getLow();
			if( body > diffHighToOpen && body > diffCloseToLow ){ // If Body is largest
				// Sell
				return Movement.DUMP;
			}else if( diffHighToOpen > diffCloseToLow ){ // If HighToOpen is largest
				// Sell
				return Movement.DUMP;
			}else if( diffCloseToLow > diffHighToOpen ){
				// Buy
				return Movement.PUMP;
			}else{
				return Movement.NONE;
			}
		}else{ // Step3: Other (DOJI)
			// Split OHLC into 3 parts
			final Double diffHighToOpen = ids.getHigh() - ids.getOpen();
			final Double body = ids.getOpen() - ids.getClose();
			final Double diffCloseToLow = ids.getClose() - ids.getLow();
			if( diffHighToOpen > diffCloseToLow ){ // If HighToOpen is largest
				// Sell
				return Movement.DUMP;
			}else if( diffCloseToLow > diffHighToOpen ){
				// Buy
				return Movement.PUMP;
			}else{
				return Movement.NONE;
			}
		}
	}
	
	/**
	 * 
	 * @return True of the current time is after 4:15PM, false otherwise
	 */
	public static boolean isAfter415(){
		Calendar c415 = Calendar.getInstance();
		c415.set( Calendar.HOUR, 4 );
		c415.set( Calendar.MINUTE, 15 );
		c415.set( Calendar.AM_PM, Calendar.PM );

		return Calendar.getInstance().after(c415);
	}
	
	public static void main(String args[]){
//		String arr[] = new String[]{"MMM", "ABB", "ABT", "ACL", "MO", "AMZN", "AXP", "AMGN", "AAPL", "AZN", "T", "AXAHY", "BBD", "STD", "BCS", "BRK.A", "BHP", "BBL", "BP", "BMY", "BTI", "CAJ", "CAT", "CVX", "LFC", "CHL", "SNP", "CSCO", "C", "CL", "CMCSA", "ABV", "COP", "CS", "CVS", "DB", "DT", "DEO", "EC", "LLY", "E", "XOM", "F", "FTE", "GE", "GSK", "GS", "GOOG", "HPQ", "HMC", "HBC", "INTC", "IBM", "JNJ", "JPM", "KFT", "LYG", "MCD", "MDT", "MRK", "MSFT", "MS", "NTT", "NOK", "NVS", "NVO", "DCM", "OXY", "ORCL", "PEP", "PTR", "PBR", "PFE", "PM", "QCOM", "RTP", "RY", "RDS.A", "SNY", "SAP", "SLB", "SI", "STO", "SU", "TGT", "TEF", "TEVA", "BNS", "BA", "KO", "HD", "PG", "DIS", "TD", "TOT", "USB", "UN", "UL", "UPS", "UTX", "VALE", "VZ", "V", "VOD", "WMT", "WFC", "WBK"};
//		String djia[] = new String[]{"MMM", "AA", "AXP", "T", "BAC", "BA", "CAT", "CVX", "CSCO", "KO", "DD", "XOM", "GE", "HPQ", "HD", "INTC", "IBM", "JNJ", "JPM", "KFT", "MCD", "MRK", "MSFT", "PFE", "PG", "TRV", "UTX", "VZ", "WMT", "DIS"};
//		List<String> arrList = new ArrayList(Arrays.asList(arr));
//		List<String> djiaList = new ArrayList(Arrays.asList(djia));
//		arrList.removeAll(djiaList);
//		System.out.println( arrList );
/*
		convertFinancials( "1.2K" );
		convertFinancials( "1.22K" );
		convertFinancials( "1.222K" );
		convertFinancials( "1.3m" );
		convertFinancials( "1.33m" );
		convertFinancials( "1.5b" );
		convertFinancials( "1.55b" );
		convertFinancials( "3k" );
		convertFinancials( "3m" );
		convertFinancials( "3b" );
		convertFinancials( "3.1t" );
*/
		
/*		
		System.out.println( getFormattedNumber( null ) );
		System.out.println( getFormattedNumber( 0.00 ) );
		System.out.println( getFormattedNumber( 123456789.50 ) );
		System.out.println( getFormattedNumber( 123456789012.00 ) );
*/
	}
}
