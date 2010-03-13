package com.stocks.dao.impl;

import java.util.List;

import javax.persistence.EntityManagerFactory;

import com.stocks.dao.AbstractDao;
import com.stocks.dao.BseIciciMappingDao;
import com.stocks.model.Alert;
import com.stocks.model.BseIciciMapping;

public class BseIciciMappingDaoImpl extends AbstractDao implements BseIciciMappingDao {
	private EntityManagerFactory emf;
	
	public EntityManagerFactory getEmf() {
		return emf;
	}

	public void setEmf(EntityManagerFactory emf) {
		this.emf = emf;
		entityManager = emf.createEntityManager();
	}

	public void delete(BseIciciMapping bseIciciMapping) {
		entityManager.remove(bseIciciMapping);
	}

	public List<BseIciciMapping> findAll() {
		throw new RuntimeException("Not Implemented.");
	}

	public BseIciciMapping read(String stockCode) {
		return get(BseIciciMapping.class, stockCode);
	}

	public void save(BseIciciMapping bseIciciMapping) {
		try{
			entityManager.getTransaction().begin();
			entityManager.persist(bseIciciMapping);
			entityManager.flush();
			entityManager.getTransaction().commit();
		}
		catch(Exception e){
			entityManager.getTransaction().rollback();
			throw new RuntimeException(e);
		}
	}
}
