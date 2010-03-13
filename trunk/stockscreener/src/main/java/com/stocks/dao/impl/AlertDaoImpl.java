package com.stocks.dao.impl;

import java.util.List;

import javax.persistence.EntityManagerFactory;

import com.stocks.dao.AbstractDao;
import com.stocks.dao.AlertDao;
import com.stocks.model.Alert;

public class AlertDaoImpl extends AbstractDao implements AlertDao {
	private EntityManagerFactory emf;
	
	public EntityManagerFactory getEmf() {
		return emf;
	}

	public void setEmf(EntityManagerFactory emf) {
		this.emf = emf;
		entityManager = emf.createEntityManager();
	}

	public void delete(Alert alert) {
		entityManager.remove(alert);
	}

	public List<Alert> findAll() {
		return entityManager.createNamedQuery("allAlerts").getResultList();
	}

	public Alert read(Long id) {
		return get(Alert.class, id);
	}

	public void save(Alert alert) {
		try{
			entityManager.getTransaction().begin();
			if( alert.getAlertId() == null ){
				System.out.println( "entityManager: " +entityManager );
				entityManager.persist(alert);
				System.out.println( "alert.getAlertId(): " +alert.getAlertId() );
			}else{
				entityManager.merge(alert);
			}
			entityManager.flush();
			entityManager.getTransaction().commit();
			System.out.println( "Alert Saved." );
		}
		catch(Exception e){
			e.printStackTrace();
			entityManager.getTransaction().rollback();
			throw new RuntimeException(e);
		}
	}
}
