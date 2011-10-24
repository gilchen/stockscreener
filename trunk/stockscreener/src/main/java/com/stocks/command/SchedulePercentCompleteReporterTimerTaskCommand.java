package com.stocks.command;

import java.util.Date;
import java.util.Timer;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

import com.stocks.util.PercentCompleteReporter;

public class SchedulePercentCompleteReporterTimerTaskCommand implements Command {
	private Timer timer;
	private PercentCompleteReporter percentCompleteReporter;
	
	public SchedulePercentCompleteReporterTimerTaskCommand(Timer timer, PercentCompleteReporter percentCompleteReporter) {
		this.setTimer(timer);
		this.setPercentCompleteReporter(percentCompleteReporter);
		
		this.getTimer().scheduleAtFixedRate(getPercentCompleteReporter(), new Date(), 15*1000 );
	}
	
	public boolean execute(Context context) throws Exception {
		System.out.println( "SchedulePercentCompleteReporterTimerTaskCommand" );
		getPercentCompleteReporter().setLoggingEnabled(true);
		return Command.CONTINUE_PROCESSING;
	}

	public Timer getTimer() {
		return timer;
	}

	public void setTimer(Timer timer) {
		this.timer = timer;
	}

	public PercentCompleteReporter getPercentCompleteReporter() {
		return percentCompleteReporter;
	}

	public void setPercentCompleteReporter(
			PercentCompleteReporter percentCompleteReporter) {
		this.percentCompleteReporter = percentCompleteReporter;
	}
	
	@Override
	public String toString() {
		return this.getClass().getName();
	}
}
