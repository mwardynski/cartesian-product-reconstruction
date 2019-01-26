package at.ac.unileoben.mat.dissertation.reconstruction.services.impl.squarehandling;

import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.SingleSquaresHandlingService;
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
  ReconstructionData reconstructionData;

  @Autowired
  protected ColoringService coloringService;

  @Autowired
  SingleSquaresHandlingService singleSquaresHandlingService;

  protected int findExtensionColor(Edge baseEdge, Edge squareEdge, Edge otherColorBaseEdge, SquareReconstructionData squareReconstructionData)
  {
    int extensionColor = -1;

    SquareMatchingEdgeData[][] squareMatchingEdgesByEdge = squareReconstructionData.getSquareMatchingEdgesByEdge();
    SquareMatchingEdgeData squareMatchingEdgesData = squareMatchingEdgesByEdge[otherColorBaseEdge.getOrigin().getVertexNo()][otherColorBaseEdge.getEndpoint().getVertexNo()];

    if (squareMatchingEdgesData == null)
    {
      return extensionColor;
    }
    Edge[][] adjacencyMatrix = graph.getAdjacencyMatrix();
    Edge otherColorSquareEdge = adjacencyMatrix[baseEdge.getEndpoint().getVertexNo()][squareEdge.getEndpoint().getVertexNo()];

    for (Integer existingColor : squareMatchingEdgesData.getExistingColors())
    {
      List<Edge> squareMatchingEdges = squareMatchingEdgesData.getEdgesByColors()[existingColor];

      for (Edge squareMatchingEdge : squareMatchingEdges)
      {
        if (squareMatchingEdge == otherColorSquareEdge)
        {
          continue;
        }
        Edge baseEdgeExtendingEdge = adjacencyMatrix[otherColorBaseEdge.getOrigin().getVertexNo()][squareMatchingEdge.getOrigin().getVertexNo()];
        Edge squareEdgeExtendingEdge = adjacencyMatrix[otherColorBaseEdge.getEndpoint().getVertexNo()][squareMatchingEdge.getEndpoint().getVertexNo()];

        extensionColor = findExtensionColor(baseEdge, baseEdgeExtendingEdge, squareEdge, squareEdgeExtendingEdge, squareReconstructionData);

        if (extensionColor != -1)
        {
          break;
        }
      }

      if (extensionColor != -1)
      {
        break;
      }
    }
    return extensionColor;
  }

  private int findExtensionColor(Edge baseEdge, Edge baseEdgeExtendingEdge, Edge squareEdge, Edge squareEdgeExtendingEdge, SquareReconstructionData squareReconstructionData)
  {
    SingleSquareList baseEdgeSquares = singleSquaresHandlingService.findSquaresForGivenEdges(baseEdgeExtendingEdge, baseEdge, squareReconstructionData);
    SingleSquareList squareEdgeSquares = singleSquaresHandlingService.findSquaresForGivenEdges(squareEdgeExtendingEdge, squareEdge, squareReconstructionData);

    int resultExtensionColor = -1;
    if (CollectionUtils.isEmpty(baseEdgeSquares) && CollectionUtils.isEmpty(squareEdgeSquares))
    {
      resultExtensionColor = baseEdgeExtendingEdge.getLabel().getColor();
    }
    else if (reconstructionData.getOperationOnGraph() == OperationOnGraph.SINGLE_EDGE_RECONSTRUCTION
            && (CollectionUtils.isEmpty(baseEdgeSquares) || CollectionUtils.isEmpty(squareEdgeSquares)))
    {
      if (CollectionUtils.isEmpty(baseEdgeSquares))
      {
        findExtensionColorForSingleEdgeReconstructionSpecialCase(baseEdge, baseEdgeExtendingEdge, squareEdge, squareEdgeExtendingEdge, squareEdgeSquares, squareReconstructionData);
      }
      else
      {
        findExtensionColorForSingleEdgeReconstructionSpecialCase(squareEdge, squareEdgeExtendingEdge, baseEdge, baseEdgeExtendingEdge, baseEdgeSquares, squareReconstructionData);
      }
    }
    return resultExtensionColor;
  }

  private int findExtensionColorForSingleEdgeReconstructionSpecialCase(Edge notHavingSquareEdge, Edge notHavingSquareExtendingEdge,
                                                                       Edge havingSquareEdge, Edge havingSquareExtendingEdge,
                                                                       SingleSquareList foundSquares, SquareReconstructionData squareReconstructionData)
  {
    int resultExtensionColor = -1;
    if (foundSquares.size() != 1)
    {
      //TODO consider returning here the extending color - which will by anyway assigned, but at the different point together with
      return resultExtensionColor;
    }
    Edge[][] adjacencyMatrix = graph.getAdjacencyMatrix();
    SingleSquareData foundSquare = foundSquares.get(0);

    Edge otherColorEdge = adjacencyMatrix[havingSquareEdge.getEndpoint().getVertexNo()][notHavingSquareEdge.getEndpoint().getVertexNo()];
    Edge otherColorExtendingEdge = adjacencyMatrix[havingSquareExtendingEdge.getEndpoint().getVertexNo()][notHavingSquareExtendingEdge.getEndpoint().getVertexNo()];

    SingleSquareList otherColorEdgeSquares = singleSquaresHandlingService.findSquaresForGivenEdges(otherColorEdge, foundSquare.getSquareBaseEdge(), squareReconstructionData);
    SingleSquareList otherColorExtendingEdgeSquares = singleSquaresHandlingService.findSquaresForGivenEdges(otherColorExtendingEdge, foundSquare.getSquareOtherEdge(), squareReconstructionData);

    if (CollectionUtils.isEmpty(otherColorEdgeSquares) && CollectionUtils.isEmpty(otherColorExtendingEdgeSquares))
    {
      resultExtensionColor = notHavingSquareExtendingEdge.getLabel().getColor();
    }

    return resultExtensionColor;
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
      squareMatchingEdgesByEdge[baseEdge.getOrigin().getVertexNo()][baseEdge.getEndpoint().getVertexNo()] = squareMatchingEdgesToBaseEdge;
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
        squareMatchingEdgesToBaseEdge.getExistingColors().add(otherColor);
      }
      edgesByColor.add(squareEdge);
    }
  }

  @Override
  public void storeMissingSquareEntry(Edge baseEdge, Edge otherEdge, MissingSquaresData missingSquaresData)
  {
    MissingSquaresEntryData missingSquaresEntryData = missingSquaresData.getMissingSquaresEntriesByBaseEdge()[baseEdge.getOrigin().getVertexNo()][baseEdge.getEndpoint().getVertexNo()];

    if (missingSquaresEntryData == null)
    {
      int graphSize = missingSquaresData.getMissingSquaresEntriesByBaseEdge().length;
      missingSquaresEntryData = new MissingSquaresEntryData(baseEdge, graphSize);

      missingSquaresData.getMissingSquaresEntriesByBaseEdge()[baseEdge.getOrigin().getVertexNo()][baseEdge.getEndpoint().getVertexNo()] = missingSquaresEntryData;
      missingSquaresData.getMissingSquaresEntries().add(missingSquaresEntryData);
    }

    if (missingSquaresEntryData.getIncludedOtherEdges()[otherEdge.getEndpoint().getVertexNo()] == null)
    {
      missingSquaresEntryData.getIncludedOtherEdges()[otherEdge.getEndpoint().getVertexNo()] = otherEdge;
      int otherEdgeColor = otherEdge.getLabel().getColor();
      List<Edge> edgesForColor = missingSquaresEntryData.getOtherEdgesByColors()[otherEdgeColor];
      if (CollectionUtils.isEmpty(edgesForColor))
      {
        edgesForColor = new LinkedList<>();
        missingSquaresEntryData.getOtherEdgesByColors()[otherEdgeColor] = edgesForColor;
        missingSquaresEntryData.getExistingColors().add(otherEdgeColor);
      }
      edgesForColor.add(otherEdge);
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
