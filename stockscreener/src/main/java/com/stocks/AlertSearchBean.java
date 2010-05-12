package com.stocks;

import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import com.stocks.model.Alert;
import com.stocks.model.Nyse;
import com.stocks.model.NyseAlert;
import com.stocks.model.Report;
import com.stocks.search.AlertResult;
import com.stocks.search.NyseAlertResult;
import com.stocks.service.StockService;
import com.stocks.util.Utility;

public class AlertSearchBean {
	public static final Double BROKERAGE = 3.00;
	
    private String trxType;
    private String stockExchange;
    private String isActive;
    private String graphHtmlContent;
    
    private AlertBean alertBean;
    private DataModel dmBseAlertResults;
    private DataModel dmNyseAlertResults;
    
    // Simulation Properties
    private String symbols;
    private Date simulationBuyDate;
    private Double simulationSltpPercent;
    private Double simulationExpectedGainPercent;
    private Double investmentAmount;
    private DataModel dmNyseSimulation;
    
    /* Doodh-Chhachh Pattern
     * Rules to follow in trading
     * ==========================
     * Rule 1:
     *	If a Stop Loss is hit Then
     *		Do not enter that Stock until it achieves one target
     *
     *	Reenter when a Stop Loss is hit after one target achieved.
    */
    private int totalSltpHit;
    private int totalTargetHit;

    // Services
    private StockService stockService;
    
    public AlertSearchBean() {
    	clear(null);
	}

    public void clear(ActionEvent ae){
    	this.setTrxType(null);
    	this.setStockExchange(null);
    	this.setIsActive("");
    	this.setGraphHtmlContent(null);
    	this.setDmBseAlertResults(null);
    	this.setDmNyseAlertResults(null);
    	this.setSymbols("MMM, AA, AXP, T, BAC, BA, CAT, CVX, CSCO, KO, DD, XOM, GE, HPQ, HD, INTC, IBM, JNJ, JPM, KFT, MCD, MRK, MSFT, PFE, PG, TRV, UTX, VZ, WMT, DIS");
    	
    	Calendar calendar = Calendar.getInstance();
    	calendar.set(Calendar.DATE, 1);
    	calendar.set(Calendar.MONTH, Calendar.AUGUST);
    	calendar.set(Calendar.YEAR, 2009);
    	this.setSimulationBuyDate( calendar.getTime() );

    	this.setSimulationSltpPercent( 1.0 );
    	this.setSimulationExpectedGainPercent( 6.0 );
    	this.setInvestmentAmount(5000.00);
    	this.setTotalSltpHit(0);
    	this.setTotalTargetHit(0);
    }
    
    // Getter / Setters
	public String getTrxType() {
		return trxType;
	}
	public void setTrxType(String trxType) {
		this.trxType = trxType;
	}

	public int getTotalSltpHit() {
		return totalSltpHit;
	}

	public void setTotalSltpHit(int totalSltpHit) {
		this.totalSltpHit = totalSltpHit;
	}

	public int getTotalTargetHit() {
		return totalTargetHit;
	}

	public void setTotalTargetHit(int totalTargetHit) {
		this.totalTargetHit = totalTargetHit;
	}

	public String getStockExchange() {
		return stockExchange;
	}

	public void setStockExchange(String stockExchange) {
		this.stockExchange = stockExchange;
	}

	public String getIsActive() {
		return isActive;
	}

	public void setIsActive(String isActive) {
		this.isActive = isActive;
	}

	public String getGraphHtmlContent() {
		return graphHtmlContent;
	}

	public void setGraphHtmlContent(String graphHtmlContent) {
		this.graphHtmlContent = graphHtmlContent;
	}

	public AlertBean getAlertBean() {
		return alertBean;
	}

	public void setAlertBean(AlertBean alertBean) {
		this.alertBean = alertBean;
	}

	public DataModel getDmBseAlertResults() {
		return dmBseAlertResults;
	}

	public void setDmBseAlertResults(DataModel dmBseAlertResults) {
		this.dmBseAlertResults = dmBseAlertResults;
	}

	public DataModel getDmNyseAlertResults() {
		return dmNyseAlertResults;
	}

	public void setDmNyseAlertResults(DataModel dmNyseAlertResults) {
		this.dmNyseAlertResults = dmNyseAlertResults;
	}

	public String getSymbols() {
		return symbols;
	}

	public void setSymbols(String symbols) {
		this.symbols = symbols;
	}

	public Date getSimulationBuyDate() {
		return simulationBuyDate;
	}

	public void setSimulationBuyDate(Date simulationBuyDate) {
		this.simulationBuyDate = simulationBuyDate;
	}

	public Double getSimulationSltpPercent() {
		return simulationSltpPercent;
	}

	public void setSimulationSltpPercent(Double simulationSltpPercent) {
		this.simulationSltpPercent = simulationSltpPercent;
	}

