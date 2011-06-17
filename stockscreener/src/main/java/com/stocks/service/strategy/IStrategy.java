package com.stocks.service.strategy;

import java.util.Date;
import java.util.List;

public interface IStrategy {
	final Double EXPECTED_GAIN = 1.20;

	List<Strategy> getStrategyList(Date startDate, String symbol) throws Exception;
	List<Strategy> getStrategyList(Date startDate, List<String> symbols) throws Exception;
	
}
