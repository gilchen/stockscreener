package com.stocks.standalone;

import java.text.NumberFormat;
import java.util.List;

import com.stocks.util.Utility;

public class CallerQuote {
	//final static String[] GOOGLE_ETF_LIST = {"INDEXDJX:.DJI", "INDEXSP:.INX", "INDEXNASDAQ:.IXIC", "INDEXFTSE:.FTSE", "ABX", "AGNC", "AU", "BA", "BTI", "DIA", "EWT", "GDX", "GG", "HYG", "IEF", "JNK", "LH", "MCK", "MDT", "MRK", "OEF", "PCL", "VALE", "WAT", "WY", "XLV", "YUM" };
	final static String[] GOOGLE_ETF_LIST = {"INDEXDJX:.DJI", "INDEXSP:.INX", "INDEXNASDAQ:.IXIC", "INDEXFTSE:.FTSE", "INDEXEURO:PX1", "-", "AGQ", "BAL", "NFLX", "SDOW", "IRE", "-", "ERX", "UCO", "-", "UGL", "NBG" }; //"AGG", "AGNC", "BAX", "BND", "BSX", "CFT", "FXY", "GMCR", "JNJ", "KO", "KR", "LLY", "LQD", "NFLX", "NLY", "NVS", "PCG", "PCS", "PG", "SPXU", "SYK", "SYY", "T", "TIP", "TWM", "TZA", "WDC", "WHR", "XLP"
	//final static String[] CNBC_ETF_LIST = {".DJIA", ".SPX", "COMP", ".FTSE" };
	final static String[] CNBC_ETF_LIST = {".DJIA", ".SPX", "COMP", ".FTSE", ".FCHI", ".GDAXI", "-", "AA", "AVP", "BTU", "CSC", "ECA", "FTR", "MCP", "RIG", "RIMM", "S", "AGQ", "BAL", "NFLX", "SDOW", "IRE", "NBG", "-", "ERX", "UCO", "CLCV1", "-", "UGL", "INDL", "NLR", "PCX" }; // "BAL", "LIT", "UCO", "NLR", "TMF", "RIG", "CREE", "ECA", 
	//final static String[] CNBC_ETF_LIST = {"PCX"}; //{"LVS", "MGM", "CCL", "RCL"};
	
	final static String GOOGLE_URL = "http://www.google.com/finance?q=";
	final static String CNBC_URL = "http://data.cnbc.com/quotes/";
	final static String CNBC_URL_EXTN = "http://apps.cnbc.com/company/quote/index.asp?symbol=";
	
	final static String ROW_FORMAT = "%-20s %-10s %-25s %-12s %-12s %-25s %-16s";
	
	final static Double RECOMMENDATION_52W_CORRECTION_PC   = -40.00;
	final static Double RECOMMENDATION_52W_APPRECIATION_PC = 9.00;
	
