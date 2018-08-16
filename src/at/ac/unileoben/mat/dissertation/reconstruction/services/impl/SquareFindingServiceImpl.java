package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.reconstruction.services.SquareFindingService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.SquareHandlingStrategy;
import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.Graph;
import at.ac.unileoben.mat.dissertation.structure.MissingSquareData;
import at.ac.unileoben.mat.dissertation.structure.SquareReconstructionData;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
  public boolean findAndProcessSquareForTwoEdges(SquareReconstructionData squareReconstructionData, Edge iEdge, Edge jEdge)
  {
    List<List<Edge>> squareEdgesList = graphHelper.findSquaresForTwoEdges(iEdge, jEdge);
    boolean squareFound = CollectionUtils.isNotEmpty(squareEdgesList);

    squareEdgesList.stream()
            .forEach(edgesPair ->
            {
              Edge iSquareEdge = edgesPair.get(0);
              Edge jSquareEdge = edgesPair.get(1);

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
                System.out.println("iSquareEdge");
                colorEdgesFormingSquare(jEdge.getOpposite(), jSquareEdge.getOpposite(), iSquareEdge, iEdge, squareReconstructionData);
              }
              else if (jSquareEdge.getLabel() != null)
              {
                System.out.println("jSquareEdge");
                colorEdgesFormingSquare(iEdge.getOpposite(), iSquareEdge.getOpposite(), jSquareEdge, jEdge, squareReconstructionData);
              }
              else
              {
                throw new RuntimeException(String.format("no way to color edges: %s, %s, %s, %s", iEdge, jEdge, iSquareEdge, jSquareEdge));
              }

              storeSquareFormingEdges(iEdge, jEdge, iSquareEdge, jSquareEdge, squareReconstructionData);

              squareHandlingStrategy.queueSquareTopVertexToNextVertices(iSquareEdge.getEndpoint(), squareReconstructionData);
            });

    squareHandlingStrategy.queueSquareSideVertexToNextVertices(iEdge.getEndpoint(), squareReconstructionData);
    squareHandlingStrategy.queueSquareSideVertexToNextVertices(jEdge.getEndpoint(), squareReconstructionData);

    if (!squareFound)
    {
      MissingSquareData missingSquare = new MissingSquareData(squareReconstructionData.getCurrentVertex(), iEdge, jEdge);
      squareReconstructionData.getMissingSquares().add(missingSquare);
    }

    return squareFound;
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
