package at.ac.unileoben.mat.dissertation.reconstruction.services.impl.squarehandling;

import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.Label;
import at.ac.unileoben.mat.dissertation.structure.SquareReconstructionData;
import at.ac.unileoben.mat.dissertation.structure.Vertex;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("factorXK2")
public class FactorXK2SquareHandlingStrategyImpl extends AbstractSquareHandlingStrategy
{

  private static final int FIRST_COLOR = 1;
  private static final int SECOND_COLOR = 0;

  @Override
  public void colorEdge(Edge baseEdge, Edge squareEdge, Edge otherColorBaseEdge, SquareReconstructionData squareReconstructionData)
  {
    if (baseEdge.getLabel() == null && squareEdge.getLabel() == null && otherColorBaseEdge.getLabel() != null && otherColorBaseEdge.getLabel().getColor() == SECOND_COLOR)
    {
      int color = handleExtensionColor(baseEdge, squareEdge, otherColorBaseEdge, squareReconstructionData);
      if (color != -1)
      {
        baseEdge.setLabel(new Label(color, -1));
        baseEdge.getOpposite().setLabel(new Label(color, -1));

        squareEdge.setLabel(new Label(color, -1));
        squareEdge.getOpposite().setLabel(new Label(color, -1));

        storeSquareMatchingEdges(baseEdge, squareEdge, otherColorBaseEdge, squareReconstructionData);
      }
    }
    else if (squareEdge.getLabel() == null && baseEdge.getLabel() != null && baseEdge.getLabel().getColor() == SECOND_COLOR && otherColorBaseEdge.getLabel() != null && otherColorBaseEdge.getLabel().getColor() == FIRST_COLOR)
    {
      squareEdge.setLabel(new Label(SECOND_COLOR, -1));
      squareEdge.getOpposite().setLabel(new Label(SECOND_COLOR, -1));

      storeSquareMatchingEdges(baseEdge, squareEdge, otherColorBaseEdge, squareReconstructionData);
    }

  }

  @Override
  public void colorEdgesWithoutSquare(List<Edge> edgesWithoutSquare)
  {
    return;
  }

  @Override
  public void colorEdgeWithNewColor(Edge edge, boolean edgeWithSquare)
  {
  }

  @Override
  public void queueSquareSideVertexToNextVertices(Vertex squareSideVertex, SquareReconstructionData squareReconstructionData)
  {
    boolean vertexToQueue = squareSideVertex.getEdges().stream()
            .filter(e -> e.getLabel() != null && e.getLabel().getColor() == FIRST_COLOR)
            .filter(e -> squareReconstructionData.getIncludedVertices()[e.getEndpoint().getVertexNo()])
            .findAny().isPresent();

    if (vertexToQueue)
    {
      addVertexToNextVertices(squareSideVertex, squareReconstructionData);
    }
  }

  @Override
  public void queueSquareTopVertexToNextVertices(Vertex squareTopVertex, SquareReconstructionData squareReconstructionData)
  {
  }
}
