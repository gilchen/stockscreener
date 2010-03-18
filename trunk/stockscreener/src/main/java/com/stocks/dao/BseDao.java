package com.stocks.dao;

import java.util.Date;
import java.util.List;

import com.stocks.model.Bse;
import com.stocks.model.BsePK;

public interface BseDao extends Dao<Bse, BsePK> {
	public List<Bse> findStockByScCodeAndTradeDate(final Integer scCode, final Date tradeDate);
	public List<Bse> findStockByScCode(final Integer scCode);
	public List<Integer> getAllScCodes();
}
