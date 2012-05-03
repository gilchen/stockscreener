package com.stocks.dao.impl;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import com.stocks.command.AbstractCommand;
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
		query.setHint("org.hibernate.fetchSize", "500");
		query.setParameter("scCode", scCode);
		query.setParameter("tradeDate", tradeDate);
		return query.getResultList();
	}
	
	public List<Bse> findStockByScCode(Integer scCode) {
		Query query = entityManager.createNamedQuery("stockByScCode");
		query.setHint("org.hibernate.fetchSize", "500");
		query.setParameter("scCode", scCode);
		return query.getResultList();
	}
	
	public List<Integer> getAllScCodes() {
		return entityManager.createNamedQuery("allScCodes").setHint("org.hibernate.fetchSize", "500").getResultList();
	}
	
	public List<Integer> getAllScCodesWithExpectedVxC(){
		return entityManager.createNativeQuery("select distinct sc_code from (select sc_code from bse where sc_type='Q' and (close*NO_OF_SHRS) >= " +AbstractCommand.RUPEE_VxC+ " order by sc_code) a").setHint("org.hibernate.fetchSize", "500").getResultList();
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
