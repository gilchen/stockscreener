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
@Table(name = "nyse")
@NamedQueries({
	@NamedQuery(name = "stockBySymbol", query = "select a from Nyse a where a.nysePK.symbol = :symbol and a.volume > 0 order by a.nysePK.tradeDate asc"),
	@NamedQuery(name = "stockBySymbolBetweenTradeDates", query = "select a from Nyse a where a.nysePK.symbol = :symbol and date(a.nysePK.tradeDate) >= date(:tradeStartDate) and date(a.nysePK.tradeDate) <= date(:tradeEndDate) and a.volume > 0 order by a.nysePK.tradeDate asc"),
	@NamedQuery(name = "allSymbols", query = "select a.nysePK.symbol from Nyse a where a.nysePK.tradeDate = (select max(b.nysePK.tradeDate) from Nyse b where b.nysePK.symbol='DJI.IDX') and a.nysePK.symbol not like '%.IDX' and a.nysePK.symbol not like '%-%' and a.nysePK.symbol not like '%.%' and a.close > 1 and a.nysePK.symbol in (select c.nysePK.symbol from Nyse c where c.nysePK.tradeDate = (select max(d.nysePK.tradeDate) from Nyse d where d.nysePK.symbol='DJI.IDX') and (c.close*c.volume) > 100000000 )"),
	@NamedQuery(name = "allSymbolsWithExpectedVxC", query = "select distinct a.nysePK.symbol from Nyse a where a.nysePK.symbol not like '%-%' and a.nysePK.symbol not like '%.%' and a.close > 1 and (a.close*a.volume) > 100000000"),
	@NamedQuery(name = "allTradingDates", query = "select a.nysePK.tradeDate from Nyse a where a.nysePK.symbol='DJI.IDX' and a.close > 1 order by a.nysePK.tradeDate")
})

// "allSymbols" considers stocks that trade for more than $250 mln recently.
public class Nyse implements Serializable{
    private final static long serialVersionUID = 2l;

    @Id
    @Embedded
    private NysePK nysePK;
    
    @Column(name="OPEN")
    private Double open;
    
    @Column(name="HIGH")
    private Double high;
    
    @Column(name="LOW")
    private Double low;

    @Column(name="CLOSE")
    private Double close;

    @Column(name="VOLUME")
    private Long volume;
    
    private transient Nyse previous;

	public NysePK getNysePK() {
		return nysePK;
	}

	public void setNysePK(NysePK nysePK) {
		this.nysePK = nysePK;
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


	public Long getVolume() {
		return volume;
	}


	public void setVolume(Long volume) {
		this.volume = volume;
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

        final NysePK nysePK = ((Nyse)o).getNysePK();
        if ( nysePK == null){
        	return false;
        }

        if (!getNysePK().getTradeDate().equals(nysePK.getTradeDate())){
        	return false;
        }
        if (!getNysePK().getSymbol().equals(nysePK.getSymbol())){
        	return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = getNysePK().getTradeDate().hashCode();
        result = 31 * result + getNysePK().getSymbol().hashCode();
        return result;
    }

	public Nyse getPrevious() {
		return previous;
	}

	public void setPrevious(Nyse previous) {
		this.previous = previous;
	}
}
