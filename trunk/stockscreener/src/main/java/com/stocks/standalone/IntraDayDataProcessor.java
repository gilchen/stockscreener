package com.stocks.standalone;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.josql.Query;
import org.josql.QueryResults;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.stocks.model.Nyse;
import com.stocks.model.SymbolMetadata;
import com.stocks.service.StockService;
import com.stocks.util.Utility;

public class IntraDayDataProcessor {
	//http://1.chart.apis.google.com/chart?cht=lc&chs=700x200&chd=t:5.2,5.5,5.53,5.6,5.77,5.8,5.99,5.95,6.0,5.88,5.96,5.82,5.71,5.8,5.81|6.27,6.7,6.74,6.73,6.66,6.61,6.56,6.62,6.63,6.52,6.24,6.2,6.23,6.7,7.12&chg=0,2,1,1&chds=4.18,7.12&chco=FF0000,00FF00
	private static final String GOOGLE_CHART_URL = "http://1.chart.apis.google.com/chart?cht=lc&chs=700x200&chco=0000FF,000000&chg=1,-1,1,1&chm=o,0000FF,0,-1,5|o,000000,1,-1,5|H,FF0000,0,0,1&chd=t:~CLOSE_DATA|~VOL_DATA&chds=~MIN,~MAX";
	
	private StockService stockService;
	
	public StockService getStockService() {
		return stockService;
	}

	@Required
	public void setStockService(StockService stockService) {
		this.stockService = stockService;
	}

	// This URL fetches 1 min data for a period of 1 Month:  "http://www.google.com/finance/getprices?i=90&p=1M&f=d,c,v,o,h,l&df=cpct&auto=1&q=";
	// This URL fetches 2 min data for a period of 2 Months: "http://www.google.com/finance/getprices?i=150&p=2M&f=d,c,v,o,h,l&df=cpct&auto=1&q=";
	// This URL fetches 3 min data for a period of 3 Months: "http://www.google.com/finance/getprices?i=210&p=3M&f=d,c,v,o,h,l&df=cpct&auto=1&q=";
	//private static final String GOOGLE_URL = "http://www.google.com/finance/getprices?i=90&p=1M&f=d,c,v,o,h,l&df=cpct&auto=1&q=";
	//private static final String GOOGLE_URL = "http://www.google.com/finance/getprices?i=150&p=2M&f=d,c,v,o,h,l&df=cpct&auto=1&q=";
	private static final String GOOGLE_URL = "http://www.google.com/finance/getprices?i=210&p=3M&f=d,c,v,o,h,l&df=cpct&auto=1&q=";
	
	private static final String YAHOO_URL = "http://chartapi.finance.yahoo.com/instrument/1.0/AA/chartdata;type=quote;range=30d/csv";
	// http://markingthemarket.blogspot.com/2010/02/free-intraday-data.html
	// http://www.quantshare.com/sa-426-6-ways-to-download-free-intraday-and-tick-data-for-the-us-stock-market

