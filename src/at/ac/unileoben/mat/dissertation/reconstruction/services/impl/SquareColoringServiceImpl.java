package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.SquareColoringService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
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
      return;
    }
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

      squareReconstructionData.getSquareMatchingEdgesByEdgeAndColor()[baseEdge.getOrigin().getVertexNo()][baseEdge.getEndpoint().getVertexNo()][otherColorBaseEdge.getLabel().getColor()] = squareEdge;
    }
    if (squareEdge.getLabel() == null)
    {
      squareEdge.setLabel(new Label(color, -1));
      squareEdge.getOpposite().setLabel(new Label(color, -1));

      squareReconstructionData.getSquareMatchingEdgesByEdgeAndColor()[squareEdge.getOrigin().getVertexNo()][squareEdge.getEndpoint().getVertexNo()][otherColorBaseEdge.getLabel().getColor()] = baseEdge;
    }
  }

  private int findExtensionColor(Edge baseEdge, Edge squareEdge, Edge otherColorBaseEdge, SquareReconstructionData squareReconstructionData)
  {
    int extensionColor = -1;

    Edge[][][] squareMatchingEdgesByEdgeAndColor = squareReconstructionData.getSquareMatchingEdgesByEdgeAndColor();
    Edge[] squareMatchingEdgesByColor = squareMatchingEdgesByEdgeAndColor[otherColorBaseEdge.getOrigin().getVertexNo()][otherColorBaseEdge.getEndpoint().getVertexNo()];

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
        extensionColor = i;
        break;
      }
    }
    return extensionColor;
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
