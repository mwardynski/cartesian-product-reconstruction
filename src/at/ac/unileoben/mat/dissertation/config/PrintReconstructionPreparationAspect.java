package at.ac.unileoben.mat.dissertation.config;

import at.ac.unileoben.mat.dissertation.printout.GraphPrinter;
import at.ac.unileoben.mat.dissertation.printout.impl.GraphPrinterImpl;
import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.Graph;
import at.ac.unileoben.mat.dissertation.structure.MergeTagEnum;
import at.ac.unileoben.mat.dissertation.structure.ReconstructionData;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by Marcin on 17.09.2017.
 */
@Aspect
@Component
@Profile("printReconstructionPreparation")
public class PrintReconstructionPreparationAspect
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
    graphPrinter.createLayerSnapshot(GraphPrinterImpl.LAYER_DONE);
  }

  @AfterReturning("execution(* at.ac.unileoben.mat.dissertation.linearfactorization.ConsistencyChecker.checkConsistency(..))")
  public void addLabeledAndConsistentGraphLayerSnapshot(JoinPoint joinPoint)
  {
    if (graph.getLayers().size() - 1 == reconstructionData.getCurrentLayerNo()
            && graph.getVertices().get(graph.getVertices().size() - 1).getEdges().get(0).getLabel() != null)
    {
      graphPrinter.createLayerSnapshot(GraphPrinterImpl.LAYER_DONE);
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
    if (prevGraphColorsAmount != actGraphColorsAmount && mergeTag != MergeTagEnum.PREPARE)
    {
      graphPrinter.createMergeSnapshot(edges, mergeTag);
    }
    return result;

  }

  @AfterReturning("execution(* at.ac.unileoben.mat.dissertation.reconstruction.services.ReconstructionBackupLayerService.recoverAfterCompleteMerge(..))")
  public void addReconstructionPreparationSnapshot(JoinPoint joinPoint)
  {
    graphPrinter.createLayerSnapshot(GraphPrinterImpl.RECONSTRUCTION_RECOVERY);
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