	public Double getSimulationExpectedGainPercent() {
		return simulationExpectedGainPercent;
	}

	public void setSimulationExpectedGainPercent(
			Double simulationExpectedGainPercent) {
		this.simulationExpectedGainPercent = simulationExpectedGainPercent;
	}

	public Double getInvestmentAmount() {
		return investmentAmount;
	}

	public void setInvestmentAmount(Double investmentAmount) {
		this.investmentAmount = investmentAmount;
	}

	public DataModel getDmNyseSimulation() {
		return dmNyseSimulation;
	}

	public void setDmNyseSimulation(DataModel dmNyseSimulation) {
		this.dmNyseSimulation = dmNyseSimulation;
	}

	public StockService getStockService() {
		return stockService;
	}
	public void setStockService(StockService stockService) {
		this.stockService = stockService;
	}
	
	public void search(ActionEvent ae){
		this.setGraphHtmlContent(null);
		
		if( getStockExchange().equals("BSE") ){
			this.setDmBseAlertResults( new ListDataModel( getStockService().getAllAlertResultsByTrxType( getTrxType(), getIsActive()) ) );
		}else if( getStockExchange().equals("NYSE") ){
			this.setDmNyseAlertResults( new ListDataModel(getStockService().getAllNyseAlertResultsByTrxType( getTrxType(), getIsActive() ) ) );
		}
	}
	
	public void editAlert(ActionEvent ae){
		AlertBean ab = this.getAlertBean();
		Alert alert = ((AlertResult) getDmBseAlertResults().getRowData()).getAlert();
		ab.setAlertId(alert.getAlertId());
		ab.setStockCode(alert.getBseIciciMapping().getStockCode());
		ab.setTrxType(alert.getTrxType());
		ab.setEventDate(alert.getEventDate());
		ab.setOpportunityType(alert.getOpportunityType());
		ab.setEventPrice(alert.getEventPrice());
		ab.setTargetPrice(alert.getTargetPrice());
		ab.setSltp(alert.getSltp());
		ab.setEventType(alert.getEventType());
		ab.setQty(alert.getQty());
		ab.setIsActive(alert.getIsActive());
		ab.setAlertFor("BSE");
	}

	public void editNyseAlert(ActionEvent ae){
		AlertBean ab = this.getAlertBean();
		NyseAlert alert = ((NyseAlertResult) getDmNyseAlertResults().getRowData()).getNyseAlert();
		ab.setAlertId(alert.getAlertId());
		ab.setStockCode(alert.getSymbol());
		ab.setTrxType(alert.getTrxType());
		ab.setEventDate(alert.getEventDate());
		ab.setOpportunityType(alert.getOpportunityType());
		ab.setEventPrice(alert.getEventPrice());
		ab.setTargetPrice(alert.getTargetPrice());
		ab.setSltp(alert.getSltp());
		ab.setEventType(alert.getEventType());
		ab.setQty(alert.getQty());
		ab.setIsActive(alert.getIsActive());
		ab.setAlertFor("NYSE");
	}

	public void viewGraph(ActionEvent ae){
		Alert alert = ((AlertResult) getDmBseAlertResults().getRowData()).getAlert();
		final String stockCode = alert.getBseIciciMapping().getStockCode() +",";
		try {
			Report report = getStockService().getReport(Report.ReportName.BseAlertReportCommand.toString());
			LineNumberReader reader = new LineNumberReader( new StringReader(report.getContent()) );
			String line = null;
			StringBuffer sb = new StringBuffer( "<pre>" );
			while( (line = reader.readLine()) != null ){
				if( line.contains( stockCode ) ){
					sb.append( line +"\n"+ reader.readLine()+"\n" );
					break;
				}
			}
			sb.append("</pre>");
			setGraphHtmlContent( sb.toString() );
		} catch (Exception e) {
			setGraphHtmlContent( e.getMessage() );
		}
	}
	
	public void viewNyseGraph(ActionEvent ae){
		NyseAlert nyseAlert = ((NyseAlertResult) getDmNyseAlertResults().getRowData()).getNyseAlert();
		final String symbol = nyseAlert.getSymbol() +",";
		try {
			Report report = getStockService().getReport(Report.ReportName.NyseAlertReportCommand.toString());
			LineNumberReader reader = new LineNumberReader( new StringReader(report.getContent()) );
			String line = null;
			StringBuffer sb = new StringBuffer( "<pre>" );
			while( (line = reader.readLine()) != null ){
				if( line.contains( symbol ) ){
					sb.append( line +"\n"+ reader.readLine()+"\n" );
					break;
				}
			}
			sb.append("</pre>");
			setGraphHtmlContent( sb.toString() );
		} catch (Exception e) {
			setGraphHtmlContent( e.getMessage() );
		}
	}
	
