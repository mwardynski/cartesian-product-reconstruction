package at.ac.unileoben.mat.dissertation.config;

import at.ac.unileoben.mat.dissertation.printout.GraphPrinter;
import at.ac.unileoben.mat.dissertation.printout.impl.GraphPrinterImpl;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Marcin
 * Date: 13.11.15
 * Time: 18:41
 * To change this template use File | Settings | File Templates.
 */
@Aspect
@Component
@Profile("print")
public class PrintFactorizationAspect
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

  @Before("execution(* at.ac.unileoben.mat.dissertation.linearfactorization.ConsistencyChecker.checkConsistency(..))")
  public void addLabeledNotNecessarilyConsistentGraphLayerSnapshot(JoinPoint joinPoint)
  {
    graphPrinter.createLayerSnapshot(GraphPrinterImpl.LAYER_BEFORE_CONSISTENCY_CHECK);
  }

  @AfterReturning("execution(* at.ac.unileoben.mat.dissertation.linearfactorization.ConsistencyChecker.checkConsistency(..))")
  public void addLabeledAndConsistentGraphLayerSnapshot(JoinPoint joinPoint)
  {
    graphPrinter.createLayerSnapshot(GraphPrinterImpl.LAYER_DONE);
  }


  @Pointcut("execution(* at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService.mergeColorsForEdges(..)) && args(edges,mergeTag)")
  private void mergeColorsOperation(List<Edge> edges, MergeTagEnum mergeTag)
  {
  }

  @Around("mergeColorsOperation(edges,mergeTag)")
  public Object addColorsMergeSnapshot(ProceedingJoinPoint proceedingJoinPoint, List<Edge> edges, MergeTagEnum mergeTag) throws Throwable
  {
    int prevGraphColorsAmount = graph.getGraphColoring().getActualColors().size();
    Object result = proceedingJoinPoint.proceed();
    int actGraphColorsAmount = graph.getGraphColoring().getActualColors().size();
    if (prevGraphColorsAmount != actGraphColorsAmount)
    {
      graphPrinter.createMergeSnapshot(edges, mergeTag);
    }
    return result;

  }

  @Pointcut("execution(* at.ac.unileoben.mat.dissertation.linearfactorization.services.EdgeService.addLabel(..)) && args(edge,color,name,squareMatchingEdge,labelOperationDetail)")
  private void labelEdgeOperation(Edge edge, int color, int name, Edge squareMatchingEdge, LabelOperationDetail labelOperationDetail)
  {
  }

  @Before("labelEdgeOperation(edge,color,name,squareMatchingEdge,labelOperationDetail)")
  public void addEdgeLabelSnapshot(Edge edge, int color, int name, Edge squareMatchingEdge, LabelOperationDetail labelOperationDetail)
  {
    if (labelOperationDetail.getType() != LabelOperationEnum.PREPARE && labelOperationDetail.getType() != LabelOperationEnum.OPPOSITE)
    {
      graphPrinter.createLabelSnapshot(edge, color, name, labelOperationDetail);
    }
  }

  @AfterReturning("execution(* at.ac.unileoben.mat.dissertation.linearfactorization.GraphFactorizer.factorize(..))")
  public void printFactorizedGraph(JoinPoint joinPoint)
  {
    graphPrinter.printFactorization();
  }

  @AfterReturning("execution(* at.ac.unileoben.mat.dissertation.reconstruction.Reconstruction.reconstruct(..))")
  public void printGraph(JoinPoint joinPoint)
  {
    graphPrinter.printFactorization();
  }

  @AfterThrowing("execution(* at.ac.unileoben.mat.dissertation.reconstruction.Reconstruction.reconstruct(..))")
  public void printGraphOnFailure()
  {
    graphPrinter.printFactorization();
  }

}
