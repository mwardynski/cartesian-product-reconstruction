package at.ac.unileoben.mat.dissertation.reconstruction.services.impl.uncoloredpart;

import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.uncoloredpart.PartOfCycleNoSquareAtAllMissingSquaresFindingService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.uncoloredpart.PartOfCycleNoSquareAtAllMissingSquaresGroupingService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.uncoloredpart.UncoloredEdgesHandlerService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Stream;

@Component
public class PartOfCycleNoSquareAtAllMissingSquaresFindingServiceImpl implements PartOfCycleNoSquareAtAllMissingSquaresFindingService
{
  private static final int CYCLE_LENGTH = 8;

  @Autowired
  private Environment environment;

  @Autowired
  Graph graph;

  @Autowired
  ColoringService coloringService;

  @Autowired
  UncoloredEdgesHandlerService uncoloredEdgesHandlerService;

  @Autowired
  PartOfCycleNoSquareAtAllMissingSquaresGroupingService partOfCycleNoSquareAtAllMissingSquaresGroupingService;

  public List<MissingSquaresUniqueEdgesData> findCorrectPartOfCycleNoSquareAtAllMissingSquares(List<MissingSquaresUniqueEdgesData> noSquareAtAllMissingSquares, SquareReconstructionData squareReconstructionData)
  {
    NoSquareAtAllGroupsData noSquareAtAllGroupsData = partOfCycleNoSquareAtAllMissingSquaresGroupingService.splitPartOfCycleNoSquareAtAllMissingSquaresIntoGroups(noSquareAtAllMissingSquares);
    return findCycleForNoSquareAtAllGroups(noSquareAtAllGroupsData, squareReconstructionData);
  }

  private List<MissingSquaresUniqueEdgesData> findCycleForNoSquareAtAllGroups(NoSquareAtAllGroupsData noSquareAtAllGroupsData, SquareReconstructionData squareReconstructionData)
  {
    List<List<Edge>> groupedNoSquareAtAllEdges = noSquareAtAllGroupsData.getGroupedNoSquareAtAllEdges();

    NoSquareAtAllCycleNode[] noSquareAtAllCycleNodesByVertexNo = new NoSquareAtAllCycleNode[graph.getVertices().size()];
    Edge arbitraryGroupFirstEdge = groupedNoSquareAtAllEdges.get(0).get(0);

    List<List<NoSquareAtAllCycleNode>> correctCycles = findCycleUsingBfs(arbitraryGroupFirstEdge, groupedNoSquareAtAllEdges, noSquareAtAllGroupsData.getGroupNumbersForNoSquareAtAllEdgesEndpoints(), squareReconstructionData, noSquareAtAllCycleNodesByVertexNo);

    List<MissingSquaresUniqueEdgesData> correctNoSquareAtAllMissingSquares = splitCyclesIntoMissingSquares(correctCycles, noSquareAtAllCycleNodesByVertexNo);
    return correctNoSquareAtAllMissingSquares;
  }

  @Override
  public List<List<NoSquareAtAllCycleNode>> findCycleUsingBfs(Edge arbitraryGroupFirstEdge, List<List<Edge>> groupedNoSquareAtAllEdges, Integer[] groupNumbersForNoSquareAtAllEdgesEndpoints, SquareReconstructionData squareReconstructionData, NoSquareAtAllCycleNode[] noSquareAtAllCycleNodesByVertexNo)
  {

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
    UniqueList collectedMappedColors = new UniqueList(graph.getVertices().size());
    UniqueList collectedGroups = new UniqueList(groupedNoSquareAtAllEdges.size());

    processCycle(noSquareAtAllCycleNodesByVertexNo[firstVertex.getVertexNo()], noSquareAtAllCycleNodesByVertexNo[endVertex.getVertexNo()], null,
            correctCycles, currentCycle, collectedMappedColors, collectedGroups, groupedNoSquareAtAllEdges, groupNumbersForNoSquareAtAllEdgesEndpoints);

    if (CollectionUtils.isNotEmpty(correctCycles)
            && isLongestCycleOfLengthSix(noSquareAtAllCycleNodesByVertexNo[endVertex.getVertexNo()]))
    {
      extendCycleNodesForCycleOfLengthEight(noSquareAtAllCycleNodesByVertexNo, correctCycles);

      correctCycles = new LinkedList<>();
      currentCycle = new LinkedList<>();
      collectedMappedColors = new UniqueList(graph.getVertices().size());
      collectedGroups = new UniqueList(groupedNoSquareAtAllEdges.size());

      processCycle(noSquareAtAllCycleNodesByVertexNo[firstVertex.getVertexNo()], noSquareAtAllCycleNodesByVertexNo[endVertex.getVertexNo()], null,
              correctCycles, currentCycle, collectedMappedColors, collectedGroups, groupedNoSquareAtAllEdges, groupNumbersForNoSquareAtAllEdgesEndpoints);
    }


    return correctCycles;
  }

  private boolean isLongestCycleOfLengthSix(NoSquareAtAllCycleNode noSquareAtAllCycleNode)
  {
    return noSquareAtAllCycleNode.getDistance() == 5;
  }

