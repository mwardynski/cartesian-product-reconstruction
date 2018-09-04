package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.MissingSquaresAnalyzerService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
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
  TestCaseContext testCaseContext;

  @Override
  public void analyseMissingSquares(SquareReconstructionData squareReconstructionData, SquareMatchingEdgeData[][] squareMatchingEdges)
  {
    IrregularMissingSquaresData irregularMissingSquaresData = orderProbablyCorrectMissingSquaresByColor(squareReconstructionData, squareMatchingEdges);
    //TODO reconsider MissingSquaresUniqueEdgesData of newly merged colors

//    printOutFoundIrregularMissingSquares(irregularMissingSquaresData);
    compareMisssingVertexNeighbors(irregularMissingSquaresData);

  }

  private void compareMisssingVertexNeighbors(IrregularMissingSquaresData irregularMissingSquaresData)
  {
    Set<Integer> expectedNeighborsVertexNumbers = testCaseContext.getRemovedVertexNeighbors();

    Set<Integer> colorIndependentMissingSquaresVertexNumbers = mapIrregularMissingSquaresToOriginVertexNumbers(irregularMissingSquaresData.getIrregularColorIndependentMissingSquares());
    Set<Integer> noSquareAtAllMissingSquaresVertexNumbers = mapIrregularMissingSquaresToOriginVertexNumbers(irregularMissingSquaresData.getIrregularNoSquareAtAllMissingSquares());
    Set<Integer> allColorsAccordingMissingSquaresVertexNumbers = mapIrregularMissingSquaresToOriginVertexNumbers(irregularMissingSquaresData.getIrregularAccordingToAllColorsMissingSquares());

    List<MissingSquaresUniqueEdgesData>[] irregularMissingSquaresByColor = irregularMissingSquaresData.getIrregularMissingSquaresByColor();
    long notEmptyirregularMissingSquaresByColorCount = Arrays.stream(irregularMissingSquaresByColor)
            .filter(irregularMissingSquares -> CollectionUtils.isNotEmpty(irregularMissingSquares))
            .count();

    System.out.println("EXPECTED NEIGHBORS: " + StringUtils.join(expectedNeighborsVertexNumbers, ","));

    HashSet actualNeighborsVertexNumber = new HashSet(noSquareAtAllMissingSquaresVertexNumbers);
//    actualNeighborsVertexNumber.addAll(allColorsAccordingMissingSquaresVertexNumbers);

    if (expectedNeighborsVertexNumbers.equals(actualNeighborsVertexNumber))
    {
      System.out.println("SUCCESS - FOUND CORRECT NEIGHBORS FOR MISSING SQUARE: " + StringUtils.join(actualNeighborsVertexNumber, ","));
    }
    else
    {
      List<Integer> origAllColorsAccordingMissingSquaresVertexNumbers = allColorsAccordingMissingSquaresVertexNumbers.stream()
              .map(vNo -> graph.getReverseReindexArray()[vNo])
              .collect(Collectors.toList());

      System.out.println("color independent missing squares: " + StringUtils.join(colorIndependentMissingSquaresVertexNumbers, ",")); //not needed
      System.out.println("no square at all missing squares: " + StringUtils.join(noSquareAtAllMissingSquaresVertexNumbers, ","));
      System.out.println("all colors approved missing squares: " + StringUtils.join(allColorsAccordingMissingSquaresVertexNumbers, ","));
      System.out.println("orig all colors approved missing squares: " + StringUtils.join(origAllColorsAccordingMissingSquaresVertexNumbers, ","));
      List<Integer> fittingResultColors = new LinkedList<>();
      for (int i = 0; i < irregularMissingSquaresByColor.length; i++)
      {
        List<MissingSquaresUniqueEdgesData> irregularMissingSquares = irregularMissingSquaresByColor[i];
        if (CollectionUtils.isNotEmpty(irregularMissingSquares))
        {
          Set<Integer> irregularMissingSquaresByColorVertexNumbers = mapIrregularMissingSquaresToOriginVertexNumbers(irregularMissingSquares);

          Set<Integer> actualNeighborsVertexNumbers = new HashSet<>(irregularMissingSquaresByColorVertexNumbers);
//          actualNeighborsVertexNumbers.addAll(allColorsAccordingMissingSquaresVertexNumbers);
          actualNeighborsVertexNumbers.addAll(noSquareAtAllMissingSquaresVertexNumbers);

          System.out.println(String.format("color: %d, vertices: %s ", i, StringUtils.join(actualNeighborsVertexNumbers, ",")));

          if (expectedNeighborsVertexNumbers.equals(actualNeighborsVertexNumbers))
          {
            fittingResultColors.add(i);
          }
        }
      }

      if (CollectionUtils.isNotEmpty(fittingResultColors))
      {
        fittingResultColors.stream()
                .forEach(color ->
                {
                  List<MissingSquaresUniqueEdgesData> irregularMissingSquares = irregularMissingSquaresByColor[color];
                  System.out.println("COLOR: " + color +
                          ", SUCCESS - FOUND CORRECT NEIGHBORS FOR MISSING SQUARE: "
                          + StringUtils.join(colorIndependentMissingSquaresVertexNumbers, ",")
                          + ","
                          + StringUtils.join(irregularMissingSquares, ","));
                });
      }
      else
      {
        System.out.println("FAILURE - NOT MATCHING VERTICES");
      }
    }
  }

  private Set<Integer> mapIrregularMissingSquaresToOriginVertexNumbers(List<MissingSquaresUniqueEdgesData> missingSquares)
  {
    return missingSquares.stream()
            .map(missingSquare -> Arrays.asList(missingSquare.getBaseEdge().getEndpoint(), missingSquare.getOtherEdge().getEndpoint()))
            .flatMap(verticesPairs -> verticesPairs.stream())
            .map(v -> v.getVertexNo())
            .map(vNo -> graph.getReverseReindexArray()[vNo])
            .collect(Collectors.toSet());
  }

  private void printOutFoundIrregularMissingSquares(IrregularMissingSquaresData irregularMissingSquaresData)
  {
    System.out.println("CorrectNoOtherColorToCheckMissingSquares:");
    irregularMissingSquaresData.getIrregularColorIndependentMissingSquares().stream()
            .forEach(missingSquare -> System.out.println(String.format("(%d-%d)-(%d-%d)",
                    missingSquare.getBaseEdge().getOrigin().getVertexNo(),
                    missingSquare.getBaseEdge().getEndpoint().getVertexNo(),
                    missingSquare.getOtherEdge().getOrigin().getVertexNo(),
                    missingSquare.getOtherEdge().getEndpoint().getVertexNo()
            )));

    System.out.println("IrregularMissingSquaresByColor:");
    for (int i = 0; i < irregularMissingSquaresData.getIrregularMissingSquaresByColor().length; i++)
    {
      List<MissingSquaresUniqueEdgesData> missingSquaresUniqueEdges = irregularMissingSquaresData.getIrregularMissingSquaresByColor()[i];
      if (CollectionUtils.isNotEmpty(missingSquaresUniqueEdges))
      {
        System.out.println(String.format("color %d: ", i));
        missingSquaresUniqueEdges.stream()
                .forEach(missingSquare -> System.out.println(String.format("(%d-%d)-(%d-%d)",
                        missingSquare.getBaseEdge().getOrigin().getVertexNo(),
                        missingSquare.getBaseEdge().getEndpoint().getVertexNo(),
                        missingSquare.getOtherEdge().getOrigin().getVertexNo(),
                        missingSquare.getOtherEdge().getEndpoint().getVertexNo()
                )));
      }
    }
  }


  private IrregularMissingSquaresData orderProbablyCorrectMissingSquaresByColor(SquareReconstructionData squareReconstructionData, SquareMatchingEdgeData[][] squareMatchingEdges)
  {
    List<MissingSquaresEntryData> missingSquaresEntries = squareReconstructionData.getMissingSquaresData().getMissingSquaresEntries();

    List<MissingSquaresUniqueEdgesData> noSquareAtAllMissingSquares = new LinkedList<>();
    List<MissingSquaresUniqueEdgesData> irregularColorIndependentMissingSquares = new LinkedList<>();
    List<MissingSquaresUniqueEdgesData>[] irregularMissingSquaresByColor = new List[graph.getVertices().size()];
    List<MissingSquaresUniqueEdgesData>[] irregularMissingSquaresByColorTry = new List[graph.getVertices().size()];
    List<MissingSquaresUniqueEdgesData>[] irregularMissingSquaresByColorTryTry = new List[graph.getVertices().size()];

    List<MissingSquaresUniqueEdgesData> irregularAccordingToAllColorsMissingSquares = new LinkedList<>();

    for (MissingSquaresEntryData missingSquaresEntry : missingSquaresEntries)
    {
      Edge baseEdge = missingSquaresEntry.getBaseEdge();
      int baseEdgeMappedColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), baseEdge.getLabel().getColor());

      List<MissingSquaresUniqueEdgesData> tmpMissingSquares = irregularMissingSquaresByColorTryTry[baseEdgeMappedColor];
      if (tmpMissingSquares == null)
      {
        tmpMissingSquares = new LinkedList<>();
        irregularMissingSquaresByColorTryTry[baseEdgeMappedColor] = tmpMissingSquares;
      }
      List<MissingSquaresUniqueEdgesData> allOtherEdges = Arrays.stream(missingSquaresEntry.getIncludedOtherEdges())
              .filter(otherEdge -> otherEdge != null)
              .filter(otherEdge -> baseEdge.getLabel().getName() != -2 /*&& otherEdge.getLabel().getName() != -2*/)
              .filter(otherEdge -> coloringService.getCurrentColorMapping(graph.getGraphColoring(), otherEdge.getLabel().getColor()) != baseEdgeMappedColor)
              .map(otherEdge -> new MissingSquaresUniqueEdgesData(baseEdge, otherEdge))
              .collect(Collectors.toList());
      tmpMissingSquares.addAll(allOtherEdges);

      Edge[] potentiallyCorrectEdgesByMappedColor = new Edge[graph.getVertices().size()];
      List<Integer> existingMappedColors = new LinkedList<>();
      List<Edge> noSquareMatchingForOtherEdges = new LinkedList<>();

      Set<Integer> baseEdgeOriginCollors = baseEdge.getOrigin().getEdges().stream()
              .filter(edge -> edge.getLabel().getName() != -2)
              .map(edge -> coloringService.getCurrentColorMapping(graph.getGraphColoring(), edge.getLabel().getColor()))
              .collect(Collectors.toSet());

