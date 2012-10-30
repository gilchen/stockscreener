package com.stocks.standalone;

import com.stocks.util.Utility;

public class CurrentSnapshotBean {
	private String symbol;
	private String realTime;
	private String range;
	private String pcChange;
	private String industry;
	private String range52wL_pc;
	private String range52wH_pc;
	private String mktCap;
	private String avgVol10Day;
	private String dailyAvgTradeValue;
	private String pe;
	private String beta;
	private String divYield;
	private String analystConsensus;
	private String time;
	private String range52wL;
	private String range52wH;
	private String bestMatch;

	public CurrentSnapshotBean(String symbol, String realTime, String range,
			String pcChange, String industry, String range52wL_pc,
			String range52wH_pc, String mktCap, String avgVol10Day,
			String dailyAvgTradeValue, String pe, String beta, String divYield,
			String analystConsensus, String time, String range52wL,
			String range52wH, String bestMatch) {
		super();
		this.symbol = symbol;
		this.realTime = realTime;
		this.range = range;
		this.pcChange = pcChange;
		this.industry = industry;
		this.range52wL_pc = range52wL_pc;
		this.range52wH_pc = range52wH_pc;
		this.mktCap = mktCap;
		this.avgVol10Day = avgVol10Day;
		this.dailyAvgTradeValue = dailyAvgTradeValue;
		this.pe = pe;
		this.beta = beta;
		this.divYield = divYield;
		this.analystConsensus = analystConsensus;
		this.time = time;
		this.range52wL = range52wL;
		this.range52wH = range52wH;
		this.bestMatch = bestMatch;
	}
	
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public String getRealTime() {
		return realTime;
	}
	public void setRealTime(String realTime) {
		this.realTime = realTime;
	}
	public String getRange() {
		return range;
	}
	public void setRange(String range) {
		this.range = range;
	}
	public String getPcChange() {
		return pcChange;
	}
	public void setPcChange(String pcChange) {
		this.pcChange = pcChange;
	}
	public String getIndustry() {
		return industry;
	}
	public void setIndustry(String industry) {
		this.industry = industry;
	}
	public String getRange52wL_pc() {
		return range52wL_pc;
	}
	public void setRange52wL_pc(String range52wL_pc) {
		this.range52wL_pc = range52wL_pc;
	}
	public String getRange52wH_pc() {
		return range52wH_pc;
	}
	public void setRange52wH_pc(String range52wH_pc) {
		this.range52wH_pc = range52wH_pc;
	}
	public String getMktCapExpanded() {
		return Utility.convertFinancials(mktCap);
	}
	public String getMktCap() {
		return mktCap;
	}
	public void setMktCap(String mktCap) {
		this.mktCap = mktCap;
	}
	public String getAvgVol10DayExpanded() {
		return Utility.convertFinancials(avgVol10Day);
	}
	public String getAvgVol10Day() {
		return avgVol10Day;
	}
	public void setAvgVol10Day(String avgVol10Day) {
		this.avgVol10Day = avgVol10Day;
	}
	public String getDailyAvgTradeValue() {
		return dailyAvgTradeValue;
	}
	public void setDailyAvgTradeValue(String dailyAvgTradeValue) {
		this.dailyAvgTradeValue = dailyAvgTradeValue;
	}
	public String getPe() {
		return pe;
	}
	public void setPe(String pe) {
		this.pe = pe;
	}
	public String getBeta() {
		return beta;
	}
	public void setBeta(String beta) {
		this.beta = beta;
	}
	public String getDivYield() {
		return divYield;
	}
	public void setDivYield(String divYield) {
		this.divYield = divYield;
	}
	public String getAnalystConsensus() {
		return analystConsensus;
	}
	public void setAnalystConsensus(String analystConsensus) {
		this.analystConsensus = analystConsensus;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getRange52wL() {
		return range52wL;
	}
	public void setRange52wL(String range52wL) {
		this.range52wL = range52wL;
	}
	public String getRange52wH() {
		return range52wH;
	}
	public void setRange52wH(String range52wH) {
		this.range52wH = range52wH;
	}
	public String getBestMatch() {
		return bestMatch;
	}
	public void setBestMatch(String bestMatch) {
		this.bestMatch = bestMatch;
	}
	
	@Override
	public String toString() {
		return String.format(
				CurrentSnapshot.ROW_FORMAT,
				symbol,
				realTime,
				range,
				pcChange,
				industry,
				range52wL_pc,
				range52wH_pc,
				mktCap,
				getMktCapExpanded(),
				avgVol10Day,
				getAvgVol10DayExpanded(),
				dailyAvgTradeValue,
				pe,
				beta,
				divYield,
				analystConsensus,
				time,
				range52wL,
				range52wH,
				"\""+bestMatch+"\"");
	}

	
}
