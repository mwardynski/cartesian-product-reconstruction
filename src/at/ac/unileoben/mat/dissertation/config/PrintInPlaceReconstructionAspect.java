package at.ac.unileoben.mat.dissertation.config;

import at.ac.unileoben.mat.dissertation.printout.GraphPrinter;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
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
public class PrintInPlaceReconstructionAspect
{

  @Autowired
  Graph graph;

  @Autowired
  ReconstructionData reconstructionData;

  @Autowired
  GraphPrinter graphPrinter;

  @Before("execution(* at.ac.unileoben.mat.dissertation.linearfactorization.GraphFactorizer.factorize(..))")
  public void addGraphInitialSnapshot(JoinPoint joinPoint)
  {
    if (reconstructionData.getOperationOnGraph() == OperationOnGraph.IN_PLACE_RECONSTRUCTION)
    {
      graphPrinter.createLayerSnapshot();
    }
  }

  @AfterReturning("execution(* at.ac.unileoben.mat.dissertation.linearfactorization.ConsistencyChecker.checkConsistency(..))")
  public void addGraphLayerSnapshot(JoinPoint joinPoint)
  {
    if (reconstructionData.getOperationOnGraph() == OperationOnGraph.IN_PLACE_RECONSTRUCTION)
    {
      graphPrinter.createLayerSnapshot();
    }
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
      if (reconstructionData.getOperationOnGraph() == OperationOnGraph.IN_PLACE_RECONSTRUCTION)
      {
        graphPrinter.createMergeSnapshot(edges, mergeTag);
      }
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
      if (reconstructionData.getOperationOnGraph() == OperationOnGraph.IN_PLACE_RECONSTRUCTION)
      {
        graphPrinter.createLabelSnapshot(edge, color, name, labelOperationDetail);
      }
    }
  }

  @AfterReturning("execution(* at.ac.unileoben.mat.dissertation.linearfactorization.GraphFactorizer.factorize(..))")
  public void printFactorizedGraph(JoinPoint joinPoint)
  {
    if (reconstructionData.getOperationOnGraph() == OperationOnGraph.IN_PLACE_RECONSTRUCTION)
    {
      graphPrinter.printFactorization();
    }
  }

  @AfterReturning("execution(* at.ac.unileoben.mat.dissertation.reconstruction.Reconstruction.reconstruct(..))")
  public void printGraph(JoinPoint joinPoint)
  {
    if (reconstructionData.getOperationOnGraph() == OperationOnGraph.IN_PLACE_RECONSTRUCTION)
    {
      graphPrinter.printFactorization();
    }
  }

}
