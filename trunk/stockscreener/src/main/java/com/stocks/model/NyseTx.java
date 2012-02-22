package com.stocks.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Entity
@Table(name = "nyse_tx")
@NamedQueries({
	@NamedQuery(name = "allNyseTransactions", query = "select a from NyseTx a order by a.symbol, a.txDate, a.nyseTxId"),
	@NamedQuery(name = "nyseTransactionsBySymbol", query = "select a from NyseTx a where a.symbol=:symbol order by a.symbol, a.txDate, a.nyseTxId")
})
public class NyseTx implements Serializable{
    private final static long serialVersionUID = 2l;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "NYSE_TX_ID")
    private Long nyseTxId;

    @Column(name = "TRX_TYPE", length = 20)
    private String trxType;
    
    @Column(name = "SYMBOL", length = 30)
    private String symbol;

    @Column(name = "TX_DATE")
    private Date txDate;
    
    @Column(name = "PRICE")
    private Double price;

    @Column(name = "QTY", length = 5)
    private Integer qty;

    @Column(name = "TX_FEE")
    private Double txFee;

    @Column(name = "COMMENTS", length = 100)
    private String comments;
    
    public Long getNyseTxId() {
		return nyseTxId;
	}

	public void setNyseTxId(Long nyseTxId) {
		this.nyseTxId = nyseTxId;
	}

	public String getTrxType() {
		return trxType;
	}

	public void setTrxType(String trxType) {
		this.trxType = trxType;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public Date getTxDate() {
		return txDate;
	}

	public void setTxDate(Date txDate) {
		this.txDate = txDate;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public Integer getQty() {
		return qty;
	}

	public void setQty(Integer qty) {
		this.qty = qty;
	}

	public Double getTxFee() {
		return txFee;
	}

	public void setTxFee(Double txFee) {
		this.txFee = txFee;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((comments == null) ? 0 : comments.hashCode());
		result = prime * result
				+ ((nyseTxId == null) ? 0 : nyseTxId.hashCode());
		result = prime * result + ((price == null) ? 0 : price.hashCode());
		result = prime * result + ((qty == null) ? 0 : qty.hashCode());
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
		result = prime * result + ((trxType == null) ? 0 : trxType.hashCode());
		result = prime * result + ((txDate == null) ? 0 : txDate.hashCode());
		result = prime * result + ((txFee == null) ? 0 : txFee.hashCode());
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
		NyseTx other = (NyseTx) obj;
		if (comments == null) {
			if (other.comments != null)
				return false;
		} else if (!comments.equals(other.comments))
			return false;
		if (nyseTxId == null) {
			if (other.nyseTxId != null)
				return false;
		} else if (!nyseTxId.equals(other.nyseTxId))
			return false;
		if (price == null) {
			if (other.price != null)
				return false;
		} else if (!price.equals(other.price))
			return false;
		if (qty == null) {
			if (other.qty != null)
				return false;
		} else if (!qty.equals(other.qty))
			return false;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		if (trxType == null) {
			if (other.trxType != null)
				return false;
		} else if (!trxType.equals(other.trxType))
			return false;
		if (txDate == null) {
			if (other.txDate != null)
				return false;
		} else if (!txDate.equals(other.txDate))
			return false;
		if (txFee == null) {
			if (other.txFee != null)
				return false;
		} else if (!txFee.equals(other.txFee))
			return false;
		return true;
	}

	public String toString() {
        return "<TD>" +getSymbol() +"</TD>"+
        	"<TD>" +getTrxType() +"</TD>"+
        	"<TD>" +new SimpleDateFormat("MMM dd, yyyy").format(getTxDate()) +"</TD>"+
        	"<TD align=right>" +getPrice() +"</TD>"+
        	"<TD align=right>" +getQty()+ "</TD>";
    }

}
