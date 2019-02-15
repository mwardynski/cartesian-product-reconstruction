package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.SingleSquaresHandlingService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.SquareMatchingEdgesMergingService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class SquareMatchingEdgesMergingServiceImpl implements SquareMatchingEdgesMergingService
{

  @Autowired
  Graph graph;

  @Autowired
  ReconstructionData reconstructionData;

  @Autowired
  ColoringService coloringService;

  @Autowired
  SingleSquaresHandlingService singleSquaresHandlingService;

  @Override
  public void mergeColorsBasedOnSquareMatching(SquareReconstructionData squareReconstructionData)
  {
    for (Vertex vertex : graph.getVertices())
    {
      for (Edge edge : vertex.getEdges())
      {
        SquareMatchingEdgeData squareMatchingEdgeData = squareReconstructionData
                .getSquareMatchingEdgesByEdge()[edge.getOrigin().getVertexNo()][edge.getEndpoint().getVertexNo()];

        if (squareMatchingEdgeData != null)
        {
          checkAllSquareMatchingPairsForPotentialMerges(edge, squareMatchingEdgeData, squareReconstructionData);
        }
      }
    }
  }

  private void checkAllSquareMatchingPairsForPotentialMerges(Edge baseEdge, SquareMatchingEdgeData squareMatchingEdgeData, SquareReconstructionData squareReconstructionData)
  {
    int squareMatchingsColorsQuantity = squareMatchingEdgeData.getExistingColors().size();
    if (squareMatchingsColorsQuantity < 2)
    {
      return;
    }

    List<Edge> edgesOfColorsToBeMerged = new LinkedList<>();

    for (int i = 0; i < squareMatchingsColorsQuantity - 1; i++)
    {
      for (int j = i + 1; j < squareMatchingsColorsQuantity; j++)
      {
        Integer firstColor = squareMatchingEdgeData.getExistingColors().get(i);
        Integer secondColor = squareMatchingEdgeData.getExistingColors().get(j);

        int baseEdgeMappedColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), baseEdge.getLabel().getColor());
        int firstMappedColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), firstColor);
        int secondMappedColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), secondColor);

        if (baseEdgeMappedColor != firstMappedColor && baseEdgeMappedColor != secondMappedColor && firstMappedColor != secondMappedColor)
        {
          List<Edge> firstGroupOfSquareMatchingEdges = squareMatchingEdgeData.getEdgesByColors()[firstColor];
          List<Edge> secondGroupOfSquareMatchingEdges = squareMatchingEdgeData.getEdgesByColors()[secondColor];

          collectEdgesToMergeForGivenColors(baseEdge, firstGroupOfSquareMatchingEdges, secondGroupOfSquareMatchingEdges, edgesOfColorsToBeMerged, squareReconstructionData);
        }

      }
    }

    if (CollectionUtils.isNotEmpty(edgesOfColorsToBeMerged))
    {
      coloringService.mergeColorsForEdges(edgesOfColorsToBeMerged, MergeTagEnum.MULTIPLE_COLORING_EXTENSIONS);
    }
  }

  private void collectEdgesToMergeForGivenColors(Edge middleEdge, List<Edge> firstGroupOfSquareMatchingEdges, List<Edge> secondGroupOfSquareMatchingEdges, List<Edge> edgesOfColorsToBeMerged, SquareReconstructionData squareReconstructionData)
  {
    for (Edge firstGroupSquareMatchingEdge : firstGroupOfSquareMatchingEdges)
    {
      for (Edge secondGroupSquareMatchingEdge : secondGroupOfSquareMatchingEdges)
      {
        Edge baseEdge = graph.getAdjacencyMatrix()[middleEdge.getOrigin().getVertexNo()][firstGroupSquareMatchingEdge.getOrigin().getVertexNo()];
        Edge baseEdgeExtendingEdge = graph.getAdjacencyMatrix()[middleEdge.getOrigin().getVertexNo()][secondGroupSquareMatchingEdge.getOrigin().getVertexNo()];

        Edge squareEdge = graph.getAdjacencyMatrix()[middleEdge.getEndpoint().getVertexNo()][firstGroupSquareMatchingEdge.getEndpoint().getVertexNo()];
        Edge squareEdgeExtendingEdge = graph.getAdjacencyMatrix()[middleEdge.getEndpoint().getVertexNo()][secondGroupSquareMatchingEdge.getEndpoint().getVertexNo()];

        if (isColorToBeExtended(baseEdge, baseEdgeExtendingEdge, squareEdge, squareEdgeExtendingEdge, squareReconstructionData))
        {
          edgesOfColorsToBeMerged.add(baseEdge);
          edgesOfColorsToBeMerged.add(baseEdgeExtendingEdge);
        }
      }
    }
  }

  @Override
  public boolean isColorToBeExtended(Edge baseEdge, Edge baseEdgeExtendingEdge, Edge squareEdge, Edge squareEdgeExtendingEdge, SquareReconstructionData squareReconstructionData)
  {
    SingleSquareList baseEdgeSquares = singleSquaresHandlingService.findSquaresForGivenEdges(baseEdgeExtendingEdge, baseEdge, squareReconstructionData);
    SingleSquareList squareEdgeSquares = singleSquaresHandlingService.findSquaresForGivenEdges(squareEdgeExtendingEdge, squareEdge, squareReconstructionData);

    boolean extendColor = false;
    if (CollectionUtils.isEmpty(baseEdgeSquares) && CollectionUtils.isEmpty(squareEdgeSquares))
    {
      extendColor = true;
    }
    else if (reconstructionData.getOperationOnGraph() == OperationOnGraph.SINGLE_EDGE_RECONSTRUCTION
            && (CollectionUtils.isEmpty(baseEdgeSquares) || CollectionUtils.isEmpty(squareEdgeSquares)))
    {
      if (CollectionUtils.isEmpty(baseEdgeSquares))
      {
        extendColor = isColorToBeExtendedForSingleEdgeReconstructionSpecialCase(baseEdge, baseEdgeExtendingEdge, squareEdge, squareEdgeExtendingEdge, squareEdgeSquares, squareReconstructionData);
      }
      else
      {
        extendColor = isColorToBeExtendedForSingleEdgeReconstructionSpecialCase(squareEdge, squareEdgeExtendingEdge, baseEdge, baseEdgeExtendingEdge, baseEdgeSquares, squareReconstructionData);
      }
    }
    return extendColor;
  }

  private boolean isColorToBeExtendedForSingleEdgeReconstructionSpecialCase(Edge notHavingSquareEdge, Edge notHavingSquareExtendingEdge,
                                                                            Edge havingSquareEdge, Edge havingSquareExtendingEdge,
                                                                            SingleSquareList foundSquares, SquareReconstructionData squareReconstructionData)
  {
    boolean extendColor = false;
    if (foundSquares.size() != 1)
    {
      return extendColor;
    }
    Edge[][] adjacencyMatrix = graph.getAdjacencyMatrix();
    SingleSquareData foundSquare = foundSquares.get(0);

    Edge otherColorEdge = adjacencyMatrix[havingSquareEdge.getEndpoint().getVertexNo()][notHavingSquareEdge.getEndpoint().getVertexNo()];
    Edge otherColorExtendingEdge = adjacencyMatrix[havingSquareExtendingEdge.getEndpoint().getVertexNo()][notHavingSquareExtendingEdge.getEndpoint().getVertexNo()];

    SingleSquareList otherColorEdgeSquares = singleSquaresHandlingService.findSquaresForGivenEdges(otherColorEdge, foundSquare.getSquareBaseEdge(), squareReconstructionData);
    SingleSquareList otherColorExtendingEdgeSquares = singleSquaresHandlingService.findSquaresForGivenEdges(otherColorExtendingEdge, foundSquare.getSquareOtherEdge(), squareReconstructionData);

    if (CollectionUtils.isEmpty(otherColorEdgeSquares) && CollectionUtils.isEmpty(otherColorExtendingEdgeSquares))
    {
      extendColor = true;
    }
    return extendColor;
  }
}