//      Set<Integer> baseEdgeEndpointCollors = baseEdge.getEndpoint().getEdges().stream()
//              .filter(edge -> edge.getLabel().getName() != -2)
//              .map(edge -> coloringService.getCurrentColorMapping(graph.getGraphColoring(), edge.getLabel().getColor()))
//              .collect(Collectors.toSet());

      Set<Integer> allColors = new HashSet<>(baseEdgeOriginCollors);
//      allColors.retainAll(baseEdgeEndpointCollors);

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
          if (allColors.equals(resultColors))
          {
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

    List<MissingSquaresUniqueEdgesData> irregularNoSquareAtAllMissingSquares = handleNoSquareAtAllMissingSquares(noSquareAtAllMissingSquares, squareReconstructionData);

    IrregularMissingSquaresData irregularMissingSquaresData = new IrregularMissingSquaresData(irregularColorIndependentMissingSquares, irregularMissingSquaresByColorTryTry,
            irregularNoSquareAtAllMissingSquares, irregularAccordingToAllColorsMissingSquares);
    return irregularMissingSquaresData;
  }

  private List<MissingSquaresUniqueEdgesData> handleNoSquareAtAllMissingSquares(List<MissingSquaresUniqueEdgesData> noSquareAtAllMissingSquares, SquareReconstructionData squareReconstructionData)
  {
    List<MissingSquaresUniqueEdgesData> bothEdgesWithNoSquareAtAllMissingSquares = new LinkedList<>();
    List<MissingSquaresUniqueEdgesData> singleEdgeWithNoSquareAtAllMissingSquares = new LinkedList<>();

    boolean startFromBothEdgesWithNoSquareAtAllMissingSquares = false;

    for (MissingSquaresUniqueEdgesData noSquareAtAllMissingSquare : noSquareAtAllMissingSquares)
    {
      if (noSquareAtAllMissingSquare.getBaseEdge().getLabel().getName() == -2 && noSquareAtAllMissingSquare.getOtherEdge().getLabel().getName() == -2)
      {
        bothEdgesWithNoSquareAtAllMissingSquares.add(noSquareAtAllMissingSquare);
        continue;
      }

      Edge noSquareEdge;
      Edge otherEdge;

      if (noSquareAtAllMissingSquare.getBaseEdge().getLabel().getName() == -2)
      {
        noSquareEdge = noSquareAtAllMissingSquare.getBaseEdge();
        otherEdge = noSquareAtAllMissingSquare.getOtherEdge();
      }
      else
      {
        noSquareEdge = noSquareAtAllMissingSquare.getOtherEdge();
        otherEdge = noSquareAtAllMissingSquare.getBaseEdge();
      }
      Edge otherEdgeOpposite = otherEdge.getOpposite();

      int otherEdgeOfSingleEdgeWithNoSquareAtAllMissingSquareCounter = 0;
      MissingSquaresEntryData missingSquaresEntryData = squareReconstructionData.getMissingSquaresData().getMissingSquaresEntriesByBaseEdge()[otherEdgeOpposite.getOrigin().getVertexNo()][otherEdgeOpposite.getEndpoint().getVertexNo()];
      if (missingSquaresEntryData != null)
      {
        for (int i : missingSquaresEntryData.getExistingColors())
        {
          otherEdgeOfSingleEdgeWithNoSquareAtAllMissingSquareCounter += missingSquaresEntryData.getOtherEdgesByColors()[i].size();
        }
      }
      boolean otherEdgeOfSingleEdgeWithNoSquareAtAllMissingSquareHavingManyMissingSquares = otherEdgeOfSingleEdgeWithNoSquareAtAllMissingSquareCounter != 0;

      List<Edge> sameColorToOtherEdges = otherEdge.getEndpoint().getEdges().stream()
              .filter(edge -> edge != otherEdgeOpposite)
              .filter(edge -> edge.getLabel().getColor() == otherEdge.getLabel().getColor())
              .map(Edge::getOpposite)
              .collect(Collectors.toList());

      //FIXME optimize this process - store other found correctNoSquareAtAllMissingSquare and don't check it again - probably having a color is enough
      List<Edge> sameColorToOtherEdgesHavingMissingSquares = sameColorToOtherEdges.stream()
              .filter(edge -> squareReconstructionData.getMissingSquaresData()
                      .getMissingSquaresEntriesByBaseEdge()[edge.getOrigin().getVertexNo()][edge.getEndpoint().getVertexNo()] != null)
              .collect(Collectors.toList());

      if (otherEdgeOfSingleEdgeWithNoSquareAtAllMissingSquareHavingManyMissingSquares
              && sameColorToOtherEdgesHavingMissingSquares.size() != 0)
      {
        throw new RuntimeException("otherEdgeOfSingleEdgeWithNoSquareAtAllMissingSquareHavingManyMissingSquares && sameColorToOtherEdgesHavingMissingSquares.size() != 0");
      }

      if (startFromBothEdgesWithNoSquareAtAllMissingSquares && !otherEdgeOfSingleEdgeWithNoSquareAtAllMissingSquareHavingManyMissingSquares)
      {
        throw new RuntimeException("startFromBothEdgesWithNoSquareAtAllMissingSquares && !otherEdgeOfSingleEdgeWithNoSquareAtAllMissingSquareHavingManyMissingSquares");
      }
      startFromBothEdgesWithNoSquareAtAllMissingSquares = otherEdgeOfSingleEdgeWithNoSquareAtAllMissingSquareHavingManyMissingSquares;


      if (otherEdgeOfSingleEdgeWithNoSquareAtAllMissingSquareHavingManyMissingSquares
              || CollectionUtils.isNotEmpty(sameColorToOtherEdgesHavingMissingSquares))
      {
        singleEdgeWithNoSquareAtAllMissingSquares.add(noSquareAtAllMissingSquare);

        if (!otherEdgeOfSingleEdgeWithNoSquareAtAllMissingSquareHavingManyMissingSquares
                && sameColorToOtherEdgesHavingMissingSquares.size() != sameColorToOtherEdges.size())
        {
          throw new RuntimeException("sameColorToOtherEdgesHavingMissingSquares.size() != sameColorToOtherEdges.size()");
        }
      }
    }

    List<MissingSquaresUniqueEdgesData> correctNoSquareAtAllMissingSquares = new LinkedList<>();
    List<MissingSquaresUniqueEdgesData> correctBothEdgesWithNoSquareAtAllMissingSquares = Collections.emptyList();


    boolean[] correctMissingSquaresEndpointVertices = new boolean[graph.getVertices().size()];
    if (startFromBothEdgesWithNoSquareAtAllMissingSquares)
    {
      singleEdgeWithNoSquareAtAllMissingSquares.stream()
              .forEach(missingSquare -> correctMissingSquaresEndpointVertices[missingSquare.getBaseEdge().getOrigin().getVertexNo()] = true);
    }
    else
    {
      singleEdgeWithNoSquareAtAllMissingSquares.stream()
              .forEach(missingSquare ->
              {
                correctMissingSquaresEndpointVertices[missingSquare.getBaseEdge().getEndpoint().getVertexNo()] = true;
                correctMissingSquaresEndpointVertices[missingSquare.getOtherEdge().getEndpoint().getVertexNo()] = true;
              });
    }

    correctBothEdgesWithNoSquareAtAllMissingSquares = bothEdgesWithNoSquareAtAllMissingSquares.stream()
            .filter(missingSquare ->
                    correctMissingSquaresEndpointVertices[missingSquare.getBaseEdge().getEndpoint().getVertexNo()]
                            || correctMissingSquaresEndpointVertices[missingSquare.getOtherEdge().getEndpoint().getVertexNo()]
            )
            .collect(Collectors.toList());

    correctNoSquareAtAllMissingSquares.addAll(correctBothEdgesWithNoSquareAtAllMissingSquares);
    if (!startFromBothEdgesWithNoSquareAtAllMissingSquares)
    {
      correctNoSquareAtAllMissingSquares.addAll(singleEdgeWithNoSquareAtAllMissingSquares);
    }

//    boolean edgeConnectingSingleEdgesWithNoSquareExists = bothEdgesWithNoSquareAtAllMissingSquares.stream()
//            .filter(missingSquare -> singleEdgesWithNoSquareAtAllEndpoints[missingSquare.getBaseEdge().getEndpoint().getVertexNo()]
//                    && singleEdgesWithNoSquareAtAllEndpoints[missingSquare.getOtherEdge().getEndpoint().getVertexNo()])
//            .findAny().isPresent();
//
//    if (edgeConnectingSingleEdgesWithNoSquareExists)
//    {
//      correctBothEdgesWithNoSquareAtAllMissingSquares = bothEdgesWithNoSquareAtAllMissingSquares.stream()
//              .filter(missingSquare -> singleEdgesWithNoSquareAtAllEndpoints[missingSquare.getBaseEdge().getOrigin().getVertexNo()])
//              .collect(Collectors.toList());
//    }
//    else
//    {
//      correctBothEdgesWithNoSquareAtAllMissingSquares = bothEdgesWithNoSquareAtAllMissingSquares.stream()
//              .filter(missingSquare -> singleEdgesWithNoSquareAtAllEndpoints[missingSquare.getBaseEdge().getEndpoint().getVertexNo()]
//                      || singleEdgesWithNoSquareAtAllEndpoints[missingSquare.getOtherEdge().getEndpoint().getVertexNo()])
//              .collect(Collectors.toList());
//    }

    return correctNoSquareAtAllMissingSquares;
  }
}
