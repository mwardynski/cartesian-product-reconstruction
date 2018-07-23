package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.FactorsFromIntervalReconstructionService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.SingleSquareReconstructionService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class FactorsFromIntervalReconstructionServiceImpl implements FactorsFromIntervalReconstructionService
{

  @Autowired
  Graph graph;

  @Autowired
  ColoringService coloringService;

  @Autowired
  SingleSquareReconstructionService singleSquareReconstructionService;

  @Autowired
  GraphHelper graphHelper;

  @Override
  public void reconstructUsingIntervalFactors(Vertex intervalRoot)
  {
    Edge[][] adjacencyMatrix = graphHelper.createAdjacencyMatrix();
    IntervalEdgeReconstructionData[][] missingSquareMatrix = new IntervalEdgeReconstructionData[adjacencyMatrix.length][adjacencyMatrix.length];
    List<IntervalEdgeReconstructionData> missingSquareEntries = new LinkedList<>();
    List<Integer> factorsColors = coloringService.getColorsForEdges(graph.getGraphColoring(), intervalRoot.getEdges());

    Edge[][][] squareFormingEdgesByIntervalEdges = new Edge[graph.getVertices().size()][graph.getVertices().size()][graph.getVertices().size()];

    SpreadingIntervalVerticesCollection spreadingIntervalVerticesCollection = new SpreadingIntervalVerticesCollection(graph.getVertices().size());

    collectMissingSquareEntries(intervalRoot, factorsColors, adjacencyMatrix, missingSquareMatrix, missingSquareEntries,
            spreadingIntervalVerticesCollection, squareFormingEdgesByIntervalEdges);

    if (CollectionUtils.isNotEmpty(spreadingIntervalVerticesCollection.getVertices()))
    {
      Queue<Vertex> spreadingIntervalVerticesQueue = new LinkedList<>(spreadingIntervalVerticesCollection.getVertices());
      spreadingIntervalVerticesCollection.setVertices(new LinkedList<>());

      while (!spreadingIntervalVerticesQueue.isEmpty())
      {
        Vertex vertex = spreadingIntervalVerticesQueue.poll();

        boolean anyUncoloredEdgesExist = hasVertexAnyNotLabeledEdges(vertex);

        if (anyUncoloredEdgesExist)
        {
          collectMissingSquareEntries(vertex, factorsColors, adjacencyMatrix, missingSquareMatrix, missingSquareEntries,
                  spreadingIntervalVerticesCollection, squareFormingEdgesByIntervalEdges);

          spreadingIntervalVerticesQueue.addAll(spreadingIntervalVerticesCollection.getVertices());
          spreadingIntervalVerticesCollection.setVertices(new LinkedList<>());
        }
      }
    }


    if (CollectionUtils.isNotEmpty(missingSquareEntries))
    {
      int currentFactorColor = -1;
      List<Integer> otherFactorsColors = null;

      List<Edge> notMatchingEdges = new LinkedList<>();

      for (IntervalEdgeReconstructionData missingSquareEntry : missingSquareEntries)
      {
        if (missingSquareEntry.isChecked())
        {
          continue;
        }

        missingSquareEntry.setChecked(true);
        Edge intervalColorEdge = missingSquareEntry.getIntervalColorEdge();

        if (intervalColorEdge.getLabel().getColor() != currentFactorColor)
        {
          currentFactorColor = intervalColorEdge.getLabel().getColor();
          otherFactorsColors = collectOtherFactorsColors(currentFactorColor, factorsColors);
        }

        for (Integer otherFactorColor : otherFactorsColors)
        {
          Edge[] intervalColorEdgesByOriginVertex = new Edge[graph.getVertices().size()];
          intervalColorEdgesByOriginVertex[intervalColorEdge.getOrigin().getVertexNo()] = intervalColorEdge;

          List<Edge> otherColorIntervalFactorEdges = graphHelper.getGraphConnectedComponentEdgesForColor(intervalColorEdge.getOrigin(), graph.getVertices(), Optional.of(otherFactorColor), adjacencyMatrix);

          //FIXME not sure if it's really needed
          int[] missingSquareEntriesDistances = new int[graph.getVertices().size()];
          missingSquareEntriesDistances[intervalColorEdge.getOrigin().getVertexNo()] = 0;


          for (Edge otherColorIntervalFactorEdge : otherColorIntervalFactorEdges)
          {
            Edge primaryEdge = intervalColorEdgesByOriginVertex[otherColorIntervalFactorEdge.getOrigin().getVertexNo()];
            if (primaryEdge == null)
            {
              System.out.println(String.format("Wrong missingSquareEntry: %s, otherColorIntervalFactorEdges:", intervalColorEdge + missingSquareEntry.getMissingSquareEdges().toString(), otherColorIntervalFactorEdges));
              break;
            }
            missingSquareEntriesDistances[otherColorIntervalFactorEdge.getEndpoint().getVertexNo()] = missingSquareEntriesDistances[otherColorIntervalFactorEdge.getOrigin().getVertexNo()] + 1;

            Optional<Edge> correspondingEdgeOptional = findCorrespondingEdgeToPrimaryEdge(primaryEdge, otherColorIntervalFactorEdge.getEndpoint(), adjacencyMatrix, intervalColorEdgesByOriginVertex);
            if (correspondingEdgeOptional.isPresent())
            {
              Edge correspondingEdge = correspondingEdgeOptional.get();
              intervalColorEdgesByOriginVertex[correspondingEdge.getOrigin().getVertexNo()] = correspondingEdge;

              IntervalEdgeReconstructionData correspondingEdgeMissingSquareEntry = missingSquareMatrix[correspondingEdge.getOrigin().getVertexNo()][correspondingEdge.getEndpoint().getVertexNo()];

              if (correspondingEdgeMissingSquareEntry != null)
              {
                correspondingEdgeMissingSquareEntry.setChecked(true);

                int missingSquareEntriesDistance = missingSquareEntriesDistances[otherColorIntervalFactorEdge.getEndpoint().getVertexNo()];
                missingSquareEntriesDistances[otherColorIntervalFactorEdge.getEndpoint().getVertexNo()] = 0;

                if (missingSquareEntriesDistance == 1)
                {
                  List<Edge> tmpList = findNotMatchingEdges(missingSquareEntry.getMissingSquareEdges(), correspondingEdgeMissingSquareEntry.getMissingSquareEdges(), squareFormingEdgesByIntervalEdges,
                          primaryEdge.getOrigin().getVertexNo(), otherColorIntervalFactorEdge.getEndpoint().getVertexNo());
                  notMatchingEdges.addAll(tmpList);
                  missingSquareEntry.setChecked(true);

                  tmpList = findNotMatchingEdges(correspondingEdgeMissingSquareEntry.getMissingSquareEdges(), missingSquareEntry.getMissingSquareEdges(), squareFormingEdgesByIntervalEdges,
                          otherColorIntervalFactorEdge.getEndpoint().getVertexNo(), intervalColorEdge.getOrigin().getVertexNo());
                  notMatchingEdges.addAll(tmpList);
                  correspondingEdgeMissingSquareEntry.setChecked(true);

                  missingSquareEntry = correspondingEdgeMissingSquareEntry;
                }
                else if (missingSquareEntriesDistance == 2)
                {
                  //store potential reconstruction
                }
              }
            }
          }

        }

      }
    }


    return;
  }

  private Optional<Edge> findCorrespondingEdgeToPrimaryEdge(Edge primaryEdge, Vertex otherVertex, Edge[][] adjacencyMatrix, Edge[] intervalColorEdgesByOriginVertex)
  {
    Optional<Edge> correspondingEdgeOptional = Optional.ofNullable(intervalColorEdgesByOriginVertex[otherVertex.getVertexNo()]);

    if (!correspondingEdgeOptional.isPresent())
    {
      correspondingEdgeOptional = otherVertex.getEdges().stream()
              .filter(e -> e.getLabel().getColor() == primaryEdge.getLabel().getColor() &&
                      adjacencyMatrix[e.getEndpoint().getVertexNo()][primaryEdge.getEndpoint().getVertexNo()] != null).findAny();
    }

    return correspondingEdgeOptional;
  }

  private List<Edge> findNotMatchingEdges(List<Edge> edges, List<Edge> correspondingEdges, Edge[][][] squreFormingEdgesByIntervalEdges,
                                          int intervalFactorVertex1No, int intervalFactorVertex2No)
  {
    Edge[] correspondingEdgesAdjacencyVector = new Edge[graph.getVertices().size()];
    correspondingEdges.stream().forEach(e -> correspondingEdgesAdjacencyVector[e.getEndpoint().getVertexNo()] = e);

    List<Edge> notMachingEdges = edges.stream()
            .filter(e ->
            {
              Edge correspondingEdge = squreFormingEdgesByIntervalEdges[intervalFactorVertex1No][intervalFactorVertex2No][e.getEndpoint().getVertexNo()];
              if (correspondingEdge == null || correspondingEdgesAdjacencyVector[correspondingEdge.getEndpoint().getVertexNo()] == null)
              {
                return true;
              }
              else
              {
                return false;
              }
            }).collect(Collectors.toList());

    return notMachingEdges;
  }

  private void collectMissingSquareEntries(Vertex intervalRoot, List<Integer> factorsColors, Edge[][] adjacencyMatrix,
                                           IntervalEdgeReconstructionData[][] missingSquareMatrix, List<IntervalEdgeReconstructionData> missingSquareEntries,
                                           SpreadingIntervalVerticesCollection spreadingIntervalVerticesCollection, Edge[][][] squareFormingEdgesByIntervalEdges)
  {
    int maxColor = factorsColors.stream().mapToInt(i -> i.intValue()).max().getAsInt();
    boolean[] intervalColorsVector = new boolean[maxColor + 1];
    factorsColors.forEach(color -> intervalColorsVector[color] = true);

    for (Integer currentFactorColor : factorsColors)
    {
      //FIXME propagate the new amount of colors, not the original one
      SquareReconstructionData squareReconstructionData = new SquareReconstructionData(graph.getGraphColoring().getOriginalColorsAmount(), graph.getVertices().size());
      squareReconstructionData.setSquareFormingEdges(squareFormingEdgesByIntervalEdges);
      squareReconstructionData.setMultipleSquaresWardenEdge(new Edge(graph.getRoot(), graph.getRoot()));
      List<Integer> otherFactorsColors = collectOtherFactorsColors(currentFactorColor, factorsColors);

      int lastOtherColorIndex = otherFactorsColors.size() - 1;
      int lastOtherColor = otherFactorsColors.get(lastOtherColorIndex);
      List<Integer> remainingColors = otherFactorsColors.subList(0, lastOtherColorIndex);

      List<Vertex> delayedProcessedVertices = new LinkedList<>();

      graphHelper.traverseBfsGivenColors(intervalRoot, graph.getVertices(), lastOtherColor, remainingColors,
              startVertex -> processFactorOfGivenColor(startVertex, currentFactorColor, intervalColorsVector, adjacencyMatrix, missingSquareMatrix,
                      missingSquareEntries, squareReconstructionData, spreadingIntervalVerticesCollection, delayedProcessedVertices, true));

    }
  }

  private List<Integer> collectOtherFactorsColors(int currentFactorColor, List<Integer> factorsColors)
  {
    return factorsColors.stream().filter(color -> !color.equals(currentFactorColor)).collect(Collectors.toList());
  }


  private void processFactorOfGivenColor(Vertex startVertex, int currentFactorColor, boolean[] intervalColorsVector, Edge[][] adjacencyMatrix, IntervalEdgeReconstructionData[][] missingSquareMatrix,
                                         List<IntervalEdgeReconstructionData> missingSquareEntries, SquareReconstructionData squareReconstructionData, SpreadingIntervalVerticesCollection spreadingIntervalVerticesCollection,
                                         List<Vertex> delayedProcessedVertices, boolean processDelayedVertices)
  {
    List<Edge> intervalFactorEdges = graphHelper.getGraphConnectedComponentEdgesForColor(startVertex, graph.getVertices(), Optional.of(currentFactorColor), adjacencyMatrix);
    if (CollectionUtils.isNotEmpty(intervalFactorEdges))
    {
      intervalFactorEdges.add(0, intervalFactorEdges.get(0).getOpposite());
      for (Edge intervalFactorEdge : intervalFactorEdges)
      {
        intervalFactorEdge = intervalFactorEdge.getOpposite();

        List<Edge> checkedEdges = new LinkedList<>();
        List<Edge> missingSquareEdges = new LinkedList<>();


        checkNotIntervalEdgesOfIntervalEdge(intervalFactorEdge, intervalColorsVector, squareReconstructionData,
                spreadingIntervalVerticesCollection, checkedEdges, missingSquareEdges);

        if (!missingSquareEdges.isEmpty())
        {
          storeIntervalEdgeWithoutSquare(intervalFactorEdge, missingSquareEdges, missingSquareMatrix, missingSquareEntries);
        }
      }
    }

    if (processDelayedVertices)
    {
      Iterator<Vertex> delayedProcessedVerticesIterator = delayedProcessedVertices.iterator();
      while (delayedProcessedVerticesIterator.hasNext())
      {
        Vertex delayedPRocessedVertex = delayedProcessedVerticesIterator.next();

        if (!hasVertexAnyNotLabeledEdges(delayedPRocessedVertex))
        {
          delayedProcessedVerticesIterator.remove();

          processFactorOfGivenColor(delayedPRocessedVertex, currentFactorColor, intervalColorsVector, adjacencyMatrix, missingSquareMatrix,
                  missingSquareEntries, squareReconstructionData, spreadingIntervalVerticesCollection, delayedProcessedVertices, false);
        }
      }
    }
    if (hasVertexAnyNotLabeledEdges(startVertex))
    {
      delayedProcessedVertices.add(startVertex);
    }
  }

  private boolean hasVertexAnyNotLabeledEdges(Vertex vertex)
  {
    return vertex.getEdges().stream().filter(e -> e.getLabel() == null).findAny().isPresent();
  }

  private void checkNotIntervalEdgesOfIntervalEdge(Edge intervalFactorEdge, boolean[] intervalColorsVector, SquareReconstructionData squareReconstructionData,
                                                   SpreadingIntervalVerticesCollection spreadingIntervalVerticesCollection, List<Edge> checkedEdges, List<Edge> missingSquareEdges)
  {
    Vertex intervalFactorEdgeVertex = intervalFactorEdge.getOrigin();
    squareReconstructionData.setCurrentVertex(intervalFactorEdgeVertex);

    for (Edge edge : intervalFactorEdgeVertex.getEdges())
    {
//      if (edge.getLabel() != null && edge.getLabel().getColor() < intervalColorsVector.length && intervalColorsVector[edge.getLabel().getColor()])
      if (edge.getLabel() != null && edge.getLabel().getColor() == intervalFactorEdge.getLabel().getColor())
      {
        continue;
      }
      checkedEdges.add(edge);
      boolean squareFound = singleSquareReconstructionService.findSquareForTwoEdges(squareReconstructionData, intervalFactorEdge, edge);
//      squareReconstructionData.getUsedEdges()[edge.getOrigin().getVertexNo()][edge.getEndpoint().getVertexNo()] = true;
//      squareReconstructionData.getUsedEdges()[edge.getEndpoint().getVertexNo()][edge.getOrigin().getVertexNo()] = true;

      if (!spreadingIntervalVerticesCollection.getVerticesOccurance()[edge.getEndpoint().getVertexNo()])
      {
        spreadingIntervalVerticesCollection.getVerticesOccurance()[edge.getOrigin().getVertexNo()] = true;
        spreadingIntervalVerticesCollection.getVerticesOccurance()[edge.getEndpoint().getVertexNo()] = true;
        spreadingIntervalVerticesCollection.getVertices().add(edge.getEndpoint());
      }

      //add vertex of the edge to the new vertices
      if (!squareFound)
      {
        missingSquareEdges.add(edge);
      }
    }
  }

  private void storeIntervalEdgeWithoutSquare(Edge intervalFactorEdge, List<Edge> missingSquareEdges, IntervalEdgeReconstructionData[][] missingSquareMatrix, List<IntervalEdgeReconstructionData> missingSquareEntries)
  {
    //FIXME check for existance - avoid duplicates
    IntervalEdgeReconstructionData intervalEdgeReconstructionData = new IntervalEdgeReconstructionData();
    intervalEdgeReconstructionData.setIntervalColorEdge(intervalFactorEdge);
    intervalEdgeReconstructionData.setMissingSquareEdges(missingSquareEdges);

    missingSquareEntries.add(intervalEdgeReconstructionData);
    missingSquareMatrix[intervalFactorEdge.getOrigin().getVertexNo()][intervalFactorEdge.getEndpoint().getVertexNo()] = intervalEdgeReconstructionData;
  }

  private List<Vertex> provideNewVerticesForColor(int currentColorIndex, List<Integer> otherFactorsColors, List<List<Vertex>> counterBfsVertices)
  {
    List<Vertex> resultVertices = Collections.emptyList();
    int nextColorIndex = currentColorIndex + 1;
    if (counterBfsVertices.size() > nextColorIndex)
    {
      List<Vertex> nextColorVertices = counterBfsVertices.get(nextColorIndex);
      Iterator<Vertex> nextColorVerticesIt = nextColorVertices.iterator();
      nextColorVerticesIt.next();
      nextColorVerticesIt.remove();

      if (!nextColorVerticesIt.hasNext())
      {
        List<Vertex> updatedBfsVertices = provideNewVerticesForColor(nextColorIndex, otherFactorsColors, counterBfsVertices);
        counterBfsVertices.set(nextColorIndex, updatedBfsVertices);
        nextColorVerticesIt = updatedBfsVertices.iterator();
      }


      if (nextColorVerticesIt.hasNext())
      {
        Vertex nextVertex = nextColorVerticesIt.next();
        resultVertices = graphHelper.getGraphConnectedComponentVerticesForColor(nextVertex, graph.getVertices(), Optional.of(otherFactorsColors.get(currentColorIndex)));
        resultVertices.add(0, nextVertex);
      }
    }
    return resultVertices;
  }

}
