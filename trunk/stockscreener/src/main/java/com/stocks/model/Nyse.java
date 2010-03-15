package com.stocks.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

@Entity
@Table(name = "nyse")
//@IdClass(BsePK.class)
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

}
