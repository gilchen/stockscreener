package com.stocks.dao.impl;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import com.stocks.dao.AbstractDao;
import com.stocks.dao.Summary52WkNyseDao;
import com.stocks.model.Summary52WkNyse;
import com.stocks.util.Utility;

public class Summary52WkNyseDaoImpl extends AbstractDao implements Summary52WkNyseDao {
	public void delete(Summary52WkNyse summary52Wk) {
		entityManager.remove(summary52Wk);
	}

	public List<Summary52WkNyse> findAll() {
		throw new RuntimeException("Not Implemented.");
	}
	
	public void syncUp(){
		System.out.println( "::: Deleting from SUMMARY_52_WK_NYSE" );
		String sqlDelete = "DELETE FROM SUMMARY_52_WK_NYSE";
		Query query = entityManager.createNativeQuery(sqlDelete);
		int rowsUpdated = query.executeUpdate();
		System.out.println( "Done. Deleted " +rowsUpdated+ " rows." );
		
		System.out.println( "::: Inserting into SUMMARY_52_WK_NYSE @"+Utility.getStrTime(new Date()) );
		String sqlSync = "INSERT INTO SUMMARY_52_WK_NYSE (SELECT CURDATE() 'SNAPSHOT_DT', SYMBOL 'SYMBOL', MIN(LOW) 'MIN_LOW', MAX(HIGH) 'MAX_HIGH', MIN(CLOSE) 'MIN_CLOSE', MAX(CLOSE) 'MAX_CLOSE' FROM NYSE WHERE TRADE_DATE BETWEEN (CURDATE() - INTERVAL 12 MONTH) AND CURDATE() AND (SYMBOL NOT LIKE '%-%' AND SYMBOL NOT LIKE '%.%') GROUP BY SYMBOL)";
		query = entityManager.createNativeQuery(sqlSync);
		rowsUpdated = query.executeUpdate();
		System.out.println( "Done. Inserted " +rowsUpdated+ " rows into SUMMARY_52_WK_NYSE @"+Utility.getStrTime(new Date()) );
	}
	
	public Summary52WkNyse read(String symbol) {
		return get(Summary52WkNyse.class, symbol);
	}

	public void save(Summary52WkNyse summary52Wk) {
		throw new RuntimeException("Not Implemented");
	}
}
