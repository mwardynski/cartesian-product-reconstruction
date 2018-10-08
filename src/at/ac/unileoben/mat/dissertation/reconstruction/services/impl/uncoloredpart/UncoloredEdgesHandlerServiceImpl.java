package at.ac.unileoben.mat.dissertation.reconstruction.services.impl.uncoloredpart;

import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.uncoloredpart.PartOfCycleNoSquareAtAllMissingSquaresFindingService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.uncoloredpart.SingleNoSquareAtAllMissingSquaresFindingService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.uncoloredpart.UncoloredEdgesHandlerService;
import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.Graph;
import at.ac.unileoben.mat.dissertation.structure.MissingSquaresUniqueEdgesData;
import at.ac.unileoben.mat.dissertation.structure.SquareReconstructionData;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class UncoloredEdgesHandlerServiceImpl implements UncoloredEdgesHandlerService
{
  @Autowired
  Graph graph;

  @Autowired
  ColoringService coloringService;

  @Autowired
  PartOfCycleNoSquareAtAllMissingSquaresFindingService partOfCycleNoSquareAtAllMissingSquaresGeneralService;

  @Autowired
  SingleNoSquareAtAllMissingSquaresFindingService singleNoSquareAtAllMissingSquaresFindingService;


  @Override
  public List<MissingSquaresUniqueEdgesData> filterCorrectNoSquareAtAllMissingSquares(List<MissingSquaresUniqueEdgesData> noSquareAtAllMissingSquares, SquareReconstructionData squareReconstructionData)
  {
    if (CollectionUtils.isEmpty(noSquareAtAllMissingSquares))
    {
      return Collections.emptyList();
    }
    else
    {
      boolean cycleToBeSearchedFor = isCycleToBeSearchedFor(noSquareAtAllMissingSquares);

      if (cycleToBeSearchedFor)
      {
        return partOfCycleNoSquareAtAllMissingSquaresGeneralService.findCorrectPartOfCycleNoSquareAtAllMissingSquares(noSquareAtAllMissingSquares, squareReconstructionData);
      }
      else
      {
        return singleNoSquareAtAllMissingSquaresFindingService.findCorrectSingleNoSquareAtAllMissingSquares(noSquareAtAllMissingSquares, squareReconstructionData);
      }
    }
  }

  private boolean isCycleToBeSearchedFor(List<MissingSquaresUniqueEdgesData> noSquareAtAllMissingSquares)
  {
    boolean cycleToBeSearchedFor = false;

    MissingSquaresUniqueEdgesData arbitraryMissingSquare = noSquareAtAllMissingSquares.get(0);
    int baseEdgeEndpointEdgesQuantity = arbitraryMissingSquare.getBaseEdge().getEndpoint().getEdges().size();
    int otherEdgeEndpointEdgesQuantity = arbitraryMissingSquare.getOtherEdge().getEndpoint().getEdges().size();

    if (baseEdgeEndpointEdgesQuantity > 1 && otherEdgeEndpointEdgesQuantity > 1)
    {
      cycleToBeSearchedFor = true;
    }
    return cycleToBeSearchedFor;
  }

  @Override
  public boolean areNormalEdgesOfGivenColorProperty(Edge baseEdge, Edge otherEdge, boolean havingSameColorWanted)
  {
    int baseEdgeColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), baseEdge.getLabel().getColor());
    int otherEdgeColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), otherEdge.getLabel().getColor());

    if (baseEdgeColor == 0 || otherEdgeColor == 0)
    {
      return false;
    }

    boolean edgesOfSameColor = baseEdgeColor == otherEdgeColor;
    return edgesOfSameColor == havingSameColorWanted;
  }
}
