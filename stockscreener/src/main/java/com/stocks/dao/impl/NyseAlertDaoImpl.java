package com.stocks.dao.impl;

import java.util.List;

import com.stocks.dao.AbstractDao;
import com.stocks.dao.NyseAlertDao;
import com.stocks.model.NyseAlert;
import com.stocks.search.NyseAlertResult;

public class NyseAlertDaoImpl extends AbstractDao implements NyseAlertDao {
	public void delete(NyseAlert nyseAlert) {
		entityManager.remove(nyseAlert);
	}

	public List<NyseAlert> findAll() {
		return entityManager.createNamedQuery("allNyseAlerts").getResultList();
	}
	
	public List<NyseAlertResult> findAlertResultsByTrxType(final String trxType, final String isActive) {
		return entityManager.createNamedQuery("allNyseAlertResultsByTrxType").setParameter("trxType", trxType).setParameter("isActive", isActive).getResultList();
	}
	
	public NyseAlert read(Long id) {
		return get(NyseAlert.class, id);
	}

	public void save(NyseAlert nyseAlert) {
		try{
			if( nyseAlert.getAlertId() == null ){
				entityManager.persist(nyseAlert);
			}else{
				entityManager.merge(nyseAlert);
			}
		}
		catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
