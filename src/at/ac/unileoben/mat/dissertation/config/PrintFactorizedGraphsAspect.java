package at.ac.unileoben.mat.dissertation.config;

import at.ac.unileoben.mat.dissertation.printout.GraphPrinter;
import at.ac.unileoben.mat.dissertation.printout.impl.GraphPrinterImpl;
import at.ac.unileoben.mat.dissertation.structure.Graph;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Created by Marcin on 17.09.2017.
 */
@Aspect
@Component
@Profile("printFactorizedGraphs")
public class PrintFactorizedGraphsAspect
{

  @Autowired
  Graph graph;

  @Autowired
  GraphPrinter graphPrinter;

  @Before("execution(* at.ac.unileoben.mat.dissertation.linearfactorization.GraphFactorizer.factorize(..))")
  public void addGraphInitialSnapshot(JoinPoint joinPoint)
  {
    graphPrinter.createLayerSnapshot(GraphPrinterImpl.LAYER_DONE);
  }

  @AfterReturning("execution(* at.ac.unileoben.mat.dissertation.linearfactorization.ConsistencyChecker.checkConsistency(..))")
  public void addLabeledAndConsistentGraphLayerSnapshot(JoinPoint joinPoint)
  {
    if (graph.getVertices().get(graph.getVertices().size() - 1).getEdges().get(0).getLabel() != null)
    {
      graphPrinter.createLayerSnapshot(GraphPrinterImpl.LAYER_DONE);
    }
  }

  @AfterReturning("execution(* at.ac.unileoben.mat.dissertation.linearfactorization.GraphFactorizer.factorize(..))")
  public void printFactorizedGraph(JoinPoint joinPoint)
  {
    graphPrinter.printFactorization();
  }

}
