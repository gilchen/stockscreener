package com.stocks.standalone;

import java.io.File;
import java.util.Calendar;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.stocks.service.strategy.IStrategy;
import com.stocks.service.strategy.Strategy;
import com.stocks.service.strategy.impl.PastWeekdaySuccessBasedStrategy;
import com.stocks.service.strategysimulation.impl.StrategySimulator;

public class Caller {
	final static String[] ETF_LIST = {"UDOW", "UPRO", "TQQQ", "UMDD", "URTY", "FAS", "TNA", "BGU", "TMF", "CZM", "DZK", "EDC", "ERX", "LBJ", "MWJ", "DRN", "SOXL", "TYH", "SDOW", "SPXU", "SQQQ", "SRTY", "FAZ", "TZA", "BGZ", "TMV", "DPK", "EDZ", "ERY", "MWN", "DRV", "SOXS", "TYP"};
	//final static String[] ETF_LIST = {"INDL", "INDZ"};

	private List<String> symbols;
	
	public List<String> getSymbols() {
		return symbols;
	}

	public void setSymbols(List<String> symbols) {
		this.symbols = symbols;
	}

	public static void main(String args[]) throws Exception {
		ApplicationContext context = new FileSystemXmlApplicationContext("src/main/resources/applicationContext.xml");
		PastWeekdaySuccessBasedStrategy pastWeekdaySuccessBasedStrategy = (PastWeekdaySuccessBasedStrategy) context.getBean("pastWeekdaySuccessBasedStrategy");
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DATE, 1);
		calendar.set(Calendar.MONDAY, Calendar.MARCH);
		calendar.set(Calendar.YEAR, 2010);

		for(String symbol : ETF_LIST){
			final List<Strategy> strategyList = pastWeekdaySuccessBasedStrategy.getStrategyList( calendar.getTime(), symbol );
			StrategySimulator strategySimulator = (StrategySimulator) context.getBean("strategySimulator");
			File file = strategySimulator.runStrategy(strategyList, IStrategy.EXPECTED_GAIN);
			//System.out.println( "Report generated @ " +file.getAbsolutePath() );
		}		
		System.out.println( "Done." );
		System.exit(0);
	}
}
