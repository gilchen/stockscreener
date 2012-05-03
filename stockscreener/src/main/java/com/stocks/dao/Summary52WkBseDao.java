package com.stocks.dao;

import com.stocks.model.Summary52WkBse;

public interface Summary52WkBseDao extends Dao<Summary52WkBse, String> {
	void syncUp();
}
