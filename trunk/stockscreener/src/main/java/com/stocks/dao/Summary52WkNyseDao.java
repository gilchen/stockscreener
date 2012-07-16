package com.stocks.dao;

import com.stocks.model.Summary52WkNyse;

public interface Summary52WkNyseDao extends Dao<Summary52WkNyse, String> {
	public enum DURATION {_52_, _104_, _156_};
	void syncUp(DURATION duration);
}
