package com.stocks;

import com.stocks.command.BseAlertReportCommand;
import com.stocks.command.BseReportCommand;
import com.stocks.command.NyseReportCommand;

public class ReportsBean{    
	private BseAlertReportCommand bseAlertReportCommand;
	private BseReportCommand bseReportCommand;
	private NyseReportCommand nyseReportCommand;

	public BseAlertReportCommand getBseAlertReportCommand() {
		return bseAlertReportCommand;
	}
	public void setBseAlertReportCommand(BseAlertReportCommand bseAlertReportCommand) {
		this.bseAlertReportCommand = bseAlertReportCommand;
	}
	public BseReportCommand getBseReportCommand() {
		return bseReportCommand;
	}
	public void setBseReportCommand(BseReportCommand bseReportCommand) {
		this.bseReportCommand = bseReportCommand;
	}
	public NyseReportCommand getNyseReportCommand() {
		return nyseReportCommand;
	}
	public void setNyseReportCommand(NyseReportCommand nyseReportCommand) {
		this.nyseReportCommand = nyseReportCommand;
	}
}
