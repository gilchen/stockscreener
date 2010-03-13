package com.stocks.service;

import java.util.List;

import com.stocks.model.Alert;
import com.stocks.model.BseIciciMapping;

public interface StockService {
	void saveAlert(Alert alert) throws Exception;
	List<Alert> getAllAlerts();
	
	void saveBseIciciMapping(BseIciciMapping bseIciciMapping) throws Exception;
	Integer getBseScCode(String stockCode) throws Exception;
}