	public static void main(String... args) throws Exception{
		ApplicationContext context = new FileSystemXmlApplicationContext("src/main/resources/applicationContext.xml");
		IntraDayDataProcessor iddp = (IntraDayDataProcessor) context.getBean("intraDayDataProcessor");
		
		//String symbols[] = new String[]{"GNOM", "HDY", "DCTH", "LDK", "ERY", "EDZ", "DVR", "ACW", "STP", "VXZ", "SVNT", "OSG", "VIXY", "GNK", "SCMR", "TVIX", "JRCC", "AUMN", "COCO", "PVA", "RSH", "FOE", "CECO", "FRO", "XIV", "TZOO", "TSL", "MTG", "DANG", "SVU", "DMND", "GDOT", "TC", "KCG", "ITMN", "WFR", "RMBS", "AKS", "DNDN", "AGQ", "GFA", "QSII", "APKT", "PNG", "MCP", "NIHD", "RENN", "SFUN", "HK", "ZNGA", "PLCM", "ANR", "FSLR", "BID", "ALU", "MTL", "EDU", "TROX", "GRPN", "GMCR", "RIMM"};
		String symbols[] = new String[]{"ALU", "ZNGA"};
		
		final Set<String> set = new HashSet<String>();
		set.addAll( Arrays.asList(symbols) );
		
		StringBuilder sb = new StringBuilder();
		for( String symbol : set ){
			System.out.println( "Processing " +symbol );
			final SymbolMetadata symbolMetadata = iddp.getStockService().getSymbolMetadata(symbol);
			try{
				iddp.process(symbolMetadata, sb);
			}
			catch(Exception e){
				e.printStackTrace();
				System.out.println( sb.toString() );
			}
		}
		
		PrintWriter writer = new PrintWriter( "C:/Temp/ForSrid/intra/rpts/report.html" );
		try{
			writer.println( "<HTML>" );
			writer.println( "<head>");
			writer.println( "<script>");
			writer.println( "function summarize(){");
			writer.println( "	totalPos = 0;");
			writer.println( "	totalNeg = 0;");
			writer.println( "	frm = document.forms[0];");
			writer.println( "   positiveStocks = \"\";");
			writer.println( "	for(i=0; i<frm.elements.length; i++ ){");
			writer.println( "		elem = frm.elements[i];");
			writer.println( "		if( elem.name == \"positive\" && elem.checked ){");
			writer.println( "			totalPos++;");
			writer.println( "           positiveStocks += elem.value +\", \";");
			writer.println( "		}else if( elem.name == \"negative\" && elem.checked ){");
			writer.println( "			totalNeg++;");
			writer.println( "		}");
			writer.println( "	}");
			writer.println( "	frm.totalPos.value = totalPos;");
			writer.println( "	frm.totalNeg.value = totalNeg;");
			writer.println( "   frm.positiveStocks.value = positiveStocks;");
			writer.println( "");
			writer.println( "}");
			writer.println( "</script>");
			writer.println( "</head>");
			
			writer.println("<BODY><PRE><form>");
			writer.println("TotalPos: <input type=text name=\"totalPos\"> <input type=text name=\"positiveStocks\">");
			writer.println("TotalNeg: <input type=text name=\"totalNeg\">");
			
			writer.println( sb.toString() );
			writer.println( "</form></PRE></BODY></HTML>" );
		}
		catch(Exception e){
			e.printStackTrace();
			System.out.println( sb.toString() );
		}
		finally{
			if(writer != null){
				writer.close();
			}
		}
		

//		final SymbolMetadata symbolMetadata = iddp.getStockService().getSymbolMetadata("SYNC");
//		iddp.process(symbolMetadata, null);

		System.out.println( "Done" );
		System.exit(0);
	}

