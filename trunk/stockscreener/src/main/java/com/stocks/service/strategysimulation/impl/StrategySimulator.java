package com.stocks.service.strategysimulation.impl;

import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Required;

import com.stocks.model.Nyse;
import com.stocks.model.NysePK;
import com.stocks.service.StockService;
import com.stocks.service.strategy.Strategy;
import com.stocks.service.strategysimulation.IStrategySimulator;
import com.stocks.util.Utility;

public class StrategySimulator implements IStrategySimulator {

	private StockService stockService;
	
	public StockService getStockService() {
		return stockService;
	}

	@Required
	public void setStockService(StockService stockService) {
		this.stockService = stockService;
	}
	
	private LinkedHashMap<Date, String> getWeeklyCalendar(Date startDate, Date endDate){
		final LinkedHashMap<Date, String> calendarMap = new LinkedHashMap<Date, String>();

		Calendar cStart = Calendar.getInstance();
		cStart.setTime( new Date( startDate.getTime() ) );
		cStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		
		Calendar cEnd = Calendar.getInstance();
		cEnd.setTime( new Date( endDate.getTime() ) );
		cEnd.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
		while( cStart.compareTo( cEnd ) <= 0 ){
			calendarMap.put( new Date( cStart.getTimeInMillis() ) , null);
			cStart.add(Calendar.DATE, 1);
		}
		
		return calendarMap;
	}
	
	private File mapToFile(final LinkedHashMap<Date, String> calendarMap, String symbol) throws Exception{
		final StringBuffer sb = new StringBuffer();
		sb.append( "<html><head><style>TD{font-size:8pt;}TR{background-color:#DADADA}</style></head><body style='font-size:8pt;'><table border='1'>" );
		
		final Map<Integer, Integer> mTotalTradingDays = new HashMap<Integer, Integer>(); //WeekDay Mon=1, TotalTradingDays
		final Map<Integer, Integer> mTotalSuccessDays = new HashMap<Integer, Integer>(); //WeekDay Mon=1, TotalSuccessDays

		Iterator<Date> iterator = calendarMap.keySet().iterator();
		while(iterator.hasNext()){
			Date date = iterator.next();
			Calendar cDate = Calendar.getInstance();
			cDate.setTime(date);
			
			if( cDate.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cDate.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ){
				continue;
			}
			
			int totalTradingDays = 0;
			if( mTotalTradingDays.get( cDate.get(Calendar.DAY_OF_WEEK) ) != null ){
				totalTradingDays = mTotalTradingDays.get( cDate.get(Calendar.DAY_OF_WEEK) );
			}
			
			int totalSuccessDays = 0;
			if( mTotalSuccessDays.get( cDate.get(Calendar.DAY_OF_WEEK) ) != null ){
				totalSuccessDays = mTotalSuccessDays.get( cDate.get(Calendar.DAY_OF_WEEK) );
			}

			if( cDate.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY ){
				sb.append("\n<tr>");
			}
			
			//sb.append( "\n\t<td title='" +Utility.getStrDate(date)+ "'>" +calendarMap.get( date )+ "</td>" );
			String value = calendarMap.get( date );
			if( value != null ){
				mTotalTradingDays.put( cDate.get(Calendar.DAY_OF_WEEK), totalTradingDays+1 );
				if( value.equals(SUCCESS_COLOR) ){
					mTotalSuccessDays.put( cDate.get(Calendar.DAY_OF_WEEK), totalSuccessDays+1 );
				}
			}
			
			sb.append( "\n\t<td title='" +Utility.getStrDate(date)+ "'");
			if( value != null ){
				sb.append( " bgcolor='" +value+ "'" );
			}
			sb.append( "></td>" );
			
			if( cDate.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY ){
				sb.append("\n</tr>");
			}
		}
		
		sb.append( "<tr>");
		int iGrandTotal = 0;
		int iGrandTotalSuccess = 0;
		for( int weekDay=Calendar.MONDAY; weekDay<= Calendar.FRIDAY; weekDay++ ){
			Integer totalTradingDays = mTotalTradingDays.get( weekDay );
			Integer totalSuccessDays = mTotalSuccessDays.get( weekDay );
			
			if( totalTradingDays != null ){
				iGrandTotal += totalTradingDays;
			}
			
			if( totalTradingDays != null && totalSuccessDays != null ){
				iGrandTotalSuccess += totalSuccessDays;
				sb.append( "<td>(" +totalSuccessDays+ "/" +totalTradingDays+ ") " +Utility.round( ((double)totalSuccessDays/(double)totalTradingDays) * 100.0)+ "%</td>");
			}else{
				sb.append( "<td></td>");
			}
		}
		sb.append( "</tr>");

		sb.append( "<tr><td align='center' colspan='5'>" +symbol+ "(" +iGrandTotalSuccess+ "/" +iGrandTotal+ ") " +Utility.round( ((double)iGrandTotalSuccess/(double)iGrandTotal) * 100.0)+ "%</td></tr>" );
		sb.append( "</table></body></html>" );
		
		File file = new File(RPT_FOLDER +symbol+"_rpt.html");
		FileWriter writer = new FileWriter(file, false);
		writer.append( sb.toString() );
		writer.close();
		
		return file;
	}

	public File runStrategy(final List<Strategy> strategyList, final Double expectedGainPercent) throws Exception{
		Collections.sort(strategyList, new Comparator(){
			public int compare(Object o1, Object o2) {
				Strategy s1 = (Strategy) o1;
				Strategy s2 = (Strategy) o2;
				return s1.getBuyDate().compareTo(s2.getBuyDate());
			}
		});
		
		LinkedHashMap<Date, String> calendarMap = getWeeklyCalendar(
				strategyList.get(0).getBuyDate(), 
				strategyList.get(strategyList.size()-1).getSellDate());

		for( final Strategy strategy : strategyList ){
			Nyse nyseBuy = getStockService().read( new NysePK(strategy.getBuyDate(), strategy.getSymbol()) );
			Nyse nyseSell = getStockService().read( new NysePK(strategy.getSellDate(), strategy.getSymbol()) );
			
			if( nyseSell != null ){
				Double expectedGain = nyseBuy.getClose() + (nyseBuy.getClose() * expectedGainPercent/100.0);
				if( nyseSell.getHigh() > expectedGain ){
					calendarMap.put( strategy.getSellDate(), SUCCESS_COLOR );
				}else{
					calendarMap.put( strategy.getSellDate(), FAILURE_COLOR );
				}
			}
		}
		
		return mapToFile( calendarMap, strategyList.get(0).getSymbol() );
	}

}
