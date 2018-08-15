package at.ac.unileoben.mat.dissertation.reconstruction.services.impl.squarehandling;

import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.SquareHandlingStrategy;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractSquareHandlingStrategy implements SquareHandlingStrategy
{

  @Autowired
  protected Graph graph;

  @Autowired
  protected GraphHelper graphHelper;

  @Autowired
  protected ColoringService coloringService;

  protected int findExtensionColor(Edge baseEdge, Edge squareEdge, Edge otherColorBaseEdge, SquareReconstructionData squareReconstructionData)
  {
    int extensionColor = -1;

    SquareMatchingEdgeData[][] squareMatchingEdgesByEdge = squareReconstructionData.getSquareMatchingEdgesByEdge();
    SquareMatchingEdgeData squareMatchingEdgesData = squareMatchingEdgesByEdge[otherColorBaseEdge.getOrigin().getVertexNo()][otherColorBaseEdge.getEndpoint().getVertexNo()];

    List<Edge> possibleExtensionEdges = new LinkedList<>();

    if (squareMatchingEdgesData == null)
    {
      return extensionColor;
    }

    for (int i = 0; i < squareMatchingEdgesData.getEdgesByColors().length; i++)
    {
      List<Edge> squareMatchingEdges = squareMatchingEdgesData.getEdgesByColors()[i];
      if (CollectionUtils.isEmpty(squareMatchingEdges))
      {
        continue;
      }

      for (Edge squareMatchingEdge : squareMatchingEdges)
      {
        Edge[][] adjacencyMatrix = graph.getAdjacencyMatrix();

        Edge baseEdgeExtendingEdge = adjacencyMatrix[baseEdge.getOrigin().getVertexNo()][squareMatchingEdge.getOrigin().getVertexNo()];
        Edge squareEdgeExtendingEdge = adjacencyMatrix[squareEdge.getOrigin().getVertexNo()][squareMatchingEdge.getEndpoint().getVertexNo()];

        List<List<Edge>> baseEdgeSquares = graphHelper.findSquaresForTwoEdges(baseEdgeExtendingEdge, baseEdge);
        List<List<Edge>> squareEdgeSquares = graphHelper.findSquaresForTwoEdges(squareEdgeExtendingEdge, squareEdge);

        if (CollectionUtils.isEmpty(baseEdgeSquares) && CollectionUtils.isEmpty(squareEdgeSquares))
        {
          possibleExtensionEdges.add(baseEdgeExtendingEdge);
        }
      }
    }

    List<Integer> possibleExtensionEdgesColors = possibleExtensionEdges.stream().map(edge -> edge.getLabel().getColor()).collect(Collectors.toList());
    if (possibleExtensionEdgesColors.size() == 1)
    {
      extensionColor = possibleExtensionEdgesColors.get(0);
    }
    else if (possibleExtensionEdgesColors.size() > 1)
    {
      extensionColor = possibleExtensionEdgesColors.stream().mapToInt(color -> color).min().getAsInt();
      coloringService.mergeColorsForEdges(possibleExtensionEdges, MergeTagEnum.MULTIPLE_COLORING_EXTENIONS);
    }
    return extensionColor;
  }

  protected void storeSquareMatchingEdges(Edge baseEdge, Edge squareEdge, Edge otherColorBaseEdge, SquareReconstructionData squareReconstructionData)
  {
    int graphSize = graph.getVertices().size();
    storeSingleSquareMatchingEdge(baseEdge, squareEdge, otherColorBaseEdge.getLabel().getColor(), graphSize, squareReconstructionData.getSquareMatchingEdgesByEdge());
    storeSingleSquareMatchingEdge(baseEdge.getOpposite(), squareEdge.getOpposite(), otherColorBaseEdge.getLabel().getColor(), graphSize, squareReconstructionData.getSquareMatchingEdgesByEdge());
    storeSingleSquareMatchingEdge(squareEdge, baseEdge, otherColorBaseEdge.getLabel().getColor(), graphSize, squareReconstructionData.getSquareMatchingEdgesByEdge());
    storeSingleSquareMatchingEdge(squareEdge.getOpposite(), baseEdge.getOpposite(), otherColorBaseEdge.getLabel().getColor(), graphSize, squareReconstructionData.getSquareMatchingEdgesByEdge());
  }

  @Override
  public void storeSingleSquareMatchingEdge(Edge baseEdge, Edge squareEdge, int otherColor, int graphSize, SquareMatchingEdgeData[][] squareMatchingEdgesByEdge)
  {
    SquareMatchingEdgeData squareMatchingEdgesToBaseEdge = squareMatchingEdgesByEdge[baseEdge.getOrigin().getVertexNo()][baseEdge.getEndpoint().getVertexNo()];

    if (squareMatchingEdgesToBaseEdge == null)
    {
      squareMatchingEdgesToBaseEdge = new SquareMatchingEdgeData(graphSize);
    }
    Edge[] includedEdges = squareMatchingEdgesToBaseEdge.getIncludedEdges();
    Edge includedEdge = includedEdges[squareEdge.getOrigin().getVertexNo()];
    if (includedEdge == null)
    {
      includedEdges[squareEdge.getOrigin().getVertexNo()] = squareEdge;
      List<Edge> edgesByColor = squareMatchingEdgesToBaseEdge.getEdgesByColors()[otherColor];
      if (CollectionUtils.isEmpty(edgesByColor))
      {
        edgesByColor = new LinkedList<>();
        squareMatchingEdgesToBaseEdge.getEdgesByColors()[otherColor] = edgesByColor;
      }
      edgesByColor.add(squareEdge);
    }
    else if (includedEdge != squareEdge)
    {
      throw new RuntimeException("colors to merge");
    }
  }

  protected void addVertexToNextVertices(Vertex vertex, SquareReconstructionData squareReconstructionData)
  {
    if (!squareReconstructionData.getIncludedVertices()[vertex.getVertexNo()])
    {
      squareReconstructionData.getNextVertices().add(vertex);
      squareReconstructionData.getIncludedVertices()[vertex.getVertexNo()] = true;
    }
  }
}
