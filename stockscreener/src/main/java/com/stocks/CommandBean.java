package com.stocks;

import javax.faces.event.ActionEvent;

import org.apache.commons.chain.Chain;
import org.apache.commons.chain.impl.ContextBase;

import com.stocks.command.StockBoxCommand;

public class CommandBean {
//    private StockBoxCommand stockBoxCommand;
    private Chain reportChain;

//	public StockBoxCommand getStockBoxCommand() {
//		return stockBoxCommand;
//	}
//
//	public void setStockBoxCommand(StockBoxCommand stockBoxCommand) {
//		this.stockBoxCommand = stockBoxCommand;
//	}
    
	public Chain getReportChain() {
		return reportChain;
	}

	public void setReportChain(Chain reportChain) {
		this.reportChain = reportChain;
	}

	public void executeStockBoxCommand(ActionEvent ae){
		try{
			System.out.println( "executeStockBoxCommand" );
			reportChain.execute(new ContextBase());
			//boolean returnCode = getStockBoxCommand().execute(new ContextBase());
			//System.out.println( "returnCode: " +returnCode );
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

}
