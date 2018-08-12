package at.ac.unileoben.mat.dissertation.reconstruction.impl;


import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.common.ReconstructionCase;
import at.ac.unileoben.mat.dissertation.config.FactorizationConfig;
import at.ac.unileoben.mat.dissertation.linearfactorization.GraphFactorizationPreparer;
import at.ac.unileoben.mat.dissertation.linearfactorization.LinearFactorization;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.EdgeService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.SingleSquareReconstructionService;
import at.ac.unileoben.mat.dissertation.structure.*;
import at.ac.unileoben.mat.dissertation.structure.exception.CompleteMergeException;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static at.ac.unileoben.mat.dissertation.structure.OperationOnGraph.RECONSTRUCTION_ANALYSIS;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {FactorizationConfig.class})
public class MissingVertexToRootDistanceTest
{

  @Autowired
  Graph graph;

  @Autowired
  ReconstructionData reconstructionData;

  @Autowired
  GraphHelper graphHelper;

  @Autowired
  GraphFactorizationPreparer graphFactorizationPreparer;

  @Autowired
  LinearFactorization linearFactorization;

  @Autowired
  EdgeService edgeService;

  @Autowired
  ColoringService coloringService;

  @Autowired
  SingleSquareReconstructionService singleSquareReconstructionService;

  private final static List<ReconstructionCase> examplesList = new LinkedList<ReconstructionCase>();


  static
  {

    examplesList.add(new ReconstructionCase("K3pExP3.txt", 2, 0, null));
//    examplesList.add(new ReconstructionCase("bP3xK2bpExP3.txt", 2));
//    examplesList.add(new ReconstructionCase("K3xK3.txt", 2));
//    examplesList.add(new ReconstructionCase("K4-ExS2.txt", 2));
//    examplesList.add(new ReconstructionCase("C6xS2.txt", 2));
//    examplesList.add(new ReconstructionCase("cartFactExample.txt", 2));
//    examplesList.add(new ReconstructionCase("C4-ExC4-E.txt", 2));
//    examplesList.add(new ReconstructionCase("handP3.txt", 2));

    //xK2
//    examplesList.add(new ReconstructionCase("K23xK2.txt", 2));
//    examplesList.add(new ReconstructionCase("S2xK2.txt", 2));
//    examplesList.add(new ReconstructionCase("S2xK2xK2.txt", 3));
//    examplesList.add(new ReconstructionCase("P3xK2.txt", 2));
//    examplesList.add(new ReconstructionCase("K23xK2-mirrored.txt", 2));
//    examplesList.add(new ReconstructionCase("K4-ExK2.txt", 2));
//    examplesList.add(new ReconstructionCase("C3xK2xK2.txt", 3));
//    examplesList.add(new ReconstructionCase("C3xK2.txt", 2));
//    examplesList.add(new ReconstructionCase("handP2.txt", 2));

    //not needed:
//    examplesList.add(new ReconstructionCase("victory.txt", 3));
//    examplesList.add(new ReconstructionCase("cd.txt", 1));
//    examplesList.add(new ReconstructionCase("g3", 1));
//    examplesList.add(new ReconstructionCase("newExCart-mod.txt", 1));
//    examplesList.add(new ReconstructionCase("przyklad.txt", 1));
//    examplesList.add(new ReconstructionCase("simpleExample.txt", 1));
//    examplesList.add(new ReconstructionCase("example.txt", 1));
  }


  @Test
  public void runTest()
  {
    reconstructionData.setOperationOnGraph(RECONSTRUCTION_ANALYSIS);
    examplesList.stream().forEach(reconstructionCase -> checkAllCasesForGraph(reconstructionCase));
  }

