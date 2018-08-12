package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.SquareColoringService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Component
public class SquareColoringServiceImpl implements SquareColoringService
{

  @Autowired
  Graph graph;

  @Autowired
  GraphHelper graphHelper;

  @Autowired
  ColoringService coloringService;

  @Override
  public void colorEdge(Edge baseEdge, Edge squareEdge, Edge otherColorBaseEdge, SquareReconstructionData squareReconstructionData)
  {
    if (baseEdge.getLabel() != null && squareEdge.getLabel() != null)
    {
      int baseEdgeColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), baseEdge.getLabel().getColor());
      int squareEdgeColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), squareEdge.getLabel().getColor());

      if (baseEdgeColor != squareEdgeColor)
      {
        coloringService.mergeColorsForEdges(Arrays.asList(baseEdge, squareEdge), MergeTagEnum.DOUBLE_SQUARE);
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
        color = findExtensionColor(baseEdge, squareEdge, otherColorBaseEdge, squareReconstructionData);

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

    storeSquareMatchingEdges(baseEdge, squareEdge, otherColorBaseEdge, squareReconstructionData);
  }

  private int findExtensionColor(Edge baseEdge, Edge squareEdge, Edge otherColorBaseEdge, SquareReconstructionData squareReconstructionData)
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

  private void storeSquareMatchingEdges(Edge baseEdge, Edge squareEdge, Edge otherColorBaseEdge, SquareReconstructionData squareReconstructionData)
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

  @Override
  public void colorEdgesWithoutSquare(List<Edge> edgesWithoutSquare)
  {
    edgesWithoutSquare.stream()
            .forEach(e ->
            {
              int newColor = addNewColorToGraphColoring();
              e.setLabel(new Label(newColor, -1));
              e.getOpposite().setLabel(new Label(newColor, -1));
            });
  }

  private int addNewColorToGraphColoring()
  {
    GraphColoring graphColoring = graph.getGraphColoring();

    int newColor = graphColoring.getColorsMapping().size();

    graphColoring.getColorsMapping().add(newColor);
    graphColoring.getActualColors().add(newColor);

    return newColor;
  }

}
