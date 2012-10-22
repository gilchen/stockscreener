package com.stocks.standalone;

import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Arrays;
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

import com.stocks.util.Utility;

public class IntraDayDataProcessor {
	private static final String CHART_HTML = "<applet code='com.objectplanet.chart.ChartApplet' archive='chart.jar' width='700' height='350'>\n<param name='chart' value='line'>\n<param name='chartTitle' value='~SYMBOL'>\n<param name='sampleValues_0' value='~CLOSE_DATA'>\n<param name='sampleValues_1' value='~VOLUME_DATA'>\n<param name='sampleLabels' value='~DATES_DATA'>\n<param name='seriesRange_0' value='2'>\n<param name='sampleLabelsOn' value='true'>\n<param name='sampleLabelStyle' value='floating'>\n<param name='floatingLabelFont' value='Verdana, plain, 10'>\n<param name='sampleColors' value='blue, red'>\n<param name='sampleHighlightOn' value='true'>\n<param name='sampleHighlightStyle' value='circle_opaque'>\n<param name='sampleHighlightSize' value='6'>\n<param name='rangeColor' value='red'>\n<param name='rangeColor_2' value='blue'>\n<param name='seriesCount' value='2'>\n<param name='valueLabelsOn' value='true'>\n<param name='valueLabelStyle' value='floating'>\n<param name='valueLinesOn' value='true'>\n<param name='legendOn' value='true'>\n<param name='legendPosition' value='top'>\n<param name='legendLabels' value='Close,Volume'>\n<param name='rangeOn_2' value='true'>\n<param name='rangePosition' value='right'>\n<param name='rangePosition_2' value='left'>\n<param name='rangeAxisLabel' value='Volume'>\n<param name='rangeAxisLabelFont' value='Verdana, bold, 16'>\n<param name='rangeAxisLabelAngle' value='90'>\n<param name='rangeAxisLabel_2' value='Close'>\n<param name='rangeAxisLabelAngle_2' value='270'>\n<param name='rangeLabelPrefix_2' value='$'>\n<param name='multiSeriesOn' value='true'>\n<param name='rangeDecimalCount' value='0'>\n<param name='rangeDecimalCount_2' value='2'>\n<param name='sampleDecimalCount' value='2'>\n<param name='sampleDecimalCount_2' value='0'>\n<param name='chartBackground' value='#DADAFF'>\n</applet>";

	// This URL fetches 1 min data for a period of 1 Month:  "http://www.google.com/finance/getprices?i=90&p=1M&f=d,c,v,o,h,l&df=cpct&auto=1&q=";
	// This URL fetches 2 min data for a period of 2 Months: "http://www.google.com/finance/getprices?i=150&p=2M&f=d,c,v,o,h,l&df=cpct&auto=1&q=";
	// This URL fetches 3 min data for a period of 3 Months: "http://www.google.com/finance/getprices?i=210&p=3M&f=d,c,v,o,h,l&df=cpct&auto=1&q=";
	//private static final String GOOGLE_URL = "http://www.google.com/finance/getprices?i=90&p=1M&f=d,c,v,o,h,l&df=cpct&auto=1&q=";
	//private static final String GOOGLE_URL = "http://www.google.com/finance/getprices?i=150&p=2M&f=d,c,v,o,h,l&df=cpct&auto=1&q=";
	private static final String GOOGLE_URL_INTRA_DAY = "http://www.google.com/finance/getprices?i=210&p=3M&f=d,c,v,o,h,l&df=cpct&auto=1&q=";
	private static final String GOOGLE_URL_DAY_CLOSE = "http://www.google.com/finance/getprices?p=3M&f=d,c,v,o,h,l&df=cpct&auto=1&q=";
	
	public static void main(String... args) throws Exception{
		String symbols[] = null;
		if( args.length == 0 ){
			System.out.println( "Usage: java IntraDayDataProcessor \"AA,MMM\"" );
			return;
		}else{
			symbols = args[0].split(",");
		}
		IntraDayDataProcessor iddp = new IntraDayDataProcessor();
		//String symbols[] = new String[]{"AA"};
		
		final Set<String> set = new HashSet<String>();
		set.addAll( Arrays.asList(symbols) );
		iddp.generateReport(set);
		
		System.out.println( "Done" );
	}
	
