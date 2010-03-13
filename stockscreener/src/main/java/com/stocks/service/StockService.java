package com.stocks.service;

import java.util.List;

import com.stocks.model.Alert;

public interface StockService {
	void saveAlert(Alert alert);
	List<Alert> getAllAlerts();
}
