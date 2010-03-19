package com.stocks;

import javax.faces.event.ActionEvent;

import org.apache.commons.chain.Chain;
import org.apache.commons.chain.impl.ContextBase;

import com.stocks.command.StockBoxCommand;

public class CommandBean {
//    private StockBoxCommand stockBoxCommand;
    private Chain reportChain;
    private ContextBase chainContext;

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

	public ContextBase getChainContext() {
		return chainContext;
	}

	public void setChainContext(ContextBase chainContext) {
		this.chainContext = chainContext;
	}
	
	public void executeStockBoxCommand(ActionEvent ae){
		try{
			System.out.println( "executeStockBoxCommand. chainContext: " +chainContext );
			reportChain.execute(chainContext);
			//boolean returnCode = getStockBoxCommand().execute(new ContextBase());
			//System.out.println( "returnCode: " +returnCode );
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

}