	private void generateReport(final Set<String> symbols) throws Exception{
		StringBuilder sb = new StringBuilder();
		for( String symbol : symbols ){
			System.out.println( "Processing " +symbol );
			try{
				process(symbol.trim(), sb);
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
			
			writer.println("<BODY><PRE><B>Description</B>: This report displays two series of data for each symbol viz. Close and Volume.\nWhen you hover over the round marks on each series it will display the data they represent.\nVolume is actually Buy/Sell Volume in the sense that every rise in intraday data is considered to be buy and every fall is considered sell.\nLook for spikes in Buy volume. This should be at least 4 times average buy volume.\nIf there are small or no sell volumes recently and a sudden buy volume comes, then this is a very bullish sign.\nDo not consider symbols where you see many sell instances recently.\nAlso, there are 2 checkboxes provided by each chart. \nThey are for marking Positive/Negative outcome of the strategy as exceptions are always there.\nThe textboxes on the top will summarize this information for you with Positive Symbols displayed as comma-separated string.\nIf you do not see charts, then copy the <U>chart.jar</U> to current folder where this html file resides.<form>");
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
		
	}

	private void process(String symbol, StringBuilder sb) throws Exception{
//		final Map<Date, List<IntraDayStructure>> mapGIntraDay = fetchG( Utility.getContent( "file:/C:/Temp/ForSrid/intra/rpts/Intra"+symbol ), true );
//		final Map<Date, List<IntraDayStructure>> mapGDayClose = fetchG( Utility.getContent( "file:/C:/Temp/ForSrid/intra/rpts/"+symbol ), false );
		
		final Map<Date, List<IntraDayStructure>> mapGIntraDay = fetchG( Utility.getContent( GOOGLE_URL_INTRA_DAY+symbol ), true );
		final Map<Date, List<IntraDayStructure>> mapGDayClose = fetchG( Utility.getContent( GOOGLE_URL_DAY_CLOSE+symbol ), false );
		
		final TreeSet<Date> dateSet = new TreeSet<Date>( mapGIntraDay.keySet() );
		
		final TreeMap<String, IntraDayStructure> mDayClose = new TreeMap<String, IntraDayStructure>(new Comparator(){
			public int compare(Object o1, Object o2) {
				Date d1 = null, d2 = null;
				try{
					d1 = Utility.getDate(o1.toString());
					d2 = Utility.getDate(o2.toString());
					
					return d1.compareTo(d2);
				}
				catch(Exception e){
					e.printStackTrace();
				}

				return 0;
			}
		});
		for(Date d : mapGDayClose.keySet()){
			mDayClose.put(Utility.getStrDate(d), mapGDayClose.get(d).get(0));
		}
		
		final List<Long> volumeList = new ArrayList<Long>();
		final StringBuilder sbDates = new StringBuilder();
		final StringBuilder sbCloseData = new StringBuilder();
		for(final Date tradeDate : dateSet){
			final IntraDayStructure idsClose = mDayClose.get(Utility.getStrDate(tradeDate));
			
			final List<IntraDayStructure> idsList = mapGIntraDay.get(tradeDate);
			Collections.sort(idsList, new Comparator<IntraDayStructure>() {
				public int compare(IntraDayStructure o1, IntraDayStructure o2) {
					return o1.getTime().compareTo(o2.getTime());
				}
			});
			
			Long bVol = 0L;
			Long sVol = 0L;
			for(int i=0; i<idsList.size(); i++){
				final IntraDayStructure ids = idsList.get(i);
				// Get Total Buy/Sell Volume on a given day.
				Double prevClose = null;
				if( i == 0 ){
					final Map.Entry<String, IntraDayStructure> me = mDayClose.lowerEntry( Utility.getStrDate(tradeDate) );
					if( me != null ){
						prevClose = me.getValue().getClose();
					}else{
						prevClose = idsList.get(i).getClose();
					}
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
			sbDates.append(",").append(Utility.getStrDate(tradeDate));
			sbCloseData.append(",").append( idsClose != null ? idsClose.getClose() : idsList.get(idsList.size()-1).getClose() );
		}
	
		String CHART_HTML_DATA = CHART_HTML;

		CHART_HTML_DATA = CHART_HTML_DATA.replace("~SYMBOL", symbol);
		CHART_HTML_DATA = CHART_HTML_DATA.replace("~CLOSE_DATA", sbCloseData.substring(1));
		CHART_HTML_DATA = CHART_HTML_DATA.replace("~VOLUME_DATA", volumeList.toString().replaceAll("\\[|\\]", "").replaceAll(" ", ""));
		CHART_HTML_DATA = CHART_HTML_DATA.replace("~DATES_DATA", sbDates.substring(1));
		


		sb.append( "P<input type=checkbox onclick=\"summarize()\" name=positive value=\"" +symbol+ "\"> N<input type=checkbox onclick=\"summarize()\" name=negative value=\"" +symbol+ "\"> " );
		sb.append( "<a href='http://www.google.com/finance?q=" +symbol+ "' target='_new'>" +symbol+ "</a> " );
		sb.append( CHART_HTML_DATA +"\n\n" );
	}

	private Map<Date, List<IntraDayStructure>> fetchG(String data, boolean isIntraDay) throws Exception{
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
				Date adjustedDate = null;
				if( isIntraDay ){
					adjustedDate = new Date( date.getTime() +Long.parseLong(cols[INDX_D]) );
				}else{
					adjustedDate = new Date( date.getTime() +(Long.parseLong(cols[INDX_D]) * 60 * 1000 * 60 * 24 ) );
				}
				
				if( previousDate == null || !Utility.areDatesEqual(previousDate, adjustedDate) ){
					list = new LinkedList<IntraDayStructure>();
					previousDate = adjustedDate;
					map.put(adjustedDate, list);
				}
				
				
				final Double close = Double.parseDouble(cols[INDX_C]);
				final Double high  = Double.parseDouble(cols[INDX_H]);
				final Double low   = Double.parseDouble(cols[INDX_L]);
				final Double open  = Double.parseDouble(cols[INDX_O]);
				final Long volume  = Long.parseLong(cols[INDX_V]);

				list.add(new IntraDayStructure(++index, adjustedDate.getTime(), close, high, low, open, volume));
			}
		}
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
