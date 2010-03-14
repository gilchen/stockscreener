package com.stocks.service;

import com.stocks.model.Bse;

public interface StockImportService {
	void saveBse(Bse bse) throws Exception;
	//void saveNyse(Nyse nyse) throws Exception;
}
