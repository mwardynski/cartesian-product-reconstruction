package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.MissingSquaresAnalyzerService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.ReconstructionResultVerifier;
import at.ac.unileoben.mat.dissertation.reconstruction.services.uncoloredpart.UncoloredEdgesHandlerService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
  ReconstructionData reconstructionData;

  @Autowired
  ColoringService coloringService;

  @Autowired
  UncoloredEdgesHandlerService uncoloredEdgesHandlerService;

  @Autowired
  @Qualifier("reconstructionResultVerifierImpl")
  ReconstructionResultVerifier reconstructionResultVerifier;

  @Autowired
  @Qualifier("reconstructionSingleEdgeResultVerifierImpl")
  ReconstructionResultVerifier reconstructionSingleEdgeResultVerifier;


  @Override
  public void analyseMissingSquares(SquareReconstructionData squareReconstructionData, SquareMatchingEdgeData[][] squareMatchingEdges)
  {
    ResultMissingSquaresData resultMissingSquaresData = orderProbablyCorrectMissingSquaresByColor(squareReconstructionData, squareMatchingEdges);
    if (reconstructionData.getOperationOnGraph() == OperationOnGraph.FINDING_SQUARES)
    {
      reconstructionResultVerifier.compareFoundMissingVertexWithCorrectResult(resultMissingSquaresData);
    }
    else if (reconstructionData.getOperationOnGraph() == OperationOnGraph.SINGLE_EDGE_RECONSTRUCTION)
    {
      reconstructionSingleEdgeResultVerifier.compareFoundMissingVertexWithCorrectResult(resultMissingSquaresData);
    }
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

          MissingSquaresUniqueEdgesData missingSquaresUniqueEdgesData = new MissingSquaresUniqueEdgesData(baseEdge, otherEdge);
          if (baseEdgeMappedColor == 0)
          {
            noSquareAtAllMissingSquares.add(missingSquaresUniqueEdgesData);
            noSquareAtAllEdgesPairIncludedColors.add(baseEdgeMappedColor);
          }

          else if (otherEdgesColor != 0)
          {
            normalColorsEdgesPairIncludedColors.add(baseEdgeMappedColor);
            collectedMissingSquares.add(missingSquaresUniqueEdgesData);
          }
        }
      }
    }

    List<MissingSquaresUniqueEdgesData> irregularNoSquareAtAllMissingSquares = Collections.emptyList();
    if (CollectionUtils.isNotEmpty(noSquareAtAllMissingSquares))
    {
      defineFormationToSearchFor(noSquareAtAllMissingSquares, squareReconstructionData);
      irregularNoSquareAtAllMissingSquares = uncoloredEdgesHandlerService.filterCorrectNoSquareAtAllMissingSquares(noSquareAtAllMissingSquares, squareReconstructionData);
    }

    List<Integer> includedColorsEdges;
    if (squareReconstructionData.getMissingEdgesFormation() == MissingEdgesFormation.SPIKE)
    {
      includedColorsEdges = noSquareAtAllEdgesPairIncludedColors.getEntries();
    }
    else
    {
      includedColorsEdges = normalColorsEdgesPairIncludedColors.getEntries();
    }

    ResultMissingSquaresData resultMissingSquaresData = new ResultMissingSquaresData(irregularNoSquareAtAllMissingSquares,
            irregularMissingSquaresByColor, includedColorsEdges, squareReconstructionData.getMissingEdgesFormation());
    return resultMissingSquaresData;
  }

  private void defineFormationToSearchFor(List<MissingSquaresUniqueEdgesData> noSquareAtAllMissingSquares, SquareReconstructionData squareReconstructionData)
  {
    boolean spikeIncluded = false;
    boolean[][] collectedEdgesIncluded = new boolean[graph.getVertices().size()][graph.getVertices().size()];
    List<Edge> collectedEdges = new LinkedList<>();

    for (MissingSquaresUniqueEdgesData noSquareAtAllMissingSquare : noSquareAtAllMissingSquares)
    {
      Edge baseEdge = noSquareAtAllMissingSquare.getBaseEdge();
      Edge otherEdge = noSquareAtAllMissingSquare.getOtherEdge();

      List<Edge> currentNoSquareAtAllMissingEdges = new LinkedList<>();
      if (baseEdge.getLabel().getColor() == 0)
      {
        currentNoSquareAtAllMissingEdges.add(baseEdge);
      }
      if (otherEdge.getLabel().getColor() == 0)
      {
        currentNoSquareAtAllMissingEdges.add(otherEdge);
      }

      for (Edge currentNoSquareAtAllMissingEdge : currentNoSquareAtAllMissingEdges)
      {
        if (!collectedEdgesIncluded[currentNoSquareAtAllMissingEdge.getEndpoint().getVertexNo()][currentNoSquareAtAllMissingEdge.getOrigin().getVertexNo()])
        {
          collectedEdgesIncluded[currentNoSquareAtAllMissingEdge.getEndpoint().getVertexNo()][currentNoSquareAtAllMissingEdge.getOrigin().getVertexNo()] = true;
          collectedEdgesIncluded[currentNoSquareAtAllMissingEdge.getOrigin().getVertexNo()][currentNoSquareAtAllMissingEdge.getEndpoint().getVertexNo()] = true;
          collectedEdges.add(currentNoSquareAtAllMissingEdge);

          if (currentNoSquareAtAllMissingEdge.getEndpoint().getEdges().size() == 1)
          {
            spikeIncluded = true;
            break;
          }
        }
      }
    }

    if (spikeIncluded)
    {
      squareReconstructionData.setMissingEdgesFormation(MissingEdgesFormation.SPIKE);
    }
    else
    {
      if (collectedEdges.size() == 1)
      {
        squareReconstructionData.setSingleBridgeEdge(collectedEdges.get(0));
      }
      squareReconstructionData.setMissingEdgesFormation(MissingEdgesFormation.CYCLE);
    }
  }
}
