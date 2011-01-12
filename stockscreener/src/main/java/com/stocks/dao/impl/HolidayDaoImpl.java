package com.stocks.dao.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.stocks.dao.AbstractDao;
import com.stocks.dao.HolidayDao;
import com.stocks.model.Holiday;

public class HolidayDaoImpl extends AbstractDao implements HolidayDao {
	public Holiday read(Date date) {
		return get(Holiday.class, date);
	}

	public void delete(Holiday holiday) {
		throw new RuntimeException("Not Implemented.");
	}
	
	public List<Holiday> findAll() {
		throw new RuntimeException("Not Implemented.");
	}
	
	public void save(Holiday holiday) {
		throw new RuntimeException("Not Implemented.");
	}
	
	public Date getPreviousBusinessDay(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		
		calendar.add(Calendar.DATE, -1);
		while( calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || 
			calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
			read( calendar.getTime() ) != null  ){
			calendar.add(Calendar.DATE, -1);
		}
		
		return calendar.getTime();
	}
}
