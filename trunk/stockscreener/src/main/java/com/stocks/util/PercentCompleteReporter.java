package com.stocks.util;

import java.util.TimerTask;

import org.springframework.beans.factory.annotation.Required;

public class PercentCompleteReporter extends TimerTask{
	private Double percentComplete;
	private boolean loggingEnabled;

	public Double getPercentComplete() {
		return percentComplete;
	}

	public void setPercentComplete(Double percentComplete) {
		this.percentComplete = percentComplete;
	}

	@Override
	public void run() {
		if( isLoggingEnabled() ){
			System.out.println( "\t" +Math.round(percentComplete) +"% completed." );
		}
	}

	public boolean isLoggingEnabled() {
		return loggingEnabled;
	}

	@Required
	public void setLoggingEnabled(boolean loggingEnabled) {
		this.loggingEnabled = loggingEnabled;
	}
}
