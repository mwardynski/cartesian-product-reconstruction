package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.MissingSquaresAnalyzerService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.ReconstructionResultVerifier;
import at.ac.unileoben.mat.dissertation.reconstruction.services.uncoloredpart.UncoloredEdgesHandlerService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

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
    List<MissingSquaresUniqueEdgesData> irregularColorIndependentMissingSquares = new LinkedList<>();
    List<MissingSquaresUniqueEdgesData>[] irregularMissingSquaresByColor = new List[graph.getVertices().size()];
    List<MissingSquaresUniqueEdgesData>[] irregularMissingSquaresByColorTry = new List[graph.getVertices().size()];
    List<MissingSquaresUniqueEdgesData>[] irregularMissingSquaresByColorTryTry = new List[graph.getVertices().size()];
    List<Integer> includedColors = new LinkedList<>();

    List<MissingSquaresUniqueEdgesData> irregularAccordingToAllColorsMissingSquares = new LinkedList<>();

    for (MissingSquaresEntryData missingSquaresEntry : missingSquaresEntries)
    {
      Edge baseEdge = missingSquaresEntry.getBaseEdge();
      int baseEdgeMappedColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), baseEdge.getLabel().getColor());

      List<MissingSquaresUniqueEdgesData> collectedMissingSquares = irregularMissingSquaresByColorTryTry[baseEdgeMappedColor];
      if (collectedMissingSquares == null)
      {
        collectedMissingSquares = new LinkedList<>();
        irregularMissingSquaresByColorTryTry[baseEdgeMappedColor] = collectedMissingSquares;
      }
      List<MissingSquaresUniqueEdgesData> allOtherEdges = Arrays.stream(missingSquaresEntry.getIncludedOtherEdges())
              .filter(otherEdge -> otherEdge != null)
              .filter(otherEdge -> baseEdge.getLabel().getName() != -2 && otherEdge.getLabel().getName() != -2)
              .filter(otherEdge -> coloringService.getCurrentColorMapping(graph.getGraphColoring(), otherEdge.getLabel().getColor()) != baseEdgeMappedColor)
              .map(otherEdge -> new MissingSquaresUniqueEdgesData(baseEdge, otherEdge))
              .collect(Collectors.toList());
      if (CollectionUtils.isNotEmpty(allOtherEdges) && CollectionUtils.isEmpty(collectedMissingSquares))
      {
        includedColors.add(baseEdgeMappedColor);
      }
      collectedMissingSquares.addAll(allOtherEdges);

      Edge[] potentiallyCorrectEdgesByMappedColor = new Edge[graph.getVertices().size()];
      List<Integer> existingMappedColors = new LinkedList<>();
      List<Edge> noSquareMatchingForOtherEdges = new LinkedList<>();

      Set<Integer> baseEdgeOriginColors = baseEdge.getOrigin().getEdges().stream()
              .filter(edge -> edge.getLabel().getName() != -2)
              .map(edge -> coloringService.getCurrentColorMapping(graph.getGraphColoring(), edge.getLabel().getColor()))
              .collect(Collectors.toSet());

//      Set<Integer> baseEdgeEndpointCollors = baseEdge.getEndpoint().getEdges().stream()
//              .filter(edge -> edge.getLabel().getName() != -2)
//              .map(edge -> coloringService.getCurrentColorMapping(graph.getGraphColoring(), edge.getLabel().getColor()))
//              .collect(Collectors.toSet());

      Set<Integer> allColors = new HashSet<>(baseEdgeOriginColors);
