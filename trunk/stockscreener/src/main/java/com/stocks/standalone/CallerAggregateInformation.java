package com.stocks.standalone;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.TreeSet;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.stocks.model.AggregateInformation;
import com.stocks.model.AggregateInformationDetails;
import com.stocks.model.AggregateInformationDetailsPK;
import com.stocks.model.AggregateInformationPK;
import com.stocks.service.StockService;
import com.stocks.util.Utility;

public class CallerAggregateInformation {

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
	
	private static final String NATIVE_SQL = "SELECT A.SYMBOL, A.CLOSE, B.EXPANDED_SHARES_OUTSTANDING*A.CLOSE MKT_CAP FROM NYSE A LEFT JOIN SYMBOL_METADATA B ON A.SYMBOL=B.SYMBOL WHERE A.TRADE_DATE=(SELECT MAX(TRADE_DATE) FROM NYSE WHERE SYMBOL='DJI.IDX' AND VOLUME>0) AND A.CLOSE*A.VOLUME > 10000000 AND A.SYMBOL NOT LIKE '%-%' AND A.SYMBOL NOT LIKE '%.%' AND LENGTH(A.SYMBOL) <= 4 ORDER BY B.EXPANDED_SHARES_OUTSTANDING DESC";
	
	public static void main(String args[]) throws Exception {
		final ApplicationContext context = new FileSystemXmlApplicationContext("src/main/resources/applicationContext.xml");
		final StockService stockService = (StockService) context.getBean("stockService");
		
		// Step 1: Pull Qualifying Symbols
		final List<MetaData> metaDataList = new ArrayList<MetaData>();
		for( final Object[] result : stockService.getNativeQueryResults(NATIVE_SQL) ){
			final String symbol = result[0].toString();
			Double mktCap = null;
			try{
				mktCap = Double.valueOf( result[2].toString() );
			}
			catch(Exception e){
			}
			
			metaDataList.add(new MetaData( symbol, mktCap) );
		}

		// Step 2: Pull Aggregate Information from Google and save it to database.
		pullAndSaveAggregateInformationFromGoogle(metaDataList, stockService);
		
		//symbols.clear();
		//symbols.add("NBG");
		
		// Step 3: Generate Report
		generateReport(metaDataList, stockService);
		
		//System.out.println( "--> "+ convertByteArrayToJavaObject( stockService.getAggregateInformationDetails("A", Utility.getDate("05/15/2013")).getJavaObject() ) );
		System.out.println( "Done." );
		System.exit(0);
	}
	
	private static void generateReport(final List<MetaData> metaDataList, final StockService stockService) throws Exception{
		final StringBuilder sbMain = new StringBuilder("<html><body><pre>\n");
		final StringBuilder sbQualified = new StringBuilder("<html><body><pre>\n");
		
		for( final MetaData metaData : metaDataList ){
			System.out.println( "Generating Report for " +metaData.getSymbol() );
			
			final StringBuilder sbCloseData = new StringBuilder();
			final StringBuilder sbDates = new StringBuilder();
			final List<Long> volumeList = new ArrayList<Long>();
			
			Long maxVolume = 0L, sumOfVolume = 0L;
			Double maxVxC = 0.0, diffFromPrevClose = 0.0, prevClose = null;
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
					noteMaxVolxClose = "Note: V x C on " +Utility.getStrDate(ai.getAggregateInformationPK().getTradeDate())+ " was $" +Utility.getFormattedInteger(maxVxC);
					
					if( maxVxC < 0 ){
						noteMaxVolxClose = "<font color=red>" +noteMaxVolxClose+ "</font>";
					}
					
					if( prevClose != null ){
						diffFromPrevClose = ((ai.getClose() - prevClose)/ai.getClose())*100.0;
					}
				}
				
				prevClose = ai.getClose();
			}
			
			final Double avgVolume = new Double(sumOfVolume) / new Double(volumeList.size());
			
			String CHART_HTML_DATA = IntraDayDataProcessor.CHART_HTML;

			CHART_HTML_DATA = CHART_HTML_DATA.replace("~SYMBOL", metaData.getSymbol());
			CHART_HTML_DATA = CHART_HTML_DATA.replace("~CLOSE_DATA", sbCloseData.substring(1));
			CHART_HTML_DATA = CHART_HTML_DATA.replace("~VOLUME_DATA", volumeList.toString().replaceAll("\\[|\\]", "").replaceAll(" ", ""));
			CHART_HTML_DATA = CHART_HTML_DATA.replace("~DATES_DATA", sbDates.substring(1));
			
