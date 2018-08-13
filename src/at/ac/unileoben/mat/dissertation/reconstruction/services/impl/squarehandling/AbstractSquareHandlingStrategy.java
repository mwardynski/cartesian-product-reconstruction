package at.ac.unileoben.mat.dissertation.reconstruction.services.impl.squarehandling;

import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.SquareHandlingStrategy;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedList;
import java.util.List;

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

    Edge[][][] squareMatchingEdgesByEdgeAndColor = squareReconstructionData.getSquareMatchingEdgesByEdgeAndColor();
    Edge[] squareMatchingEdgesByColor = squareMatchingEdgesByEdgeAndColor[otherColorBaseEdge.getOrigin().getVertexNo()][otherColorBaseEdge.getEndpoint().getVertexNo()];

    List<Edge> possibleExtensionEdges = new LinkedList<>();

    for (int i = 0; i < squareMatchingEdgesByColor.length; i++)
    {
      Edge squareMatchingEdge = squareMatchingEdgesByColor[i];
      if (squareMatchingEdge == null)
      {
        continue;
      }

      Edge[][] adjacencyMatrix = graph.getAdjacencyMatrix();

      Edge baseEdgeExtendingEdge = adjacencyMatrix[baseEdge.getOrigin().getVertexNo()][squareMatchingEdge.getOrigin().getVertexNo()];
      Edge squareEdgeExtendingEdge = adjacencyMatrix[squareEdge.getOrigin().getVertexNo()][squareMatchingEdge.getEndpoint().getVertexNo()];

      List<List<Edge>> baseEdgeSquares = graphHelper.findSquaresForTwoEdges(baseEdgeExtendingEdge, baseEdge);
      List<List<Edge>> squareEdgeSquares = graphHelper.findSquaresForTwoEdges(squareEdgeExtendingEdge, squareEdge);

      if (CollectionUtils.isEmpty(baseEdgeSquares) && CollectionUtils.isEmpty(squareEdgeSquares))
      {
        possibleExtensionEdges.add(baseEdgeExtendingEdge);
      }

      if (possibleExtensionEdges.size() == 1)
      {
        extensionColor = possibleExtensionEdges.get(0).getLabel().getColor();
      }
      else if (possibleExtensionEdges.size() > 1)
      {
        //TODO analyse whether this merge is really ok!!
        extensionColor = possibleExtensionEdges.stream().mapToInt(edge -> edge.getLabel().getColor()).min().getAsInt();
        coloringService.mergeColorsForEdges(possibleExtensionEdges, MergeTagEnum.MULTIPLE_COLORING_EXTENIONS);
      }
    }
    return extensionColor;
  }

  protected void storeSquareMatchingEdges(Edge baseEdge, Edge squareEdge, Edge otherColorBaseEdge, SquareReconstructionData squareReconstructionData)
  {
    Edge squareMatchingEdgeToBaseEdge = squareReconstructionData.getSquareMatchingEdgesByEdgeAndColor()[baseEdge.getOrigin().getVertexNo()][baseEdge.getEndpoint().getVertexNo()][otherColorBaseEdge.getLabel().getColor()];
    if (squareMatchingEdgeToBaseEdge == null)
    {
      squareReconstructionData.getSquareMatchingEdgesByEdgeAndColor()[baseEdge.getOrigin().getVertexNo()][baseEdge.getEndpoint().getVertexNo()][otherColorBaseEdge.getLabel().getColor()] = squareEdge;
    }
    Edge squareMatchingEdgeToBaseEdgeOpposite = squareReconstructionData.getSquareMatchingEdgesByEdgeAndColor()[baseEdge.getEndpoint().getVertexNo()][baseEdge.getOrigin().getVertexNo()][otherColorBaseEdge.getLabel().getColor()];
    if (squareMatchingEdgeToBaseEdgeOpposite == null)
    {
      squareReconstructionData.getSquareMatchingEdgesByEdgeAndColor()[baseEdge.getEndpoint().getVertexNo()][baseEdge.getOrigin().getVertexNo()][otherColorBaseEdge.getLabel().getColor()] = squareEdge.getOpposite();
    }
    Edge squareMatchingEdgeToSquareEdge = squareReconstructionData.getSquareMatchingEdgesByEdgeAndColor()[squareEdge.getOrigin().getVertexNo()][squareEdge.getEndpoint().getVertexNo()][otherColorBaseEdge.getLabel().getColor()];
    if (squareMatchingEdgeToSquareEdge == null)
    {
      squareReconstructionData.getSquareMatchingEdgesByEdgeAndColor()[squareEdge.getOrigin().getVertexNo()][squareEdge.getEndpoint().getVertexNo()][otherColorBaseEdge.getLabel().getColor()] = baseEdge;
    }
    Edge squareMatchingEdgeToSquareEdgeOpposite = squareReconstructionData.getSquareMatchingEdgesByEdgeAndColor()[squareEdge.getEndpoint().getVertexNo()][squareEdge.getOrigin().getVertexNo()][otherColorBaseEdge.getLabel().getColor()];
    if (squareMatchingEdgeToSquareEdgeOpposite == null)
    {
      squareReconstructionData.getSquareMatchingEdgesByEdgeAndColor()[squareEdge.getEndpoint().getVertexNo()][squareEdge.getOrigin().getVertexNo()][otherColorBaseEdge.getLabel().getColor()] = baseEdge.getOpposite();
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
