package com.stocks.standalone;

import java.io.File;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.stocks.enums.Movement;
import com.stocks.util.Utility;

public class IntraDayDataProcessor {
	public static final String CHART_HTML = "<applet name='~SYMBOL' code='com.objectplanet.chart.ChartApplet' archive='chart.jar' width='700' height='350'>\n<param name='chart' value='line'>\n<param name='chartTitle' value='~SYMBOL'>\n<param name='sampleValues_0' value='~CLOSE_DATA'>\n<param name='sampleValues_1' value='~VOLUME_DATA'>\n<param name='sampleLabels' value='~DATES_DATA'>\n<param name='seriesRange_0' value='2'>\n<param name='sampleLabelsOn' value='true'>\n<param name='sampleLabelStyle' value='floating'>\n<param name='floatingLabelFont' value='Verdana, plain, 10'>\n<param name='sampleColors' value='blue, red'>\n<param name='sampleHighlightOn' value='true'>\n<param name='sampleHighlightStyle' value='circle_opaque'>\n<param name='sampleHighlightSize' value='6'>\n<param name='rangeColor' value='red'>\n<param name='rangeColor_2' value='blue'>\n<param name='seriesCount' value='2'>\n<param name='valueLabelsOn' value='true'>\n<param name='valueLabelStyle' value='floating'>\n<param name='valueLinesOn' value='true'>\n<param name='defaultGridLinesOn' value='true'>\n<param name='legendOn' value='true'>\n<param name='legendPosition' value='top'>\n<param name='legendLabels' value='Close,Volume'>\n<param name='rangeOn_2' value='true'>\n<param name='rangePosition' value='right'>\n<param name='rangePosition_2' value='left'>\n<param name='rangeAxisLabel' value='Volume'>\n<param name='rangeAxisLabelFont' value='Verdana, bold, 16'>\n<param name='rangeAxisLabelAngle' value='90'>\n<param name='rangeAxisLabel_2' value='Close'>\n<param name='rangeAxisLabelAngle_2' value='270'>\n<param name='rangeLabelPrefix_2' value='$'>\n<param name='multiSeriesOn' value='true'>\n<param name='rangeDecimalCount' value='0'>\n<param name='rangeDecimalCount_2' value='2'>\n<param name='sampleDecimalCount' value='2'>\n<param name='sampleDecimalCount_2' value='0'>\n<param name='chartBackground' value='#DADAFF'>\n</applet>";
	
