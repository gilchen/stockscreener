package com.stocks.service.strategy.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.josql.Query;
import org.josql.QueryResults;

import com.stocks.model.Nyse;
import com.stocks.service.StockService;
import com.stocks.service.strategy.IStrategy;
import com.stocks.service.strategy.Strategy;
import com.stocks.util.Utility;

public class PastWeekdaySuccessBasedStrategy implements IStrategy {
	private StockService stockService;

	public StockService getStockService() {
		return stockService;
	}

	public void setStockService(StockService stockService) {
		this.stockService = stockService;
	}

	public List<Strategy> getStrategyList(Date startDate, List<String> symbols) throws Exception {
		throw new RuntimeException("Not implemented.");
	}
	
	public List<Strategy> getStrategyList(Date startDate, String symbol) throws Exception{
		final List<Strategy> strategyList = new LinkedList<Strategy>();
		
		Calendar today = Calendar.getInstance();
		final List<Nyse> nyseList = getStockService().findStockBySymbolBetweenTradeDates(symbol, startDate, today.getTime() );

		//int weekDay = 5;
		for( int weekDay=1; weekDay <= 5; weekDay++ ){

			Query q = new Query();
			q.parse("select * from com.stocks.model.Nyse where nysePK.tradeDate.day = " +weekDay);
			//q.parse("select * from com.stocks.model.Nyse where nysePK.tradeDate.day = 5");
			QueryResults qr = q.execute( nyseList );
			
			final List<Nyse> nyseSubList = qr.getResults();
			
			for(int i=1; i<nyseSubList.size(); i++){
				Nyse currentWeekDay = nyseSubList.get(i);
				Nyse lastWeekDay = nyseSubList.get(i-1);
				
				//System.out.println( Utility.getStrDate( lastWeekDay.getNysePK().getTradeDate() )+" -- "+ Utility.getStrDate( currentWeekDay.getNysePK().getTradeDate() ) );
				
				Double cExpectedGain = currentWeekDay.getPrevious().getClose() + (currentWeekDay.getPrevious().getClose() * EXPECTED_GAIN/100.0);
				if( lastWeekDay.getPrevious() != null ){
					Double pExpectedGain = lastWeekDay.getPrevious().getClose() + (lastWeekDay.getPrevious().getClose() * EXPECTED_GAIN/100.0);
					
					if( currentWeekDay.getHigh() > cExpectedGain && lastWeekDay.getHigh() > pExpectedGain ){
						Calendar buyDate = Calendar.getInstance();
						buyDate.setTime( new Date( currentWeekDay.getPrevious().getNysePK().getTradeDate().getTime() ) );
						buyDate.add(Calendar.DATE, 8);
						// Below conditions are to set the correct weekday for Buying if currentWeekDay.getPrevious() was a Holiday
						if( weekDay == 1 ){
							buyDate.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
						}else if( weekDay == 2 ){
							buyDate.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
						}else if( weekDay == 3 ){
							buyDate.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
						}else if( weekDay == 4 ){
							buyDate.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
						}else if( weekDay == 5 ){
							buyDate.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
						}
						
						buyDate.setTime( getStockService().getPreviousBusinessDay( buyDate.getTime() ) );
						
						Calendar sellDate = Calendar.getInstance();
						sellDate.setTime( new Date( currentWeekDay.getNysePK().getTradeDate().getTime() ) );
						sellDate.add(Calendar.DATE, 7);
						
						// Checking for Holiday once should be enough
						if( getStockService().isHoliday( sellDate.getTime() ) ){
							sellDate.add(Calendar.DATE, 7);
							buyDate.add(Calendar.DATE, 7);
						}

						strategyList.add( new Strategy(currentWeekDay.getNysePK().getSymbol(), buyDate.getTime(), EXPECTED_GAIN, sellDate.getTime()) );
					}
				}
			}
		
		}
		
		return strategyList;
	}

}
