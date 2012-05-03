package com.stocks.dao.impl;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import com.stocks.dao.AbstractDao;
import com.stocks.dao.Summary52WkBseDao;
import com.stocks.model.Summary52WkBse;
import com.stocks.util.Utility;

public class Summary52WkBseDaoImpl extends AbstractDao implements Summary52WkBseDao {
	public void delete(Summary52WkBse summary52Wk) {
		entityManager.remove(summary52Wk);
	}

	public List<Summary52WkBse> findAll() {
		throw new RuntimeException("Not Implemented.");
	}
	
	public void syncUp(){
		System.out.println( "::: Deleting from SUMMARY_52_WK_BSE" );
		String sqlDelete = "DELETE FROM SUMMARY_52_WK_BSE";
		Query query = entityManager.createNativeQuery(sqlDelete);
		int rowsUpdated = query.executeUpdate();
		System.out.println( "Done. Deleted " +rowsUpdated+ " rows." );
		
		System.out.println( "::: Inserting into SUMMARY_52_WK_BSE @"+Utility.getStrTime(new Date()) );
		String sqlSync = "INSERT INTO SUMMARY_52_WK_BSE (SELECT CURDATE() 'SNAPSHOT_DT', SC_CODE 'SC_CODE', MIN(LOW) 'MIN_LOW', MAX(HIGH) 'MAX_HIGH', MIN(CLOSE) 'MIN_CLOSE', MAX(CLOSE) 'MAX_CLOSE' FROM BSE WHERE TRADE_DATE BETWEEN (CURDATE() - INTERVAL 12 MONTH) AND CURDATE() GROUP BY SC_CODE)";
		query = entityManager.createNativeQuery(sqlSync);
		rowsUpdated = query.executeUpdate();
		System.out.println( "Done. Inserted " +rowsUpdated+ " rows into SUMMARY_52_WK_BSE @"+Utility.getStrTime(new Date()) );
	}
	
	public Summary52WkBse read(String symbol) {
		return get(Summary52WkBse.class, symbol);
	}

	public void save(Summary52WkBse summary52Wk) {
		throw new RuntimeException("Not Implemented");
	}
}
