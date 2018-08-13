package at.ac.unileoben.mat.dissertation.reconstruction.services.impl.squarehandling;

import at.ac.unileoben.mat.dissertation.structure.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Profile("whole")
public class WholeGraphSquareHandlingStrategyImpl extends AbstractSquareHandlingStrategy
{

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
