package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.reconstruction.services.SquareColoringService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.SquareFindingService;
import at.ac.unileoben.mat.dissertation.structure.*;
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
  SquareColoringService squareColoringService;

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

              if (jEdge.getLabel() != null)
              {
                squareColoringService.colorEdge(iEdge, iSquareEdge, jEdge, squareReconstructionData);
                squareColoringService.colorEdge(jEdge, jSquareEdge, iEdge, squareReconstructionData);
              }
              else
              {
                squareColoringService.colorEdge(jEdge, jSquareEdge, iEdge, squareReconstructionData);
                squareColoringService.colorEdge(iEdge, iSquareEdge, jEdge, squareReconstructionData);
              }

              storeSquareFormingEdges(iEdge, jEdge, iSquareEdge, jSquareEdge, squareReconstructionData);

              Vertex iSquareEdgeEndpoint = iSquareEdge.getEndpoint();
              if (!squareReconstructionData.getIncludedVertices()[iSquareEdgeEndpoint.getVertexNo()])
              {
                squareReconstructionData.getNextVertices().add(iSquareEdgeEndpoint);
                squareReconstructionData.getIncludedVertices()[iSquareEdgeEndpoint.getVertexNo()] = true;
              }
            });

    if (!squareFound)
    {
      MissingSquareData missingSquare = new MissingSquareData(squareReconstructionData.getCurrentVertex(), iEdge, jEdge);
      squareReconstructionData.getMissingSquares().add(missingSquare);
    }

    return squareFound;
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
