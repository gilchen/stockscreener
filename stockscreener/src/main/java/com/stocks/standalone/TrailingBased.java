package com.stocks.standalone;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;

import com.stocks.util.Utility;

public class TrailingBased {
	final Double SL_PC = 2.0;
	//final String GOOGLE_CHART_URL = "http://5.chart.apis.google.com/chart?cht=lc&chs=700x200&chd=t:9.91,9.64,8.71,8.85,8.65,9.00,10.15,11.1,10.5&chg=50,2,1,1&chds=8.65,11.1";
	final String GOOGLE_CHART_URL = "http://chart.apis.google.com/chart?cht=lc&chs=700x200&chd=t:~SERIES1|~SERIES2&chg=50,2,1,1&chds=~MIN,~MAX";

	public static void main(String... args){
		TrailingBased tb = new TrailingBased();
		tb.process();
	}

	private void process(){
		try{
			File[] fileList = new File("C:/Shailesh/Classroom/JSF/int_ref/workspace/trunk/stk/Analysis/Fiddler2/").listFiles( new FileFilter(){
				public boolean accept(File pathname) {
					return pathname.getName().endsWith("txt");
				}
			});

			for(File file : fileList){
				List<Data> dataCollection = populateDataCollection( file.toURL().toString() );
				Map<Integer, TrailingData> trailingDataCollectionMap = populateTrailingDataCollection(dataCollection);
				System.out.println( file.getName()+"<BR>" );
				for(Integer key : trailingDataCollectionMap.keySet()){
					//System.out.println( "--------------> " +key );
					printMap(trailingDataCollectionMap, key);
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	private void printMap(Map<Integer, TrailingData> map, Integer key){
		TrailingData td = map.get(key);
		final List<Data> trailingDataCollection = td.getTrailingDataCollection();
		final List<Data> dataCollection = td.getDataCollection();
		
		final List<Double> trailingOpenCollection = new ArrayList<Double>();
		final List<Double> dataOpenCollection = new ArrayList<Double>();
		for(int i=0; i<dataCollection.size(); i++){
			final Data trailingData = trailingDataCollection.get(i);
			final Data data = dataCollection.get(i);
			
			trailingOpenCollection.add( Utility.round( trailingData.getOpen() ) );
			dataOpenCollection.add( Utility.round( data.getOpen() ) );
		}
		String series1 = StringUtils.join(trailingOpenCollection.toArray(new Double[]{}), ",");
		String series2 = StringUtils.join(dataOpenCollection.toArray(new Double[]{}), ",");
		
		Double min = Math.min( Collections.min(trailingOpenCollection), Collections.min(dataOpenCollection) );
		Double max = Math.max( Collections.max(trailingOpenCollection), Collections.max(dataOpenCollection) );
		
		String googleChartUrl = GOOGLE_CHART_URL
			.replace("~SERIES1", series1)
			.replace("~SERIES2", series2)
			.replace("~MIN", Utility.round(min)+"")
			.replace("~MAX", Utility.round(max)+"");

		System.out.println( "<img src=\"" +googleChartUrl+ "\"><BR><BR>" );
	}

	private Map<Integer, TrailingData> populateTrailingDataCollection(List<Data> dataCollection) throws Exception {
		final Map<Integer, TrailingData> map = new TreeMap<Integer, TrailingData>();
		
		List<Data> trailingDataCollection = new ArrayList<Data>();
		Double slPcValue = dataCollection.get(0).getOpen() + (dataCollection.get(0).getOpen() * (SL_PC/100.0));
		int rawDate = dataCollection.get(0).getRawDate().intValue();
		
		int subListIndex = 0;
		int key = 0;
		for( int i=0; i<dataCollection.size(); i++ ){
			Data data = dataCollection.get(i);
			Double newSlPcValue = data.getOpen() + (data.getOpen() * (SL_PC/100.0));
			if( newSlPcValue < slPcValue ){
				slPcValue = newSlPcValue;
			}
			
			int newRawDate = data.getRawDate().intValue();
			if( newRawDate != rawDate ){
				// Next Day
				map.put((++key), new TrailingData( 
						new ArrayList<Data>( dataCollection.subList(subListIndex, i) ), 
						trailingDataCollection ) );
				subListIndex = i;
				rawDate = newRawDate;
				trailingDataCollection = new ArrayList<Data>();
				slPcValue = newSlPcValue; // 
			}
			
			trailingDataCollection.add( new Data(slPcValue) );
		}
		// For Last Day
		map.put((++key), new TrailingData( 
				new ArrayList<Data>( dataCollection.subList(subListIndex, dataCollection.size()) ), 
				trailingDataCollection ) );
		
		return map;
	}
	
	private List<Data> populateDataCollection( String fileUrl ) throws Exception {
		String fileContent = Utility.getContent( fileUrl );
		fileContent = fileContent.substring( fileContent.indexOf("dataPoints")+13, fileContent.indexOf("labels")-4);
		fileContent = fileContent.replaceAll("[\\\\\"]", "");
		String rows[] = fileContent.split("},");
		fileContent = fileContent.replaceAll("[\\\\\"]", "");
		
		final List<Data> dataCollection = new ArrayList<Data>();
		int i=0;
		for( String row : rows ){
			row = row.substring(row.indexOf(":{")+2);
			dataCollection.add( getDataForRow(row) );
		}
		return dataCollection;
	}
	
	private Data getDataForRow(String row){
		// date:09:33 am,close:20.08,open:20.10,low:20.05,high:20.17,volume:50,884.00,y:52,rawDate:40686.564583
		Data data = null;
		String[] elements = row.split(",");
		Map<String, String> map = new HashMap<String, String>();
		for(String strElement : elements){
			String[] element = strElement.split(":");
			if( element.length > 1 ){
				map.put(element[0], element[1]);
			}
		}
		
		data = new Data(
				new Double( map.get("close").replaceAll(",", "") ),
				new Double( map.get("open").replaceAll(",", "") ),
				new Double( map.get("low").replaceAll(",", "") ),
				new Double( map.get("high").replaceAll(",", "") ),
				new Double( map.get("rawDate").replace("}", "") )
		);
		
		return data;
	}

	class TrailingData{
		private List<Data> trailingDataCollection;
		private List<Data> dataCollection;
		public TrailingData(List<Data> dataCollection, 
				List<Data> trailingDataCollection
				) {
			super();
			this.dataCollection = dataCollection;
			this.trailingDataCollection = trailingDataCollection;
		}
		
		public List<Data> getTrailingDataCollection() {
			return trailingDataCollection;
		}
		public void setTrailingDataCollection(List<Data> trailingDataCollection) {
			this.trailingDataCollection = trailingDataCollection;
		}
		public List<Data> getDataCollection() {
			return dataCollection;
		}
		public void setDataCollection(List<Data> dataCollection) {
			this.dataCollection = dataCollection;
		}
	}
	
	class Data{
		Double close, open, low, high, rawDate;
		
		public Data(Double open){
			this(null, open, null, null, null);
		}
		
		public Data(Double close, Double open, Double low,
				Double high, Double rawDate) {
			super();
			this.close = close;
			this.open = open;
			this.low = low;
			this.high = high;
			this.rawDate = rawDate;
		}
		
		public Double getClose() {
			return close;
		}
		public void setClose(Double close) {
			this.close = close;
		}
		public Double getOpen() {
			return open;
		}
		public void setOpen(Double open) {
			this.open = open;
		}
		public Double getLow() {
			return low;
		}
		public void setLow(Double low) {
			this.low = low;
		}
		public Double getHigh() {
			return high;
		}
		public void setHigh(Double high) {
			this.high = high;
		}
		public Double getRawDate() {
			return rawDate;
		}
		public void setRawDate(Double rawDate) {
			this.rawDate = rawDate;
		}
		@Override
		public String toString() {
			return "Data [rawDate=" +rawDate+ ", close=" + close + ", high=" + high
					+ ", low=" + low + ", open=" + open + "]";
		}
		
	}
}
