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
@Table(name = "bse_icici_mapping")
public class BseIciciMapping implements Serializable{
    private final static long serialVersionUID = 2l;
    @Id
    @Column(name = "STOCK_CODE", length = 10)
    private String stockCode;
    
    @Column(name = "SC_CODE", length = 11)
    private Integer scCode;

	public String getStockCode() {
		return stockCode;
	}

	public void setStockCode(String stockCode) {
		this.stockCode = stockCode;
	}

    public Integer getScCode() {
		return scCode;
	}

	public void setScCode(Integer scCode) {
		this.scCode = scCode;
	}

	public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof BseIciciMapping)) {
            return false;
        }

        final BseIciciMapping bseIciciMapping = (BseIciciMapping) obj;

        return new EqualsBuilder().append(getStockCode(), bseIciciMapping.getStockCode()).isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder().append(getStockCode()).hashCode();
    }

    public String toString() {
        return ToStringBuilder
                .reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
    }

}