  private void extendCycleNodesForCycleOfLengthEight(NoSquareAtAllCycleNode[] noSquareAtAllCycleNodesByVertexNo, List<List<NoSquareAtAllCycleNode>> cycles)
  {
    NoSquareAtAllCycleNode[] cycleNodesIncludedInCycles = new NoSquareAtAllCycleNode[graph.getVertices().size()];
    cycles.stream()
            .flatMap(cycle -> cycle.stream())
            .forEach(cycleNode -> cycleNodesIncludedInCycles[cycleNode.getVertex().getVertexNo()] = cycleNode);

    for (int vertexNumber = 0; vertexNumber < noSquareAtAllCycleNodesByVertexNo.length; vertexNumber++)
    {
      NoSquareAtAllCycleNode cycleNode = noSquareAtAllCycleNodesByVertexNo[vertexNumber];
      if (cycleNode == null)
      {
        continue;
      }
      if (cycleNodesIncludedInCycles[cycleNode.getVertex().getVertexNo()] == null
              && (cycleNode.getDistance() == 6 || cycleNode.getDistance() == 5 || cycleNode.getDistance() == 3))
      {
        List<NoSquareAtAllCycleNode> neighborCycleNodesInCycles = new LinkedList<>();
        List<NoSquareAtAllCycleNode> neighborCycleNodesOutOfCycles = new LinkedList<>();

        for (NoSquareAtAllCycleNode previousCycleNode : cycleNode.getPreviousVerticesNodes())
        {
          if (cycleNodesIncludedInCycles[previousCycleNode.getVertex().getVertexNo()] != null)
          {
            neighborCycleNodesInCycles.add(previousCycleNode);
          }
          else
          {
            neighborCycleNodesOutOfCycles.add(previousCycleNode);
          }
        }

        if (CollectionUtils.isNotEmpty(neighborCycleNodesInCycles))
        {
          neighborCycleNodesInCycles
                  .forEach(cycleNodeInCycle -> cycleNodeInCycle.getPreviousVerticesNodes().add(cycleNode));
        }
      }

    }
  }

  private boolean includeVerticesDifferentThanEndVertex(Vertex currentVertex, Vertex nextVertex, Vertex endVertex, NoSquareAtAllCycleNode[] noSquareAtAllCycleNodesByVertexNo)
  {
    return !(nextVertex == endVertex && noSquareAtAllCycleNodesByVertexNo[currentVertex.getVertexNo()].getDistance() == 0);
  }

  private void processCycle(NoSquareAtAllCycleNode endVertexNode, NoSquareAtAllCycleNode currentVertexNode, NoSquareAtAllCycleNode recentVertexNode, List<List<NoSquareAtAllCycleNode>> correctCycles, List<NoSquareAtAllCycleNode> currentCycle,
                            UniqueList collectedMappedColors, UniqueList collectedGroups, List<List<Edge>> groupedNoSquareAtAllEdges, Integer[] groupNumbersForNoSquareAtAllEdgesEndpoints)
  {
    if (currentVertexNode == null)
    {
      return;
    }

    currentCycle.add(currentVertexNode);
    collectedGroups.add(groupNumbersForNoSquareAtAllEdgesEndpoints[currentVertexNode.getVertex().getVertexNo()]);

    if (currentVertexNode == endVertexNode)
    {
      boolean missingEdgesProfile = Stream.of(environment.getActiveProfiles())
              .filter(profile -> "missingEdges".equals(profile))
              .findAny().isPresent();
      if (missingEdgesProfile)
      {
        correctCycles.add(currentCycle);
      }
      else if (groupedNoSquareAtAllEdges.size() == collectedGroups.size())
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
        UniqueList iterationCollectedMappedColors;
        UniqueList iterationCollectedGroups;

        if (recentVertexNode != null && recentVertexNode == previousVertexNode)
        {
          continue;
        }

        if (i < currentVertexNode.getPreviousVerticesNodes().size() - 1)
        {
          iterationCurrentCycle = new LinkedList<>(currentCycle);
          iterationCollectedMappedColors = new UniqueList(collectedMappedColors);
          iterationCollectedGroups = new UniqueList(collectedGroups);
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

        processCycle(endVertexNode, previousVertexNode, currentVertexNode, correctCycles, iterationCurrentCycle, iterationCollectedMappedColors, iterationCollectedGroups, groupedNoSquareAtAllEdges, groupNumbersForNoSquareAtAllEdgesEndpoints);
      }
    }
  }

  private List<MissingSquaresUniqueEdgesData> splitCyclesIntoMissingSquares(List<List<NoSquareAtAllCycleNode>> correctCycles, NoSquareAtAllCycleNode[] noSquareAtAllCycleNodesByVertexNo)
  {
    List<MissingSquaresUniqueEdgesData> correctNoSquareAtAllMissingSquares = new LinkedList<>();
    for (List<NoSquareAtAllCycleNode> correctCycle : correctCycles)
    {
      for (int i = 1; i < CYCLE_LENGTH - 1; i++)
      {
        Edge firstEdge = graph.getAdjacencyMatrix()[correctCycle.get(i - 1).getVertex().getVertexNo()][correctCycle.get(i).getVertex().getVertexNo()];
        Edge secondEdge = graph.getAdjacencyMatrix()[correctCycle.get(i).getVertex().getVertexNo()][correctCycle.get(i + 1).getVertex().getVertexNo()];

        int vertexNeighborhoodSizeInCycles = noSquareAtAllCycleNodesByVertexNo[correctCycle.get(i).getVertex().getVertexNo()].getPreviousVerticesNodes().size();

        if (vertexNeighborhoodSizeInCycles > 1 || uncoloredEdgesHandlerService.areNormalEdgesOfGivenColorProperty(firstEdge, secondEdge, true))
        {
          for (int j = i - 1; j < i + CYCLE_LENGTH; j += 2)
          {
            int missingSquareEdgesStartIndex = (j + CYCLE_LENGTH - 1) % CYCLE_LENGTH;
            int missingSquareEdgesMiddleIndex = j % CYCLE_LENGTH;
            int missingSquareEdgesEndIndex = (j + 1) % CYCLE_LENGTH;

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
}
