package com.stocks.dao;

import com.stocks.model.Summary52WkNyse;

public interface Summary52WkNyseDao extends Dao<Summary52WkNyse, String> {
	void syncUp();
}
