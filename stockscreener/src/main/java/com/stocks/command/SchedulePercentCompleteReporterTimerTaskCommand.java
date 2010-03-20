package com.stocks.command;

import java.util.Date;
import java.util.Timer;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Required;

import com.stocks.util.PercentCompleteReporter;

public class SchedulePercentCompleteReporterTimerTaskCommand implements Command {
	private Timer timer;
	private PercentCompleteReporter percentCompleteReporter;
	
	public boolean execute(Context context) throws Exception {
		System.out.println( "SchedulePercentCompleteReporterTimerTaskCommand" );
		this.getTimer().scheduleAtFixedRate(getPercentCompleteReporter(), new Date(), 15*1000 );
		return Command.CONTINUE_PROCESSING;
	}

	public Timer getTimer() {
		return timer;
	}

	@Required
	public void setTimer(Timer timer) {
		this.timer = timer;
	}

	public PercentCompleteReporter getPercentCompleteReporter() {
		return percentCompleteReporter;
	}

	@Required
	public void setPercentCompleteReporter(
			PercentCompleteReporter percentCompleteReporter) {
		this.percentCompleteReporter = percentCompleteReporter;
	}
	
	

}
