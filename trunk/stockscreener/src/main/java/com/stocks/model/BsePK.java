package com.stocks.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang.builder.ToStringBuilder;

@Embeddable
public class BsePK implements Serializable {
	private static final long serialVersionUID = 2443589965444053628L;

	@Column(name="TRADE_DATE", nullable=false)
	private Date tradeDate;

	@Column(name="SC_CODE", nullable=false)
	private Integer scCode;

	public BsePK() {
	}
	
	public BsePK(final Date tradeDate, final Integer scCode) {
		this.tradeDate = tradeDate;
		this.scCode = scCode;
	}
	
    public Date getTradeDate() {
		return tradeDate;
	}

	public void setTradeDate(Date tradeDate) {
		this.tradeDate = tradeDate;
	}

	public Integer getScCode() {
		return scCode;
	}

	public void setScCode(Integer scCode) {
		this.scCode = scCode;
	}

	@Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final BsePK that = (BsePK) o;

        if (!tradeDate.equals(that.tradeDate)) return false;
        if (!scCode.equals(that.scCode)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = tradeDate.hashCode();
        result = 31 * result + scCode.hashCode();
        return result;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
