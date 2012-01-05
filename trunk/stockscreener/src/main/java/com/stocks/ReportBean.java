package com.stocks;

import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.impl.ContextBase;

import com.stocks.command.AbstractCommand;
import com.stocks.command.ChainBase;
import com.stocks.model.Report;
import com.stocks.service.StockService;

public class ReportBean{    
	private StockService stockService;
	private String content;
    private ChainBase reportChain;
    private List<String> commandsToExecute;
    private String filterBseEventType;
    private String filterNyseEventType;
    private Date startDate;
    private Date endDate;

    public ReportBean() {
		Calendar dt = Calendar.getInstance();
		setEndDate((Date) dt.getTime().clone());

		dt.add( Calendar.DAY_OF_YEAR, -30*6 ); // 6 months ago
		setStartDate( dt.getTime() );
	}
    
	public ChainBase getReportChain() {
		return reportChain;
	}

	public void setReportChain(ChainBase reportChain) {
		this.reportChain = reportChain;
	}
	
	public List<SelectItem> getCommands(){
		final List<SelectItem> list = new ArrayList<SelectItem>();
		Command[] commands = getReportChain().getCommands();
		for(final Command command : commands){
			list.add( new SelectItem( command.toString(), command.toString() ) );
		}
		return list;
	}

	public void setCommands(List<SelectItem> list){
		// do nothing
	}
	
	public List<String> getCommandsToExecute() {
		return commandsToExecute;
	}

	public void setCommandsToExecute(List<String> commandsToExecute) {
		this.commandsToExecute = commandsToExecute;
	}

	public StockService getStockService() {
		return stockService;
	}

	public void setStockService(StockService stockService) {
		this.stockService = stockService;
	}
	
	public String getContent() {
		return content;
	}

	public String getFilterBseEventType() {
		return filterBseEventType;
	}

	public void setFilterBseEventType(String filterBseEventType) {
		this.filterBseEventType = filterBseEventType;
	}

	public String getFilterNyseEventType() {
		return filterNyseEventType;
	}

	public void setFilterNyseEventType(String filterNyseEventType) {
		this.filterNyseEventType = filterNyseEventType;
	}

	public void setContent(String content) {
		if(content != null && !content.trim().equals("")){
			this.content = content;
		}else{
			this.content = "No Data";
		}
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public void executeReportChain(ActionEvent ae){
		Map<String, Object> commandsMap = new HashMap<String, Object>();
		commandsMap.put(AbstractCommand.COMMANDS_TO_EXECUTE, this.getCommandsToExecute());
		commandsMap.put(AbstractCommand.START_DATE, this.getStartDate());
		commandsMap.put(AbstractCommand.END_DATE, this.getEndDate());
		
		try{
			reportChain.execute(new ContextBase( commandsMap ));
			FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_INFO, "Reports generated successfully.", "Reports generated successfully."));
		}
		catch(Exception e){
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage("", new FacesMessage(FacesMessage.SEVERITY_INFO, e.getMessage(), e.getMessage()));
		}
	}

	public void getBseAlertReport(ActionEvent ae){
		try {
			Report report = getStockService().getReport(Report.ReportName.BseAlertReportCommand.toString());
			setContent( report.getContent() );
		} catch (Exception e) {
			setContent( e.getMessage() );
		}
	}

	public void getNyseAlertReport(ActionEvent ae){
		try {
			Report report = getStockService().getReport(Report.ReportName.NyseAlertReportCommand.toString());
			setContent( report.getContent() );
		} catch (Exception e) {
			setContent( e.getMessage() );
		}
	}

	public void getBseReport(ActionEvent ae){
		try {
			Report report = getStockService().getReport(Report.ReportName.BseReportCommand.toString());
			setContent( report.getContent() );
		} catch (Exception e) {
			setContent( e.getMessage() );
		}
	}

	public void getNyseReport(ActionEvent ae){
		try {
			Report report = getStockService().getReport(Report.ReportName.NyseReportCommand.toString());
			setContent( report.getContent() );
		} catch (Exception e) {
			setContent( e.getMessage() );
		}
	}
	
	public void getNyseNPercentCorrectionInMMonthsReport(ActionEvent ae){
		try {
			Report report = getStockService().getReport(Report.ReportName.NyseNPercentCorrectionInMMonthsReportCommand.toString());
			setContent( report.getContent() );
		} catch (Exception e) {
			setContent( e.getMessage() );
		}
	}
	
	public void getNyseNPercentUpFromBottomReport(ActionEvent ae){
		try {
			Report report = getStockService().getReport(Report.ReportName.NyseNPercentUpFromBottomCommand.toString());
			setContent( report.getContent() );
		} catch (Exception e) {
			setContent( e.getMessage() );
		}
	}

	public void getNyseNPercentUpFromBottomSimulationReport(ActionEvent ae){
		try {
			Report report = getStockService().getReport(Report.ReportName.NyseNPercentUpFromBottomSimulationCommand.toString());
			setContent( report.getContent() );
		} catch (Exception e) {
			setContent( e.getMessage() );
		}
	}
		
	public void getNyseNPercentUpFromBottomScanningSimulationReport(ActionEvent ae){
		try {
			Report report = getStockService().getReport(Report.ReportName.NyseNPercentUpFromBottomScanningSimulationCommand.toString());
			setContent( report.getContent() );
		} catch (Exception e) {
			setContent( e.getMessage() );
		}
	}
	
	public void getNyseBreakingHighsEachTimeReport(ActionEvent ae){
		try {
			Report report = getStockService().getReport(Report.ReportName.NyseBreakingHighsEachTimeCommand.toString());
			setContent( report.getContent() );
		} catch (Exception e) {
			setContent( e.getMessage() );
		}
	}

	public void getNyseTxReport(ActionEvent ae){
		try {
			Report report = getStockService().getReport(Report.ReportName.NyseTxReportCommand.toString());
			setContent( report.getContent() );
		} catch (Exception e) {
			setContent( e.getMessage() );
		}
	}
	
	public void filterBseAlertReport(ActionEvent ae){
		try {
			Report report = getStockService().getReport(Report.ReportName.BseAlertReportCommand.toString());
			LineNumberReader reader = new LineNumberReader( new StringReader(report.getContent()) );
			String line = null;
			StringBuffer sb = new StringBuffer( "<pre>" );
			while( (line = reader.readLine()) != null ){
				if( line.contains( getFilterBseEventType() ) ){
					sb.append( line +"\n"+ reader.readLine()+"\n" );
				}
			}
			sb.append("</pre>");
			setContent( sb.toString() );
		} catch (Exception e) {
			setContent( e.getMessage() );
		}
	}

	public void filterNyseAlertReport(ActionEvent ae){
		try {
			Report report = getStockService().getReport(Report.ReportName.NyseAlertReportCommand.toString());
			LineNumberReader reader = new LineNumberReader( new StringReader(report.getContent()) );
			String line = null;
			StringBuffer sb = new StringBuffer( "<pre>" );
			while( (line = reader.readLine()) != null ){
				if( line.contains( getFilterNyseEventType() ) ){
					sb.append( line +"\n"+ reader.readLine()+"\n" );
				}
			}
			sb.append("</pre>");
			setContent( sb.toString() );
		} catch (Exception e) {
			setContent( e.getMessage() );
		}
	}
}
