package com.stocks.standalone;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.josql.Query;
import org.josql.QueryResults;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.stocks.model.AggregateInformation;
import com.stocks.model.AggregateInformationDetails;
import com.stocks.model.AggregateInformationDetailsPK;
import com.stocks.model.AggregateInformationPK;
import com.stocks.model.SymbolMetadata;
import com.stocks.service.StockService;
import com.stocks.util.Utility;

public class CallerAggregateInformation {
	static Properties properties = new Properties();
	static{
		FileInputStream fis = null;
		try{
			fis = new FileInputStream( System.getProperty("propsFilePath") );
			properties.load( fis );
		}
		catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException("Exception in loading properties: " +e);
		}
		finally{
			if( fis != null ){
				try{
					fis.close();
				}
				catch(Exception e){
					e.printStackTrace();
					throw new RuntimeException("Exception in closing properties file: " +e);
				}
			}
		}
	}
	
	//private static final String NATIVE_SQL = "SELECT A.SYMBOL, A.CLOSE, B.EXPANDED_SHARES_OUTSTANDING*A.CLOSE MKT_CAP FROM NYSE A LEFT JOIN SYMBOL_METADATA B ON A.SYMBOL=B.SYMBOL WHERE A.TRADE_DATE=(SELECT MAX(TRADE_DATE) FROM NYSE WHERE SYMBOL='AA' AND VOLUME>0) AND A.CLOSE*A.VOLUME > 20000000 AND A.SYMBOL NOT LIKE '%-%' AND A.SYMBOL NOT LIKE '%.%' AND LENGTH(A.SYMBOL) <= 4 ORDER BY B.EXPANDED_SHARES_OUTSTANDING DESC";
	
	public static void main(String args[]) throws Exception {
		ApplicationContext context = null;
		try{
			context = new FileSystemXmlApplicationContext("src/main/resources/applicationContext.xml");
		}
		catch(Exception e){
			// For running from jar file as in: java -jar ....
			// When building jar using mvn assembly:single, it gets packaged such that applicationContext.xml is placed at root "/" rather than src/main/resources
			System.out.println( "\nWarning: Could not find applicationContext.xml file. Trying classpath.\n" );
			context = new ClassPathXmlApplicationContext("applicationContext.xml");
		}
		
		final StockService stockService = (StockService) context.getBean("stockService");
		
		// Step 1: Pull Qualifying Symbols
		final List<MetaData> metaDataList = new ArrayList<MetaData>();
		
//		List<Object[]> results = stockService.getNativeQueryResults(NATIVE_SQL);
//		for( final Object[] result : results ){
//			final String symbol = result[0].toString();
//			Double mktCap = null;
//			try{
//				mktCap = Double.valueOf( result[2].toString() );
//			}
//			catch(Exception e){
//			}
//			
//			metaDataList.add(new MetaData( symbol, mktCap) );
//		}

		long start = System.currentTimeMillis();
		for( final String symbol : properties.getProperty("symbols").split(",") ){
			final SymbolMetadata symbolMetadata = stockService.getSymbolMetadata(symbol.trim());
			metaDataList.add(new MetaData( symbol.trim(), symbolMetadata != null ? symbolMetadata.getExpandedSharesOutstanding() : null ) );
		}
		long end = System.currentTimeMillis();
		System.out.println( "MetaData pulled in " +(end-start)+ " ms." );

		System.out.println( "Starting to process a total of " +metaDataList.size()+ " symbols." );
		
		final List<String> exceptionInSymbols = new ArrayList<String>();
		final List<String> exceptionOneMinuteSymbols = new ArrayList<String>();
		// Step 2: Pull Aggregate Information from Google and save it to database.
		if( properties.getProperty("generate.report.only").equalsIgnoreCase("false") ){
			pullAndSaveAggregateInformationFromGoogle(metaDataList, stockService, exceptionInSymbols, exceptionOneMinuteSymbols);
		}
		
		// Step 3: Generate Report
		generateReport(metaDataList, stockService);
		
		if( exceptionInSymbols != null && !exceptionInSymbols.isEmpty() ){
			System.out.println( "\nCould not process " +exceptionInSymbols.size()+ " symbols: " +exceptionInSymbols );
		}
		if( exceptionOneMinuteSymbols != null && !exceptionOneMinuteSymbols.isEmpty() ){
			System.out.println( "\nCould not save one.minute data for : " +exceptionOneMinuteSymbols+". To resolve delete <SYMBOL>-INTRA-DAY file from cache.folder, remove any zero-volume entries towards the end of one.minute file for these symbols and rerun the process for these symbols with generate.report.only=false. \nCaution: Keep a copy of generated report otherwise it will overwrite it and you will have to re-run report again for all symbols." );
		}
		
		System.out.println( "\n\nNote: Run GoogleStockScreener every 2 weeks to include new qualifying symbols.\n" );
		System.out.println( "Done." );
		System.exit(0);
	}
	
	private static void generateReport(final List<MetaData> metaDataList, final StockService stockService) throws Exception{
		Writer bwMain = null;
		Writer bwQualified = null;
		
		final List<String> noRecentActivity = new ArrayList<String>();
		try{
			bwMain = new BufferedWriter( new FileWriter(properties.getProperty("rpt.aggregate.path")) );
			bwQualified = new BufferedWriter( new FileWriter(properties.getProperty("rpt.aggregate.for.qualified.path")) );
			
			final StringBuilder sbHead = new StringBuilder();
			sbHead.append( "<head>\n" );
			sbHead.append( "<script>\n" );
			sbHead.append( "function showIntraDayDetails(appletName){\n" );
			sbHead.append( "	chrt = document[appletName].chart;\n" );
			sbHead.append( "	selectedIndex = chrt.getLastSelectedSample();\n" );
			sbHead.append( "	if( selectedIndex != -1 ){\n" );
			sbHead.append( "		symbol = chrt.getTitle();\n" );
			sbHead.append( "		sample0 = chrt.getChartData().getSample(0, selectedIndex);\n" );
			sbHead.append( "		fileName = './cache/' +symbol+ '_'+ sample0.getLabel().replace(/\\//g, '_')+ '.html';\n" );
			sbHead.append( "		window.open(fileName, '_intraDaySnapshot', 'width=830,height=600,scrollbars,resizable')\n" );
			sbHead.append( "	}else{\n" );
			sbHead.append( "		alert('Please make a selection in Chart.');\n" );
			sbHead.append( "	}\n" );
			sbHead.append( "}\n" );
			sbHead.append( "</script>\n" );
			sbHead.append( "</head>\n" );
			
			bwMain.append( sbHead ).append( "<body><DIV id='div_ids' style='position:absolute;top:0px;left:710px' ondblclick=\"this.style.visibility='hidden'\">&nbsp;</DIV><pre>\n" );
			bwQualified.append( sbHead ).append( "<body><DIV id='div_ids' style='position:absolute;top:0px;left:710px' ondblclick=\"this.style.visibility='hidden'\">&nbsp;</DIV><pre>\n" );
			
			// Populate list of Marginable stocks from sogotrade.
			final List<String> sogoMarginables = new ArrayList<String>();
			String shortList = "";
			try{
				shortList = Utility.getContent( "http://wangvestonline.com/sogotrade/list/shortlist.txt" );
			}
			catch(Exception e){
				System.out.println( "\n\nUnable to pull shortlist.txt from wangvestonline.com: " +e.getMessage()+ "\n\n" );
			}
			for( String line : shortList.split("\n") ){
				sogoMarginables.add( line.split("\t")[0] );
			}
			//
			
			final List<String> exceptionSymbols = new ArrayList<String>();
			
			for( final MetaData metaData : metaDataList ){
				System.out.println( "Generating Report for " +metaData.getSymbol() );
				try{
					final StringBuilder sbCloseData = new StringBuilder();
					final StringBuilder sbDates = new StringBuilder();
					final List<Long> volumeList = new ArrayList<Long>();
					
					Long maxVolume = 0L, sumOfVolume = 0L;
					Double maxVxC = 0.0;
					String noteMaxVolxClose = "";
					final List<AggregateInformation> aiList = stockService.getAggregateInformationBySymbol(metaData.getSymbol());
					for( final AggregateInformation ai : aiList ){
						sbCloseData.append(",").append(ai.getClose());
						volumeList.add( ai.getVolume() );
						sbDates.append(",").append(Utility.getStrDate(ai.getAggregateInformationPK().getTradeDate()));
						
						sumOfVolume += ai.getVolume();
						//
						if( Math.abs(ai.getVolume()) > Math.abs(maxVolume) ){
							maxVolume = ai.getVolume();
							maxVxC = (maxVolume * ai.getClose());
							final Number mktCap = (metaData.getSharesOutstanding() != null ? (metaData.getSharesOutstanding()*ai.getClose()) : null);
							noteMaxVolxClose = "Note: V x C on " +Utility.getStrDate(ai.getAggregateInformationPK().getTradeDate())+ " was $" +Utility.getFormattedInteger(maxVxC)+ ". MktCap: " +Utility.getFormattedInteger(mktCap);
							if( mktCap != null ){
								noteMaxVolxClose += ". Best if (MktCap/maxVxC) less than 25 -> " +Utility.round(mktCap.doubleValue()/Math.abs(maxVxC));
							}
							if( maxVxC < 0 ){
								noteMaxVolxClose = "<font color=red>" +noteMaxVolxClose+ "</font>";
							}
						}
					}
					
					final Double avgVolume = new Double(sumOfVolume) / new Double(volumeList.size());
					
					// *********** Start: Sub List ***************** //
					final int DURATION_DAYS = 20;
					Long subMaxVolume = 0L;
					Double subAvgVolume = 0.0;
					if( volumeList.size() > DURATION_DAYS ){
						final List<Long> subVolumeList = volumeList.subList(volumeList.size() - DURATION_DAYS, volumeList.size());
						Long sumOfSubVolume = 0L;
						for( Long volume : subVolumeList ){
							sumOfSubVolume += volume;
							if( Math.abs(volume) > Math.abs(subMaxVolume) ){
								subMaxVolume = Math.abs(volume);
							}
						}
						subAvgVolume = new Double(sumOfSubVolume) / new Double(subVolumeList.size());
					}
					// *********** End: Sub List ***************** //
					
					String CHART_HTML_DATA = IntraDayDataProcessor.CHART_HTML;
		
					CHART_HTML_DATA = CHART_HTML_DATA.replaceAll("~SYMBOL", metaData.getSymbol());
					CHART_HTML_DATA = CHART_HTML_DATA.replace("~CLOSE_DATA", sbCloseData.substring(1));
					CHART_HTML_DATA = CHART_HTML_DATA.replace("~VOLUME_DATA", volumeList.toString().replaceAll("\\[|\\]", "").replaceAll(" ", ""));
					CHART_HTML_DATA = CHART_HTML_DATA.replace("~DATES_DATA", sbDates.substring(1));
					
					final StringBuilder sbTmp = new StringBuilder();
					sbTmp.append( "<a href='http://www.google.com/finance?q=" +metaData.getSymbol()+ "' target='_new' " +(sogoMarginables.contains(metaData.getSymbol()) ? "style='background-color:#00FF00'" : "")+ ">" +metaData.getSymbol()+ "</a> " );
					sbTmp.append( "<input type=button value='Show Intra Day Details' onclick=\"showIntraDayDetails('" +metaData.getSymbol()+ "')\"><BR>" );
					sbTmp.append( noteMaxVolxClose +" \n" );
					sbTmp.append( CHART_HTML_DATA +"\n\n" );
					bwMain.append( sbTmp.toString() );
					
					// Checks for Qualified only.
					boolean bQualified = false;
					if( Math.abs(maxVxC) >= Double.parseDouble( properties.getProperty("qualification.max.vxc.greater.than") )
						&& ( 
							Math.abs(maxVolume) >= (avgVolume * Double.parseDouble(properties.getProperty("qualification.max.vol.times.of.average.vol")) ) ||
							Math.abs(subMaxVolume) >= (subAvgVolume * Double.parseDouble(properties.getProperty("qualification.max.vol.times.of.average.vol")) )
						)){
						
						if( metaData.getSharesOutstanding() != null ){
							Double mktCap = metaData.getSharesOutstanding() * (maxVxC / maxVolume);
							if( Math.abs(maxVxC) >= (mktCap / 30.0) || Math.abs(maxVxC) >= 1000000000.00 ){ // Added OR condition --> if maxVxC >= 1B
								bQualified = true;
							}
						}else{
							bQualified = true;
						}
					}
					
					if( bQualified ){
						bwQualified.append( sbTmp.toString() );
						generateSnapshotFor( metaData.getSymbol(), stockService );
					}
					
					Calendar ONE_WEEK_FROM_TODAY = Calendar.getInstance();
					ONE_WEEK_FROM_TODAY.add(Calendar.DATE, -7);
					if( aiList != null && !aiList.isEmpty() && aiList.get( aiList.size()-1 ).getAggregateInformationPK().getTradeDate().before( ONE_WEEK_FROM_TODAY.getTime() ) ){
						noRecentActivity.add( metaData.getSymbol() );
					}
					
				}
				catch(Exception e){
					System.out.println( "Exception occured while processing " +metaData.getSymbol() );
					exceptionSymbols.add(metaData.getSymbol());
				}
				
				
			}
			bwMain.append( "</pre></body></html>" );
			bwQualified.append( "</pre></body></html>" );
			
			if( !exceptionSymbols.isEmpty() ){
				System.out.println( "\nCould not process " +exceptionSymbols.size()+ " symbols: " +exceptionSymbols );
			}

			if( noRecentActivity != null && !noRecentActivity.isEmpty() ){
				System.out.println( "\nNo Recent activity found in " +noRecentActivity+ ". Consider removing them from properties file." );
			}
		}
		finally{
			if( bwMain != null ){
				bwMain.close();
			}
			if( bwQualified != null ){
				bwQualified.close();
			}
		}
	}
	
	private static void generateSnapshotFor(String symbol, final StockService stockService) throws Exception{
		final List<AggregateInformationDetails> aggregateInformationDetailsList = stockService.getAggregateInformationDetailsBySymbol(symbol);
		for( final AggregateInformationDetails aid : aggregateInformationDetailsList ){
			final StringBuilder sb = new StringBuilder();
			try{
				final List<IntraDayStructure> idsList = convertByteArrayToJavaObject( aid.getJavaObject() );
				// Start: Top n
				QueryResults qrTopN = Query.parseAndExec("SELECT * FROM com.stocks.standalone.IntraDayStructure order by volume desc", new ArrayList<IntraDayStructure>(idsList) );
				List<IntraDayStructure> results = qrTopN.getResults();
				
				final ArrayList<Integer> topIndices = new ArrayList<Integer>();
				for( int i=0; i<20 && results.size() > i; i++ ){
					topIndices.add( results.get(i).getIndex() );
				}
				// End: Top n
				
				StringBuilder sbTable = new StringBuilder( "<table border=1><tr><td>&nbsp;</td> <td>index</td> <td>time</td> <td>%change from prev</td> <td>open</td> <td>high</td> <td>low</td> <td>close</td> <td>volume</td> <td>CxV</td></tr>\n" );
				IntraDayStructure idsPrev = null;
				for( final IntraDayStructure ids : idsList ){
					String pcChange = "";
					if( idsPrev != null ){
						double change = Utility.round( ((ids.getClose() - idsPrev.getClose()) / idsPrev.getClose()) * 100.00 );
						pcChange = change+"%";
					}
					
					if( topIndices.contains( ids.getIndex() ) ){
						sbTable.append("<tr " +"style='background-color:#DADADA;color:" +(pcChange.startsWith("-") ? "red" : "green")+ "'"+ ">");
					}else{
						sbTable.append("<tr>");
					}
					
					sbTable.append("<td>").append("<input type=checkbox>").append("</td>");
					sbTable.append("<td>").append(ids.getIndex()).append("</td>");
					sbTable.append("<td>").append( new Date( ids.getTime() ) ).append("</td>");
					sbTable.append("<td align=right>").append( pcChange ).append("&nbsp;</td>");
					sbTable.append("<td>").append( ids.getOpen() ).append("</td>");
					sbTable.append("<td>").append( ids.getHigh() ).append("</td>");
					sbTable.append("<td>").append( ids.getLow() ).append("</td>");
					sbTable.append("<td>").append( ids.getClose() ).append("</td>");
					sbTable.append("<td>").append( Utility.getFormattedInteger( ids.getVolume() ) ).append("</td>");
					sbTable.append("<td>").append( "$" +Utility.getFormattedInteger( ids.getClose() * ids.getVolume() ) ).append("</td>");
					sbTable.append("</tr>\n");
					idsPrev = ids;
				}
				sbTable.append( "</table>\n" );
				sb.append( sbTable );
			}
			catch(Exception e){
				sb.append( "Exception in reading java object: " +e.getMessage() );
			}
			
			String path = properties.getProperty("cache.folder") +aid.getAggregateInformationDetailsPK().getSymbol()+ "_" +Utility.getStrDate( aid.getAggregateInformationDetailsPK().getTradeDate(), "MM_dd_yyyy" )+ ".html";
			Utility.saveContent(path, sb.toString());
		}
	}
	
	private static void pullAndSaveAggregateInformationFromGoogle(final List<MetaData> metaDataList, final StockService stockService, final List<String> exceptionSymbols, final List<String> exceptionOneMinuteSymbols) throws Exception{
		final Map<String, String> mapSymbolContent = new HashMap<String, String>();
		
		final String GOOGLE_URL_INTRA_DAY = properties.getProperty("google.url.intra.day");
		final String GOOGLE_URL_DAY_CLOSE = properties.getProperty("google.url.day.close");
		
		for(final MetaData metaData : metaDataList){
			System.out.println( "Processing " +metaData.getSymbol() );
			
			if( exceptionSymbols.size() >= 5 ){
				System.out.println( "\nException occured for more than 5 symbols " +exceptionSymbols+ " at " +new Date()+ ".\nExiting now." );
				//System.out.println( "\007\007\007" ); // Beep Character \007
				System.exit(-1);
			}
			
			try{
				final List<AggregateInformation> aiList = new ArrayList<AggregateInformation>();
				
				// ********************************* START: CACHE IMPLEMENTATION ********************************** //
				final String CACHE_FILE_SUFFIX_INTRA_DAY = "-INTRA-DAY";
				final String CACHE_FILE_SUFFIX_DAY_CLOSE = "-DAY-CLOSE";
				
				String intraDayContent = null;
				String dayCloseContent = null;
				//if( properties.getProperty("use.cache").equals("true") ){
					try{
						File intraDayFile = new File( properties.getProperty("cache.folder") + metaData.getSymbol() + CACHE_FILE_SUFFIX_INTRA_DAY );
						if( intraDayFile.exists() ){
							intraDayContent = Utility.getContent( intraDayFile.toURL().toString() );
						}
					}
					catch(Exception e){
						e.printStackTrace();
					}

					try{
						File dayCloseFile = new File( properties.getProperty("cache.folder") + metaData.getSymbol() + CACHE_FILE_SUFFIX_DAY_CLOSE );
						if( dayCloseFile.exists() ){
							dayCloseContent = Utility.getContent( dayCloseFile.toURL().toString() );
						}
					}
					catch(Exception e){
						e.printStackTrace();
					}
					
				//}

				if( intraDayContent == null ){
					intraDayContent = Utility.getContent( GOOGLE_URL_INTRA_DAY.replaceAll("~SYMBOL", metaData.getSymbol()) );
					try{
						Utility.saveContent(properties.getProperty("cache.folder") + metaData.getSymbol() + CACHE_FILE_SUFFIX_INTRA_DAY, intraDayContent);
						mapSymbolContent.put(metaData.getSymbol(), intraDayContent);
					}
					catch(Exception e){
						e.printStackTrace();
					}
				}
				if( dayCloseContent == null ){
					dayCloseContent = Utility.getContent( GOOGLE_URL_DAY_CLOSE.replaceAll("~SYMBOL", metaData.getSymbol()) );

					try{
						Utility.saveContent(properties.getProperty("cache.folder") + metaData.getSymbol() + CACHE_FILE_SUFFIX_DAY_CLOSE, dayCloseContent);
					}
					catch(Exception e){
						e.printStackTrace();
					}
				}
				// ********************************* END: CACHE IMPLEMENTATION ********************************** //
				
				final Map<Date, List<IntraDayStructure>> mapGIntraDay = IntraDayDataProcessor.fetchG( intraDayContent, true );
				final Map<Date, List<IntraDayStructure>> mapGDayClose = IntraDayDataProcessor.fetchG( dayCloseContent, false );
				
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
				
				Long maxVolume = 0L;
				Date dateWithMaxVolume = null;
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
					}
					
					final Double close = idsClose != null ? idsClose.getClose() : idsList.get(idsList.size()-1).getClose();
					
					final Long vol = bVol-sVol;
		
					if( Math.abs(vol) > Math.abs(maxVolume) ){
						maxVolume = vol;
						dateWithMaxVolume = tradeDate;
					}
		
					final AggregateInformationPK aiPK = new AggregateInformationPK();
					aiPK.setSymbol(metaData.getSymbol());
					aiPK.setTradeDate( Utility.getDate( Utility.getStrDate(tradeDate) ) ); // To strip-off time.
					final AggregateInformation ai = new AggregateInformation();
					ai.setClose(close);
					ai.setVolume(vol);
					ai.setAggregateInformationPK(aiPK);
					aiList.add(ai);
				}
				
				for( final AggregateInformation ai : aiList ){
					if( Utility.areDatesEqual( ai.getAggregateInformationPK().getTradeDate(), dateWithMaxVolume ) ){
						//System.out.println( "Trying to save AggregateInformationDetails." );
						final AggregateInformationDetailsPK aggregateInformationDetailsPK = new AggregateInformationDetailsPK();
						aggregateInformationDetailsPK.setSymbol(metaData.getSymbol());
						aggregateInformationDetailsPK.setTradeDate( Utility.getDate( Utility.getStrDate( dateWithMaxVolume ) ) );
						final AggregateInformationDetails aggregateInformationDetails = new AggregateInformationDetails();
						aggregateInformationDetails.setJavaObject( convertJavaObjectToByteArray( mapGIntraDay.get(dateWithMaxVolume) ) );
						
						aggregateInformationDetails.setAggregateInformationDetailsPK(aggregateInformationDetailsPK);
						
						stockService.saveAggregateInformationDetails(aggregateInformationDetails);
						//System.out.println( "AggregateInformationDetails saved." );
						break;
					}
				}
				
				stockService.saveAggregateInformation(aiList);
			}
			catch(Exception e){
				System.out.println( "****************** Exception occured while processing " +metaData.getSymbol()+ " -> " +e.getMessage() );
				exceptionSymbols.add(metaData.getSymbol());
			}
			
			try{
				if( !mapSymbolContent.isEmpty() && mapSymbolContent.size() >= 20 ){
					exceptionOneMinuteSymbols.addAll( appendToIntraDayFile(mapSymbolContent) );
					mapSymbolContent.clear();
					int sleepTimeMillis = Integer.parseInt(properties.getProperty("sleep.time.millis"));
					System.out.println( "\nSleeping for " +sleepTimeMillis+ " millis .... " );
					Thread.sleep(sleepTimeMillis);
				}
			}
			catch(Exception e){
			}
		}

		// Run again for any left overs IntraDayContent not appended.
		exceptionOneMinuteSymbols.addAll( appendToIntraDayFile(mapSymbolContent) );
		mapSymbolContent.clear();

		
		if( !exceptionSymbols.isEmpty() ){
			System.out.println( "\nCould not process " +exceptionSymbols.size()+ " symbols: " +exceptionSymbols );
		}
		
		if( !exceptionOneMinuteSymbols.isEmpty() ){
			System.out.println( "\nCould not save one.minute data for : " +exceptionOneMinuteSymbols+". To resolve delete <SYMBOL>-INTRA-DAY file from cache.folder, remove any zero-volume entries towards the end of one.minute file for these symbols and rerun the process for these symbols with generate.report.only=false. \nCaution: Keep a copy of generated report otherwise it will overwrite it and you will have to re-run report again for all symbols." );
		}
	}
	
	private static byte[] convertJavaObjectToByteArray(Object obj) throws Exception{
		final ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream o = new ObjectOutputStream(b);
        o.writeObject(obj);
        try{
        	return b.toByteArray();
        }
        finally{
        	o.close();
        }
	}
	
	private static List<IntraDayStructure> convertByteArrayToJavaObject(byte[] bArray) throws Exception{
		ByteArrayInputStream b = new ByteArrayInputStream(bArray);
        ObjectInputStream o = new ObjectInputStream(b);
        try{
        	return (List<IntraDayStructure>) o.readObject();
        }
        finally{
        	o.close();
        }
	}
	
	private static List<String> appendToIntraDayFile(final Map<String, String> mapSymbolContent){
		final List<String> exceptionOneMinuteSymbols = new ArrayList<String>();
		
		final Set<Entry<String, String>> entrySet = mapSymbolContent.entrySet();
		
		if( !entrySet.isEmpty() ){
			Iterator<Entry<String, String>> iterator = entrySet.iterator();
			while( iterator.hasNext() ){
				final Map.Entry<String, String> entry = iterator.next();
				final String symbol = entry.getKey();
				final String content = entry.getValue();
				
				try{
					File existingIntraDayFile = new File( properties.getProperty("one.minute.data.folder") +"/"+ symbol+"-INTRA-DAY" );
					if( existingIntraDayFile.exists() ){ // Append
						String existingContent = Utility.getContent( existingIntraDayFile.toURL().toString() );
						// Read Last line
						final String[] arrExistingContent = existingContent.split("[\n]");
						String lastLineWithVolume = null;
						if( arrExistingContent.length >= 25 ){ // To be on safe side, pull last 25 min data
							for( int i=(arrExistingContent.length-25); i<arrExistingContent.length; i++ ){
								final String line = arrExistingContent[i];
								
								String cols[] = line.split(",");
								if( !cols[cols.length-1].equals("0") ){ // Last Column Volume
									lastLineWithVolume = arrExistingContent[i];
									break;
								}
							}
							System.out.println( "\tlastLineWithVolume: "+lastLineWithVolume );
						}
						
						// Find last line in content
						int indexOfLastLineWithVolume = -1;
						if( lastLineWithVolume != null ){
							indexOfLastLineWithVolume = content.lastIndexOf( lastLineWithVolume );
						}
						
						// Append to file.
						final String contentToAppend = content.substring( indexOfLastLineWithVolume );
						Utility.saveContent( existingIntraDayFile.getAbsolutePath(), existingContent.substring(0, existingContent.indexOf(lastLineWithVolume) ).trim()+"\n"+contentToAppend.trim());
						System.out.println( "Successfully appended one.minute data for " +symbol );
					}else{ // Save as is.
						Utility.saveContent(existingIntraDayFile.getAbsolutePath(), content);
						System.out.println( "Successfully created new one.minute data for [" +symbol+ "] as no existing one.minute data was present." );
					}
				}
				catch(Exception e){
					exceptionOneMinuteSymbols.add( symbol );
					System.out.println( "********* Exception in saving one.minute data for " +symbol );
					e.printStackTrace();
				}
			}
		}
		
		return exceptionOneMinuteSymbols;
	}
	
}

class MetaData{
	private String symbol;
	private Long sharesOutstanding;
	
	public MetaData(String symbol, Long sharesOutstanding) {
		this.symbol = symbol;
		this.sharesOutstanding = sharesOutstanding;
	}
	
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public Long getSharesOutstanding() {
		return sharesOutstanding;
	}

	public void setSharesOutstanding(Long sharesOutstanding) {
		this.sharesOutstanding = sharesOutstanding;
	}

}

