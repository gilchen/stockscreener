package com.stocks.dao;

import java.util.Date;
import java.util.List;

import com.stocks.model.AggregateInformationDetails;
import com.stocks.model.AggregateInformationDetailsPK;

public interface AggregateInformationDetailsDao extends Dao<AggregateInformationDetails, AggregateInformationDetailsPK> {
	public List<AggregateInformationDetails> findAggregateInformationDetailsBySymbolAndTradeDate(final String symbol, final Date tradeDate);
}
