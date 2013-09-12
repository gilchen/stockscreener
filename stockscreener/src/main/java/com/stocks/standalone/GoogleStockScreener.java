package com.stocks.standalone;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.stocks.util.Utility;

public class GoogleStockScreener {
//	final static String MKT_CAP_FROM = "750000000";
//	final static String MKT_CAP_TO   = "9723390000000";
//	final static String DIV_YIELD_FROM = "5%";
//	final static String DIV_YIELD_TO = "100%";
//	
//	final static String GOOGLE_URL = "http://www.google.com/finance?output=json&start=0&num=5000&noIL=1&q=[currency%20%3D%3D%20%22USD%22%20%26%20%28%28exchange%20%3D%3D%20%22NYSEARCA%22%29%20%7C%20%28exchange%20%3D%3D%20%22NYSEAMEX%22%29%20%7C%20%28exchange%20%3D%3D%20%22NYSE%22%29%20%7C%20%28exchange%20%3D%3D%20%22NASDAQ%22%29%29%20%26%20%28market_cap%20%3E%3D%20" +MKT_CAP_FROM+ "%29%20%26%20%28market_cap%20%3C%3D%20" +MKT_CAP_TO+ "%29%20%26%20%28dividend_yield%20%3E%3D%20" +DIV_YIELD_FROM+ "29%20%26%20%28dividend_yield%20%3C%3D%20" +DIV_YIELD_TO+ "29%20]&restype=company&ei=CrpHUditA4myqgHD_AE";

	public static void main(final String... args) throws Exception{
		final Properties properties = new Properties();
		try{
			properties.load( IntraDayDataProcessor.class.getResourceAsStream(args[0]) );
		}
		catch(Exception e){
			System.out.println("Usage: GoogleStockScreener <properties file>.\nException in loading properties: " +args);
			e.printStackTrace();
			return;
		}

		final String content = Utility.getContent( properties.getProperty("google.stock.screener.url") );
		final List<String> symbols = new ArrayList<String>();
		for( String line : content.split("\n") ){
			if( line.startsWith("\"ticker\"") ){
				String symbol = line.substring( 12, line.indexOf("\",") );
				if( symbol.indexOf("-") == -1 && symbol.indexOf(".") == -1 ){
					symbols.add( symbol );
				}
			}
		}
		System.out.println( "Total " +symbols.size()+ " symbols pulled from Google Stock Screener." );
		System.out.println( symbols );
		if(true) return;
		
		final IntraDayDataProcessor iddp = new IntraDayDataProcessor();
		iddp.properties.setProperty("symbols", symbols.toString().replaceAll("\\[|\\]", ""));
		iddp.properties.setProperty("qualification.max.vxc.greater.than", properties.getProperty("qualification.max.vxc.greater.than"));
		iddp.properties.setProperty("qualification.max.vol.times.of.average.vol", properties.getProperty("qualification.max.vol.times.of.average.vol"));
		iddp.properties.setProperty("rpt.path", properties.getProperty("rpt.path"));
		iddp.properties.setProperty("rpt.path.for.qualified", properties.getProperty("rpt.path.for.qualified"));
		iddp.properties.setProperty("use.cache", properties.getProperty("use.cache"));
		
		iddp.main(args);
	}
}
