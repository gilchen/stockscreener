package com.stocks.service.statistics;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.stocks.model.Nyse;
import com.stocks.service.StockService;
import com.stocks.util.Utility;

public class IntraDayStatistics {
	private StockService stockService;

	public StockService getStockService() {
		return stockService;
	}

	public void setStockService(StockService stockService) {
		this.stockService = stockService;
	}
	
	public static void main(String args[]){
		final Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		map.put( 0, 0 );
		map.put( 2, 0 );
		map.put( 4, 0 );
		map.put( 6, 0 );
		map.put( 8, 0 );

		for(Double d = -4.5; d < 5.5; d++){
			System.out.println( getKeyForRange(d) + "(" +d+ ")" );
		}
		
	}
	
	private static String getKeyForRange(Double value){
		BigDecimal bd = new BigDecimal( value );
		bd = bd.setScale(0, BigDecimal.ROUND_DOWN);
		int key = 0;
		
		if(value >= 0){
			key = bd.intValue() % 2 == 1 ? bd.intValue()-1 : bd.intValue();
			return key+"% to "+(key+2)+ "%";
		}else{
			key = bd.intValue() % 2 == -1 ? bd.intValue()+1 : bd.intValue();
			return key+ "% to " +(key-2)+"%";
		}
	}

	public void generateStatistics(Date startDate, String symbol){
		Calendar today = Calendar.getInstance();
		final List<Nyse> nyseList = getStockService().findStockBySymbolBetweenTradeDates(symbol, startDate, today.getTime() );
		
		final Map<String, Integer> mPercentKeyWithTotalCount = new HashMap<String, Integer>();

		//int weekDay = 5;
		//for( int weekDay=1; weekDay <= 5; weekDay++ ){

//			Query q = new Query();
//			q.parse("select * from com.stocks.model.Nyse where nysePK.tradeDate.day = " +weekDay);
//			QueryResults qr = q.execute( nyseList );
//			
//			final List<Nyse> nyseSubList = qr.getResults();
			final List<Nyse> nyseSubList = nyseList; 
			
			for(int i=1; i<nyseSubList.size(); i++){
				Nyse current = nyseSubList.get(i);

				Double highPercentChange = ( (current.getHigh() - current.getPrevious().getClose()) / current.getPrevious().getClose() ) * 100.0;
				Double lowPercentChange = ( (current.getLow() - current.getPrevious().getClose()) / current.getPrevious().getClose() ) * 100.0;
				
				String highKey = getKeyForRange( highPercentChange );
				String lowKey = getKeyForRange( lowPercentChange );
				if( mPercentKeyWithTotalCount.get( highKey ) == null ){
					mPercentKeyWithTotalCount.put(highKey, new Integer(0));
				}
				if( mPercentKeyWithTotalCount.get( lowKey ) == null ){
					mPercentKeyWithTotalCount.put(lowKey, new Integer(0));
				}

				if( highKey.indexOf("-") == -1 ){
					mPercentKeyWithTotalCount.put(highKey, mPercentKeyWithTotalCount.get(highKey)+1);
				}

				if( lowKey.indexOf("-") != -1 ){
					mPercentKeyWithTotalCount.put(lowKey, mPercentKeyWithTotalCount.get(lowKey)+1);
				}
			}

			final TreeSet<String> keySet = new TreeSet<String>(new Comparator(){
				public int compare(Object o1, Object o2) {
					String s1 = o1.toString();
					String s2 = o2.toString();
					// 0% to -2%
					//      ^  ^
					//      |  |
					Integer i1 = new Integer( s1.substring( s1.lastIndexOf(" ")+1, s1.lastIndexOf("%")) );
					Integer i2 = new Integer( s2.substring( s2.lastIndexOf(" ")+1, s2.lastIndexOf("%")) );
					
					return i1.compareTo(i2);
				}
			});
			
			keySet.addAll( mPercentKeyWithTotalCount.keySet() );
			for( String key : keySet ){
				int count = mPercentKeyWithTotalCount.get(key);
				System.out.println( key +" -> " +count+ " (" +Utility.round(((double)count/(double)nyseSubList.size())*100.0)+ "%)" );
			}
		//}

	}
}