  private void checkAllCasesForGraph(ReconstructionCase reconstructionCase)
  {
    String graphName = reconstructionCase.getFileName();
    List<Vertex> vertices = graphHelper.parseGraph(graphName);

    for (int vertexNumberToRemove = 0; vertexNumberToRemove < vertices.size(); vertexNumberToRemove++)
    {
      if (reconstructionCase.getVertexNoToRemove() != null && reconstructionCase.getVertexNoToRemove() != vertexNumberToRemove)
      {
        continue;
      }
      int[] distanceVector = calculateDistanceVector(vertices, vertices.get(vertexNumberToRemove));
      graphHelper.revertGraphBfsStructure();

      List<Vertex> incompleteVertices = graphHelper.parseGraph(graphName);
      graphFactorizationPreparer.removeVertex(incompleteVertices, vertexNumberToRemove);

      for (Vertex vertex : incompleteVertices)
      {
        if (reconstructionCase.getRootVertexNo() != null && reconstructionCase.getRootVertexNo() != vertex.getVertexNo())
        {
          continue;
        }
        try
        {
          logCurrentCase(graphName, vertex.getVertexNo(), vertexNumberToRemove);
          linearFactorization.factorize(incompleteVertices, vertex);
          System.out.println(" - vertex missing on TOP of the graph");
        }
        catch (CompleteMergeException completeMergeException)
        {
          graphHelper.revertGraphBfsStructure();
          storeAdjacencyMatrix();
          logDistanceToMissingVertex(vertexNumberToRemove, distanceVector, completeMergeException);
        }
        clearVerticesAndEdges(incompleteVertices);
      }
    }
  }

  private int[] calculateDistanceVector(List<Vertex> vertices, Vertex root)
  {
    graphHelper.prepareGraphBfsStructure(vertices, root);

    int[] distanceVector = new int[vertices.size()];
    vertices.stream().forEach(v ->
    {
      int vOriginalNo = graph.getReverseReindexArray()[v.getVertexNo()];
      distanceVector[vOriginalNo] = v.getBfsLayer();
    });

    return distanceVector;
  }

  private void storeAdjacencyMatrix()
  {
    Edge[][] adjacencyMatrix = graphHelper.createAdjacencyMatrix();
    graph.setAdjacencyMatrix(adjacencyMatrix);
  }

  private void clearVerticesAndEdges(List<Vertex> vertices)
  {
    for (Vertex vertex : vertices)
    {
      vertex.setUnitLayer(false);
      vertex.getEdges().stream().forEach(edgeService::clearEdgeLabeling);
    }
  }

  private void logCurrentCase(String graphPath, int rootNo, int vertexNumberToRemove)
  {
    int rootCorrectedNo = calculateVertexCorrectedNo(vertexNumberToRemove, rootNo);

    System.out.println(String.format("%s, m:%d r:%d(%d)", graphPath, vertexNumberToRemove, rootNo, rootCorrectedNo));
  }

  private void logDistanceToMissingVertex(int vertexNumberToRemove, int[] distanceVector, CompleteMergeException completeMergeException)
  {
    int rootNumber = graph.getRoot().getVertexNo();
    int rootCorrectedNumber = calculateVertexCorrectedNo(vertexNumberToRemove, rootNumber);

    int distanceToRemovedVertex = distanceVector[rootCorrectedNumber];

    System.out.println(String.format(" - expected: layer %d, actual layer: %d(%b), diff=%d",
            distanceToRemovedVertex, completeMergeException.getLayerNo(), completeMergeException.getAfterConsistencyCheck(),
            completeMergeException.getLayerNo() - distanceToRemovedVertex));

    if (CollectionUtils.isNotEmpty(graph.getAnalyzeData().getMergeOperations()))
    {
      String mergeOperationsOutput = prepareMergeOperationsLog(vertexNumberToRemove, distanceVector);
      System.out.println(mergeOperationsOutput);
    }
    else if (graph.getRoot().getEdges().size() == 1)
    {
      Edge singleRootEdge = graph.getRoot().getEdges().get(0);
      System.out.println(String.format(" -- No merges [eQty:1,mC:0,diff:1]: %d-%d[1,2]", singleRootEdge.getOrigin().getVertexNo(), singleRootEdge.getEndpoint().getVertexNo()));
    }
    else
    {
      fail();
    }


  }

