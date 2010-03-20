package com.stocks.util;

import java.util.TimerTask;

public class PercentCompleteReporter extends TimerTask{
	private Double percentComplete;

	public Double getPercentComplete() {
		return percentComplete;
	}

	public void setPercentComplete(Double percentComplete) {
		this.percentComplete = percentComplete;
	}

	@Override
	public void run() {
		System.out.println( Math.round(percentComplete) +"% completed." );
	}
}
