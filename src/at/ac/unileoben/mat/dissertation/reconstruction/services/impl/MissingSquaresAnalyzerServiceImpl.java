package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.MissingSquaresAnalyzerService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.ReconstructionResultVerifier;
import at.ac.unileoben.mat.dissertation.reconstruction.services.uncoloredpart.UncoloredEdgesHandlerService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    List<Integer> includedColors = new LinkedList<>();

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

          if (baseEdgeMappedColor == otherEdgeMappedColor)
          {
            otherEdgesItertor.remove();
            continue;
          }
          else
          {
            MissingSquaresUniqueEdgesData missingSquaresUniqueEdgesData = new MissingSquaresUniqueEdgesData(baseEdge, otherEdge);
            if (baseEdge.getLabel().getName() == -2 || otherEdge.getLabel().getName() == -2)
            {
              noSquareAtAllMissingSquares.add(missingSquaresUniqueEdgesData);
            }
            else if (coloringService.getCurrentColorMapping(graph.getGraphColoring(), otherEdge.getLabel().getColor()) != baseEdgeMappedColor)
            {
              if (CollectionUtils.isEmpty(collectedMissingSquares))
              {
                includedColors.add(baseEdgeMappedColor);
              }
              collectedMissingSquares.add(missingSquaresUniqueEdgesData);
            }
          }
        }
      }
    }

    List<MissingSquaresUniqueEdgesData> irregularNoSquareAtAllMissingSquares = uncoloredEdgesHandlerService.filterCorrectNoSquareAtAllMissingSquares(noSquareAtAllMissingSquares, squareReconstructionData);

    ResultMissingSquaresData resultMissingSquaresData = new ResultMissingSquaresData(irregularNoSquareAtAllMissingSquares,
            irregularMissingSquaresByColor, includedColors);
    return resultMissingSquaresData;
  }
}