  private String prepareMergeOperationsLog(int removedVertexNumber, int[] distanceVector)
  {
    List<MergeOperation> mergeOperations = graph.getAnalyzeData().getMergeOperations();

//    NOT_WORKING - ALL MERGES ARE NECESSARY
//    List<MergeOperation> notPrepareAndLabelDownEdgesMergeOperations = graph.getAnalyzeData().getMergeOperations().stream()
//            .filter(mergeOperation -> mergeOperation.getMergeTag() != MergeTagEnum.LABEL_DOWN && mergeOperation.getMergeTag() != MergeTagEnum.PREPARE)
//            .collect(Collectors.toList());
//    if (CollectionUtils.isNotEmpty(notPrepareAndLabelDownEdgesMergeOperations))
//    {
//      mergeOperations = notPrepareAndLabelDownEdgesMergeOperations;
//    }

//    NOT_WORKING - IT CAN'T BE ONLY THE LAST MERGE
//    MergeOperation lastMergeOperation = mergeOperations.get(mergeOperations.size() - 1);
//    mergeOperations = Collections.singletonList(lastMergeOperation);

    StringBuilder mergeOperationsOutput = new StringBuilder("");

    boolean foundCorrectEdgesPairForMergeOperations = mergeOperations.stream()
            .map(mergeOperation ->
            {
              int[] incidentVertices = new int[graph.getVertices().size()];

              String mergedEdgesOutput = mergeOperation.getEdges().stream()
                      .map(e ->
                      {
                        int originNumber = e.getOrigin().getVertexNo();
                        int endpointNumber = e.getEndpoint().getVertexNo();

                        incidentVertices[originNumber]++;
                        incidentVertices[endpointNumber]++;

                        return String.format("%d-%d%s", originNumber, endpointNumber,
                                formatOutputOfDistancesToRemovedVertexForEdge(e, removedVertexNumber, distanceVector));
                      })
                      .collect(Collectors.joining(", "));

              //all edges of a merge operation are incident to a single vertex
              int maxIncidentEdgesForSingleVertex = Arrays.stream(incidentVertices).max().getAsInt();


              boolean readyForDownEdges = mergeOperation.getMergeTag() == MergeTagEnum.LABEL_DOWN && mergeOperation.getEdges().size() > 1;

              String mergeSummaryOutput = String.format(" -- %s [eQty:%d,mC:%d,diff:%d](incidentV:%b)(rde:%b) %s\n", mergeOperation.getMergeTag(),
                      mergeOperation.getEdges().size(), mergeOperation.getMergedColors().size(), mergeOperation.getEdges().size() - mergeOperation.getMergedColors().size(),
                      maxIncidentEdgesForSingleVertex == mergeOperation.getEdges().size(), readyForDownEdges, mergedEdgesOutput);

              mergeOperationsOutput.append(mergeSummaryOutput);

              boolean properPairOfEdgesWIthMissingSquareExists = findMissingSquaresForMergeEdges(mergeOperation.getEdges(), mergeOperation.getMergeGraphColoring(), removedVertexNumber, distanceVector);

              return properPairOfEdgesWIthMissingSquareExists;
            })
            .filter(correct -> correct == true)
            .findAny().isPresent();

    boolean mergeWihtEdgesOfProperDistancesExists;

    mergeWihtEdgesOfProperDistancesExists =
            mergeOperations.stream()
                    .filter(mergeOperation -> checkIfEdgeOfProperDistancesExists(mergeOperation, distanceVector, removedVertexNumber))
                    .findAny().isPresent();

    return mergeOperationsOutput.toString() + "\n - properMergeExists: " + (mergeWihtEdgesOfProperDistancesExists || graph.getRoot().getEdges().size() == 1)
            + "\n - foundCorrectEdgesPairForMergeOperations: " + foundCorrectEdgesPairForMergeOperations;
  }

