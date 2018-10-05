package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.MissingSquaresAnalyzerService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.ReconstructionResultVerifier;
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

    List<MissingSquaresUniqueEdgesData> irregularNoSquareAtAllMissingSquares = handleNoSquareAtAllMissingSquares(noSquareAtAllMissingSquares, squareReconstructionData);

    ResultMissingSquaresData resultMissingSquaresData = new ResultMissingSquaresData(irregularNoSquareAtAllMissingSquares,
            irregularMissingSquaresByColorTryTry, includedColors);
    return resultMissingSquaresData;
  }

  private List<MissingSquaresUniqueEdgesData> handleNoSquareAtAllMissingSquares(List<MissingSquaresUniqueEdgesData> noSquareAtAllMissingSquares, SquareReconstructionData squareReconstructionData)
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
        return filterOutCorrectPartOfCycleNoSquareAtAllMissingSquares(noSquareAtAllMissingSquares, squareReconstructionData);
      }
      else
      {
        return filterOutCorrectSingleNoSquareAtAllMissingSquares(noSquareAtAllMissingSquares, squareReconstructionData);
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

  private List<MissingSquaresUniqueEdgesData> filterOutCorrectPartOfCycleNoSquareAtAllMissingSquares(List<MissingSquaresUniqueEdgesData> noSquareAtAllMissingSquares, SquareReconstructionData squareReconstructionData)
  {
    NoSquareAtAllGroupsData noSquareAtAllGroupsData = splitPartOfCycleNoSquareAtAllMissingSquaresIntoGroups(noSquareAtAllMissingSquares);
    return findCycleForNoSquareAtAllGroups(noSquareAtAllGroupsData, squareReconstructionData);
  }

  private NoSquareAtAllGroupsData splitPartOfCycleNoSquareAtAllMissingSquaresIntoGroups(List<MissingSquaresUniqueEdgesData> noSquareAtAllMissingSquares)
  {
    List<Edge> boarderNoSquareAtAllEdges = new LinkedList<>();
    Edge[][] noSquareAtAllEdgesByEndpoints = new Edge[graph.getVertices().size()][2];

    noSquareAtAllMissingSquares.stream().forEach(
            missingSquare ->
            {
              Edge baseEdge = missingSquare.getBaseEdge();
              Edge otherEdge = missingSquare.getOtherEdge();

              if (baseEdge.getLabel().getName() == -2)
              {
                assignNoSquareAtAllEdgesToArrays(baseEdge, otherEdge, boarderNoSquareAtAllEdges, noSquareAtAllEdgesByEndpoints);
              }
              if (otherEdge.getLabel().getName() == -2)
              {
                assignNoSquareAtAllEdgesToArrays(otherEdge, baseEdge, boarderNoSquareAtAllEdges, noSquareAtAllEdgesByEndpoints);
              }
            }
    );

    Integer[] groupNumbersForNoSquareAtAllEdgesEndpoints = new Integer[graph.getVertices().size()];
    List<List<Edge>> groupedNoSquareAtAllEdges = new ArrayList<>();

    assignNoSquareAtAllEdgesIntoIncidentGroups(boarderNoSquareAtAllEdges, noSquareAtAllEdgesByEndpoints, groupNumbersForNoSquareAtAllEdgesEndpoints, groupedNoSquareAtAllEdges);
    NoSquareAtAllGroupsData noSquareAtAllGroupsData =
            new NoSquareAtAllGroupsData(groupNumbersForNoSquareAtAllEdgesEndpoints, groupedNoSquareAtAllEdges);
    return noSquareAtAllGroupsData;
  }

  private void assignNoSquareAtAllEdgesToArrays(Edge edge1, Edge edge2, List<Edge> boarderNoSquareAtAllEdges, Edge[][] noSquareAtAllEdgesByEndpoints)
  {
    Edge[] boarderNoSquareAtAllEdgesByEndpoints = new Edge[graph.getVertices().size()];
    int originVertexNo = edge1.getOrigin().getVertexNo();
    int endpointVertexNo = edge1.getEndpoint().getVertexNo();

    boolean assignedToOriginNo = assignEdgeToArray(edge1, originVertexNo, noSquareAtAllEdgesByEndpoints);
    boolean assignedToEndpointNo = assignEdgeToArray(edge1.getOpposite(), endpointVertexNo, noSquareAtAllEdgesByEndpoints);

    if (!assignedToOriginNo || !assignedToEndpointNo)
    {
      throw new RuntimeException("!assignedToOriginNo || !assignedToEndpointNo");
    }

    if (edge2.getLabel().getName() != -2 && boarderNoSquareAtAllEdgesByEndpoints[edge1.getOrigin().getVertexNo()] == null)
    {
      boarderNoSquareAtAllEdges.add(edge1);
      boarderNoSquareAtAllEdgesByEndpoints[edge1.getOrigin().getVertexNo()] = edge1;
    }
  }

  private boolean assignEdgeToArray(Edge edge, int vertexNo, Edge[][] noSquareAtAllEdgesByEndpoint)
  {
    boolean edgeAssigned = false;
    for (int i = 0; i < 2; i++)
    {
      if (noSquareAtAllEdgesByEndpoint[vertexNo][i] == null ||
              noSquareAtAllEdgesByEndpoint[vertexNo][i] == edge)
      {
        noSquareAtAllEdgesByEndpoint[vertexNo][i] = edge;
        edgeAssigned = true;
        break;
      }
    }
    return edgeAssigned;
  }

  private void assignNoSquareAtAllEdgesIntoIncidentGroups(List<Edge> boarderNoSquareAtAllEdges, Edge[][] noSquareAtAllEdgesByEndpoints, Integer[] groupNumbersForNoSquareAtAllEdgesEndpoints, List<List<Edge>> groupedNoSquareAtAllEdges)
  {
    int groupNumber = 0;
    for (Edge boarderNoSquareAtAllEdge : boarderNoSquareAtAllEdges)
    {
      if (groupNumbersForNoSquareAtAllEdgesEndpoints[boarderNoSquareAtAllEdge.getOrigin().getVertexNo()] != null)
      {
        continue;
      }
      else
      {
        groupedNoSquareAtAllEdges.add(new LinkedList<>());
      }

      Edge currentEdge = boarderNoSquareAtAllEdge;
      while (currentEdge != null)
      {
        groupNumbersForNoSquareAtAllEdgesEndpoints[currentEdge.getOrigin().getVertexNo()] = groupNumber;
        groupNumbersForNoSquareAtAllEdgesEndpoints[currentEdge.getEndpoint().getVertexNo()] = groupNumber;

        groupedNoSquareAtAllEdges.get(groupNumber).add(currentEdge);

        Edge possibleNextEdge1 = noSquareAtAllEdgesByEndpoints[currentEdge.getEndpoint().getVertexNo()][0];
        Edge possibleNextEdge2 = noSquareAtAllEdgesByEndpoints[currentEdge.getEndpoint().getVertexNo()][1];
        if (possibleNextEdge1 != null && possibleNextEdge1 != currentEdge.getOpposite())
        {
          currentEdge = possibleNextEdge1;
        }
        else if (possibleNextEdge2 != null && possibleNextEdge2 != currentEdge.getOpposite())
        {
          currentEdge = possibleNextEdge2;
        }
        else
        {
          currentEdge = null;
        }
      }

      groupNumber++;
    }
  }

  private List<MissingSquaresUniqueEdgesData> findCycleForNoSquareAtAllGroups(NoSquareAtAllGroupsData noSquareAtAllGroupsData, SquareReconstructionData squareReconstructionData)
  {
    List<List<Edge>> groupedNoSquareAtAllEdges = noSquareAtAllGroupsData.getGroupedNoSquareAtAllEdges();

    Edge arbitraryGroupFirstEdge = groupedNoSquareAtAllEdges.get(0).get(0);
    List<List<NoSquareAtAllCycleNode>> correctCycles = findCycleUsingBfs(arbitraryGroupFirstEdge, groupedNoSquareAtAllEdges, noSquareAtAllGroupsData.getGroupNumbersForNoSquareAtAllEdgesEndpoints(), squareReconstructionData);

    List<MissingSquaresUniqueEdgesData> correctNoSquareAtAllMissingSquares = splitCyclesIntoMissingSquares(correctCycles);
    return correctNoSquareAtAllMissingSquares;
  }

  private List<List<NoSquareAtAllCycleNode>> findCycleUsingBfs(Edge arbitraryGroupFirstEdge, List<List<Edge>> groupedNoSquareAtAllEdges, Integer[] groupNumbersForNoSquareAtAllEdgesEndpoints, SquareReconstructionData squareReconstructionData)
  {
    NoSquareAtAllCycleNode[] noSquareAtAllCycleNodesByVertexNo = new NoSquareAtAllCycleNode[graph.getVertices().size()];

    Vertex endVertex = arbitraryGroupFirstEdge.getEndpoint();
    Vertex firstVertex = arbitraryGroupFirstEdge.getOrigin();
    NoSquareAtAllCycleNode firstNoSquareAtAllCycleNode = new NoSquareAtAllCycleNode(firstVertex, 0);
    noSquareAtAllCycleNodesByVertexNo[firstVertex.getVertexNo()] = firstNoSquareAtAllCycleNode;

    Queue<Vertex> nextVertices = new LinkedList<>();
    nextVertices.add(firstVertex);

    while (CollectionUtils.isNotEmpty(nextVertices))
    {
      Vertex currentVertex = nextVertices.poll();

      NoSquareAtAllCycleNode currentVertexNode = noSquareAtAllCycleNodesByVertexNo[currentVertex.getVertexNo()];
      if (currentVertexNode.getDistance() >= 7)
      {
        break;
      }

      MissingSquaresEntryData[][] missingSquaresByEdges = squareReconstructionData.getMissingSquaresData().getMissingSquaresEntriesByBaseEdge();

      currentVertex.getEdges().stream()
              .map(edge -> edge.getEndpoint())
              .filter(nextVertex -> includeVerticesDifferentThanEndVertex(currentVertex, nextVertex, endVertex, noSquareAtAllCycleNodesByVertexNo))
              .forEach(nextVertex ->
              {
                if (missingSquaresByEdges[currentVertex.getVertexNo()][nextVertex.getVertexNo()] == null
                        && missingSquaresByEdges[nextVertex.getVertexNo()][currentVertex.getVertexNo()] == null)
                {
                  return;
                }

                if (noSquareAtAllCycleNodesByVertexNo[nextVertex.getVertexNo()] == null)
                {
                  noSquareAtAllCycleNodesByVertexNo[nextVertex.getVertexNo()] = new NoSquareAtAllCycleNode(nextVertex, currentVertexNode.getDistance() + 1);
                  nextVertices.add(nextVertex);
                }

                NoSquareAtAllCycleNode nextVertexNode = noSquareAtAllCycleNodesByVertexNo[nextVertex.getVertexNo()];
                if (nextVertexNode.getDistance() == currentVertexNode.getDistance() + 1)
                {
                  nextVertexNode.getPreviousVerticesNodes().add(currentVertexNode);
                }
              });
    }

    List<List<NoSquareAtAllCycleNode>> correctCycles = new LinkedList<>();
    List<NoSquareAtAllCycleNode> currentCycle = new LinkedList<>();
    Set<Integer> collectedMappedColors = new HashSet<>();
    Set<Integer> collectedGroups = new HashSet<>();

    processCycle(noSquareAtAllCycleNodesByVertexNo[firstVertex.getVertexNo()], noSquareAtAllCycleNodesByVertexNo[endVertex.getVertexNo()],
            correctCycles, currentCycle, collectedMappedColors, collectedGroups, groupedNoSquareAtAllEdges, groupNumbersForNoSquareAtAllEdgesEndpoints);
    return correctCycles;
  }

  private boolean includeVerticesDifferentThanEndVertex(Vertex currentVertex, Vertex nextVertex, Vertex endVertex, NoSquareAtAllCycleNode[] noSquareAtAllCycleNodesByVertexNo)
  {
    return !(nextVertex == endVertex && noSquareAtAllCycleNodesByVertexNo[currentVertex.getVertexNo()].getDistance() == 0);
  }

  private void processCycle(NoSquareAtAllCycleNode endVertexNode, NoSquareAtAllCycleNode currentVertexNode, List<List<NoSquareAtAllCycleNode>> correctCycles, List<NoSquareAtAllCycleNode> currentCycle,
                            Set<Integer> collectedMappedColors, Set<Integer> collectedGroups, List<List<Edge>> groupedNoSquareAtAllEdges, Integer[] groupNumbersForNoSquareAtAllEdgesEndpoints)
  {
    currentCycle.add(currentVertexNode);
    collectedGroups.add(groupNumbersForNoSquareAtAllEdgesEndpoints[currentVertexNode.getVertex().getVertexNo()]);

    if (currentVertexNode == endVertexNode)
    {
      collectedGroups.remove(null);
      if (groupedNoSquareAtAllEdges.size() == collectedGroups.size())
      {
        if (groupedNoSquareAtAllEdges.size() > 1 ||
                (groupedNoSquareAtAllEdges.get(0).size() >= 6 ||
                        (groupedNoSquareAtAllEdges.get(0).size() < 6 & collectedMappedColors.size() > 1)))
        {
          correctCycles.add(currentCycle);
        }
      }
    }
    else
    {
      for (int i = 0; i < currentVertexNode.getPreviousVerticesNodes().size(); i++)
      {
        NoSquareAtAllCycleNode previousVertexNode = currentVertexNode.getPreviousVerticesNodes().get(i);

        List<NoSquareAtAllCycleNode> iterationCurrentCycle;
        Set<Integer> iterationCollectedMappedColors;
        Set<Integer> iterationCollectedGroups;

        if (i < currentVertexNode.getPreviousVerticesNodes().size() - 1)
        {
          iterationCurrentCycle = new LinkedList<>(currentCycle);
          iterationCollectedMappedColors = new HashSet<>(collectedMappedColors);
          iterationCollectedGroups = new HashSet<>(collectedGroups);
        }
        else
        {
          iterationCurrentCycle = currentCycle;
          iterationCollectedMappedColors = collectedMappedColors;
          iterationCollectedGroups = collectedGroups;
        }


        Edge edge = graph.getAdjacencyMatrix()[currentVertexNode.getVertex().getVertexNo()][previousVertexNode.getVertex().getVertexNo()];
        if (edge.getLabel().getName() != -2)
        {
          int edgeMappedColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), edge.getLabel().getColor());
          iterationCollectedMappedColors.add(edgeMappedColor);
        }

        processCycle(endVertexNode, previousVertexNode, correctCycles, iterationCurrentCycle, iterationCollectedMappedColors, iterationCollectedGroups, groupedNoSquareAtAllEdges, groupNumbersForNoSquareAtAllEdgesEndpoints);
      }
    }
  }

  private List<MissingSquaresUniqueEdgesData> splitCyclesIntoMissingSquares(List<List<NoSquareAtAllCycleNode>> correctCycles)
  {
    List<MissingSquaresUniqueEdgesData> correctNoSquareAtAllMissingSquares = new LinkedList<>();
    for (List<NoSquareAtAllCycleNode> correctCycle : correctCycles)
    {
      for (int i = 0; i < 6; i++)
      {
        Edge firstEdge = graph.getAdjacencyMatrix()[correctCycle.get(i).getVertex().getVertexNo()][correctCycle.get(i + 1).getVertex().getVertexNo()];
        Edge secondEdge = graph.getAdjacencyMatrix()[correctCycle.get(i + 1).getVertex().getVertexNo()][correctCycle.get(i + 2).getVertex().getVertexNo()];

        int firstEdgeMappedColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), firstEdge.getLabel().getColor());
        int secondEdgeMappedColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), secondEdge.getLabel().getColor());

        if (firstEdgeMappedColor == secondEdgeMappedColor)
        {
          for (int j = i; j < i + 8; j += 2)
          {
            int missingSquareEdgesStartIndex = (j + 8 - 1) % 8;
            int missingSquareEdgesMiddleIndex = j % 8;
            int missingSquareEdgesEndIndex = (j + 1) % 8;

            int missingSquareEdgesStartVertexNo = correctCycle.get(missingSquareEdgesStartIndex).getVertex().getVertexNo();
            int missingSquareEdgesMiddleVertexNo = correctCycle.get(missingSquareEdgesMiddleIndex).getVertex().getVertexNo();
            int missingSquareEdgesEndVertexNo = correctCycle.get(missingSquareEdgesEndIndex).getVertex().getVertexNo();

            Edge firstMissingSquareEdge = graph.getAdjacencyMatrix()[missingSquareEdgesMiddleVertexNo][missingSquareEdgesStartVertexNo];
            Edge secondMissingSquareEdge = graph.getAdjacencyMatrix()[missingSquareEdgesMiddleVertexNo][missingSquareEdgesEndVertexNo];

            MissingSquaresUniqueEdgesData correctNoSquareAtAllMissingSquare = new MissingSquaresUniqueEdgesData(firstMissingSquareEdge, secondMissingSquareEdge);
            correctNoSquareAtAllMissingSquares.add(correctNoSquareAtAllMissingSquare);
          }
          break;
        }
      }
    }
    return correctNoSquareAtAllMissingSquares;
  }

  private List<MissingSquaresUniqueEdgesData> filterOutCorrectSingleNoSquareAtAllMissingSquares(List<MissingSquaresUniqueEdgesData> noSquareAtAllMissingSquares, SquareReconstructionData squareReconstructionData)
  {
    List<MissingSquaresUniqueEdgesData> correctSingleNoSquareAtAllMissingSquares = new LinkedList<>();

    for (MissingSquaresUniqueEdgesData noSquareAtAllMissingSquare : noSquareAtAllMissingSquares)
    {
      Edge normallyColoredEdge;

      if (noSquareAtAllMissingSquare.getBaseEdge().getLabel().getName() == -2)
      {
        normallyColoredEdge = noSquareAtAllMissingSquare.getOtherEdge();
      }
      else
      {
        normallyColoredEdge = noSquareAtAllMissingSquare.getBaseEdge();
      }
      Edge otherEdgeOpposite = normallyColoredEdge.getOpposite();

      List<Edge> sameColorToNormallyColoredEdgesHavingMissingSquares = normallyColoredEdge.getEndpoint().getEdges().stream()
              .filter(edge -> edge != otherEdgeOpposite)
              .filter(edge -> coloringService.getCurrentColorMapping(graph.getGraphColoring(), edge.getLabel().getColor())
                      == coloringService.getCurrentColorMapping(graph.getGraphColoring(), normallyColoredEdge.getLabel().getColor()))
              .map(Edge::getOpposite)
              .filter(edge -> squareReconstructionData.getMissingSquaresData()
                      .getMissingSquaresEntriesByBaseEdge()[edge.getOrigin().getVertexNo()][edge.getEndpoint().getVertexNo()] != null)
              .collect(Collectors.toList());

      if (CollectionUtils.isNotEmpty(sameColorToNormallyColoredEdgesHavingMissingSquares))
      {
        correctSingleNoSquareAtAllMissingSquares.add(noSquareAtAllMissingSquare);
      }
    }
    return correctSingleNoSquareAtAllMissingSquares;
  }
}
