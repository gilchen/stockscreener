package com.stocks.dao;

import java.util.Date;
import java.util.List;

import com.stocks.model.Holiday;

public interface HolidayDao extends Dao<Holiday, Date> {
	public Date getPreviousBusinessDay(Date date);
}
