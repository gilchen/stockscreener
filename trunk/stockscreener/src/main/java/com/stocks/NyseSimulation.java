package com.stocks;

import com.stocks.model.Nyse;

public class NyseSimulation{
	private Nyse nyseBuy;
	private Nyse nyseStopLoss;
	private Nyse nyseTarget;

	public NyseSimulation(Nyse nyseBuy, Nyse nyseStopLoss, Nyse nyseTarget) {
		this.nyseBuy = nyseBuy;
		this.nyseStopLoss = nyseStopLoss;
		this.nyseTarget = nyseTarget;
	}
	public Nyse getNyseBuy() {
		return nyseBuy;
	}
	public void setNyseBuy(Nyse nyseBuy) {
		this.nyseBuy = nyseBuy;
	}
	public Nyse getNyseStopLoss() {
		return nyseStopLoss;
	}
	public void setNyseStopLoss(Nyse nyseStopLoss) {
		this.nyseStopLoss = nyseStopLoss;
	}
	public Nyse getNyseTarget() {
		return nyseTarget;
	}
	public void setNyseTarget(Nyse nyseTarget) {
		this.nyseTarget = nyseTarget;
	}	
}
