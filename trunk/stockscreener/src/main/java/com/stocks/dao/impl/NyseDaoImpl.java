package com.stocks.dao.impl;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import com.stocks.dao.AbstractDao;
import com.stocks.dao.NyseDao;
import com.stocks.model.Bse;
import com.stocks.model.Nyse;
import com.stocks.model.NysePK;

public class NyseDaoImpl extends AbstractDao implements NyseDao {
	public void delete(Nyse nyse) {
		entityManager.remove(nyse);
	}

	public List<Nyse> findAll() {
		throw new RuntimeException("Not Implemented.");
	}

	public List<Nyse> findStockBySymbolAndTradeDate(final String symbol, final Date tradeDate){
		Query query = entityManager.createNamedQuery("stockBySymbolAndTradeDate");
		query.setParameter("symbol", symbol);
		query.setParameter("tradeDate", tradeDate);
		return query.getResultList();
	}
	
	public List<String> getAllSymbols() {
		return entityManager.createNamedQuery("allSymbols").getResultList();
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