	/**
	 * Scenario: Stock is bought at close price on the given date.
	 */
	public void simulateTransactions(ActionEvent ae){
		this.setTotalSltpHit(0);
		this.setTotalTargetHit(0);
		
		List<NyseSimulation> nyseSimulationList = new ArrayList<NyseSimulation>();

		int totalSltp = 0;
		int totalTarget = 0;

		String djia[] = this.getSymbols().split("[,]"); //new String[]{"MMM", "AA", "AXP", "T", "BAC", "BA", "CAT", "CVX", "CSCO", "KO", "DD", "XOM", "GE", "HPQ", "HD", "INTC", "IBM", "JNJ", "JPM", "KFT", "MCD", "MRK", "MSFT", "PFE", "PG", "TRV", "UTX", "VZ", "WMT", "DIS"};
		for( String symbol : djia ){
			symbol = symbol.trim();

			List<Nyse> nyseList = getStockService().findStockBySymbolAndTradeDate(symbol, getSimulationBuyDate());
			if( nyseList == null || nyseList.isEmpty() ){
				System.out.println( "No data found for " +symbol );
				continue;
			}

			Nyse nyseBuy = nyseList.get(0);
			Nyse nyseStopLoss = null;
			Nyse nyseTarget = null;
			
			double sltpPrice = nyseBuy.getClose() - (nyseBuy.getClose() * (getSimulationSltpPercent()/100.0));
			double expectedGainPrice = nyseBuy.getClose() + ( nyseBuy.getClose() * (getSimulationExpectedGainPercent()/100.0) );
			
			int noOfSltpHit = 0;
			String renderClass = null;
			for( int i=1; i<nyseList.size(); i++ ){
				Nyse nyse = nyseList.get(i);
				renderClass = null;
				if( (sltpPrice > nyse.getLow() && sltpPrice < nyse.getHigh()) || sltpPrice > nyse.getHigh() ){
					// Stop Loss hit
					noOfSltpHit++;
					nyseStopLoss = nyse;

					if( noOfSltpHit == 2 ){
						renderClass = "error";
					}
					nyseSimulationList.add(new NyseSimulation(nyseBuy, nyseStopLoss, nyseTarget, (nyseStopLoss == null ? null : Utility.round(sltpPrice)), (nyseTarget == null ? null : Utility.round(expectedGainPrice) ), renderClass));
					nyseStopLoss = null;

					// Reset vars
					nyseBuy = nyseList.get(i);
					sltpPrice = nyseBuy.getClose() - (nyseBuy.getClose() * (getSimulationSltpPercent()/100.0));
					expectedGainPrice = nyseBuy.getClose() + ( nyseBuy.getClose() * (getSimulationExpectedGainPercent()/100.0) );
				}else if( (expectedGainPrice > nyse.getLow() && expectedGainPrice < nyse.getHigh()) || expectedGainPrice < nyse.getLow() ){
					// Target achieved
					if( noOfSltpHit == 1 ){
						totalTarget++;
						renderClass = "success";
					}
					if( noOfSltpHit > 1 ){
						totalSltp++;
					}
					noOfSltpHit = 0;
					nyseTarget = nyse;
					nyseSimulationList.add(new NyseSimulation(nyseBuy, nyseStopLoss, nyseTarget, (nyseStopLoss == null ? null : Utility.round(sltpPrice)), (nyseTarget == null ? null : Utility.round(expectedGainPrice) ), renderClass));
					nyseTarget = null;

					// Reset vars
					nyseBuy = nyseList.get(i);
					sltpPrice = nyseBuy.getClose() - (nyseBuy.getClose() * (getSimulationSltpPercent()/100.0));
					expectedGainPrice = nyseBuy.getClose() + ( nyseBuy.getClose() * (getSimulationExpectedGainPercent()/100.0) );
				}
			}
			if( noOfSltpHit > 1 ){
				totalSltp++;
			}
			nyseSimulationList.add(new NyseSimulation(nyseBuy, nyseStopLoss, nyseTarget, (nyseStopLoss == null ? null : Utility.round(sltpPrice)), (nyseTarget == null ? null : Utility.round(expectedGainPrice) ), renderClass));
		}
		this.setTotalSltpHit(totalSltp);
		this.setTotalTargetHit(totalTarget);
		
		this.setDmNyseSimulation( new ListDataModel( nyseSimulationList ) );
	}

	public Double getSimulationProfit(){
		NyseSimulation ns = (NyseSimulation) this.getDmNyseSimulation().getRowData();
		Double d = (ns.getTarget() - ns.getNyseBuy().getClose()) * ( getInvestmentAmount() / ns.getNyseBuy().getClose() )-(BROKERAGE*2.0);
		return Utility.round(d);
	}

	public Double getSimulationLoss(){
		NyseSimulation ns = (NyseSimulation) this.getDmNyseSimulation().getRowData();
		Double d = (ns.getNyseBuy().getClose() - ns.getSltp()) * ( getInvestmentAmount() / ns.getNyseBuy().getClose() )+(BROKERAGE*2.0);
		return Utility.round(-d);
	}
}
