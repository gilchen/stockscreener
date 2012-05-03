package com.stocks.dao.impl;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import com.stocks.dao.AbstractDao;
import com.stocks.dao.NyseDao;
import com.stocks.model.Nyse;
import com.stocks.model.NysePK;

public class NyseDaoImpl extends AbstractDao implements NyseDao {
	public void delete(Nyse nyse) {
		entityManager.remove(nyse);
	}

	public List<Nyse> findAll() {
		throw new RuntimeException("Not Implemented.");
	}
	
	// Ensure nyseList contains list of only One symbol in ascending order of tradeDate (without filters on trade_date).
	public void attachPrevious(List<Nyse> nyseList) {
		for( int i=1; i<nyseList.size(); i++ ){
			nyseList.get(i).setPrevious( nyseList.get(i-1) );
		}
	}

	public List<Nyse> findStockBySymbol(final String symbol){
		Query query = entityManager.createNamedQuery("stockBySymbol");
		query.setHint("org.hibernate.fetchSize", "500");
		query.setParameter("symbol", symbol);
		List<Nyse> results = query.getResultList();
		attachPrevious(results);
		
		return results;
	}
	
	public List<Nyse> findStockBySymbolBetweenTradeDates(final String symbol, final Date tradeStartDate, final Date tradeEndDate){
		Query query = entityManager.createNamedQuery("stockBySymbolBetweenTradeDates");
		query.setHint("org.hibernate.fetchSize", "500");
		query.setParameter("symbol", symbol);
		query.setParameter("tradeStartDate", tradeStartDate);
		query.setParameter("tradeEndDate", tradeEndDate);
		List<Nyse> results = query.getResultList();
		attachPrevious(results);
		
		return results;
	}
	
	public List<String> getAllSymbols() {
		return entityManager.createNamedQuery("allSymbols").setHint("org.hibernate.fetchSize", "500").getResultList();
	}
	
	public List<String> getAllSymbolsWithExpectedVxC() {
		Query query = entityManager.createNamedQuery("allSymbolsWithExpectedVxC");
		query.setHint("org.hibernate.fetchSize", "500");
		List<String> results = query.getResultList();
		
		return results;
	}
	
	public List<Date> getAllTradingDates(){
		return entityManager.createNamedQuery("allTradingDates").setHint("org.hibernate.fetchSize", "500").getResultList();
	}
	
	
	public List<Object[]> findUpwardMovingStocks(final Integer interval, final Double averagePercentage){
		final StringBuffer sbSql = new StringBuffer();
		sbSql.append("select"); 
		sbSql.append( " a.symbol,");
		sbSql.append( " a.close,");
		sbSql.append( " b.average");
		sbSql.append( " from");
		sbSql.append( " Nyse a,");
		sbSql.append( " (select symbol, avg(close) average from Nyse where trade_date between"); 
		sbSql.append( "	(SELECT DATE_SUB((SELECT STR_TO_DATE(V, '%d%m%y') FROM KEY_VALUE WHERE K='LAST_NYSE_IMPORT'), INTERVAL ~pInterval DAY)) and ");
		sbSql.append( "	(SELECT STR_TO_DATE(V, '%d%m%y') FROM KEY_VALUE WHERE K='LAST_NYSE_IMPORT') ");
		sbSql.append( " group by symbol) b");
		sbSql.append( " where");
		sbSql.append( " a.symbol=b.symbol");
		sbSql.append( " and a.trade_date = (SELECT STR_TO_DATE(V, '%d%m%y') FROM KEY_VALUE WHERE K='LAST_NYSE_IMPORT')");
		sbSql.append( " and b.average <= (a.close-(a.close*~pAveragePercentage))");
		sbSql.append( " and a.symbol not like '%.IDX' and a.symbol not like '%.%' and a.symbol not like '%-%'");

		String sql = sbSql.toString();
		sql = sql.replaceAll("~pInterval", interval.toString());
		sql = sql.replaceAll("~pAveragePercentage", averagePercentage.toString());
		System.out.println( "sql: " +sql );
		Query query = entityManager.createNativeQuery(sql);
		query.setHint("org.hibernate.fetchSize", "500");
		List<Object[]> result = query.getResultList();
		return result;
	}
	
	public Nyse read(NysePK nysePK) {
		return get(Nyse.class, nysePK);
	}

	public void save(Nyse nyse) {
		try{
			entityManager.persist(nyse);
		}
		catch(Exception e){
			throw new RuntimeException(e);
		}
	}
}
