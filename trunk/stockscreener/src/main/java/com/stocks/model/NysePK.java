package com.stocks.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang.builder.ToStringBuilder;

@Embeddable
public class NysePK implements Serializable {
	@Column(name="TRADE_DATE", nullable=false)
	private Date tradeDate;

	@Column(name="SYMBOL", nullable=false, length=30)
	private String symbol;

	public NysePK() {
	}
	
	public NysePK(final Date tradeDate, final String symbol) {
		this.tradeDate = tradeDate;
		this.symbol = symbol;
	}

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final NysePK that = (NysePK) o;

        if (!tradeDate.equals(that.tradeDate)) return false;
        if (!symbol.equals(that.symbol)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = tradeDate.hashCode();
        result = 31 * result + symbol.hashCode();
        return result;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
