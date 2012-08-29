package com.stocks.standalone;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.josql.Query;
import org.josql.QueryResults;

import com.stocks.util.Utility;

public class IntraDayDataProcessor {
	// http://www.google.com/finance/getprices?i=60&p=10d&f=d,o,h,l,c,v&df=cpct&q=IBM
	private static final String GOOGLE_URL = "http://www.google.com/finance/getprices?i=60&p=10d&f=d,o,h,l,c,v&df=cpct&q=";
	public static void main(String... args) throws Exception{
		IntraDayDataProcessor iddp = new IntraDayDataProcessor();
		iddp.process(args[0]);
	}
	
	private void process(String symbol) throws Exception{
		//final Map<Date, List<IntraDayStructure>> mapG = fetchG( Utility.getContent("file:/C:/Temp/data/AA-G.txt") );
		final Map<Date, List<IntraDayStructure>> mapG = fetchG( Utility.getContent( GOOGLE_URL+symbol ) );
		//final Map<Date, List<IntraDayStructure>> mapY = fetchY( Utility.getContent("file:/C:/Temp/data/AA-Y.txt") );
		
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
	}
	
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
		Date date = null;
		for( String row : rows ){
			if( row.startsWith("a") ){
				String[] cols = row.split(",");
				date = new Date( Long.parseLong(cols[INDX_D].substring(1)) * DATE_OFFSET );
				Double close = Double.parseDouble(cols[INDX_C]);
				Long volume = Long.parseLong(cols[INDX_V]);
				
				list = new LinkedList<IntraDayStructure>();
				map.put(date, list);
				list.add(new IntraDayStructure(date.getTime(), close, volume));
				
				bDataStarted = true;
			}else if( bDataStarted ){
				String[] cols = row.split(",");
				Date adjustedDate = new Date( date.getTime() +(Long.parseLong(cols[INDX_D]) * 60 * 1000 ) );
				Double close = Double.parseDouble(cols[INDX_C]);
				Long volume = Long.parseLong(cols[INDX_V]);
				
				list.add(new IntraDayStructure(adjustedDate.getTime(), close, volume));
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