	final static NumberFormat NF = NumberFormat.getInstance();
	
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
		System.out.println( String.format(ROW_FORMAT, "Symbol", "realTime", "range", "% Change", "time", "range52w", "range52w_pc") );
		//processGoogle();
		processCnbc();
		System.out.println( "*range52w_pc: (Closely Above +" +RECOMMENDATION_52W_APPRECIATION_PC+ "% & Anything Below " +RECOMMENDATION_52W_CORRECTION_PC+ "% is good)" );
		System.out.println( "Done." );
	}
	
	/**
	 * For processing google feed.
	 * 
	 */
	
	private static void processGoogle() throws Exception{
		for( String symbol : GOOGLE_ETF_LIST ){
			if( symbol.equals("-") ){
				System.out.println( "------------------------------------------------------------------------------------------------------------------------------" );
				continue;
			}

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
			int index7 = sb.indexOf( "<td class=", index6+1 );
			String range = "";
			try{
				range = sb.substring( sb.indexOf(">", index7+1)+1, sb.indexOf("<", index7+1) );
			}
			catch(Exception e){
			}
			
			int index8 = sb.indexOf( "data-snapfield=\"range_52week\"" );
			int index9 = sb.indexOf( "<td class=", index8 );
			String range52w = "", range52w_pc = "";
			Double low52w = null, high52w = null;
			try{
				range52w = sb.substring( sb.indexOf(">", index9+1)+1, sb.indexOf("<", index9+1) ); // "20.76 - 42.75"
				String[] arr = range52w.split("-");
				low52w = NF.parse( arr[0].trim() ).doubleValue();
				high52w = NF.parse( arr[1].trim() ).doubleValue();
				
				Double low52w_pc = 0.0;
				Double high52w_pc = 0.0;
				Double realTimePrice = NF.parse(realTime.trim()).doubleValue();
				low52w_pc = ((realTimePrice - low52w)/low52w)*100.0;
				high52w_pc = ((realTimePrice - high52w)/high52w)*100.0;
				
				range52w_pc = Utility.round(low52w_pc) +"% / "+ Utility.round(high52w_pc)+"%";
				if( (low52w_pc >= RECOMMENDATION_52W_APPRECIATION_PC && low52w_pc <= RECOMMENDATION_52W_APPRECIATION_PC+5.0)
						&& high52w_pc <= RECOMMENDATION_52W_CORRECTION_PC ){
					range52w_pc += " (HOT STOCK. See Comments*)";
				}
			}
			catch(Exception e){
			}
			
			System.out.println( String.format(ROW_FORMAT, symbol, realTime.trim(), range.trim(), pcChange.trim(), time.trim(), range52w, range52w_pc) );
		}
	}
	

	/**
	 * For processing quote feed.
	 * 
	 */
	private static void processCnbc() throws Exception{
		for( String symbol : CNBC_ETF_LIST ){
			if( symbol.equals("-") ){
				System.out.println( "------------------------------------------------------------------------------------------------------------------------------" );
				continue;
			}
			
			final StringBuffer sb = new StringBuffer( Utility.getContent( CNBC_URL +symbol ) );
			final StringBuffer sbExtn = new StringBuffer( Utility.getContent(CNBC_URL_EXTN +symbol) );
			
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
			int index2 = sbExtn.indexOf("High Today");
			if( index2 != -1 ){
				int index3 = sbExtn.indexOf("<p class=\"data\">", index2);
				high = sbExtn.substring( index3+16, sbExtn.indexOf("<", index3+1) );
			}
			
			String low = "";
			index2 = sbExtn.indexOf("Low Today", index2);
			if( index2 != -1 ){
				int index3 = sbExtn.indexOf("<p class=\"data\">", index2);
				low = sbExtn.substring( index3+16, sbExtn.indexOf("<", index3+1) );
			}
			String range = low +" - "+high;
			
			String range52w = "", range52w_pc = "";
			Double low52w = null, high52w = null;
			index2 = sbExtn.indexOf("52-Week High", index2);
			if( index2 != -1 ){
				int index3 = sbExtn.indexOf("<p class=\"data\">", index2);
				try{
					high52w = NF.parse( sbExtn.substring( index3+16, sbExtn.indexOf("<", index3+1) ) ).doubleValue();
				}
				catch(Exception e){
				}
			}

			index2 = sbExtn.indexOf("52-Week Low", index2);
			if( index2 != -1 ){
				int index3 = sbExtn.indexOf("<p class=\"data\">", index2);
				try{
					low52w = NF.parse( sbExtn.substring( index3+16, sbExtn.indexOf("<", index3+1) ) ).doubleValue();
				}
				catch(Exception e){
				}
			}
			range52w = low52w +" - "+ high52w;
			try{
				Double low52w_pc = 0.0;
				Double high52w_pc = 0.0;
				Double realTimePrice = NF.parse(realTime.trim()).doubleValue();
				low52w_pc = ((realTimePrice - low52w)/low52w)*100.0;
				high52w_pc = ((realTimePrice - high52w)/high52w)*100.0;
				
				range52w_pc = Utility.round(low52w_pc) +"% / "+ Utility.round(high52w_pc)+"%";
				if( (low52w_pc >= RECOMMENDATION_52W_APPRECIATION_PC && low52w_pc <= RECOMMENDATION_52W_APPRECIATION_PC+5.0)
						&& high52w_pc <= RECOMMENDATION_52W_CORRECTION_PC ){
					range52w_pc += " (HOT STOCK. See Comments*)";
				}
			}
			catch(Exception e){
			}

			String time = "";
			int index4 = sb.indexOf( "var promoTime = " );
			if( index4 != -1 ){
				time = sb.substring( index4+16, sb.indexOf(";", index4));
				time = time.replace("\"", "");
				time = time.split(" ")[1];
			}
			
			System.out.println( String.format(ROW_FORMAT, symbol, realTime.trim(), range.trim(), pcChange.trim(), time.trim(), range52w, range52w_pc) );
		}
	}

}
