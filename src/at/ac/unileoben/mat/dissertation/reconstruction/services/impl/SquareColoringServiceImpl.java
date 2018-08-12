package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.reconstruction.services.SquareColoringService;
import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.Label;
import at.ac.unileoben.mat.dissertation.structure.SquareReconstructionData;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SquareColoringServiceImpl implements SquareColoringService
{

  @Autowired
  GraphHelper graphHelper;

  @Override
  public void colorEdge(Edge baseEdge, Edge squareEdge, Edge otherColorBaseEdge, SquareReconstructionData squareReconstructionData)
  {
    if (baseEdge.getLabel() != null && squareEdge.getLabel() != null)
    {
      return;
    }
    int color = -1;
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
        color = squareReconstructionData.getColorCounter();
        squareReconstructionData.setColorCounter(color + 1);
      }
    }


    if (baseEdge.getLabel() == null)
    {
      baseEdge.setLabel(new Label(color, -1));
      baseEdge.getOpposite().setLabel(new Label(color, -1));

      squareReconstructionData.getMatchingSquareEdgesByEdgeAndColor()[baseEdge.getOrigin().getVertexNo()][baseEdge.getEndpoint().getVertexNo()][otherColorBaseEdge.getLabel().getColor()] = squareEdge;
    }
    if (squareEdge.getLabel() == null)
    {
      squareEdge.setLabel(new Label(color, -1));
      squareEdge.getOpposite().setLabel(new Label(color, -1));

      squareReconstructionData.getMatchingSquareEdgesByEdgeAndColor()[squareEdge.getOrigin().getVertexNo()][squareEdge.getEndpoint().getVertexNo()][otherColorBaseEdge.getLabel().getColor()] = baseEdge;
    }
  }

  private int findExtensionColor(Edge baseEdge, Edge squareEdge, Edge otherColorBaseEdge, SquareReconstructionData squareReconstructionData)
  {
    int extensionColor = -1;

    Edge[][][] matchingSquareEdgesByEdgeAndColor = squareReconstructionData.getMatchingSquareEdgesByEdgeAndColor();
    Edge[] matchingSquareEdgesByColor = matchingSquareEdgesByEdgeAndColor[otherColorBaseEdge.getOrigin().getVertexNo()][otherColorBaseEdge.getEndpoint().getVertexNo()];

    for (int i = 0; i < matchingSquareEdgesByColor.length; i++)
    {
      Edge matchingSquareEdge = matchingSquareEdgesByColor[i];
      if (matchingSquareEdge == null)
      {
        continue;
      }

      List<List<Edge>> squares1 = graphHelper.findSquaresForTwoEdges(matchingSquareEdge.getOpposite(), baseEdge);
      List<List<Edge>> squares2 = graphHelper.findSquaresForTwoEdges(matchingSquareEdge, squareEdge);

      if (CollectionUtils.isEmpty(squares1) && CollectionUtils.isEmpty(squares2))
      {
        extensionColor = i;
        break;
      }
    }
    return extensionColor;
  }

}