	private void process(SymbolMetadata symbolMetadata, StringBuilder sb) throws Exception{
		//final Map<Date, List<IntraDayStructure>> mapG = fetchG( Utility.getContent("file:/C:/Temp/ForSrid/intra/rpts/" +symbolMetadata.getSymbol()+ ".dat") );
		final Map<Date, List<IntraDayStructure>> mapG = fetchG( Utility.getContent( GOOGLE_URL+symbolMetadata.getSymbol() ) );
		
		final TreeSet<Date> dateSet = new TreeSet<Date>( mapG.keySet() );
		final Date minDate = dateSet.first();
		
		final Calendar cMinDateMinus7 = Calendar.getInstance();
		cMinDateMinus7.setTime( (Date) minDate.clone() );
		cMinDateMinus7.add(Calendar.DATE, -7);
		final List<Nyse> nyseList = getStockService().findStockBySymbolBetweenTradeDates(symbolMetadata.getSymbol(), cMinDateMinus7.getTime(), minDate);
		final List<Nyse> nyseCloseList = getStockService().findStockBySymbolBetweenTradeDates(symbolMetadata.getSymbol(), minDate, dateSet.last());
		
		Double lastClose = nyseList.get(nyseList.size()-2).getClose();
		int index = 0;
		
		final List<Long> volumeList = new ArrayList<Long>();
		for(final Date tradeDate : dateSet){
			if( index == nyseCloseList.size() ){
				break;
			}
			
			final Nyse nyse = nyseCloseList.get(index++);
			
			final List<IntraDayStructure> idsList = mapG.get(tradeDate);
			Collections.sort(idsList, new Comparator<IntraDayStructure>() {
				public int compare(IntraDayStructure o1, IntraDayStructure o2) {
					return o1.getTime().compareTo(o2.getTime());
				}
			});
			
			Long bVol = 0L;
			Long sVol = 0L;
			Long totalQty = 0L;
			Double totalValue = 0.0;
			for(int i=0; i<idsList.size(); i++){
				final IntraDayStructure ids = idsList.get(i);
				totalQty += ids.getVolume();
				totalValue += (ids.getClose() * ids.getVolume());
				
				// Get Total Buy/Sell Volume on a given day.
				Double prevClose = null;
				if( i == 0 ){
					prevClose = lastClose;
				}else{
					prevClose = idsList.get(i-1).getClose();
				}
				if( ids.getClose() >= prevClose ){
					bVol += ids.getVolume();
				}else if( ids.getClose() < prevClose ){
					sVol += ids.getVolume();
				}
			}
			
			volumeList.add(bVol-sVol);
		}
		
		String CHART_URL = GOOGLE_CHART_URL;

		QueryResults qr = Query.parseAndExec("SELECT min(close), max(close) FROM com.stocks.model.Nyse", nyseCloseList);
		final List<List<Double>> resultsMinMax = qr.getResults();
		final Double minClose = resultsMinMax.get(0).get(0);
		final Double maxClose = resultsMinMax.get(0).get(1);
		
		// 1: Get Close Data
		qr = Query.parseAndExec("SELECT close FROM com.stocks.model.Nyse", nyseCloseList);
		final List<Double> results = qr.getResults();
		String sCloseData = results.toString();
		sCloseData = sCloseData.replaceAll("\\[|\\]", "").replaceAll(" ", "");
		CHART_URL = CHART_URL.replace("~CLOSE_DATA", "0,"+sCloseData);

		// 2: Get Volume Data
		qr = Query.parseAndExec("SELECT longValue/" +(Collections.max(volumeList)/(maxClose/3.0))+ " FROM java.lang.Long", volumeList);
		final List<Double> volumeResults = qr.getResults();
		String sVolData = volumeResults.toString();
		sVolData = sVolData.replaceAll("\\[|\\]", "").replaceAll(" ", "");
		CHART_URL = CHART_URL.replace("~VOL_DATA", "0,"+sVolData);
		
		Double min = 0.0;
		for( String s : Arrays.asList( sVolData.split(",", -1) ) ){
			Double d = Double.parseDouble(s);
			if( d < min ){
				min = d;
			}
		}
		
		CHART_URL = CHART_URL.replace("~MIN", min+"");
		CHART_URL = CHART_URL.replace("~MAX", maxClose.toString());
		
		sb.append( "<input type=checkbox onclick=\"summarize()\" name=positive value=\"" +symbolMetadata.getSymbol()+ "\"> <input type=checkbox onclick=\"summarize()\" name=negative value=\"" +symbolMetadata.getSymbol()+ "\"> " );
		sb.append( "<a href='http://www.google.com/finance?q=" +symbolMetadata.getSymbol()+ "' target='_new'><img border=\"0\" title=\"" +symbolMetadata.getSymbol()+ ": Range (" +minClose+" - "+maxClose+ ")\" src=\"" +CHART_URL+"\"></a>\n\n" );
	}

/*	
	private Map<Date, List<IntraDayStructure>> fetchY(String data) throws Exception{
		System.out.println( "Y" );
		final int INDX_D = 0;
		final int INDX_C = 1;
		final int INDX_H = 2;
		final int INDX_L = 3;
		final int INDX_O = 4;
		final int INDX_V = 5;
		
		final Long DATE_OFFSET = 1000L;
		final String[] rows = data.split("\n");
		final Map<Date, List<IntraDayStructure>> map = new TreeMap<Date, List<IntraDayStructure>>(new Comparator<Date>() {
			public int compare(Date o1, Date o2) {
				return o1.compareTo(o2);
			}
		});
		List<IntraDayStructure> list = null;
		boolean bDataStarted = false;
		Date previousDate = null;
		
		for( String row : rows ){
			if( row.startsWith("volume") ){
				bDataStarted = true;
			}else if( bDataStarted ){
				String[] cols = row.split(",");
				Date date = new Date( Long.parseLong(cols[INDX_D]) * 1000 );
				Double close = Double.parseDouble(cols[INDX_C]);
				Long volume = Long.parseLong(cols[INDX_V]);
				if( previousDate == null || !Utility.areDatesEqual(previousDate, date) ){
					list = new LinkedList<IntraDayStructure>();
					map.put(date, list);
					previousDate = date;
				}
				
				list.add(new IntraDayStructure(date.getTime(), close, volume));
			}
		}
		
		for(final Date d : map.keySet() ){
//			final QueryResults qrMinMax = Query.parseAndExec("SELECT min(close), max(close) FROM com.stocks.standalone.IntraDayStructure LIMIT 1,1", map.get(d));
//			final List<List<Double>> resultMinMax = qrMinMax.getResults();
//			Double min = null;
//			Double max = null;
//			
//			try{
//				min = resultMinMax.get(0).get(0);
//				max = resultMinMax.get(0).get(1);
//			}
//			catch(Exception e){
//				System.out.println( "Exception " +e.getMessage() );
//			}
//			System.out.println( d +" -> min: "+ min+ ", max: " +max );
			final QueryResults qrMinMax = Query.parseAndExec("SELECT sum(volume) FROM com.stocks.standalone.IntraDayStructure LIMIT 1,1", map.get(d));
			final List<List<Double>> resultMinMax = qrMinMax.getResults();
			Double totalVolume = null;
			
			try{
				totalVolume = resultMinMax.get(0).get(0);
			}
			catch(Exception e){
				System.out.println( "Exception " +e.getMessage() );
			}
			System.out.println( d +" -> totalVolume:\t"+totalVolume.longValue()+ "\t, total Elements:\t" +map.get(d).size() );
		}
		return map;
	}
*/

