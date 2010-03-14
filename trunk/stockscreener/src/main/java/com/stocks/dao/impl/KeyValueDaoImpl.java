package com.stocks.dao.impl;

import java.util.List;

import javax.persistence.EntityManagerFactory;

import com.stocks.dao.AbstractDao;
import com.stocks.dao.KeyValueDao;
import com.stocks.model.KeyValue;

public class KeyValueDaoImpl extends AbstractDao implements KeyValueDao {
	private EntityManagerFactory emf;
	
	public EntityManagerFactory getEmf() {
		return emf;
	}

	public void setEmf(EntityManagerFactory emf) {
		this.emf = emf;
		entityManager = emf.createEntityManager();
	}

	public List<KeyValue> findAll() {
		throw new RuntimeException("Not Implemented.");
	}

	public void delete(KeyValue keyValue) {
		entityManager.remove(keyValue);
	}

	public KeyValue read(String k) {
		return get(KeyValue.class, k);
	}

	public void save(KeyValue keyValue) {
		try{
			entityManager.getTransaction().begin();
			entityManager.merge(keyValue);
			entityManager.flush();
			entityManager.getTransaction().commit();
			System.out.println( "KeyValue Saved." );
		}
		catch(Exception e){
			e.printStackTrace();
			entityManager.getTransaction().rollback();
			throw new RuntimeException(e);
		}
	}
}
