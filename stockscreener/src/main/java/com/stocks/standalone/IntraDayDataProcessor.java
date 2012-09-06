package com.stocks.standalone;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
	private StockService stockService;
	
	public StockService getStockService() {
		return stockService;
	}

	@Required
	public void setStockService(StockService stockService) {
		this.stockService = stockService;
	}

	// This URL fetches 1 min data for a period of 1 Month:  "http://www.google.com/finance/getprices?x=NYSE&i=90&p=1M&f=d,c,v,o,h,l&df=cpct&auto=1&q=";
	// This URL fetches 2 min data for a period of 2 Months: "http://www.google.com/finance/getprices?x=NYSE&i=150&p=2M&f=d,c,v,o,h,l&df=cpct&auto=1&q=";
	// This URL fetches 3 min data for a period of 3 Months: "http://www.google.com/finance/getprices?x=NYSE&i=210&p=3M&f=d,c,v,o,h,l&df=cpct&auto=1&q=";
	private static final String GOOGLE_URL = "http://www.google.com/finance/getprices?i=210&p=3M&f=d,c,v,o,h,l&df=cpct&auto=1&q=";
	
	private static final String YAHOO_URL = "http://chartapi.finance.yahoo.com/instrument/1.0/AA/chartdata;type=quote;range=30d/csv";
	// http://markingthemarket.blogspot.com/2010/02/free-intraday-data.html
	// http://www.quantshare.com/sa-426-6-ways-to-download-free-intraday-and-tick-data-for-the-us-stock-market

	public static void main(String... args) throws Exception{
//		System.out.println( new Date(1344259860L * 1000) );
//		Calendar calendar = Calendar.getInstance();
//		calendar.add(Calendar.DATE, -2);
//		System.out.println( calendar.getTimeInMillis()+"->"+calendar.getTime() );
//		if(true) return;
		
		ApplicationContext context = new FileSystemXmlApplicationContext("src/main/resources/applicationContext.xml");
		IntraDayDataProcessor iddp = (IntraDayDataProcessor) context.getBean("intraDayDataProcessor");
		final SymbolMetadata symbolMetadata = iddp.getStockService().getSymbolMetadata("AA");
		System.out.println( symbolMetadata );
		iddp.process( symbolMetadata );
		System.out.println( "Done" );
		System.exit(0);
	}
	
	private void process(SymbolMetadata symbolMetadata) throws Exception{
		final Map<Date, List<IntraDayStructure>> mapG = fetchG( Utility.getContent("file:/C:/Temp/data/AA-3m.txt") );
		//final Map<Date, List<IntraDayStructure>> mapG = fetchG( Utility.getContent( GOOGLE_URL+symbolMetadata.getSymbol() ) );
		//final Map<Date, List<IntraDayStructure>> mapY = fetchY( Utility.getContent("file:/C:/Temp/data/AA-Y.txt") );
		
		//System.out.println( "--> "+ mapG.keySet() );
		final TreeSet<Date> dateSet = new TreeSet<Date>( mapG.keySet() );
		final Date minDate = dateSet.first();
		
//		System.out.println( "Total " +dateSet.size()+ " elements. Min: " +dateSet.first()+ ", Max: " +dateSet.last() );
//		if(true) return;
		
		
		final Calendar cMinDateMinus7 = Calendar.getInstance();
		cMinDateMinus7.setTime( (Date) minDate.clone() );
		cMinDateMinus7.add(Calendar.DATE, -7);
		final List<Nyse> nyseList = getStockService().findStockBySymbolBetweenTradeDates(symbolMetadata.getSymbol(), cMinDateMinus7.getTime(), minDate);
		final List<Nyse> nyseCloseList = getStockService().findStockBySymbolBetweenTradeDates(symbolMetadata.getSymbol(), minDate, dateSet.last());
		
		Double lastClose = nyseList.get(nyseList.size()-2).getClose();
		System.out.println( "Date\tClose\tVolume\tTotal Volume\tNew Price(Considers Total Shrs Outstg)\tAvg Price of Day\tReal Price (Diff Adj w/ Prev.Close)\tBuy Vol\tSell Vol" );
		int index = 0;
		Double realPrice = 0.0;
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
			//for(final IntraDayStructure ids : idsList){
			for(int i=0; i<idsList.size(); i++){
				final IntraDayStructure ids = idsList.get(i);
				//System.out.println( new Date(ids.getTime()) +"\t"+ ids.getClose()+"\t"+ids.getVolume()+"\t\t" );
				totalQty += ids.getVolume();
				totalValue += (ids.getClose() * ids.getVolume());
				
				// Get Total Buy/Sell Volume on a given day.
				Double prevClose = null;
				if( i == 0 ){
					prevClose = lastClose;
				}else{
					prevClose = idsList.get(i-1).getClose();
				}
				if( ids.getClose() > prevClose ){
					bVol += ids.getVolume();
				}else if( ids.getClose() < prevClose ){
					sVol += ids.getVolume();
				}
			}
			final Double avgTradedPrice = totalValue / totalQty.doubleValue();
			
			// Start: Get Real Price of Day
			if( nyse.getPrevious() != null ){
				realPrice = nyse.getPrevious().getClose() + (avgTradedPrice - nyse.getPrevious().getClose());
			}
			// End.
			
			// Adjustments:
			Long diff = nyse.getVolume() - totalQty;
			totalQty += diff;
			totalValue += (avgTradedPrice * diff.doubleValue());
			//
			
			final Long untradedQty = symbolMetadata.getExpandedSharesOutstanding() - totalQty;
			final Double untradedValue = lastClose * untradedQty.doubleValue();

			final Double newTotalValue = untradedValue + totalValue;
			Double newPrice = newTotalValue / symbolMetadata.getExpandedSharesOutstanding().doubleValue();
			lastClose = nyse.getClose();

			System.out.println( new Date(nyse.getNysePK().getTradeDate().getTime())+"\t"+ nyse.getClose()+"\t" +"\t"+nyse.getVolume()+"\t"+newPrice+"\t"+avgTradedPrice+"\t"+realPrice+"\t"+bVol+"\t"+sVol );
		}
		
