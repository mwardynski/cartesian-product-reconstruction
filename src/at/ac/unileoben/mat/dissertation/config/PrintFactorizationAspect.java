package at.ac.unileoben.mat.dissertation.config;

import at.ac.unileoben.mat.dissertation.printout.GraphPrinter;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created with IntelliJ IDEA.
 * User: Marcin
 * Date: 13.11.15
 * Time: 18:41
 * To change this template use File | Settings | File Templates.
 */
@Aspect
@Component
public class PrintFactorizationAspect
{

  @Autowired
  GraphPrinter graphPrinter;

  @Before("execution(* at.ac.unileoben.mat.dissertation.linearfactorization.GraphFactorizer.factorize(..))")
  public void addGraphInitialSnapshot(JoinPoint joinPoint)
  {
    graphPrinter.createGraphSnapshot();
  }

  @AfterReturning("execution(* at.ac.unileoben.mat.dissertation.linearfactorization.ConsistencyChecker.checkConsistency(..))")
  public void addGraphSnapshot(JoinPoint joinPoint)
  {
    graphPrinter.createGraphSnapshot();
  }

  @AfterReturning("execution(* at.ac.unileoben.mat.dissertation.linearfactorization.GraphFactorizer.factorize(..))")
  public void printFactorizedGraph(JoinPoint joinPoint)
  {
    graphPrinter.printFactorization();
  }
}