//      allColors.retainAll(baseEdgeEndpointColors);

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
          else if (baseEdge.getLabel().getName() == -2 || otherEdge.getLabel().getName() == -2)
          {
            MissingSquaresUniqueEdgesData missingSquaresUniqueEdgesData = new MissingSquaresUniqueEdgesData(baseEdge, otherEdge);
            noSquareAtAllMissingSquares.add(missingSquaresUniqueEdgesData);
            continue;
          }

          Set<Integer> collectedColors = new HashSet<>();
          collectedColors.add(baseEdgeMappedColor);
          collectedColors.add(otherEdgeMappedColor);
          Set<Integer> noSquareMatchingForBaseEdgeColors = new HashSet<>();

          SquareMatchingEdgeData otherSquareMatchingEdgeData = squareMatchingEdges[otherEdge.getOrigin().getVertexNo()][otherEdge.getEndpoint().getVertexNo()];

          int checkSquareMatchingEdgesCounter = 0;

          for (Integer squareMatchingColor : otherSquareMatchingEdgeData.getExistingColors())
          {
            int squareMatchingMappedColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), squareMatchingColor);
            if (squareMatchingMappedColor == baseEdgeMappedColor || squareMatchingMappedColor == otherEdgeMappedColor)
            {
              continue;
            }

            List<Edge> otherSquareMatchingEdgesByColor = otherSquareMatchingEdgeData.getEdgesByColors()[squareMatchingColor];
            for (Edge otherSquareMatchingEdge : otherSquareMatchingEdgesByColor)
            {
              checkSquareMatchingEdgesCounter++;

              SquareMatchingEdgeData baseSquareMatchingEdgeData = squareMatchingEdges[baseEdge.getOrigin().getVertexNo()][baseEdge.getEndpoint().getVertexNo()];
              if (baseSquareMatchingEdgeData == null)
              {
                continue;
              }
              Edge baseSquareMatchingEdge = baseSquareMatchingEdgeData.getIncludedEdges()[otherSquareMatchingEdge.getOrigin().getVertexNo()];
              if (baseSquareMatchingEdge == null)
              {
                noSquareMatchingForBaseEdgeColors.add(squareMatchingMappedColor);
                continue;
              }

              MissingSquaresEntryData missingSquaresEntryDataForOtherSquareMatchingEdge = squareReconstructionData.getMissingSquaresData().getMissingSquaresEntriesByBaseEdge()
                      [baseSquareMatchingEdge.getOrigin().getVertexNo()][baseSquareMatchingEdge.getEndpoint().getVertexNo()];

              if (missingSquaresEntryDataForOtherSquareMatchingEdge == null
                      || missingSquaresEntryDataForOtherSquareMatchingEdge.getIncludedOtherEdges()[otherSquareMatchingEdge.getEndpoint().getVertexNo()] == null)

              {
                collectedColors.add(squareMatchingMappedColor);
                //FIXME in if consider, that for the same squareMatchingMappedColor we've got again the same otherEdge or correlated baseEdge
                if (potentiallyCorrectEdgesByMappedColor[squareMatchingMappedColor] == null)
                {
                  potentiallyCorrectEdgesByMappedColor[squareMatchingMappedColor] = otherEdge;
                  existingMappedColors.add(squareMatchingMappedColor);
                }
                else
                {
                  //TODO implement colors merge for base, other and squareMatching colors, break and exclude from further checks (it's entering this condition)
//                  System.out.println("merge base, other and squareMatching colors!");
                }
              }

            }
          }

          if (checkSquareMatchingEdgesCounter == 0)
          {
            noSquareMatchingForOtherEdges.add(otherEdge);
          }

          Set<Integer> resultColors = new HashSet<>(collectedColors);
          resultColors.addAll(noSquareMatchingForBaseEdgeColors);
          //FIXME does it make sense?!?
          if (allColors.equals(resultColors))
          {
            //could be > 1, if we are inside of a single factor
            if (noSquareMatchingForBaseEdgeColors.size() > 1)
            {
              throw new RuntimeException("noSquareMatchingForBaseEdgeColors.size() > 1");
            }
            MissingSquaresUniqueEdgesData missingSquaresUniqueEdgesData = new MissingSquaresUniqueEdgesData(baseEdge, otherEdge);
            if (CollectionUtils.isEmpty(noSquareMatchingForBaseEdgeColors))
            {
              irregularAccordingToAllColorsMissingSquares.add(missingSquaresUniqueEdgesData);
            }
            else
            {
              //FIXME is the color right? maybe it should be squareMatchingColor?
              List<MissingSquaresUniqueEdgesData> irregularMissingSquares = irregularMissingSquaresByColorTry[otherEdgeMappedColor];
              if (irregularMissingSquares == null)
              {
                irregularMissingSquares = new LinkedList<>();
                irregularMissingSquaresByColorTry[otherEdgeMappedColor] = irregularMissingSquares;
              }
              irregularMissingSquares.add(missingSquaresUniqueEdgesData);
            }
          }
        }
      }

      if (noSquareMatchingForOtherEdges.size() == 1)
      {
        MissingSquaresUniqueEdgesData missingSquaresUniqueEdgesData = new MissingSquaresUniqueEdgesData(baseEdge, noSquareMatchingForOtherEdges.get(0));
        irregularColorIndependentMissingSquares.add(missingSquaresUniqueEdgesData);
      }
      else
      {
        //TODO check if this merge is ok?
//        noSquareMatchingForOtherEdges.add(baseEdge);
//        coloringService.mergeColorsForEdges(noSquareMatchingForOtherEdges, MergeTagEnum.MULTIPLE_NO_ALONG_TO_OTHER_EDGES);
      }

      for (Integer existingMappedColor : existingMappedColors)
      {
        List<MissingSquaresUniqueEdgesData> irregularMissingSquares = irregularMissingSquaresByColor[existingMappedColor];
        if (CollectionUtils.isEmpty(irregularMissingSquares))
        {
          irregularMissingSquares = new LinkedList<>();
          irregularMissingSquaresByColor[existingMappedColor] = irregularMissingSquares;
        }


        Edge irregularMissingSquareOtherEdge = potentiallyCorrectEdgesByMappedColor[existingMappedColor];
        MissingSquaresUniqueEdgesData missingSquaresUniqueEdgesData = new MissingSquaresUniqueEdgesData(baseEdge, irregularMissingSquareOtherEdge);

        irregularMissingSquares.add(missingSquaresUniqueEdgesData);
      }
    }

    List<MissingSquaresUniqueEdgesData> irregularNoSquareAtAllMissingSquares = uncoloredEdgesHandlerService.filterCorrectNoSquareAtAllMissingSquares(noSquareAtAllMissingSquares, squareReconstructionData);

    ResultMissingSquaresData resultMissingSquaresData = new ResultMissingSquaresData(irregularNoSquareAtAllMissingSquares,
            irregularMissingSquaresByColorTryTry, includedColors);
    return resultMissingSquaresData;
  }
}
