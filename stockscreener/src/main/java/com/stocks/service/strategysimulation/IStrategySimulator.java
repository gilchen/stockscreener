package com.stocks.service.strategysimulation;

import java.io.File;
import java.util.List;

import com.stocks.service.strategy.Strategy;

public interface IStrategySimulator {
	final String SUCCESS_COLOR = "GREEN";
	final String FAILURE_COLOR = "RED";
	final String HOLIDAY_COLOR = "magenta";
	
	final String RPT_FOLDER = "C:/Classroom/JSF/int_ref/workspace/trunk/stk/Analysis/reports/";
	File runStrategy(List<Strategy> strategyList, Double expectedGainPercent) throws Exception;
}
