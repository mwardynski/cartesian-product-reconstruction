package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
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
  ColoringService coloringService;

  @Autowired
  GraphHelper graphHelper;

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
          checkAllSquareMatchingPairsForPotentialMerges(edge, squareMatchingEdgeData);
        }
      }
    }
  }

  private void checkAllSquareMatchingPairsForPotentialMerges(Edge baseEdge, SquareMatchingEdgeData squareMatchingEdgeData)
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

        int firstColorMapped = coloringService.getCurrentColorMapping(graph.getGraphColoring(), firstColor);
        int secondColorMapped = coloringService.getCurrentColorMapping(graph.getGraphColoring(), secondColor);

        if (firstColorMapped != secondColorMapped)
        {
          List<Edge> firstGroupOfSquareMatchingEdges = squareMatchingEdgeData.getEdgesByColors()[firstColor];
          List<Edge> secondGroupOfSquareMatchingEdges = squareMatchingEdgeData.getEdgesByColors()[secondColor];

          collectEdgesToMergeForGivenColors(baseEdge, firstGroupOfSquareMatchingEdges, secondGroupOfSquareMatchingEdges, edgesOfColorsToBeMerged);
        }

      }
    }

    if (CollectionUtils.isNotEmpty(edgesOfColorsToBeMerged))
    {
      coloringService.mergeColorsForEdges(edgesOfColorsToBeMerged, MergeTagEnum.MULTIPLE_COLORING_EXTENSIONS);
    }
  }

  private void collectEdgesToMergeForGivenColors(Edge baseEdge, List<Edge> firstGroupOfSquareMatchingEdges, List<Edge> secondGroupOfSquareMatchingEdges, List<Edge> edgesOfColorsToBeMerged)
  {
    for (Edge firstGroupSquareMatchingEdge : firstGroupOfSquareMatchingEdges)
    {
      for (Edge secondGroupSquareMatchingEdge : secondGroupOfSquareMatchingEdges)
      {
        Edge squareBaseEdge = graph.getAdjacencyMatrix()[baseEdge.getOrigin().getVertexNo()][firstGroupSquareMatchingEdge.getOrigin().getVertexNo()];
        Edge squareOtherEdge = graph.getAdjacencyMatrix()[baseEdge.getOrigin().getVertexNo()][secondGroupSquareMatchingEdge.getOrigin().getVertexNo()];

        List<List<Edge>> originSideSquares = graphHelper.findSquaresForTwoEdges(squareBaseEdge, squareOtherEdge);


        squareBaseEdge = graph.getAdjacencyMatrix()[baseEdge.getEndpoint().getVertexNo()][firstGroupSquareMatchingEdge.getEndpoint().getVertexNo()];
        squareOtherEdge = graph.getAdjacencyMatrix()[baseEdge.getEndpoint().getVertexNo()][secondGroupSquareMatchingEdge.getEndpoint().getVertexNo()];

        List<List<Edge>> endpointSideSquares = graphHelper.findSquaresForTwoEdges(squareBaseEdge, squareOtherEdge);

        if (CollectionUtils.isEmpty(originSideSquares) && CollectionUtils.isEmpty(endpointSideSquares))
        {
          edgesOfColorsToBeMerged.add(squareBaseEdge);
          edgesOfColorsToBeMerged.add(squareOtherEdge);
        }
      }
    }
  }
}