  private boolean checkIfEdgeOfProperDistancesExists(MergeOperation mergeOperation, int[] distanceVector, int vertexNumberToRemove)
  {
    boolean edgeOfProperDistanceExists = false;

    List<Edge> mergeEdges = mergeOperation.getEdges();
    if (mergeOperation.getMergeTag() == MergeTagEnum.CONSISTENCY_UP)
    {
      Edge lastMergeEdge = mergeEdges.get(mergeEdges.size() - 1);

      List<Integer> lastEdgeDistancesFromRemovedVertex = calculateDistancesToRemovedVertexForEdge(lastMergeEdge, vertexNumberToRemove, distanceVector);

      if (areDistancesFromRemovedVertexCorrect(lastEdgeDistancesFromRemovedVertex))
      {
        edgeOfProperDistanceExists = true;
      }
      else
      {
        if ((lastEdgeDistancesFromRemovedVertex.get(0) == 1 && lastEdgeDistancesFromRemovedVertex.get(1) == 1))
        {
          Edge secondLastMergeEdge = mergeEdges.get(mergeEdges.size() - 2);
          List<Integer> secondLastEdgeDistancesFromRemovedVertex = calculateDistancesToRemovedVertexForEdge(secondLastMergeEdge, vertexNumberToRemove, distanceVector);

          edgeOfProperDistanceExists = areDistancesFromRemovedVertexCorrect(secondLastEdgeDistancesFromRemovedVertex);
        }
        else
        {
          edgeOfProperDistanceExists = false;
        }
      }
    }
    else
    {
      int properDistanceEdgesQty = mergeEdges.stream()
              .filter(e ->
              {
                List<Integer> distancesFromRemovedVertex = calculateDistancesToRemovedVertexForEdge(e, vertexNumberToRemove, distanceVector);

                return areDistancesFromRemovedVertexCorrect(distancesFromRemovedVertex);
              })
              .mapToInt(e -> 1).sum();

      edgeOfProperDistanceExists = properDistanceEdgesQty > 0;
    }


    return edgeOfProperDistanceExists;
  }

  private boolean areDistancesFromRemovedVertexCorrect(List<Integer> distancesFromRemovedVertex)
  {
    return (distancesFromRemovedVertex.get(0) == 2 && distancesFromRemovedVertex.get(1) == 1)
            || (distancesFromRemovedVertex.get(0) == 1 && distancesFromRemovedVertex.get(1) == 2);
  }

  private String formatOutputOfDistancesToRemovedVertexForEdge(Edge edge, int removedVertexNumber, int[] distanceVector)
  {
    List<Integer> distancesToRemovedVertex = calculateDistancesToRemovedVertexForEdge(edge, removedVertexNumber, distanceVector);

    return String.format("[%d,%d]", distancesToRemovedVertex.get(0), distancesToRemovedVertex.get(1));
  }

  private List<Integer> calculateDistancesToRemovedVertexForEdge(Edge edge, int removedVertexNumber, int[] distanceVector)
  {
    int originNumber = edge.getOrigin().getVertexNo();
    int endpointNumber = edge.getEndpoint().getVertexNo();
    int originCorrectedNumber = calculateVertexCorrectedNo(removedVertexNumber, originNumber);
    int endpointCorrectedNumber = calculateVertexCorrectedNo(removedVertexNumber, endpointNumber);

    List<Integer> distancesFromRemovedVertex = new ArrayList<>(2);
    distancesFromRemovedVertex.add(distanceVector[originCorrectedNumber]);
    distancesFromRemovedVertex.add(distanceVector[endpointCorrectedNumber]);
    return distancesFromRemovedVertex;
  }

