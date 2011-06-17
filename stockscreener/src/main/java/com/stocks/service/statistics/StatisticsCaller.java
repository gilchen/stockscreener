package com.stocks.service.statistics;

import java.util.Calendar;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class StatisticsCaller {
	//final static String[] ETF_LIST = {"UDOW", "UPRO", "TQQQ", "UMDD", "URTY", "FAS", "TNA", "BGU", "TMF", "CZM", "DZK", "EDC", "ERX", "LBJ", "MWJ", "DRN", "SOXL", "TYH", "SDOW", "SPXU", "SQQQ", "SRTY", "FAZ", "TZA", "BGZ", "TMV", "DPK", "EDZ", "ERY", "MWN", "DRV", "SOXS", "TYP"};
	final static String[] ETF_LIST = {"TZA"};

	private List<String> symbols;
	
	public List<String> getSymbols() {
		return symbols;
	}

	public void setSymbols(List<String> symbols) {
		this.symbols = symbols;
	}

	public static void main(String args[]) throws Exception {
		ApplicationContext context = new FileSystemXmlApplicationContext("src/main/resources/applicationContext.xml");
		IntraDayStatistics intraDayStatistics = (IntraDayStatistics) context.getBean("intraDayStatistics");
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DATE, 1);
		calendar.set(Calendar.MONDAY, Calendar.MARCH);
		calendar.set(Calendar.YEAR, 2010);

		for(String symbol : ETF_LIST){
			intraDayStatistics.generateStatistics(calendar.getTime(), symbol);
		}		
		System.out.println( "Done." );
		System.exit(0);
	}
}
