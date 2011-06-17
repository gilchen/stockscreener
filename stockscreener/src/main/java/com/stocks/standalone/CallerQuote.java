package com.stocks.standalone;

import java.util.List;

import com.stocks.util.Utility;

public class CallerQuote {
	final static String[] GOOGLE_ETF_LIST = {"INDEXDJX:.DJI", "INDEXSP:.INX", "INDEXNASDAQ:.IXIC", "INDEXFTSE:.FTSE", "ERX", "TZA", "AGQ", "ZSL", "UGL", "UCO", "UCD", "BAL"}; // "EWV", "EZJ", "UGL", "BAL"
	final static String[] CNBC_ETF_LIST = {".DJIA", ".SPX", "COMP", ".FTSE", "ERX", "TZA", "AGQ", "ZSL", "UGL", "UCO", "UCD", "BAL"}; // "BAL", "LIT", "UCO", "NLR", "TMF"
	
	final static String GOOGLE_URL = "http://www.google.com/finance?q=";
	final static String CNBC_URL = "http://data.cnbc.com/quotes/";
	
	final static String ROW_FORMAT = "%-20s %-10s %-25s %-12s %s";

	private List<String> symbols;
	
	public List<String> getSymbols() {
		return symbols;
	}

	public void setSymbols(List<String> symbols) {
		this.symbols = symbols;
	}

	/**
	 * Main method to trigger the process.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main( String args[  ] ) throws Exception {
		processGoogle();
		//processCnbc();
		System.out.println( "Done." );
	}
	
	/**
	 * For processing google feed.
	 * 
	 */
	
	private static void processGoogle() throws Exception{
		for( String symbol : GOOGLE_ETF_LIST ){
			final StringBuffer sb = new StringBuffer( Utility.getContent( GOOGLE_URL +symbol ) );
			int index1 = sb.indexOf( "<div id=price-panel" );
			int index2 = sb.indexOf("<span id=", index1);
			String realTime = sb.substring( sb.indexOf(">", index2+1)+1, sb.indexOf("<", index2+1) );
			int index3 = sb.indexOf("<span class=\"chg", index2);
			int index4 = sb.indexOf("<span class=\"chg", index3+1);
			
			if( index3 == -1 ){
				index3 = sb.indexOf("<span class=\"chr", index2);
				index4 = sb.indexOf("<span class=\"chr", index3+1);
			}
			
			String pcChange = "";
			try{
				pcChange = sb.substring( sb.indexOf(">", index4+1)+1, sb.indexOf("<", index4+1) );
			}
			catch(Exception e){
			}
			
			String str = "";
			int index4_1 = sb.indexOf("<span class=nwp>", index4);
			int index4_2 = sb.indexOf("</span>", index4_1+1);
			if( index4_1 != -1 ){
				str = sb.substring( index4_1, index4_2);
			}
			String time = "";
			int index5 = -1;
			if( str != null ){
				if( str.indexOf("Real-time") != -1 ){
					index5 = sb.indexOf("<span id", index4);
					try{
						time = sb.substring( sb.indexOf(">", index5+1)+1, sb.indexOf("<", index5+1) );
					}
					catch(Exception e){
					}
				}else{
					time = "Close";
				}
			}
			
			int index6 = sb.indexOf( "data-snapfield=\"range\"", index5 );
			int index7 = sb.indexOf( "<span class=", index6+1 );
			String range = "";
			try{
				range = sb.substring( sb.indexOf(">", index7+1)+1, sb.indexOf("<", index7+1) );
			}
			catch(Exception e){
			}
			
			System.out.println( String.format(ROW_FORMAT, symbol, realTime.trim(), range.trim(), pcChange.trim(), time.trim()) );
		}
	}
	

	/**
	 * For processing quote feed.
	 * 
	 */
	private static void processCnbc() throws Exception{
		for( String symbol : CNBC_ETF_LIST ){
			final StringBuffer sb = new StringBuffer( Utility.getContent( CNBC_URL +symbol ) );
			String realTime = "";
			String pcChange = "";
			int index1 = sb.indexOf( "cnbc_mrq_pushSymbol(" );
			if( index1 != -1 ){
				String str = sb.substring( index1+20, sb.indexOf(");", index1) );
				str = str.replace("'", "");
				final String[] arr = str.split(",");
				realTime = arr[1];
				pcChange = arr[3];
			}

			String high = "";
//			int index2 = sb.indexOf("Today's High", index1);
//			if( index2 != -1 ){
//				int index3 = sb.indexOf("<td align=\"right\">", index2);
//				high = sb.substring( index3+18, sb.indexOf("<", index3+1) );
//			}
			
			String low = "";
//			index2 = sb.indexOf("Today's Low", index1);
//			if( index2 != -1 ){
//				int index3 = sb.indexOf("<td align=\"right\">", index2);
//				low = sb.substring( index3+18, sb.indexOf("<", index3+1) );
//			}
			String range = low +" - "+high;
	
			String time = "";
			int index4 = sb.indexOf( "var promoTime = " );
			if( index4 != -1 ){
				time = sb.substring( index4+16, sb.indexOf(";", index4));
				time = time.replace("\"", "");
				time = time.split(" ")[1];
			}
			
			System.out.println( String.format(ROW_FORMAT, symbol, realTime.trim(), range.trim(), pcChange.trim(), time.trim()) );
		}
	}

}
