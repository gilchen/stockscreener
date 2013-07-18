package com.stocks.dao.impl;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import com.stocks.dao.AbstractDao;
import com.stocks.dao.AggregateInformationDetailsDao;
import com.stocks.model.AggregateInformationDetails;
import com.stocks.model.AggregateInformationDetailsPK;

public class AggregateInformationDetailsDaoImpl extends AbstractDao implements AggregateInformationDetailsDao {
	public void delete(AggregateInformationDetails aggregateInformationDetails) {
		entityManager.remove(aggregateInformationDetails);
	}

	public List<AggregateInformationDetails> findAll() {
		throw new RuntimeException("Not Implemented.");
	}
	
	public List<AggregateInformationDetails> findAggregateInformationDetailsBySymbolAndTradeDate(final String symbol, final Date tradeDate){
		Query query = entityManager.createNamedQuery("aggregateInformationDetailsBySymbolAndTradeDate");
		query.setHint("org.hibernate.fetchSize", "500");
		query.setParameter("symbol", symbol);
		query.setParameter("tradeDate", tradeDate);
		List<AggregateInformationDetails> results = query.getResultList();
		
		return results;
	}

	public List<AggregateInformationDetails> findAggregateInformationDetailsBySymbol(final String symbol){
		Query query = entityManager.createNamedQuery("aggregateInformationDetailsBySymbol");
		query.setHint("org.hibernate.fetchSize", "500");
		query.setParameter("symbol", symbol);
		List<AggregateInformationDetails> results = query.getResultList();
		
		return results;
	}
	
	public AggregateInformationDetails read(AggregateInformationDetailsPK aggregateInformationDetailsPK) {
		return get(AggregateInformationDetails.class, aggregateInformationDetailsPK);
	}

	public void save(AggregateInformationDetails aggregateInformationDetails) {
		try{
			entityManager.persist(aggregateInformationDetails);
		}
		catch(Exception e){
			throw new RuntimeException(e);
		}
	}
}
