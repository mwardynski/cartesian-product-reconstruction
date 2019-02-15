package at.ac.unileoben.mat.dissertation.reconstruction.services.impl.squarehandling;

import at.ac.unileoben.mat.dissertation.reconstruction.services.SquareMatchingEdgesMergingService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.uncoloredpart.UncoloredEdgesHandlerService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class GraphSquaresHandlingStrategyImpl extends AbstractSquareHandlingStrategy
{

  @Autowired
  UncoloredEdgesHandlerService uncoloredEdgesHandlerService;

  @Autowired
  SquareMatchingEdgesMergingService squareMatchingEdgesMergingService;

  @Override
  public void colorEdge(Edge baseEdge, Edge squareEdge, Edge otherColorEdge, SquareReconstructionData squareReconstructionData)
  {
    if (baseEdge.getLabel() != null && squareEdge.getLabel() != null)
    {
      if (uncoloredEdgesHandlerService.areNormalEdgesOfGivenColorProperty(baseEdge, squareEdge, false))
      {
        coloringService.mergeColorsForEdges(Arrays.asList(baseEdge, squareEdge), MergeTagEnum.DOUBLE_SQUARE_UNIFY_COLORING);
      }
    }
    else
    {

      int color;
      if (baseEdge.getLabel() != null)
      {
        color = baseEdge.getLabel().getColor();
      }
      else if (squareEdge.getLabel() != null)
      {
        color = squareEdge.getLabel().getColor();
      }
      else
      {
        color = findExtensionColor(baseEdge, squareEdge, otherColorEdge, squareReconstructionData);

        if (color == -1)
        {
          color = addNewColorToGraphColoring();
        }
      }

      if (baseEdge.getLabel() == null)
      {
        baseEdge.setLabel(new Label(color, -1));
        baseEdge.getOpposite().setLabel(new Label(color, -1));
      }
      if (squareEdge.getLabel() == null)
      {
        squareEdge.setLabel(new Label(color, -1));
        squareEdge.getOpposite().setLabel(new Label(color, -1));
      }
    }

    storeSquareMatchingEdges(baseEdge, squareEdge, otherColorEdge, squareReconstructionData);
  }

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

    int resultExtensionColor = -1;
    if (squareMatchingEdgesMergingService.isColorToBeExtended(baseEdge, baseEdgeExtendingEdge, squareEdge, squareEdgeExtendingEdge, squareReconstructionData))
    {
      resultExtensionColor = baseEdgeExtendingEdge.getLabel().getColor();
    }
    return resultExtensionColor;
  }

  @Override
  public void colorEdgesWithoutSquare(List<Edge> edgesWithoutSquare)
  {
    edgesWithoutSquare.stream()
            .forEach(edge -> colorEdgeWithNewColor(edge, false));
  }

  @Override
  public void colorEdgeWithNewColor(Edge edge, boolean edgeWithSquare)
  {
    int color;
    if (edgeWithSquare)
    {
      color = addNewColorToGraphColoring();
    }
    else
    {
      color = 0;
    }
    int label = edgeWithSquare ? -1 : -2;
    edge.setLabel(new Label(color, label));
    edge.getOpposite().setLabel(new Label(color, label));
  }

  private int addNewColorToGraphColoring()
  {
    GraphColoring graphColoring = graph.getGraphColoring();

    if (graphColoring.getColorsMapping().size() == 0)
    {
      addNewColorToGraphColoring();
    }

    int newColor = graphColoring.getColorsMapping().size();

    graphColoring.getColorsMapping().add(newColor);
    graphColoring.getActualColors().add(newColor);


    return newColor;
  }

  @Override
  public void queueSquareSideVertexToNextVertices(Vertex squareSideVertex, SquareReconstructionData squareReconstructionData)
  {
    addVertexToNextVertices(squareSideVertex, squareReconstructionData);
  }

  @Override
  public void queueSquareTopVertexToNextVertices(Vertex squareTopVertex, SquareReconstructionData squareReconstructionData)
  {
    addVertexToNextVertices(squareTopVertex, squareReconstructionData);
  }

}
