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
@Table(name = "alert")
@NamedQuery(name = "allAlerts", query = "select a from Alert a order by a.eventDate")
public class Alert implements Serializable{
    private final static long serialVersionUID = 2l;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ALERT_ID")
    private Long alertId;

    @Column(name = "STOCK_CODE", length = 10)
    private String stockCode;
    
    @Column(name = "TRX_TYPE", length = 20)
    private String trxType;

    @Column(name = "EVENT_DATE")
    private Date eventDate;
    
    @Column(name = "OPPORTUNITY_TYPE", length = 50)
    private String opportunityType;

    @Column(name = "EVENT_PRICE")
    private Double eventPrice;

    @Column(name = "TARGET_PRICE", length = 20)
    private String targetPrice;

    @Column(name = "EVENT_TYPE", length = 20)
    private String eventType;

    @Column(name = "QTY", length = 20)
    private Integer qty;

    @Column(name = "IS_ACTIVE", length = 1)
    private String isActive;

	public Long getAlertId() {
		return alertId;
	}

	public void setAlertId(Long alertId) {
		this.alertId = alertId;
	}

	public String getStockCode() {
		return stockCode;
	}

	public void setStockCode(String stockCode) {
		this.stockCode = stockCode;
	}

	public String getTrxType() {
		return trxType;
	}

	public void setTrxType(String trxType) {
		this.trxType = trxType;
	}

	public Date getEventDate() {
		return eventDate;
	}

	public void setEventDate(Date eventDate) {
		this.eventDate = eventDate;
	}

	public String getOpportunityType() {
		return opportunityType;
	}

	public void setOpportunityType(String opportunityType) {
		this.opportunityType = opportunityType;
	}

	public Double getEventPrice() {
		return eventPrice;
	}

	public void setEventPrice(Double eventPrice) {
		this.eventPrice = eventPrice;
	}

	public String getTargetPrice() {
		return targetPrice;
	}

	public void setTargetPrice(String targetPrice) {
		this.targetPrice = targetPrice;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public Integer getQty() {
		return qty;
	}

	public void setQty(Integer qty) {
		this.qty = qty;
	}

	public String getIsActive() {
		return isActive;
	}

	public void setIsActive(String isActive) {
		this.isActive = isActive;
	}

    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Alert)) {
            return false;
        }

        final Alert alert = (Alert) obj;

        return new EqualsBuilder().append(getAlertId(), alert.getAlertId()).isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder().append(getAlertId()).hashCode();
    }

    public String toString() {
        return ToStringBuilder
                .reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
    }

}
