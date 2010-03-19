package com.stocks.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class TimerAspect {
	@Pointcut("execution(* com.stocks.command.*.*(..))")
	public void executePointcut(){}
	
	@Before("executePointcut()")
	public void before(){
		System.out.println( "Before Called." );
	}
}
