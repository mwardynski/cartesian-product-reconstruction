package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.reconstruction.services.SquareFindingService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.SquareHandlingStrategy;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class SquareFindingServiceImpl implements SquareFindingService
{

  @Autowired
  Graph graph;

  @Autowired
  GraphHelper graphHelper;

  @Autowired
  SquareHandlingStrategy squareHandlingStrategy;

  @Override
  public SquareFindingEnum findAndProcessSquareForTwoEdges(SquareReconstructionData squareReconstructionData, Edge iEdge, Edge jEdge, boolean firstRunForEdgePair)
  {
    SquareFindingEnum squareFindingResult = SquareFindingEnum.NO_SQUARE;
    List<List<Edge>> squareEdgesList = graphHelper.findSquaresForTwoEdges(iEdge, jEdge);

    for (List<Edge> edgesPair : squareEdgesList)
    {
      Edge iSquareEdge = edgesPair.get(0);
      Edge jSquareEdge = edgesPair.get(1);

      if (isAnyEdgeInSquareColored(iEdge, jEdge, iSquareEdge, jSquareEdge))
      {
        if (iEdge.getLabel() != null)
        {
          colorEdgesFormingSquare(jEdge, jSquareEdge, iEdge, iSquareEdge, squareReconstructionData);
        }
        else if (jEdge.getLabel() != null)
        {
          colorEdgesFormingSquare(iEdge, iSquareEdge, jEdge, jSquareEdge, squareReconstructionData);
        }
        else if (iSquareEdge.getLabel() != null)
        {
          colorEdgesFormingSquare(jEdge.getOpposite(), jSquareEdge.getOpposite(), iSquareEdge, iEdge, squareReconstructionData);
        }
        else if (jSquareEdge.getLabel() != null)
        {
          colorEdgesFormingSquare(iEdge.getOpposite(), iSquareEdge.getOpposite(), jSquareEdge, jEdge, squareReconstructionData);
        }
        squareFindingResult = SquareFindingEnum.SQUARE_COLORED;
      }
      else
      {
        if (firstRunForEdgePair)
        {
          squareFindingResult = SquareFindingEnum.REPEAT_SQUARE;
        }
        else
        {
          squareHandlingStrategy.colorEdgesWithoutSquare(Collections.singletonList(iEdge));
          colorEdgesFormingSquare(jEdge, jSquareEdge, iEdge, iSquareEdge, squareReconstructionData);
          squareFindingResult = SquareFindingEnum.SQUARE_COLORED;
        }
      }

      if (firstRunForEdgePair)
      {
        storeSquareFormingEdges(iEdge, jEdge, iSquareEdge, jSquareEdge, squareReconstructionData);
        squareHandlingStrategy.queueSquareTopVertexToNextVertices(iSquareEdge.getEndpoint(), squareReconstructionData);
      }
    }

    if (firstRunForEdgePair)
    {
      squareHandlingStrategy.queueSquareSideVertexToNextVertices(iEdge.getEndpoint(), squareReconstructionData);
      squareHandlingStrategy.queueSquareSideVertexToNextVertices(jEdge.getEndpoint(), squareReconstructionData);
    }

    if (squareFindingResult == SquareFindingEnum.NO_SQUARE)
    {
      MissingSquareData missingSquare = new MissingSquareData(squareReconstructionData.getCurrentVertex(), iEdge, jEdge);
      squareReconstructionData.getMissingSquares().add(missingSquare);
    }

    return squareFindingResult;
  }

  private boolean isAnyEdgeInSquareColored(Edge iEdge, Edge jEdge, Edge iSquareEdge, Edge jSquareEdge)
  {
    return iEdge.getLabel() != null || jEdge.getLabel() != null || iSquareEdge.getLabel() != null || jSquareEdge.getLabel() != null;
  }

  private void colorEdgesFormingSquare(Edge baseEdge, Edge squareEdge, Edge otherBaseEdge, Edge otherSquareEdge, SquareReconstructionData squareReconstructionData)
  {
    squareHandlingStrategy.colorEdge(baseEdge, squareEdge, otherBaseEdge, squareReconstructionData);
    squareHandlingStrategy.colorEdge(otherBaseEdge, otherSquareEdge, baseEdge, squareReconstructionData);
  }

  private void storeSquareFormingEdges(Edge iEdge, Edge jEdge, Edge iSquareEdge, Edge jSquareEdge, SquareReconstructionData squareReconstructionData)
  {
    if (squareReconstructionData.getSquareFormingEdges() == null)
    {
      return;
    }
    storePairOfSquareFormingEdges(iEdge, jEdge, jSquareEdge, squareReconstructionData);
    storePairOfSquareFormingEdges(iEdge.getOpposite(), jSquareEdge, jEdge, squareReconstructionData);
    storePairOfSquareFormingEdges(jEdge, iEdge, iSquareEdge, squareReconstructionData);
    storePairOfSquareFormingEdges(jEdge.getOpposite(), iSquareEdge, iEdge, squareReconstructionData);

    storePairOfSquareFormingEdges(iSquareEdge, jEdge.getOpposite(), jSquareEdge.getOpposite(), squareReconstructionData);
    storePairOfSquareFormingEdges(iSquareEdge.getOpposite(), jSquareEdge.getOpposite(), jEdge.getOpposite(), squareReconstructionData);
    storePairOfSquareFormingEdges(jSquareEdge, iEdge.getOpposite(), iSquareEdge.getOpposite(), squareReconstructionData);
    storePairOfSquareFormingEdges(jSquareEdge.getOpposite(), iSquareEdge.getOpposite(), iEdge.getOpposite(), squareReconstructionData);
  }

  private void storePairOfSquareFormingEdges(Edge referenceEdge, Edge firstParallelEdge, Edge secondParallelEdge, SquareReconstructionData squareReconstructionData)
  {
    Edge[][][] squareFormingEdges = squareReconstructionData.getSquareFormingEdges();
    Edge[] parallelEdges = squareFormingEdges[referenceEdge.getOrigin().getVertexNo()][referenceEdge.getEndpoint().getVertexNo()];

    Edge parallelEdge = parallelEdges[firstParallelEdge.getEndpoint().getVertexNo()];

    if (parallelEdge == squareReconstructionData.getMultipleSquaresWardenEdge())
    {
      return;
    }
    else
    {
      Edge edgeToAssign = secondParallelEdge;

      if (parallelEdge != secondParallelEdge)
      {
        edgeToAssign = squareReconstructionData.getMultipleSquaresWardenEdge();
      }

      parallelEdges[firstParallelEdge.getEndpoint().getVertexNo()] = edgeToAssign;
    }
  }
}