	private Map<Date, List<IntraDayStructure>> fetchG(String data) throws Exception{
		//System.out.println( "G" );
		int index = 0;
		final int INDX_D = 0;
		final int INDX_C = 1;
		final int INDX_H = 2;
		final int INDX_L = 3;
		final int INDX_O = 4;
		final int INDX_V = 5;
		
		final Long DATE_OFFSET = 1000L;
		final String[] rows = data.split("\n");
		final Map<Date, List<IntraDayStructure>> map = new TreeMap<Date, List<IntraDayStructure>>(new Comparator<Date>() {
			public int compare(Date o1, Date o2) {
				return o1.compareTo(o2);
			}
		});
		List<IntraDayStructure> list = null;
		boolean bDataStarted = false;
		Date date = null, previousDate = null;
		for( String row : rows ){
			if( row.startsWith("a") ){
				String[] cols = row.split(",");
				date = new Date( Long.parseLong(cols[INDX_D].substring(1)) * DATE_OFFSET );
				
				if( previousDate == null || !Utility.areDatesEqual(previousDate, date) ){
					list = new LinkedList<IntraDayStructure>();
					previousDate = date;
					map.put(date, list);
				}

				final Double close = Double.parseDouble(cols[INDX_C]);
				final Double high  = Double.parseDouble(cols[INDX_H]);
				final Double low   = Double.parseDouble(cols[INDX_L]);
				final Double open  = Double.parseDouble(cols[INDX_O]);
				final Long volume  = Long.parseLong(cols[INDX_V]);

				list.add(new IntraDayStructure(++index, date.getTime(), close, high, low, open, volume));

				bDataStarted = true;
			}else if( bDataStarted ){
				String[] cols = row.split(",");
				//Date adjustedDate = new Date( date.getTime() +(Long.parseLong(cols[INDX_D]) * 60 * 1000 ) );
				Date adjustedDate = new Date( date.getTime() +Long.parseLong(cols[INDX_D]) );
				final Double close = Double.parseDouble(cols[INDX_C]);
				final Double high  = Double.parseDouble(cols[INDX_H]);
				final Double low   = Double.parseDouble(cols[INDX_L]);
				final Double open  = Double.parseDouble(cols[INDX_O]);
				final Long volume  = Long.parseLong(cols[INDX_V]);

				list.add(new IntraDayStructure(++index, adjustedDate.getTime(), close, high, low, open, volume));
			}
		}
		/*
		for(final Date d : map.keySet() ){
//			final QueryResults qrMinMax = Query.parseAndExec("SELECT min(close), max(close) FROM com.stocks.standalone.IntraDayStructure LIMIT 1,1", map.get(d));
//			final List<List<Double>> resultMinMax = qrMinMax.getResults();
//			Double min = null;
//			Double max = null;
//			
//			try{
//				min = resultMinMax.get(0).get(0);
//				max = resultMinMax.get(0).get(1);
//			}
//			catch(Exception e){
//				System.out.println( "Exception " +e.getMessage() );
//			}
//			System.out.println( d +" -> min: "+ min+ ", max: " +max );
			
			final QueryResults qrMinMax = Query.parseAndExec("SELECT sum(volume) FROM com.stocks.standalone.IntraDayStructure LIMIT 1,1", map.get(d));
			final List<List<Double>> resultMinMax = qrMinMax.getResults();
			Double totalVolume = null;
			
			try{
				totalVolume = resultMinMax.get(0).get(0);
			}
			catch(Exception e){
				System.out.println( "Exception " +e.getMessage() );
			}
			System.out.println( d +" -> totalVolume:\t"+totalVolume.longValue()+ "\t, total Elements:\t" +map.get(d).size() );
		}
		*/
		return map;
	}
}

class IntraDayStructure{
	private int index;
	private Long time;
	private Double close;
	private Double high;
	private Double low;
	private Double open;
	private Long volume;
	
	public IntraDayStructure(int index, Long time, Double close, Double high,
			Double low, Double open, Long volume) {
		super();
		this.index = index;
		this.time = time;
		this.close = close;
		this.high = high;
		this.low = low;
		this.open = open;
		this.volume = volume;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public Double getClose() {
		return close;
	}

	public void setClose(Double close) {
		this.close = close;
	}

	public Double getHigh() {
		return high;
	}

	public void setHigh(Double high) {
		this.high = high;
	}

	public Double getLow() {
		return low;
	}

	public void setLow(Double low) {
		this.low = low;
	}

	public Double getOpen() {
		return open;
	}

	public void setOpen(Double open) {
		this.open = open;
	}

	public Long getVolume() {
		return volume;
	}

	public void setVolume(Long volume) {
		this.volume = volume;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public String toString() {
		return "IntraDayStructure [index=" + index + ", time=" + time
				+ ", close=" + close + ", high=" + high + ", low=" + low
				+ ", open=" + open + ", volume=" + volume + "]";
	}
}
