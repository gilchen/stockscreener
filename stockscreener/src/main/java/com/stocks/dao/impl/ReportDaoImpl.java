package com.stocks.dao.impl;

import java.util.List;

import com.stocks.dao.AbstractDao;
import com.stocks.dao.ReportDao;
import com.stocks.model.Report;

public class ReportDaoImpl extends AbstractDao implements ReportDao {
	public void delete(Report report) {
		entityManager.remove(report);
	}

	public List<Report> findAll() {
		throw new RuntimeException("Not Implemented.");
	}
	
	public Report read(String reportName) {
		return get(Report.class, reportName);
	}

	public void save(Report report) {
		try{
			entityManager.merge(report);
		}
		catch(Exception e){
			throw new RuntimeException(e);
		}
	}
}
