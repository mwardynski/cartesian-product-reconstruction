package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.structure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReconstructionSingleEdgeResultVerifierImpl extends AbstractReconstructionResultVerifier
{

  @Autowired
  TestCaseContext testCaseContext;

  @Override
  public void compareFoundMissingVertexWithCorrectResult(ResultMissingSquaresData resultMissingSquaresData)
  {
    if (resultMissingSquaresData.getMissingEdgesFormation() == MissingEdgesFormation.CYCLE
            || resultMissingSquaresData.getMissingEdgesFormation() == MissingEdgesFormation.SPIKE
            || resultMissingSquaresData.getMissingEdgesFormation() == MissingEdgesFormation.BRIDGE)
    {
      boolean correctResult = checkCorrectnessUsingFactorization(resultMissingSquaresData.getResultNoSquareAtAllMissingSquares());
      testCaseContext.setCorrectResult(correctResult);
    }
    else
    {
      for (Integer selectedColor : resultMissingSquaresData.getResultIncludedColors())
      {
        List<MissingSquaresUniqueEdgesData> resultMissingSquares = resultMissingSquaresData.getResultMissingSquaresByColor()[selectedColor];

        boolean correctResult = checkCorrectnessUsingFactorization(resultMissingSquares);
        testCaseContext.setCorrectResult(correctResult);

        if (testCaseContext.isCorrectResult())
        {
          break;
        }
      }
    }
  }

  private void checkFoundMissingEdgeCorrectness(Edge missingEdge)
  {
    Integer actualMissingEdgeOriginVertexNumber = graph.getReverseReindexArray()[missingEdge.getOrigin().getVertexNo()];
    Integer actualMissingEdgeEndpointVertexNumber = graph.getReverseReindexArray()[missingEdge.getEndpoint().getVertexNo()];

    Integer expectedMissingEdgeOriginVertexNumber = testCaseContext.getRemovedEdge().getOrigin().getVertexNo();
    Integer expectedMissingEdgeEndpointVertexNumber = testCaseContext.getRemovedEdge().getEndpoint().getVertexNo();

    if ((actualMissingEdgeOriginVertexNumber == expectedMissingEdgeOriginVertexNumber && actualMissingEdgeEndpointVertexNumber == expectedMissingEdgeEndpointVertexNumber)
            || (actualMissingEdgeOriginVertexNumber == expectedMissingEdgeEndpointVertexNumber && actualMissingEdgeEndpointVertexNumber == expectedMissingEdgeOriginVertexNumber))
    {
      testCaseContext.setCorrectResult(true);
    }
  }
}
