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
@Table(name = "aggregate_information")
@NamedQueries({
	@NamedQuery(name = "aggregateInformationBySymbol", query = "select a from AggregateInformation a where a.aggregateInformationPK.symbol = :symbol order by a.aggregateInformationPK.tradeDate asc")
})
public class AggregateInformation implements Serializable{
    private final static long serialVersionUID = 2l;

    @Id
    @Embedded
    private AggregateInformationPK aggregateInformationPK;
    
    @Column(name="CLOSE")
    private Double close;

    @Column(name="VOLUME")
    private Long volume;

	public AggregateInformationPK getAggregateInformationPK() {
		return aggregateInformationPK;
	}

	public void setAggregateInformationPK(
			AggregateInformationPK aggregateInformationPK) {
		this.aggregateInformationPK = aggregateInformationPK;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((aggregateInformationPK == null) ? 0
						: aggregateInformationPK.hashCode());
		result = prime * result + ((close == null) ? 0 : close.hashCode());
		result = prime * result + ((volume == null) ? 0 : volume.hashCode());
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
		AggregateInformation other = (AggregateInformation) obj;
		if (aggregateInformationPK == null) {
			if (other.aggregateInformationPK != null)
				return false;
		} else if (!aggregateInformationPK.equals(other.aggregateInformationPK))
			return false;
		if (close == null) {
			if (other.close != null)
				return false;
		} else if (!close.equals(other.close))
			return false;
		if (volume == null) {
			if (other.volume != null)
				return false;
		} else if (!volume.equals(other.volume))
			return false;
		return true;
	}
    
}
