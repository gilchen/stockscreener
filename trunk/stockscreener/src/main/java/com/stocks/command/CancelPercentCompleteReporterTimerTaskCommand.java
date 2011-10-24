package com.stocks.command;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Required;

import com.stocks.util.PercentCompleteReporter;

public class CancelPercentCompleteReporterTimerTaskCommand implements Command {
	private PercentCompleteReporter percentCompleteReporter;
	
	public boolean execute(Context context) throws Exception {
		System.out.println( "CancelPercentCompleteReporterTimerTaskCommand" );
		getPercentCompleteReporter().setLoggingEnabled(false);
		return Command.PROCESSING_COMPLETE;
	}

	public PercentCompleteReporter getPercentCompleteReporter() {
		return percentCompleteReporter;
	}

	@Required
	public void setPercentCompleteReporter(
			PercentCompleteReporter percentCompleteReporter) {
		this.percentCompleteReporter = percentCompleteReporter;
	}
	
	@Override
	public String toString() {
		return this.getClass().getName();
	}

}
