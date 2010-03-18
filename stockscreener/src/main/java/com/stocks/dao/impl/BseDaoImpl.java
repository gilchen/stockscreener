package com.stocks.dao.impl;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import com.stocks.dao.AbstractDao;
import com.stocks.dao.BseDao;
import com.stocks.model.Bse;
import com.stocks.model.BsePK;

public class BseDaoImpl extends AbstractDao implements BseDao {
	public void delete(Bse bse) {
		entityManager.remove(bse);
	}

	public List<Bse> findAll() {
		throw new RuntimeException("Not Implemented.");
	}
	
	public List<Bse> findStockByScCodeAndTradeDate(final Integer scCode, final Date tradeDate){
		Query query = entityManager.createNamedQuery("stockByScCodeAndTradeDate");
		query.setParameter("scCode", scCode);
		query.setParameter("tradeDate", tradeDate);
		return query.getResultList();
	}
	
	public List<Bse> findStockByScCode(Integer scCode) {
		Query query = entityManager.createNamedQuery("stockByScCode");
		query.setParameter("scCode", scCode);
		return query.getResultList();
	}
	
	public List<Integer> getAllScCodes() {
		return entityManager.createNamedQuery("allScCodes").getResultList();
	}

	public Bse read(BsePK bsePK) {
		return get(Bse.class, bsePK);
	}

	public void save(Bse bse) {
		try{
			entityManager.persist(bse);
		}
		catch(Exception e){
			throw new RuntimeException(e);
		}
	}
}
