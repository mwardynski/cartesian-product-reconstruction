package at.ac.unileoben.mat.dissertation.config;


import at.ac.unileoben.mat.dissertation.printout.GraphPrinter;
import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.SquareReconstructionData;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Profile("printSquares")
public class PrintSingleSquareReconstructionAspect
{

  @Autowired
  GraphPrinter graphPrinter;

  @Pointcut("execution(* at.ac.unileoben.mat.dissertation.reconstruction.services.SquareFindingService.findAndProcessSquareForTwoEdges(..)) " +
          "&& args(squareReconstructionData,baseEdge,otherEdge)")
  private void findingSquareOperation(SquareReconstructionData squareReconstructionData, Edge baseEdge, Edge otherEdge)
  {
  }

  @Before("findingSquareOperation(squareReconstructionData,baseEdge,otherEdge)")
  public void addFindingSquareSnapshot(SquareReconstructionData squareReconstructionData, Edge baseEdge, Edge otherEdge)
  {
    graphPrinter.createFindingSquareSnapshot(baseEdge, otherEdge);
  }

  @Pointcut("execution(* at.ac.unileoben.mat.dissertation.reconstruction.services.SquareColoringService.colorEdge(..)) " +
          "&& args(baseEdge,squareEdge,otherColorBaseEdge,squareReconstructionData)")
  private void coloringSquareOperation(Edge baseEdge, Edge squareEdge, Edge otherColorBaseEdge, SquareReconstructionData squareReconstructionData)
  {
  }

  @Before("coloringSquareOperation(baseEdge,squareEdge,otherColorBaseEdge,squareReconstructionData)")
  public void addFindingSquareSnapshot(Edge baseEdge, Edge squareEdge, Edge otherColorBaseEdge, SquareReconstructionData squareReconstructionData)
  {
    graphPrinter.createColoringSquareSnapshot(baseEdge, squareEdge, otherColorBaseEdge);
  }

  @AfterReturning("execution(* at.ac.unileoben.mat.dissertation.reconstruction.services.SingleSquareReconstructionService.reconstructUsingSquares(..))")
  public void printColoredGraph(JoinPoint joinPoint)
  {
    graphPrinter.printFactorization();
  }
}
