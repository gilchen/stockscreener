package com.stocks.model;

import java.io.Serializable;


import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

@Entity
@Table(name = "bse")
@NamedQueries({
	@NamedQuery(name = "stockByScCodeAndTradeDate", query = "select a from Bse a where a.bsePK.scCode = :scCode and a.bsePK.tradeDate > :tradeDate order by a.bsePK.tradeDate asc"),
	@NamedQuery(name = "stockByScCode", query = "select a from Bse a where a.bsePK.scCode = :scCode order by a.bsePK.tradeDate asc"),
	@NamedQuery(name = "allScCodes", query = "select a.bsePK.scCode from Bse a where a.bsePK.tradeDate = (select max(b.bsePK.tradeDate) from Bse b)")
})
public class Bse implements Serializable{
    private final static long serialVersionUID = 2l;

    @Id
    @Embedded
    private BsePK bsePK;
    
    @Column(name="SC_NAME", length=30)
    private String scName;
    
    @Column(name="SC_GROUP", length=10)
    private String scGroup;
    
    @Column(name="SC_TYPE", length=10)
    private String scType;
    
    @Column(name="OPEN")
    private Double open;
    
    @Column(name="HIGH")
    private Double high;
    
    @Column(name="LOW")
    private Double low;

    @Column(name="CLOSE")
    private Double close;

    @Column(name="LAST")
    private Double last;

    @Column(name="PREVCLOSE")
    private Double prevClose;

    @Column(name="NO_TRADES")
    private Long noTrades;

    @Column(name="NO_OF_SHRS")
    private Long noOfShares;
    
    @Column(name="NET_TURNOV")
    private Double netTurnover;

    @Column(name="TDCLOINDI", length=10)
    private String tdcloindi;
    
    public BsePK getBsePK() {
		return bsePK;
	}

	public void setBsePK(BsePK bsePK) {
		this.bsePK = bsePK;
	}

	public String getScName() {
		return scName;
	}

	public void setScName(String scName) {
		this.scName = scName;
	}

	public String getScGroup() {
		return scGroup;
	}

	public void setScGroup(String scGroup) {
		this.scGroup = scGroup;
	}

	public String getScType() {
		return scType;
	}

	public void setScType(String scType) {
		this.scType = scType;
	}

	public Double getOpen() {
		return open;
	}

	public void setOpen(Double open) {
		this.open = open;
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

	public Double getClose() {
		return close;
	}

	public void setClose(Double close) {
		this.close = close;
	}

	public Double getLast() {
		return last;
	}

	public void setLast(Double last) {
		this.last = last;
	}

	public Double getPrevClose() {
		return prevClose;
	}

	public void setPrevClose(Double prevClose) {
		this.prevClose = prevClose;
	}

	public Long getNoTrades() {
		return noTrades;
	}

	public void setNoTrades(Long noTrades) {
		this.noTrades = noTrades;
	}

	public Long getNoOfShares() {
		return noOfShares;
	}

	public void setNoOfShares(Long noOfShares) {
		this.noOfShares = noOfShares;
	}

	public Double getNetTurnover() {
		return netTurnover;
	}

	public void setNetTurnover(Double netTurnover) {
		this.netTurnover = netTurnover;
	}

	public String getTdcloindi() {
		return tdcloindi;
	}

	public void setTdcloindi(String tdcloindi) {
		this.tdcloindi = tdcloindi;
	}

	public String toString() {
        return ToStringBuilder
                .reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
    }

	@Override
    public boolean equals(final Object o) {
        if (this == o){
        	return true;
        }
        if (o == null || getClass() != o.getClass()){
        	return false;
        }

        final BsePK bsePK = ((Bse)o).getBsePK();
        if ( bsePK == null){
        	return false;
        }

        if (!getBsePK().getTradeDate().equals(bsePK.getTradeDate())){
        	return false;
        }
        if (!getBsePK().getScCode().equals(bsePK.getScCode())){
        	return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = getBsePK().getTradeDate().hashCode();
        result = 31 * result + getBsePK().getScCode().hashCode();
        return result;
    }

}
