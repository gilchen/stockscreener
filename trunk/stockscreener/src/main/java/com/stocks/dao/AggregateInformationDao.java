package com.stocks.dao;

import java.util.List;

import com.stocks.model.AggregateInformation;
import com.stocks.model.AggregateInformationPK;

public interface AggregateInformationDao extends Dao<AggregateInformation, AggregateInformationPK> {
	public List<AggregateInformation> getAggregateInformationBySymbol(final String symbol);
}
