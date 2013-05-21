package com.stocks.dao.impl;

import java.util.List;

import javax.persistence.Query;

import com.stocks.dao.AbstractDao;
import com.stocks.dao.AggregateInformationDao;
import com.stocks.model.AggregateInformation;
import com.stocks.model.AggregateInformationPK;

public class AggregateInformationDaoImpl extends AbstractDao implements AggregateInformationDao {
	public void delete(AggregateInformation aggregateInformation) {
		entityManager.remove(aggregateInformation);
	}

	public List<AggregateInformation> findAll() {
		throw new RuntimeException("Not Implemented.");
	}
	
	public List<AggregateInformation> getAggregateInformationBySymbol(final String symbol){
		Query query = entityManager.createNamedQuery("aggregateInformationBySymbol");
		query.setHint("org.hibernate.fetchSize", "500");
		query.setParameter("symbol", symbol);
		List<AggregateInformation> results = query.getResultList();
		
		return results;
	}
	
	public AggregateInformation read(AggregateInformationPK aggregateInformationPK) {
		return get(AggregateInformation.class, aggregateInformationPK);
	}

	public void save(AggregateInformation aggregateInformation) {
		try{
			entityManager.persist(aggregateInformation);
		}
		catch(Exception e){
			throw new RuntimeException(e);
		}
	}
}
