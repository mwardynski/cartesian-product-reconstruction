package at.ac.unileoben.mat.dissertation.config;

import at.ac.unileoben.mat.dissertation.printout.GraphPrinter;
import at.ac.unileoben.mat.dissertation.printout.impl.GraphPrinterImpl;
import at.ac.unileoben.mat.dissertation.structure.Graph;
import at.ac.unileoben.mat.dissertation.structure.ReconstructionData;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Created by Marcin on 17.09.2017.
 */
@Aspect
@Component
@Profile("printReconstructionOnlyPreparation")
public class PrintReconstructionOnlyPreparationAspect
{

  @Autowired
  Graph graph;

  @Autowired
  ReconstructionData reconstructionData;

  @Autowired
  GraphPrinter graphPrinter;

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


}
