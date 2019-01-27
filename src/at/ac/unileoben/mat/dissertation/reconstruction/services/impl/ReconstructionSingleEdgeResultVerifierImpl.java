package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.reconstruction.services.ReconstructionResultVerifier;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReconstructionSingleEdgeResultVerifierImpl implements ReconstructionResultVerifier
{

  @Autowired
  Graph graph;

  @Autowired
  TestCaseContext testCaseContext;

  @Override
  public void compareFoundMissingVertexWithCorrectResult(ResultMissingSquaresData resultMissingSquaresData)
  {
    if (resultMissingSquaresData.getMissingEdgesFormation() == MissingEdgesFormation.SPIKE
//            || resultMissingSquaresData.getMissingEdgesFormation() == MissingEdgesFormation.CYCLE
            || resultMissingSquaresData.getMissingEdgesFormation() == MissingEdgesFormation.SINGLE)
    {
      System.out.println("specially colored edges spotted: " + resultMissingSquaresData.getMissingEdgesFormation());
      testCaseContext.setCorrectResult(true);
    }
    else if (resultMissingSquaresData.getMissingEdgesFormation() == MissingEdgesFormation.CYCLE)
    {
      Edge missingEdge = extractMissingEdge(resultMissingSquaresData.getResultNoSquareAtAllMissingSquares());
      checkFoundMissingEdgeCorrectness(missingEdge);
    }
    else
    {
      for (Integer selectedColor : resultMissingSquaresData.getResultIncludedColors())
      {
        List<MissingSquaresUniqueEdgesData> resultMissingSquares = resultMissingSquaresData.getResultMissingSquaresByColor()[selectedColor];

        Edge missingEdge = extractMissingEdge(resultMissingSquares);
        if (missingEdge == null)
        {
          continue;
        }

        checkFoundMissingEdgeCorrectness(missingEdge);
        if (testCaseContext.isCorrectResult())
        {
          break;
        }
      }
    }
  }

  private Edge extractMissingEdge(List<MissingSquaresUniqueEdgesData> resultMissingSquares)
  {
    Edge missingEdge = null;
    Edge[][] resultMissingSquarePairs = findMissingSquarePairsForSelectedColor(resultMissingSquares);
    if (resultMissingSquarePairs != null)
    {
      missingEdge = findMissingEdge(resultMissingSquares, resultMissingSquarePairs);
    }
    return missingEdge;
  }

  public Edge[][] findMissingSquarePairsForSelectedColor(List<MissingSquaresUniqueEdgesData> selectedIrregularMissingSquaresByColor)
  {
    Edge[][] oneEdgeByOtherEdge = new Edge[graph.getVertices().size()][graph.getVertices().size()];

    for (MissingSquaresUniqueEdgesData missingSquare : selectedIrregularMissingSquaresByColor)
    {
      Edge baseEdge = missingSquare.getBaseEdge();
      Edge otherEdge = missingSquare.getOtherEdge();

      boolean storedForBaseEdge = storeOneEdgeByOtherEdge(oneEdgeByOtherEdge, baseEdge, otherEdge);
      boolean storedForOtherEdge = storeOneEdgeByOtherEdge(oneEdgeByOtherEdge, otherEdge, baseEdge);

      if (!storedForBaseEdge || !storedForOtherEdge)
      {
        oneEdgeByOtherEdge = null;
        break;
      }
    }
    return oneEdgeByOtherEdge;
  }

  private boolean storeOneEdgeByOtherEdge(Edge[][] oneEdgeByOtherEdge, Edge baseEdge, Edge otherEdge)
  {
    boolean edgesStored = false;

    int edgeOriginNo = baseEdge.getOrigin().getVertexNo();
    int edgeEndpointNo = baseEdge.getEndpoint().getVertexNo();
    if (oneEdgeByOtherEdge[edgeOriginNo][edgeEndpointNo] == null)
    {
      oneEdgeByOtherEdge[edgeOriginNo][edgeEndpointNo] = otherEdge;
      edgesStored = true;
    }
    return edgesStored;
  }

  public Edge findMissingEdge(List<MissingSquaresUniqueEdgesData> selectedIrregularMissingSquares,
                              Edge[][] missingSquarePairs)
  {
    Edge missingEdge = null;

    for (MissingSquaresUniqueEdgesData missingSquare : selectedIrregularMissingSquares)
    {
      Edge baseEdge = missingSquare.getBaseEdge();
      Edge otherEdge = missingSquare.getOtherEdge();

      Edge matchingEdge = missingSquarePairs[baseEdge.getEndpoint().getVertexNo()][baseEdge.getOrigin().getVertexNo()];
      Vertex edgeEndpoint = otherEdge.getEndpoint();

      Edge otherMatchingEdge = missingSquarePairs[otherEdge.getEndpoint().getVertexNo()][otherEdge.getOrigin().getVertexNo()];
      Vertex otherEdgeEndpoint = baseEdge.getEndpoint();

      if (matchingEdge == null)
      {
        matchingEdge = otherMatchingEdge;
        edgeEndpoint = otherEdgeEndpoint;
      }

      if (matchingEdge != null)
      {
        missingEdge = new Edge(edgeEndpoint, matchingEdge.getEndpoint());
        break;
      }
    }
    return missingEdge;
  }

  private void checkFoundMissingEdgeCorrectness(Edge missingEdge)
  {
    Integer actualMissingEdgeOriginVertexNumber = graph.getReverseReindexArray()[missingEdge.getOrigin().getVertexNo()];
    Integer actualMissingEdgeEndpointVertexNumber = graph.getReverseReindexArray()[missingEdge.getEndpoint().getVertexNo()];

    Integer expectedMissingEdgeOriginVertexNumber = graph.getReverseReindexArray()[missingEdge.getOrigin().getVertexNo()];
    Integer expectedMissingEdgeEndpointVertexNumber = graph.getReverseReindexArray()[missingEdge.getEndpoint().getVertexNo()];

    if ((actualMissingEdgeOriginVertexNumber == expectedMissingEdgeOriginVertexNumber && actualMissingEdgeEndpointVertexNumber == expectedMissingEdgeEndpointVertexNumber)
            || (actualMissingEdgeOriginVertexNumber == expectedMissingEdgeEndpointVertexNumber && actualMissingEdgeEndpointVertexNumber == expectedMissingEdgeOriginVertexNumber))
    {
      testCaseContext.setCorrectResult(true);
    }
  }
}
