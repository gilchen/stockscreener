package com.stocks.model;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "aggregate_information_details")
@NamedQueries({
	@NamedQuery(name = "aggregateInformationDetailsBySymbolAndTradeDate", query = "select a from AggregateInformationDetails a where a.aggregateInformationDetailsPK.symbol = :symbol and date(a.aggregateInformationDetailsPK.tradeDate) = date(:tradeDate)")
})
public class AggregateInformationDetails implements Serializable{
    private final static long serialVersionUID = 2l;

    @Id
    @Embedded
    private AggregateInformationDetailsPK aggregateInformationDetailsPK;
    
    @Lob
    @Column(name="JAVA_OBJECT")
    private byte[] javaObject;

	public AggregateInformationDetailsPK getAggregateInformationDetailsPK() {
		return aggregateInformationDetailsPK;
	}

	public void setAggregateInformationDetailsPK(
			AggregateInformationDetailsPK aggregateInformationDetailsPK) {
		this.aggregateInformationDetailsPK = aggregateInformationDetailsPK;
	}

	public byte[] getJavaObject() {
		return javaObject;
	}

	public void setJavaObject(byte[] javaObject) {
		this.javaObject = javaObject;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((aggregateInformationDetailsPK == null) ? 0
						: aggregateInformationDetailsPK.hashCode());
		result = prime * result + Arrays.hashCode(javaObject);
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
		AggregateInformationDetails other = (AggregateInformationDetails) obj;
		if (aggregateInformationDetailsPK == null) {
			if (other.aggregateInformationDetailsPK != null)
				return false;
		} else if (!aggregateInformationDetailsPK
				.equals(other.aggregateInformationDetailsPK))
			return false;
		if (!Arrays.equals(javaObject, other.javaObject))
			return false;
		return true;
	}

}
