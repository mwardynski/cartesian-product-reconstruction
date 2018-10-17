package at.ac.unileoben.mat.dissertation.reconstruction.services.impl.squarehandling;

import at.ac.unileoben.mat.dissertation.reconstruction.services.uncoloredpart.UncoloredEdgesHandlerService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Profile("whole")
public class WholeGraphSquareHandlingStrategyImpl extends AbstractSquareHandlingStrategy
{

  @Autowired
  UncoloredEdgesHandlerService uncoloredEdgesHandlerService;

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
        color = handleExtensionColor(baseEdge, squareEdge, otherColorEdge, squareReconstructionData);

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

    handleExtensionColor(baseEdge, squareEdge, otherColorEdge, squareReconstructionData);
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
