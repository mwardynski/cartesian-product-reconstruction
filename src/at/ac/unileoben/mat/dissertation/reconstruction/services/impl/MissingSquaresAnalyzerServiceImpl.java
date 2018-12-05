package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.reconstruction.services.ReconstructionResultVerifier;
import at.ac.unileoben.mat.dissertation.reconstruction.services.uncoloredpart.UncoloredEdgesHandlerService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

@Component
@Profile("missingVertex")
public class MissingSquaresAnalyzerServiceImpl extends AbstractMissingSquareAnalyzerService
{

  @Autowired
  UncoloredEdgesHandlerService uncoloredEdgesHandlerService;

  @Autowired
  ReconstructionResultVerifier reconstructionResultVerifier;


  @Override
  public void analyseMissingSquares(SquareReconstructionData squareReconstructionData, SquareMatchingEdgeData[][] squareMatchingEdges)
  {
    ResultMissingSquaresData resultMissingSquaresData = orderProbablyCorrectMissingSquaresByColor(squareReconstructionData, squareMatchingEdges);
    reconstructionResultVerifier.compareFoundMissingVertexWithCorrectResult(resultMissingSquaresData);
  }


  private ResultMissingSquaresData orderProbablyCorrectMissingSquaresByColor(SquareReconstructionData squareReconstructionData, SquareMatchingEdgeData[][] squareMatchingEdges)
  {
    List<MissingSquaresEntryData> missingSquaresEntries = squareReconstructionData.getMissingSquaresData().getMissingSquaresEntries();

    List<MissingSquaresUniqueEdgesData> noSquareAtAllMissingSquares = new LinkedList<>();
    List<MissingSquaresUniqueEdgesData>[] irregularMissingSquaresByColor = new List[graph.getVertices().size()];

    groupMissingSquareEntries(missingSquaresEntries, noSquareAtAllMissingSquares, irregularMissingSquaresByColor);

    UniqueList normalColorsEdgesPairIncludedColors = new UniqueList(graph.getVertices().size());
    UniqueList noSquareAtAllEdgesPairIncludedColors = new UniqueList(graph.getVertices().size());

    noSquareAtAllMissingSquares.stream()
            .map(missingSquare -> missingSquare.getBaseEdge())
            .map(baseEdge -> coloringService.getCurrentColorMapping(graph.getGraphColoring(), baseEdge.getLabel().getColor()))
            .forEach(baseEdgeMappedColor -> noSquareAtAllEdgesPairIncludedColors.add(baseEdgeMappedColor));

    IntStream.range(0, irregularMissingSquaresByColor.length)
            .filter(color -> CollectionUtils.isNotEmpty(irregularMissingSquaresByColor[color]))
            .forEach(color -> normalColorsEdgesPairIncludedColors.add(color));

    boolean cycleOfIrregularNoSquareAtAllMissingSquares = false;
    List<MissingSquaresUniqueEdgesData> irregularNoSquareAtAllMissingSquares = Collections.emptyList();
    if (CollectionUtils.isNotEmpty(noSquareAtAllMissingSquares))
    {
      cycleOfIrregularNoSquareAtAllMissingSquares = isCycleToBeSearchedFor(noSquareAtAllMissingSquares);
      irregularNoSquareAtAllMissingSquares = uncoloredEdgesHandlerService.filterCorrectNoSquareAtAllMissingSquares(noSquareAtAllMissingSquares, squareReconstructionData, cycleOfIrregularNoSquareAtAllMissingSquares);
    }

    List<Integer> includedColorsEdges;
    if (!cycleOfIrregularNoSquareAtAllMissingSquares && noSquareAtAllEdgesPairIncludedColors.size() > 0)
    {
      includedColorsEdges = noSquareAtAllEdgesPairIncludedColors.getEntries();
    }
    else
    {
      includedColorsEdges = normalColorsEdgesPairIncludedColors.getEntries();
    }

    ResultMissingSquaresData resultMissingSquaresData = new ResultMissingSquaresData(irregularNoSquareAtAllMissingSquares,
            irregularMissingSquaresByColor, includedColorsEdges, cycleOfIrregularNoSquareAtAllMissingSquares);
    return resultMissingSquaresData;
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
}