  private boolean findMissingSquaresForMergeEdges(List<Edge> mergeEdges, GraphColoring mergeGraphColoring, int removedVertexNumber, int[] distanceVector)
  {
    List<List<List<Edge>>> properEdgePairs = mergeEdges.stream()
            .map(mergeEdge ->
            {
              List<Edge> incidentEdgesNotBuildingSquareForOrigin = findIncidentEdgesNotBuildingSquare(mergeEdge, mergeGraphColoring, removedVertexNumber, distanceVector);
              List<Edge> incidentEdgesNotBuildingSquareForEndpoint = findIncidentEdgesNotBuildingSquare(mergeEdge.getOpposite(), mergeGraphColoring, removedVertexNumber, distanceVector);

              List<List<Edge>> edgePairsWithoutSquare = new LinkedList<>();
              if (incidentEdgesNotBuildingSquareForOrigin.size() == 1)
              {
                edgePairsWithoutSquare.add(Arrays.asList(mergeEdge, incidentEdgesNotBuildingSquareForOrigin.get(0)));
              }
              if (incidentEdgesNotBuildingSquareForEndpoint.size() == 1)
              {
                edgePairsWithoutSquare.add(Arrays.asList(mergeEdge.getOpposite(), incidentEdgesNotBuildingSquareForEndpoint.get(0)));
              }
              return edgePairsWithoutSquare;
            })
            .filter(edgePairWithoutSquare ->
            {
              boolean properEdgePairWithoutSquare = false;
              for (int i = 0; i < 2; i++)
              {
                if (i >= edgePairWithoutSquare.size())
                {
                  break;
                }
                List<Integer> firstEdgeDistances = calculateDistancesToRemovedVertexForEdge(edgePairWithoutSquare.get(0).get(0), removedVertexNumber, distanceVector);
                List<Integer> secondEdgeDistances = calculateDistancesToRemovedVertexForEdge(edgePairWithoutSquare.get(0).get(1), removedVertexNumber, distanceVector);

                properEdgePairWithoutSquare |= firstEdgeDistances.get(0) == 2 && secondEdgeDistances.get(0) == 2
                        && firstEdgeDistances.get(1) == 1 && secondEdgeDistances.get(1) == 1;
              }


              return properEdgePairWithoutSquare;
            })
            .collect(Collectors.toList());

//    if (CollectionUtils.isNotEmpty(properEdgePairs))
//    {
//      System.out.println("found proper pair");
//    }
//    else
//    {
//      System.out.println("found no proper pair");
//    }

    return CollectionUtils.isNotEmpty(properEdgePairs);
  }

  private List<Edge> findIncidentEdgesNotBuildingSquare(Edge mergeEdge, GraphColoring mergeGraphColoring, int removedVertexNumber, int[] distanceVector)
  {
    List<Edge> incidentEdgesNotBuildingSquare = mergeEdge.getOrigin().getEdges().stream()
            .filter(incidentEdge -> incidentEdge.getLabel() != null)
//            .filter(incidentEdge ->
//            {
//              int mergeEdgeColor = coloringService.getCurrentColorMapping(mergeGraphColoring, mergeEdge.getLabel().getColor());
//              int incidentEdgeColor = coloringService.getCurrentColorMapping(mergeGraphColoring, incidentEdge.getLabel().getColor());
//
//              return mergeEdgeColor != incidentEdgeColor;
//            })
            .filter(incidentEdge ->
            {
              List<List<Edge>> squaresForTwoEdges = singleSquareReconstructionService.findSquaresForTwoEdges(mergeEdge, incidentEdge);

              return CollectionUtils.isEmpty(squaresForTwoEdges);
            })
            .filter(incidentEdge -> graph.getAdjacencyMatrix()[mergeEdge.getEndpoint().getVertexNo()][incidentEdge.getEndpoint().getVertexNo()] == null)
            .collect(Collectors.toList());

    String incidentEdgesNotBuildingSquareOutput = incidentEdgesNotBuildingSquare.stream()
            .map(incidentEdge -> String.format("%d-%d%s", incidentEdge.getOrigin().getVertexNo(), incidentEdge.getEndpoint().getVertexNo(),
                    formatOutputOfDistancesToRemovedVertexForEdge(incidentEdge, removedVertexNumber, distanceVector)))
            .collect(Collectors.joining(","));

    System.out.println(String.format("%d-%d%s: %s", mergeEdge.getOrigin().getVertexNo(), mergeEdge.getEndpoint().getVertexNo(),
            formatOutputOfDistancesToRemovedVertexForEdge(mergeEdge, removedVertexNumber, distanceVector), incidentEdgesNotBuildingSquareOutput));

    return incidentEdgesNotBuildingSquare;
  }

  private int calculateVertexCorrectedNo(int vertexNumberToRemove, int vertexNumber)
  {
    return vertexNumber >= vertexNumberToRemove ? vertexNumber + 1 : vertexNumber;
  }
}


