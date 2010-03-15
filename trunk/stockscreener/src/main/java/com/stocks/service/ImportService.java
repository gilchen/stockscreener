package com.stocks.service;

import com.stocks.model.Nyse;

public interface ImportService {
	public static final String BSE_IMPORT_KEY = "LAST_BSE_IMPORT";
	public static final String NYSE_IMPORT_KEY = "LAST_NYSE_IMPORT";

	
	public void importData() throws Exception;
}
