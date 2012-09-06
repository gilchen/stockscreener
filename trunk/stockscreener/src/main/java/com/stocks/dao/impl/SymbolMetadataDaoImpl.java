package com.stocks.dao.impl;

import java.util.List;

import com.stocks.dao.AbstractDao;
import com.stocks.dao.SymbolMetadataDao;
import com.stocks.model.KeyValue;
import com.stocks.model.SymbolMetadata;

public class SymbolMetadataDaoImpl extends AbstractDao implements SymbolMetadataDao {
	public List<SymbolMetadata> findAll() {
		throw new RuntimeException("Not Implemented.");
	}

	public void delete(SymbolMetadata symbolMetadata) {
		throw new RuntimeException("Not Implemented.");
	}

	public SymbolMetadata read(String k) {
		return get(SymbolMetadata.class, k);
	}

	public void save(SymbolMetadata symbolMetadata) {
		throw new RuntimeException("Not Implemented.");
	}
}
