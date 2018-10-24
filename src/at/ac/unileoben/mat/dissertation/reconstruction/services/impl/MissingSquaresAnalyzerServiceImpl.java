package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.MissingSquaresAnalyzerService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.ReconstructionResultVerifier;
import at.ac.unileoben.mat.dissertation.reconstruction.services.uncoloredpart.UncoloredEdgesHandlerService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@Component
public class MissingSquaresAnalyzerServiceImpl implements MissingSquaresAnalyzerService
{

  @Autowired
  Graph graph;

  @Autowired
  ColoringService coloringService;

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
    UniqueList normalColorsEdgesPairIncludedColors = new UniqueList(graph.getVertices().size());
    UniqueList noSquareAtAllEdgesPairIncludedColors = new UniqueList(graph.getVertices().size());

    for (MissingSquaresEntryData missingSquaresEntry : missingSquaresEntries)
    {
      Edge baseEdge = missingSquaresEntry.getBaseEdge();
      int baseEdgeMappedColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), baseEdge.getLabel().getColor());

      List<MissingSquaresUniqueEdgesData> collectedMissingSquares = irregularMissingSquaresByColor[baseEdgeMappedColor];
      if (collectedMissingSquares == null)
      {
        collectedMissingSquares = new LinkedList<>();
        irregularMissingSquaresByColor[baseEdgeMappedColor] = collectedMissingSquares;
      }

      for (Integer otherEdgesColor : missingSquaresEntry.getExistingColors())
      {
        List<Edge> otherEdges = missingSquaresEntry.getOtherEdgesByColors()[otherEdgesColor];

        Iterator<Edge> otherEdgesItertor = otherEdges.iterator();
        while (otherEdgesItertor.hasNext())
        {
          Edge otherEdge = otherEdgesItertor.next();
          int otherEdgeMappedColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), otherEdge.getLabel().getColor());

          MissingSquaresUniqueEdgesData missingSquaresUniqueEdgesData = new MissingSquaresUniqueEdgesData(baseEdge, otherEdge);
          if (baseEdgeMappedColor == 0 || otherEdge.getLabel().getColor() == 0)
          {
            noSquareAtAllMissingSquares.add(missingSquaresUniqueEdgesData);
            if (baseEdgeMappedColor != 0)
            {
              noSquareAtAllEdgesPairIncludedColors.add(baseEdgeMappedColor);
            }
            else if (otherEdgeMappedColor != 0)
            {
              noSquareAtAllEdgesPairIncludedColors.add(otherEdgeMappedColor);
            }
          }

          else
          {
            normalColorsEdgesPairIncludedColors.add(baseEdgeMappedColor);
            collectedMissingSquares.add(missingSquaresUniqueEdgesData);
          }
        }
      }
    }

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
