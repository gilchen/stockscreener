package com.stocks.dao.impl;

import java.util.List;

import javax.persistence.EntityManagerFactory;

import com.stocks.dao.AbstractDao;
import com.stocks.dao.BseDao;
import com.stocks.model.Bse;
import com.stocks.model.BsePK;

public class BseDaoImpl extends AbstractDao implements BseDao {
	private EntityManagerFactory emf;
	
	public EntityManagerFactory getEmf() {
		return emf;
	}

	public void setEmf(EntityManagerFactory emf) {
		this.emf = emf;
		entityManager = emf.createEntityManager();
	}

	public void delete(Bse bse) {
		entityManager.remove(bse);
	}

	public List<Bse> findAll() {
		throw new RuntimeException("Not Implemented.");
	}

	public Bse read(BsePK bsePK) {
		return get(Bse.class, bsePK);
	}

	public void save(Bse bse) {
		try{
			entityManager.getTransaction().begin();
			entityManager.persist(bse);
			entityManager.flush();
			entityManager.getTransaction().commit();
		}
		catch(Exception e){
			entityManager.getTransaction().rollback();
			throw new RuntimeException(e);
		}
	}
}
