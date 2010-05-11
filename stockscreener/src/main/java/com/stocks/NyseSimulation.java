package com.stocks;

import com.stocks.model.Nyse;

public class NyseSimulation{
	private Nyse nyseBuy;
	private Nyse nyseStopLoss;
	private Nyse nyseTarget;
	private Double sltp;
	private Double target;
	private String renderClass;

	public NyseSimulation(Nyse nyseBuy, Nyse nyseStopLoss, Nyse nyseTarget, Double sltp, Double target, String renderClass) {
		this.nyseBuy = nyseBuy;
		this.nyseStopLoss = nyseStopLoss;
		this.nyseTarget = nyseTarget;
		this.sltp = sltp;
		this.target = target;
		this.renderClass = renderClass;
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
	public Double getSltp() {
		return sltp;
	}
	public void setSltp(Double sltp) {
		this.sltp = sltp;
	}
	public Double getTarget() {
		return target;
	}
	public void setTarget(Double target) {
		this.target = target;
	}
	public String getRenderClass() {
		return renderClass;
	}
	public void setRenderClass(String renderClass) {
		this.renderClass = renderClass;
	}
}
