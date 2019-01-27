package at.ac.unileoben.mat.dissertation.reconstruction.services.impl.uncoloredpart;

import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.uncoloredpart.PartOfCycleNoSquareAtAllMissingSquaresFindingService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.uncoloredpart.SingleNoSquareAtAllMissingSquaresFindingService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.uncoloredpart.UncoloredEdgesHandlerService;
import at.ac.unileoben.mat.dissertation.structure.*;
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
  ReconstructionData reconstructionData;

  @Autowired
  ColoringService coloringService;

  @Autowired
  PartOfCycleNoSquareAtAllMissingSquaresFindingService partOfCycleNoSquareAtAllMissingSquaresGeneralService;

  @Autowired
  SingleNoSquareAtAllMissingSquaresFindingService singleNoSquareAtAllMissingSquaresFindingService;


  @Override
  public List<MissingSquaresUniqueEdgesData> filterCorrectNoSquareAtAllMissingSquares(List<MissingSquaresUniqueEdgesData> noSquareAtAllMissingSquares,
                                                                                      SquareReconstructionData squareReconstructionData, MissingEdgesFormation missingEdgesFormation)
  {
    if (missingEdgesFormation == MissingEdgesFormation.CYCLE)
    {
      return partOfCycleNoSquareAtAllMissingSquaresGeneralService.findCorrectPartOfCycleNoSquareAtAllMissingSquares(noSquareAtAllMissingSquares, squareReconstructionData);
    }
    else if (reconstructionData.getOperationOnGraph() == OperationOnGraph.FINDING_SQUARES)
    {
      return singleNoSquareAtAllMissingSquaresFindingService.findCorrectSingleNoSquareAtAllMissingSquares(noSquareAtAllMissingSquares, squareReconstructionData);
    }
    else
    {
      return Collections.emptyList();
    }
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
