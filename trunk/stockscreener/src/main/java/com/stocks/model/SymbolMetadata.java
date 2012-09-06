package com.stocks.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

@Entity
@Table(name = "SYMBOL_METADATA")
public class SymbolMetadata implements Serializable{
    private final static long serialVersionUID = 2l;
    @Id
    @Column(name = "SYMBOL", length=30)
    private String symbol;

    @Column(name = "SHARES_OUTSTANDING", length = 30)
    private String sharesOutstanding;
    
    @Column(name = "EXPANDED_SHARES_OUTSTANDING", length = 20)
    private Long expandedSharesOutstanding;
    
	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getSharesOutstanding() {
		return sharesOutstanding;
	}

	public void setSharesOutstanding(String sharesOutstanding) {
		this.sharesOutstanding = sharesOutstanding;
	}

	public Long getExpandedSharesOutstanding() {
		return expandedSharesOutstanding;
	}

	public void setExpandedSharesOutstanding(Long expandedSharesOutstanding) {
		this.expandedSharesOutstanding = expandedSharesOutstanding;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((expandedSharesOutstanding == null) ? 0
						: expandedSharesOutstanding.hashCode());
		result = prime
				* result
				+ ((sharesOutstanding == null) ? 0 : sharesOutstanding
						.hashCode());
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
		SymbolMetadata other = (SymbolMetadata) obj;
		if (expandedSharesOutstanding == null) {
			if (other.expandedSharesOutstanding != null)
				return false;
		} else if (!expandedSharesOutstanding
				.equals(other.expandedSharesOutstanding))
			return false;
		if (sharesOutstanding == null) {
			if (other.sharesOutstanding != null)
				return false;
		} else if (!sharesOutstanding.equals(other.sharesOutstanding))
			return false;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SymbolMetadata [symbol=" + symbol + ", sharesOutstanding="
				+ sharesOutstanding + ", expandedSharesOutstanding="
				+ expandedSharesOutstanding + "]";
	}

}
