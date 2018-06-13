package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.FactorsFromIntervalReconstructionService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.SingleSquareReconstructionService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    List<Integer> factorColors = coloringService.getColorsForEdges(graph.getGraphColoring(), intervalRoot.getEdges());
    int maxColor = factorColors.stream().mapToInt(i -> i.intValue()).max().getAsInt();
    List<Integer> intervalFactorSizes = IntStream.range(0, maxColor + 1).mapToObj(i -> 0).collect(Collectors.toList());
    boolean[] intervalColorsVector = new boolean[maxColor + 1];
    factorColors.forEach(color -> intervalColorsVector[color] = true);

    Edge[][] adjacencyMatrix = graphHelper.createAdjacencyMatrix();
    IntervalEdgeReconstructionData[][] missingSquareMatrix = new IntervalEdgeReconstructionData[adjacencyMatrix.length][adjacencyMatrix.length];
    List<IntervalEdgeReconstructionData> missingSquareEntries = new LinkedList<>();
    factorColors.forEach(color ->
    {
      List<Vertex> graphConnectedComponentVerticesForColor = graphHelper.getGraphConnectedComponentVerticesForColor(intervalRoot, graph.getVertices(), Optional.of(color));
      intervalFactorSizes.set(color, graphConnectedComponentVerticesForColor.size());
    });

    for (Integer currentFactorColor : factorColors)
    {
      SquareReconstructionData squareReconstructionData = new SquareReconstructionData(graph.getGraphColoring().getOriginalColorsAmount(), graph.getVertices().size());
      List<Integer> otherFactorColors = factorColors.stream().filter(color -> !color.equals(currentFactorColor)).collect(Collectors.toList());
      List<List<Vertex>> counterBfsVertices = otherFactorColors.stream()
              .map(color -> graphHelper.getGraphConnectedComponentVerticesForColor(intervalRoot, graph.getVertices(), Optional.of(color)))
              .map(bfsOrderedVertices ->
              {
                bfsOrderedVertices.add(0, intervalRoot);
                return bfsOrderedVertices;
              })
              .collect(Collectors.toList());

      Iterator<Vertex> firstFactorIt = counterBfsVertices.get(0).iterator();
      while (firstFactorIt.hasNext())
      {
        Vertex startVertex = firstFactorIt.next();

        processFactorOfGivenColor(startVertex, currentFactorColor, intervalColorsVector, adjacencyMatrix, missingSquareMatrix,
                missingSquareEntries, squareReconstructionData);

        firstFactorIt.remove();
        if (!firstFactorIt.hasNext())
        {
          List<Vertex> newVertices = provideNewVerticesForColor(0, otherFactorColors, counterBfsVertices);
          firstFactorIt = newVertices.iterator();
        }
      }
      this.getClass();
    }
    return;
  }


  private void processFactorOfGivenColor(Vertex startVertex, int color, boolean[] intervalColorsVector, Edge[][] adjacencyMatrix, IntervalEdgeReconstructionData[][] missingSquareMatrix,
                                         List<IntervalEdgeReconstructionData> missingSquareEntries, SquareReconstructionData squareReconstructionData)
  {
    List<Edge> intervalFactorEdges = graphHelper.getGraphConnectedComponentEdgesForColor(startVertex, graph.getVertices(), Optional.of(color), adjacencyMatrix);
    for (Edge intervalFactorEdge : intervalFactorEdges)
    {

      List<Edge> checkedEdges = new LinkedList<>();
      List<Edge> missingSquareEdges = new LinkedList<>();


      checkNotIntervalEdgesOfIntervalEdge(intervalFactorEdge, intervalColorsVector, squareReconstructionData, checkedEdges, missingSquareEdges, true);
      checkNotIntervalEdgesOfIntervalEdge(intervalFactorEdge, intervalColorsVector, squareReconstructionData, checkedEdges, missingSquareEdges, false);

      if (checkedEdges.isEmpty() || !missingSquareEdges.isEmpty())
      {
        storeIntervalEdgeWithoutSquare(intervalFactorEdge, missingSquareEdges, missingSquareMatrix, missingSquareEntries);
      }
    }
  }

  private void checkNotIntervalEdgesOfIntervalEdge(Edge intervalFactorEdge, boolean[] intervalColorsVector,
                                                   SquareReconstructionData squareReconstructionData, List<Edge> checkedEdges, List<Edge> missingSquareEdges,
                                                   boolean firstRun)
  {
    if (!firstRun)
    {
      intervalFactorEdge = intervalFactorEdge.getOpposite();
    }
    Vertex intervalFactorEdgeVertex = intervalFactorEdge.getOrigin();
    squareReconstructionData.setCurrentVertex(intervalFactorEdgeVertex);

    for (Edge edge : intervalFactorEdgeVertex.getEdges())
    {
      if (edge.getLabel() != null && edge.getLabel().getColor() < intervalColorsVector.length && intervalColorsVector[edge.getLabel().getColor()])
      {
        continue;
      }
      checkedEdges.add(edge);
      boolean squareFound = singleSquareReconstructionService.findSquareForTwoEdges(squareReconstructionData, intervalFactorEdge, edge);
      if (firstRun)
      {
        squareReconstructionData.getUsedEdges()[edge.getOrigin().getVertexNo()][edge.getEndpoint().getVertexNo()] = true;
        squareReconstructionData.getUsedEdges()[edge.getEndpoint().getVertexNo()][edge.getOrigin().getVertexNo()] = true;
      }
      if (!squareFound)
      {
        missingSquareEdges.add(edge);
      }
    }
  }

  private void storeIntervalEdgeWithoutSquare(Edge intervalFactorEdge, List<Edge> missingSquareEdges, IntervalEdgeReconstructionData[][] missingSquareMatrix, List<IntervalEdgeReconstructionData> missingSquareEntries)
  {
    IntervalEdgeReconstructionData intervalEdgeReconstructionData = new IntervalEdgeReconstructionData();
    intervalEdgeReconstructionData.setOriginEdge(intervalFactorEdge);
    intervalEdgeReconstructionData.setMissingSquareEdges(missingSquareEdges);
    intervalEdgeReconstructionData.setIncorrect(true);

    missingSquareEntries.add(intervalEdgeReconstructionData);
    missingSquareMatrix[intervalFactorEdge.getOrigin().getVertexNo()][intervalFactorEdge.getEndpoint().getVertexNo()] = intervalEdgeReconstructionData;
  }

  private List<Vertex> provideNewVerticesForColor(int currentColorIndex, List<Integer> otherFactorColors, List<List<Vertex>> counterBfsVertices)
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
        List<Vertex> updatedBfsVertices = provideNewVerticesForColor(nextColorIndex, otherFactorColors, counterBfsVertices);
        counterBfsVertices.set(nextColorIndex, updatedBfsVertices);
        nextColorVerticesIt = updatedBfsVertices.iterator();
      }


      if (nextColorVerticesIt.hasNext())
      {
        Vertex nextVertex = nextColorVerticesIt.next();
        resultVertices = graphHelper.getGraphConnectedComponentVerticesForColor(nextVertex, graph.getVertices(), Optional.of(otherFactorColors.get(currentColorIndex)));
        resultVertices.add(0, nextVertex);
      }
    }
    return resultVertices;
  }

}
