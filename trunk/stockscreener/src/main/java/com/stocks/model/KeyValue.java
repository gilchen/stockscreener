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
@Table(name = "KEY_VALUE")
public class KeyValue implements Serializable{
    private final static long serialVersionUID = 2l;
    @Id
    @Column(name = "K", length=50)
    private String k;

    @Column(name = "V", length = 50)
    private String v;
    
    public String getK() {
		return k;
	}

	public void setK(String k) {
		this.k = k;
	}

	public String getV() {
		return v;
	}

	public void setV(String v) {
		this.v = v;
	}

	public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof KeyValue)) {
            return false;
        }

        final KeyValue keyValue = (KeyValue) obj;

        return new EqualsBuilder().append(getK(), keyValue.getK()).isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder().append(getK()).hashCode();
    }

    public String toString() {
        return ToStringBuilder
                .reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
    }

}
