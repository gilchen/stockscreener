package com.stocks.aspect;

import java.util.Date;
import java.util.Timer;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Required;

import com.stocks.command.StockBoxCommand;
import com.stocks.util.PercentCompleteReporter;

@Aspect
public class TimerAspect {
	private Timer timer;
	private PercentCompleteReporter percentCompleteReporter;

	@Pointcut("execution(* com.stocks.command.*.*(..))")
	public void executePointcut(){}
	
	@Before("executePointcut()")
	public void before(){
		System.out.println( "Before: Setting Timer." );
		this.getPercentCompleteReporter().setPercentComplete(0D);
		this.getTimer().scheduleAtFixedRate(this.getPercentCompleteReporter(), new Date(), 15*1000);
	}
	
	@After("executePointcut()")
	public void after(){
		System.out.println( "After: Command Completed." );
		this.getPercentCompleteReporter().cancel();
	}

	public Timer getTimer() {
		return timer;
	}

	@Required
	public void setTimer(Timer timer) {
		this.timer = timer;
	}

	public PercentCompleteReporter getPercentCompleteReporter() {
		return percentCompleteReporter;
	}

	@Required
	public void setPercentCompleteReporter(
			PercentCompleteReporter percentCompleteReporter) {
		this.percentCompleteReporter = percentCompleteReporter;
	}
	
	
}