			final StringBuilder sbTmp = new StringBuilder();
			sbTmp.append( "<a href='http://www.google.com/finance?q=" +metaData.getSymbol()+ "' target='_new'>" +metaData.getSymbol()+ "</a> " );
			sbTmp.append( noteMaxVolxClose +" \n" );
			sbTmp.append( CHART_HTML_DATA +"\n\n" );
			
			sbMain.append( sbTmp.toString() );
			
			// Checks for Qualified only.
			if( Math.abs(maxVxC) >= Double.parseDouble( properties.getProperty("qualification.max.vxc.greater.than") )
				&& Math.abs(maxVolume) >= (avgVolume * Double.parseDouble(properties.getProperty("qualification.max.vol.times.of.average.vol")) ) 
				&& Math.abs(diffFromPrevClose) >= 5.0 ){
				
				if( metaData.getMktCap() != null ){
					if( Math.abs(maxVxC) >= (metaData.getMktCap() / 20.0) ){
						sbQualified.append( sbTmp.toString() );
					}
				}else{
					sbQualified.append( sbTmp.toString() );
				}
			}
			
		}
		sbMain.append( "</pre></body></html>" );
		sbQualified.append( "</pre></body></html>" );
		
		Utility.saveContent(properties.getProperty("rpt.aggregate.path"), sbMain.toString());
		Utility.saveContent(properties.getProperty("rpt.aggregate.for.qualified.path"), sbQualified.toString());
	}
	
	private static void pullAndSaveAggregateInformationFromGoogle(final List<MetaData> metaDataList, final StockService stockService) throws Exception{
		int ctr = 0;
		final List<String> exceptionSymbols = new ArrayList<String>();
		for(final MetaData metaData : metaDataList){
			System.out.println( "Processing " +metaData.getSymbol() );
			try{
				final List<AggregateInformation> aiList = new ArrayList<AggregateInformation>();
				
				String GOOGLE_URL_INTRA_DAY = properties.getProperty("google.url.intra.day"); 
				String GOOGLE_URL_DAY_CLOSE = properties.getProperty("google.url.day.close");
				
				String intraDayContent = Utility.getContent( GOOGLE_URL_INTRA_DAY.replaceAll("~SYMBOL", metaData.getSymbol()) );
				String dayCloseContent = Utility.getContent( GOOGLE_URL_DAY_CLOSE.replaceAll("~SYMBOL", metaData.getSymbol()) );
				
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
				System.out.println( "Exception occured while processing " +metaData.getSymbol() );
				exceptionSymbols.add(metaData.getSymbol());
			}
			
			try{
				if( ++ctr % 10 == 0 ){
					int sleepTimeMillis = Integer.parseInt(properties.getProperty("sleep.time.millis"));
					System.out.println( "\nSleeping for " +sleepTimeMillis+ " millis .... " +Utility.round(((double)ctr/(double)metaDataList.size())*100.00)+ "% completed.\n" );
					Thread.sleep(sleepTimeMillis);
				}
			}
			catch(Exception e){
			}
		}
		
		if( !exceptionSymbols.isEmpty() ){
			System.out.println( "\nCould not process " +exceptionSymbols.size()+ " symbols: " +exceptionSymbols );
		}
	}
	
	private static byte[] convertJavaObjectToByteArray(Object obj) throws Exception{
		final ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream o = new ObjectOutputStream(b);
        o.writeObject(obj);
        return b.toByteArray();
	}
	
	private static List<IntraDayStructure> convertByteArrayToJavaObject(byte[] bArray) throws Exception{
		ByteArrayInputStream b = new ByteArrayInputStream(bArray);
        ObjectInputStream o = new ObjectInputStream(b);
        return (List<IntraDayStructure>) o.readObject();
	}
}

class MetaData{
	private String symbol;
	private Double mktCap;
	
	public MetaData(String symbol, Double mktCap) {
		this.symbol = symbol;
		this.mktCap = mktCap;
	}
	
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public Double getMktCap() {
		return mktCap;
	}
	public void setMktCap(Double mktCap) {
		this.mktCap = mktCap;
	}
}

