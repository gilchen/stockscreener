package com.stocks.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

@Entity
@Table(name = "SUMMARY_52_WK_BSE")
public class Summary52WkBse implements Serializable{
    private final static long serialVersionUID = 2l;

    @Id
    @Column(name="SYMBOL")
    private String symbol;
    
    @Column(name="SNAPSHOT_DT")
    private Date snapshotDate;
    
    @Column(name="MIN_LOW")
    private Double minLow;
    
    @Column(name="MAX_HIGH")
    private Double maxHigh;

    @Column(name="MIN_CLOSE")
    private Double minClose;

    @Column(name="MAX_CLOSE")
    private Double maxClose;

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public Date getSnapshotDate() {
		return snapshotDate;
	}

	public void setSnapshotDate(Date snapshotDate) {
		this.snapshotDate = snapshotDate;
	}

	public Double getMinLow() {
		return minLow;
	}

	public void setMinLow(Double minLow) {
		this.minLow = minLow;
	}

	public Double getMaxHigh() {
		return maxHigh;
	}

	public void setMaxHigh(Double maxHigh) {
		this.maxHigh = maxHigh;
	}

	public Double getMinClose() {
		return minClose;
	}

	public void setMinClose(Double minClose) {
		this.minClose = minClose;
	}

	public Double getMaxClose() {
		return maxClose;
	}

	public void setMaxClose(Double maxClose) {
		this.maxClose = maxClose;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((maxClose == null) ? 0 : maxClose.hashCode());
		result = prime * result + ((maxHigh == null) ? 0 : maxHigh.hashCode());
		result = prime * result
				+ ((minClose == null) ? 0 : minClose.hashCode());
		result = prime * result + ((minLow == null) ? 0 : minLow.hashCode());
		result = prime * result
				+ ((snapshotDate == null) ? 0 : snapshotDate.hashCode());
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Summary52WkBse other = (Summary52WkBse) obj;
		if (maxClose == null) {
			if (other.maxClose != null)
				return false;
		} else if (!maxClose.equals(other.maxClose))
			return false;
		if (maxHigh == null) {
			if (other.maxHigh != null)
				return false;
		} else if (!maxHigh.equals(other.maxHigh))
			return false;
		if (minClose == null) {
			if (other.minClose != null)
				return false;
		} else if (!minClose.equals(other.minClose))
			return false;
		if (minLow == null) {
			if (other.minLow != null)
				return false;
		} else if (!minLow.equals(other.minLow))
			return false;
		if (snapshotDate == null) {
			if (other.snapshotDate != null)
				return false;
		} else if (!snapshotDate.equals(other.snapshotDate))
			return false;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		return true;
	}
    

}