	static Properties properties = new Properties();
	static{
		try{
			properties.load( IntraDayDataProcessor.class.getResourceAsStream("/IntraDayDataProcessor.properties") );
		}
		catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException("Exception in loading properties: " +e);
		}
	}

	
	// This URL fetches 1 min data for a period of 1 Month:  "http://www.google.com/finance/getprices?i=90&p=1M&f=d,c,v,o,h,l&df=cpct&auto=1&q=";
	// This URL fetches 2 min data for a period of 2 Months: "http://www.google.com/finance/getprices?i=150&p=2M&f=d,c,v,o,h,l&df=cpct&auto=1&q=";
	// This URL fetches 3 min data for a period of 3 Months: "http://www.google.com/finance/getprices?i=210&p=3M&f=d,c,v,o,h,l&df=cpct&auto=1&q=";
	//private static final String GOOGLE_URL_INTRA_DAY = "http://www.google.com/finance/getprices?i=90&p=1M&f=d,c,v,o,h,l&df=cpct&auto=1&q=";
	//private static final String GOOGLE_URL_INTRA_DAY = "http://www.google.com/finance/getprices?i=150&p=2M&f=d,c,v,o,h,l&df=cpct&auto=1&q=";
	//private static final String GOOGLE_URL_INTRA_DAY = "http://www.google.com/finance/getprices?i=210&p=3M&f=d,c,v,o,h,l&df=cpct&auto=1&q=";
	//private static final String GOOGLE_URL_INTRA_DAY = "http://www.google.com/finance/getprices?i=270&p=4M&f=d,c,v,o,h,l&df=cpct&auto=1&q=";
	//private static final String GOOGLE_URL_INTRA_DAY = "http://www.google.com/finance/getprices?i=210&p=3M&f=d,c,v,o,h,l&df=cpct&auto=1&q=";
	//private static final String GOOGLE_URL_DAY_CLOSE = "http://www.google.com/finance/getprices?p=3M&f=d,c,v,o,h,l&df=cpct&auto=1&q=";
	
	public static void main(String... args) throws Exception{
		final IntraDayDataProcessor iddp = new IntraDayDataProcessor();
		
		String[] symbols = properties.getProperty("symbols").split(",");
		final Set<String> set = new HashSet<String>();
		set.addAll( Arrays.asList(symbols) );
		PrintWriter writer = null, qualifiedWriter = null;
		try{
			writer = new PrintWriter( properties.getProperty("rpt.path") );
			qualifiedWriter = new PrintWriter( properties.getProperty("rpt.path.for.qualified") );
			iddp.generateReport(set, writer, qualifiedWriter);
		}
		finally{
			if(writer != null){
				writer.close();
			}
			if(qualifiedWriter != null){
				qualifiedWriter.close();
			}
		}

		System.out.println( "Done" );
	}
	
	public void generateReport(final Set<String> symbols, Writer writer, Writer qualifiedWriter) throws Exception{
		final StringBuilder sb = new StringBuilder();
		final StringBuilder sbQualified = new StringBuilder();
		final List<String> exceptionSymbols = new ArrayList<String>();
		int ctr = 0;
		for( String symbol : symbols ){
			System.out.println( "Processing " +symbol );
			try{
				process(symbol.trim(), sb, sbQualified, properties.getProperty("google.url.intra.day"), properties.getProperty("google.url.day.close"));
			}
			catch(Exception e){
				e.printStackTrace();
				exceptionSymbols.add(symbol);
			}
			
			try{
				if( ++ctr % 10 == 0 ){
					int sleepTimeMillis = Integer.parseInt(properties.getProperty("sleep.time.millis"));
					System.out.println( "\nSleeping for " +sleepTimeMillis+ " millis .... " +Utility.round(((double)ctr/(double)symbols.size())*100.00)+ "% completed.\n" );
					Thread.sleep(sleepTimeMillis);
				}
			}
			catch(Exception e){
			}
			
		}

		try{
			final StringBuilder sbTmp = new StringBuilder();
			
			sbTmp.append( "<script>").append("\n");
			sbTmp.append( "function summarize(){").append("\n");
			sbTmp.append( "	vTotalPos = 0;").append("\n");
			sbTmp.append( "	vTotalNeg = 0;").append("\n");
			sbTmp.append( "	arrPositive = document.getElementsByName('positive');").append("\n");
			sbTmp.append( "	arrNegative = document.getElementsByName('negative');").append("\n");
			sbTmp.append( "	arrConsiderForTopVolumeHighlighter = document.getElementsByName('considerForTopVolumeHighlighter');").append("\n");
			sbTmp.append( "	vPositiveStocks = \"\";").append("\n");
			sbTmp.append( "	vConsiderableForTopVolumeHighlighter = \"\";").append("\n");
			
			sbTmp.append( "	for(i=0; i<arrPositive.length; i++ ){").append("\n");
			sbTmp.append( "		elem = document.getElementsByName('positive').item(i);").append("\n");
			sbTmp.append( "		if( elem.checked ){").append("\n");
			sbTmp.append( "			vTotalPos++;").append("\n");
			sbTmp.append( "            vPositiveStocks += elem.value +\", \";").append("\n");
			sbTmp.append( "		}").append("\n");
			sbTmp.append( "	}").append("\n");

			sbTmp.append( "	for(i=0; i<arrNegative.length; i++ ){").append("\n");
			sbTmp.append( "		elem = document.getElementsByName('negative').item(i);").append("\n");
			sbTmp.append( "		if( elem.checked ){").append("\n");
			sbTmp.append( "			vTotalNeg++;").append("\n");
			sbTmp.append( "		}").append("\n");
			sbTmp.append( "	}").append("\n");

			sbTmp.append( "	for(i=0; i<arrConsiderForTopVolumeHighlighter.length; i++ ){").append("\n");
			sbTmp.append( "		elem = document.getElementsByName('considerForTopVolumeHighlighter').item(i);").append("\n");
			sbTmp.append( "		if( elem.checked ){").append("\n");
			sbTmp.append( "            vConsiderableForTopVolumeHighlighter += elem.value +\",\";").append("\n");
			sbTmp.append( "		}").append("\n");
			sbTmp.append( "	}").append("\n");

			sbTmp.append( "	document.getElementById('totalPos').value = vTotalPos;").append("\n");
			sbTmp.append( "	document.getElementById('totalNeg').value = vTotalNeg;").append("\n");
			sbTmp.append( "	document.getElementById('positiveStocks').value = vPositiveStocks;").append("\n");
			sbTmp.append( "	document.getElementById('tConsiderableForTopVolumeHighlighter').value = vConsiderableForTopVolumeHighlighter != '' ? vConsiderableForTopVolumeHighlighter.substring(0, vConsiderableForTopVolumeHighlighter.length-1) : vConsiderableForTopVolumeHighlighter;").append("\n");
			sbTmp.append( "").append("\n");
			sbTmp.append( "}").append("\n");
			sbTmp.append( "</script>").append("\n");
			//writer.append( "</head>").append("\n");
			
			sbTmp.append( "Report generated on <B>" + new Date() + "</B>\n" );
			sbTmp.append("<PRE><B>Description</B>: This report displays two series of data for each symbol viz. Close and Volume.\nWhen you hover over the round marks on each series it will display the data they represent.\nVolume is actually Buy/Sell Volume in the sense that every rise in intraday data is considered to be buy and every fall is considered sell.\nLook for spikes in Buy volume. This should be at least 4 times average buy volume.\nIf there are small or no sell volumes recently and a sudden buy volume comes, then this is a very bullish sign.\nDo not consider symbols where you see many sell instances recently.\nAlso, there are 2 checkboxes provided by each chart. \nThey are for marking Positive/Negative outcome of the strategy as exceptions are always there.\nThe textboxes on the top will summarize this information for you with Positive Symbols displayed as comma-separated string.\nIf you do not see charts, then copy the <U>chart.jar</U> to current folder where this html file resides.").append("\n");
			sbTmp.append("TotalPos: <input type=text name=\"totalPos\" id=\"totalPos\"> <input type=text name=\"positiveStocks\" id=\"positiveStocks\">").append("\n");
			sbTmp.append("TotalNeg: <input type=text name=\"totalNeg\" id=\"totalNeg\">").append("\n");
			sbTmp.append("To be considered for Top Volume Highlighter: <input type=text name=\"tConsiderableForTopVolumeHighlighter\" id=\"tConsiderableForTopVolumeHighlighter\" size='40' onclick='this.select();'>").append("\n\n");

			if( qualifiedWriter != null ){
				qualifiedWriter.append( sbTmp.toString() );
				qualifiedWriter.append( sbQualified.toString() ).append("\n");
				qualifiedWriter.append( "</PRE>" ).append("\n");
			}
			
			writer.append(sbTmp.toString());
			writer.append( sb.toString() ).append("\n");
			writer.append( "</PRE>" ).append("\n");
		}
		catch(Exception e){
			e.printStackTrace();
			System.out.println( sb.toString() );
		}
		
		if( !exceptionSymbols.isEmpty() ){
			System.out.println( "\n\nThe process faced exceptions in processing: " +exceptionSymbols+ "\n\n" );
		}
	}

	private void process(String symbol, final StringBuilder sb, final StringBuilder sbQualified, final String GOOGLE_URL_INTRA_DAY, String GOOGLE_URL_DAY_CLOSE) throws Exception{
		//final Map<Date, List<IntraDayStructure>> mapGIntraDay = fetchY( Utility.getContent( properties.getProperty("yahoo.url.intra.day").replaceAll("~SYMBOL", symbol) ) );
//		final Map<Date, List<IntraDayStructure>> mapGIntraDay = fetchG( Utility.getContent( "file:/C:/Temp/ForSrid/intra/rpts/Intra"+symbol ), true );
//		final Map<Date, List<IntraDayStructure>> mapGDayClose = fetchG( Utility.getContent( "file:/C:/Temp/ForSrid/intra/rpts/"+symbol ), false );

		// ********************************* START: CACHE IMPLEMENTATION ********************************** //
		final String CACHE_FILE_SUFFIX_INTRA_DAY = "-INTRA-DAY";
		final String CACHE_FILE_SUFFIX_DAY_CLOSE = "-DAY-CLOSE";
		
		String intraDayContent = null;
		String dayCloseContent = null;
		if( properties.getProperty("use.cache").equals("true") ){
			try{
				File intraDayFile = new File( properties.getProperty("cache.folder") + symbol + CACHE_FILE_SUFFIX_INTRA_DAY );
				if( intraDayFile.exists() ){
					intraDayContent = Utility.getContent( intraDayFile.toURL().toString() );
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}

			try{
				File dayCloseFile = new File( properties.getProperty("cache.folder") + symbol + CACHE_FILE_SUFFIX_DAY_CLOSE );
				if( dayCloseFile.exists() ){
					dayCloseContent = Utility.getContent( dayCloseFile.toURL().toString() );
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
			
		}

		if( intraDayContent == null ){
			intraDayContent = Utility.getContent( GOOGLE_URL_INTRA_DAY.replaceAll("~SYMBOL", symbol) );
			try{
				Utility.saveContent(properties.getProperty("cache.folder") + symbol + CACHE_FILE_SUFFIX_INTRA_DAY, intraDayContent);
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		if( dayCloseContent == null ){
			dayCloseContent = Utility.getContent( GOOGLE_URL_DAY_CLOSE.replaceAll("~SYMBOL", symbol) );

			try{
				Utility.saveContent(properties.getProperty("cache.folder") + symbol + CACHE_FILE_SUFFIX_DAY_CLOSE, dayCloseContent);
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		// ********************************* END: CACHE IMPLEMENTATION ********************************** //
		
		final Map<Date, List<IntraDayStructure>> mapGIntraDay = fetchG( intraDayContent, true );
		final Map<Date, List<IntraDayStructure>> mapGDayClose = fetchG( dayCloseContent, false );
		
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
		Long maxVolume = 0L, sumOfVolume = 0L;
		String noteMaxVolxClose = "";
		Date dateWithMaxVolume = null;
		Double maxVxC = 0.0;
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
				final IntraDayStructure prev;

				// Get Total Buy/Sell Volume on a given day.
				if( i == 0 ){
					final Map.Entry<String, IntraDayStructure> me = mDayClose.lowerEntry( Utility.getStrDate(tradeDate) );
					if( me != null ){
						prev = me.getValue();
					}else{
						prev = idsList.get(i);
					}
				}else{
					prev = idsList.get(i-1);
				}
				Double prevClose = prev.getClose();
				

				if( ids.getClose() >= prevClose ){
					bVol += ids.getVolume();
				}else if( ids.getClose() < prevClose ){
					sVol += ids.getVolume();
				}

/*				
				final Double absDiff = Math.abs( ((ids.getClose() - prev.getClose()) / prev.getClose())*100.0 );
				if( absDiff > 0.30 ){ // If difference between closes is more than 0.30% then consider close.
					if( ids.getClose() >= prevClose ){
						bVol += ids.getVolume();
						System.out.println( String.format( "%s,%s,%s,%s,%s,%s,%s,%s,%s", ids.getIndex(), ids.getOpen(), ids.getHigh(), ids.getLow(), ids.getClose(), ids.getVolume(), true, absDiff, "B" ) );
					}else if( ids.getClose() < prevClose ){
						sVol += ids.getVolume();
						System.out.println( String.format( "%s,%s,%s,%s,%s,%s,%s,%s,%s", ids.getIndex(), ids.getOpen(), ids.getHigh(), ids.getLow(), ids.getClose(), ids.getVolume(), true, absDiff, "S" ) );
					}
				}else{ // Consider CandleStick.
					Movement movement = Utility.getCandleStickType(ids);
					if( movement.equals( Movement.PUMP ) ){
						bVol += ids.getVolume();
						System.out.println( String.format( "%s,%s,%s,%s,%s,%s,%s,%s,%s", ids.getIndex(), ids.getOpen(), ids.getHigh(), ids.getLow(), ids.getClose(), ids.getVolume(), false, absDiff, "B" ) );
					}else if( movement.equals( Movement.DUMP ) ){
						sVol += ids.getVolume();
						System.out.println( String.format( "%s,%s,%s,%s,%s,%s,%s,%s,%s", ids.getIndex(), ids.getOpen(), ids.getHigh(), ids.getLow(), ids.getClose(), ids.getVolume(), false, absDiff, "S" ) );
					}else{ // Indecision
						bVol += ids.getVolume()/2;
						sVol += ids.getVolume()/2;
						System.out.println( String.format( "%s,%s,%s,%s,%s,%s,%s,%s,%s", ids.getIndex(), ids.getOpen(), ids.getHigh(), ids.getLow(), ids.getClose(), ids.getVolume(), false, absDiff, "U" ) );
					}
				}
*/				
			}
			
			final Double close = idsClose != null ? idsClose.getClose() : idsList.get(idsList.size()-1).getClose();
			
			final Long vol = bVol-sVol;
			volumeList.add(vol);
			sumOfVolume += vol;
			
			if( Math.abs(vol) > Math.abs(maxVolume) ){
				maxVolume = vol;
				maxVxC = (maxVolume * close);
				noteMaxVolxClose = "Note: V x C on " +Utility.getStrDate(tradeDate)+ " was $" +Utility.getFormattedInteger(maxVxC);
				
				if( maxVxC < 0 ){
					noteMaxVolxClose = "<font color=red>" +noteMaxVolxClose+ "</font>";
				}
				
				dateWithMaxVolume = tradeDate;
			}

			sbDates.append(",").append(Utility.getStrDate(tradeDate));
			sbCloseData.append(",").append( close );
		}
	
		String CHART_HTML_DATA = CHART_HTML;

		CHART_HTML_DATA = CHART_HTML_DATA.replaceAll("~SYMBOL", symbol);
		CHART_HTML_DATA = CHART_HTML_DATA.replace("~CLOSE_DATA", sbCloseData.substring(1));
		CHART_HTML_DATA = CHART_HTML_DATA.replace("~VOLUME_DATA", volumeList.toString().replaceAll("\\[|\\]", "").replaceAll(" ", ""));
		CHART_HTML_DATA = CHART_HTML_DATA.replace("~DATES_DATA", sbDates.substring(1));
		
		final StringBuilder sbTmp = new StringBuilder();
		sbTmp.append( "P<input type=checkbox onclick=\"summarize()\" name=positive value=\"" +symbol+ "\"> N<input type=checkbox onclick=\"summarize()\" name=negative value=\"" +symbol+ "\"> " );
		sbTmp.append( "<a href='http://www.google.com/finance?q=" +symbol+ "' target='_new'>" +symbol+ "</a> " );
		sbTmp.append( noteMaxVolxClose +" " );
		sbTmp.append( "<input type='checkbox' name='considerForTopVolumeHighlighter' value='" +symbol+ "|" +Utility.getStrDate(dateWithMaxVolume)+ "' onclick='summarize()'> Consider it for Top Volume Highlighter\n" );
		sbTmp.append( CHART_HTML_DATA +"\n\n" );
		
		sb.append( sbTmp.toString() );
		
		final Double avgVolume = new Double(sumOfVolume) / new Double(volumeList.size());
		
		// ************** Start: Filter out if LastClose is close to 52WkHigh.  *********************** //
		boolean check52WkPassed = true;
		try{
			Quote quote = CallerQuote.processCnbc(symbol);
			String range52Wk = quote.getRange52wk();
			final Double low52Wk = new Double( range52Wk.split(" - ")[0] );
			final Double high52Wk = new Double( range52Wk.split(" - ")[1] );
			final Double lastClose = new Double( sbCloseData.substring( sbCloseData.lastIndexOf(",")+1 ) );
			final Double midPoint = (high52Wk - ((high52Wk - low52Wk)/2.00) );
			if( lastClose > midPoint ){
				check52WkPassed = false;
			}
			
		}
		catch(Exception e){
			// Ignore
			System.out.println( "::: Could not do 52WkCheck for " +symbol+ " due to " +e.getMessage() );
		}
		// ***************** End ************************** //
		
		if( Math.abs(maxVolume) >= (avgVolume * Double.parseDouble(properties.getProperty("qualification.max.vol.times.of.average.vol")) ) 
				&&  Math.abs(maxVxC) >= Double.parseDouble(properties.getProperty("qualification.max.vxc.greater.than"))
				&& ((check52WkPassed && maxVxC > 0) || (!check52WkPassed && maxVxC < 0) ) ){
			sbQualified.append( sbTmp.toString() );
		}
	}

	private Map<Date, List<IntraDayStructure>> fetchY(String data) throws Exception{
		System.out.println( "Y" );
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
		Date previousDate = null;
		
		for( String row : rows ){
			if( row.startsWith("volume") ){
				bDataStarted = true;
			}else if( bDataStarted ){
				String[] cols = row.split(",");
				Date date = new Date( Long.parseLong(cols[INDX_D]) * DATE_OFFSET );
				Double close = Double.parseDouble(cols[INDX_C]);
				Double high  = Double.parseDouble(cols[INDX_H]);
				Double low   = Double.parseDouble(cols[INDX_L]);
				Double open  = Double.parseDouble(cols[INDX_O]);
				Long volume = Long.parseLong(cols[INDX_V]);
				if( previousDate == null || !Utility.areDatesEqual(previousDate, date) ){
					list = new LinkedList<IntraDayStructure>();
					map.put(date, list);
					previousDate = date;
				}
				
				list.add(new IntraDayStructure(++index, date.getTime(), close, high, low, open, volume));
			}
		}
		
		return map;
	}

	
	public static Map<Date, List<IntraDayStructure>> fetchG(String data, boolean isIntraDay) throws Exception{
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
				
				if( cols.length != 6 ){
					System.out.println( "Error parsing current row. Data -> " +row );
					continue;
				}
				
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
