package com.stocks.standalone;

import com.stocks.util.Utility;

public class IntraDayStructure{
	private int index;
	private Long time;
	private Double close;
	private Double high;
	private Double low;
	private Double open;
	private Long volume;
	
	public IntraDayStructure(int index, Long time, Double close, Double high,
			Double low, Double open, Long volume) {
		super();
		this.index = index;
		this.time = time;
		this.close = close;
		this.high = high;
		this.low = low;
		this.open = open;
		this.volume = volume;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public Double getClose() {
		return close;
	}

	public void setClose(Double close) {
		this.close = close;
	}

	public Double getHigh() {
		return high;
	}

	public void setHigh(Double high) {
		this.high = high;
	}

	public Double getLow() {
		return low;
	}

	public void setLow(Double low) {
		this.low = low;
	}

	public Double getOpen() {
		return open;
	}

	public void setOpen(Double open) {
		this.open = open;
	}

	public Long getVolume() {
		return volume;
	}

	public void setVolume(Long volume) {
		this.volume = volume;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public String toString() {
		return "IntraDayStructure [index=" + index + ", time=" + time
				+ ", close=" + close + ", high=" + high + ", low=" + low
				+ ", open=" + open + ", volume=" + volume + "]";
	}

	public String toStringFor() {
		java.util.Date dt = new java.util.Date(time);
		return "IntraDayStructure [index=" + index + ", time=" + Utility.getStrDate(dt)+ " "+Utility.getStrTime(dt)
				+ ", close=" + close + ", volume=" + volume + ", v*c=$" +Utility.getFormattedInteger((volume*close))+ "]";
	}

}
