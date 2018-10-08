package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.SingleSquaresHandlingService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.SquareFindingService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.SquareHandlingStrategy;
import at.ac.unileoben.mat.dissertation.structure.*;
import at.ac.unileoben.mat.dissertation.structure.exception.SquareWithoutAnyLabelsException;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedList;
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

  @Autowired
  SingleSquaresHandlingService singleSquaresHandlingService;

  @Autowired
  ColoringService coloringService;

  @Override
  public boolean findAndProcessSquareForTwoEdges(SquareReconstructionData squareReconstructionData, Edge iEdge, Edge jEdge)
  {
    Vertex currentVertex = squareReconstructionData.getCurrentVertex();
    SingleSquareList singleSquareList = singleSquaresHandlingService.findSquaresForGivenEdges(iEdge, jEdge, squareReconstructionData);
    boolean squareFound = CollectionUtils.isNotEmpty(singleSquareList);

    if (squareFound)
    {
      singleSquareList.stream()
              .forEach(singleSquare ->
              {
                Edge iSquareEdge = singleSquare.getSquareBaseEdge();
                Edge jSquareEdge = singleSquare.getSquareOtherEdge();

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
                else
                {
                  if (squareReconstructionData.getIncludedPostponedVertices()[currentVertex.getVertexNo()])
                  {
                    squareHandlingStrategy.colorEdgeWithNewColor(iEdge, true);
                    colorEdgesFormingSquare(jEdge, jSquareEdge, iEdge, iSquareEdge, squareReconstructionData);
                  }
                  else
                  {
                    throw new SquareWithoutAnyLabelsException(String.format("no way to color edges: %s, %s, %s, %s", iEdge, jEdge, iSquareEdge, jSquareEdge));
                  }
                }

                storeSquareFormingEdges(iEdge, jEdge, iSquareEdge, jSquareEdge, squareReconstructionData);

                squareReconstructionData.getCurrentVertexNeighborsToQueue().add(iSquareEdge.getEndpoint());

                if (CollectionUtils.isNotEmpty(singleSquare.getDiagonals()))
                {
                  singleSquare.getDiagonals().stream()
                          .forEach(diagonal ->
                          {
                            int baseEdgeColor = singleSquare.getBaseEdge().getLabel().getColor();
                            diagonal.setLabel(new Label(baseEdgeColor, -1));
                          });

                  List<Edge> edgesToMerge = new LinkedList<>(singleSquare.getDiagonals());
                  edgesToMerge.addAll(Arrays.asList(singleSquare.getBaseEdge(), singleSquare.getOtherEdge(),
                          singleSquare.getSquareBaseEdge(), singleSquare.getSquareOtherEdge()));
                  coloringService.mergeColorsForEdges(edgesToMerge, MergeTagEnum.SQUARE_WITH_DIAGONAL);
                }
              });
    }


    squareReconstructionData.getCurrentVertexNeighborsToQueue().add(iEdge.getEndpoint());
    squareReconstructionData.getCurrentVertexNeighborsToQueue().add(jEdge.getEndpoint());

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