/*		
		Long bVol = 0L;
		Long sVol = 0L;
		
		final List<IntraDayStructure> allIDS = new LinkedList<IntraDayStructure>();
		
		for(final List<IntraDayStructure> list : mapG.values()){
			allIDS.addAll(list);
		}
		
		Collections.sort(allIDS, new Comparator<IntraDayStructure>() {
			public int compare(IntraDayStructure o1, IntraDayStructure o2) {
				return o1.getTime().compareTo(o2.getTime());
			}
		});
		
		for(int i=0; i<allIDS.size()-1; i++){
			if( allIDS.get(i).getClose() < allIDS.get(i+1).getClose() ){
				bVol += allIDS.get(i+1).getVolume();
			}else if( allIDS.get(i).getClose() > allIDS.get(i+1).getClose() ){
				sVol += allIDS.get(i+1).getVolume();
			}
		}
		System.out.println( "bVol: " +Utility.getFormattedInteger(bVol)+ ", sVol: "+ Utility.getFormattedInteger(sVol) );
		System.out.println( "Diff: " +Utility.getFormattedInteger(bVol-sVol)+ " (Negative value means short position)" );
*/
		
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
		System.out.println( "G" );
		final int INDX_D = 0;
		final int INDX_O = 1;
		final int INDX_H = 2;
		final int INDX_L = 3;
		final int INDX_C = 4;
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
				Double close = Double.parseDouble(cols[INDX_C]);
				Long volume = Long.parseLong(cols[INDX_V]);
				
				if( previousDate == null || !Utility.areDatesEqual(previousDate, date) ){
					list = new LinkedList<IntraDayStructure>();
					previousDate = date;
					map.put(date, list);
				}
				list.add(new IntraDayStructure(date.getTime(), close, volume));
				
				bDataStarted = true;
			}else if( bDataStarted ){
				String[] cols = row.split(",");
				//Date adjustedDate = new Date( date.getTime() +(Long.parseLong(cols[INDX_D]) * 60 * 1000 ) );
				Date adjustedDate = new Date( date.getTime() +Long.parseLong(cols[INDX_D]) );
				Double close = Double.parseDouble(cols[INDX_C]);
				Long volume = Long.parseLong(cols[INDX_V]);
				
				list.add(new IntraDayStructure(adjustedDate.getTime(), close, volume));
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
	private Long time;
	private Double close;
	private Long volume;
	
	public IntraDayStructure(Long time, Double close, Long volume) {
		super();
		this.time = time;
		this.close = close;
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
	public Long getVolume() {
		return volume;
	}
	public void setVolume(Long volume) {
		this.volume = volume;
	}

	@Override
	public String toString() {
		return "IntraDayStructure [time=" + new Date(time) + ", close=" + close
				+ ", volume=" + volume + "]";
	}
}
