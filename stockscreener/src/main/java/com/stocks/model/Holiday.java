package com.stocks.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "holidays")
public class Holiday implements Serializable{
    private final static long serialVersionUID = 2l;
    
    @Id
    @Column(name="HOLIDAY")
    private Date holiday;
    
	public Holiday() {
	}

	public Holiday(Date holiday) {
		this.holiday = holiday;
	}

	public Date getHoliday() {
		return holiday;
	}

	public void setHoliday(Date holiday) {
		this.holiday = holiday;
	}

    

}
