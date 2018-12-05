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

                if (CollectionUtils.isNotEmpty(singleSquare.getDiagonals()))
                {
                  singleSquare.getDiagonals().stream()
                          .forEach(diagonal ->
                          {
                            int baseEdgeColor = singleSquare.getBaseEdge().getLabel().getColor();
                            diagonal.setLabel(new Label(baseEdgeColor, -1));
                            diagonal.getOpposite().setLabel(new Label(baseEdgeColor, -1));
                          });

                  List<Edge> edgesToMerge = new LinkedList<>(singleSquare.getDiagonals());
                  edgesToMerge.addAll(Arrays.asList(singleSquare.getBaseEdge(), singleSquare.getOtherEdge(),
                          singleSquare.getSquareBaseEdge(), singleSquare.getSquareOtherEdge()));
                  coloringService.mergeColorsForEdges(edgesToMerge, MergeTagEnum.SQUARE_WITH_DIAGONAL);
                }
              });
    }


    if (CollectionUtils.isNotEmpty(singleSquareList) && singleSquareList.size() > 1)
    {
      List<Edge> edgesToMerge = Arrays.asList(iEdge, jEdge);
      coloringService.mergeColorsForEdges(edgesToMerge, MergeTagEnum.SQUARE_WITH_DIAGONAL);
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
}
