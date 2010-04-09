package com.stocks.dao.impl;

import java.util.List;

import com.stocks.dao.AbstractDao;
import com.stocks.dao.AlertDao;
import com.stocks.model.Alert;
import com.stocks.search.AlertResult;

public class AlertDaoImpl extends AbstractDao implements AlertDao {
	public void delete(Alert alert) {
		entityManager.remove(alert);
	}

	public List<Alert> findAll() {
		return entityManager.createNamedQuery("allAlerts").getResultList();
	}
	
	public List<AlertResult> findAlertResultsByTrxType(String trxType) {
		return entityManager.createNamedQuery("allAlertResultsByTrxType").setParameter("trxType", trxType).getResultList();
	}

	public Alert read(Long id) {
		return get(Alert.class, id);
	}

	public void save(Alert alert) {
		try{
			if( alert.getAlertId() == null ){
				entityManager.persist(alert);
			}else{
				entityManager.merge(alert);
			}
		}
		catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
