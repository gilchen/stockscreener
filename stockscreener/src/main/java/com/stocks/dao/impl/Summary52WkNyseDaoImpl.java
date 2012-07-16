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
		//entityManager.remove(summary52Wk);
		throw new RuntimeException("Not Implemented.");
	}

	public List<Summary52WkNyse> findAll() {
		throw new RuntimeException("Not Implemented.");
	}
	
	public void syncUp(DURATION duration){
		System.out.println( "Syncing up Nyse " +duration+ " weeks." );
		System.out.println( "\t::: Deleting from SUMMARY" +duration+ "WK_NYSE" );
		String sqlDelete = "DELETE FROM SUMMARY" +duration+ "WK_NYSE";
		Query query = entityManager.createNativeQuery(sqlDelete);
		int rowsUpdated = query.executeUpdate();
		System.out.println( "\tDone. Deleted " +rowsUpdated+ " rows." );
		
		System.out.println( "\t::: Inserting into SUMMARY" +duration+ "WK_NYSE @"+Utility.getStrTime(new Date()) );
		String sqlSync = "INSERT INTO SUMMARY" +duration+ "WK_NYSE (SELECT CURDATE() 'SNAPSHOT_DT', SYMBOL 'SYMBOL', MIN(LOW) 'MIN_LOW', MAX(HIGH) 'MAX_HIGH', MIN(CLOSE) 'MIN_CLOSE', MAX(CLOSE) 'MAX_CLOSE' FROM NYSE WHERE TRADE_DATE BETWEEN (CURDATE() - INTERVAL " +duration.toString().replaceAll("_", "")+ " WEEK) AND CURDATE() AND (SYMBOL NOT LIKE '%-%' AND SYMBOL NOT LIKE '%.%') GROUP BY SYMBOL)";
		query = entityManager.createNativeQuery(sqlSync);
		rowsUpdated = query.executeUpdate();
		System.out.println( "\tDone. Inserted " +rowsUpdated+ " rows into SUMMARY" +duration+ "WK_NYSE @"+Utility.getStrTime(new Date()) );
	}
	
	public Summary52WkNyse read(String symbol) {
		throw new RuntimeException("Not Implemented.");
		//return get(Summary52WkNyse.class, symbol);
	}

	public void save(Summary52WkNyse summary52Wk) {
		throw new RuntimeException("Not Implemented");
	}
}
