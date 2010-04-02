package com.stocks.service;

public interface ImportService {
	public static final String BSE_IMPORT_KEY = "LAST_BSE_IMPORT";
	public static final String NYSE_IMPORT_KEY = "LAST_NYSE_IMPORT";
	public static final String NASDAQ_IMPORT_KEY = "LAST_NASDAQ_IMPORT";
	public static final String AMEX_IMPORT_KEY = "LAST_AMEX_IMPORT";
	

	public void importData() throws Exception;
}
