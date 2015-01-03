package at.ac.unileoben.mat.dissertation.linearfactorization;

import at.ac.unileoben.mat.dissertation.common.GraphReader;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 14-11-09
 * Time: 13:16
 * To change this template use File | Settings | File Templates.
 */
public class ReconstructionTest
{

  private final static List<FactorizationCase> examplesList = new LinkedList<FactorizationCase>();

  static
  {
//    examplesList.add(new FactorizationCase("breakExample.txt", 2)); //k2
//    examplesList.add(new FactorizationCase("breakExample2.txt", 2)); //k2
//    examplesList.add(new FactorizationCase("breakExample3.txt", 3)); //k2
//    examplesList.add(new FactorizationCase("breakExample4.txt", 2)); //nk2
//    examplesList.add(new FactorizationCase("c.txt", 3)); //k2
//    examplesList.add(new FactorizationCase("cartFactExample.txt", 2)); //k2
//    examplesList.add(new FactorizationCase("CartesianProductWithCrossEdges.txt", 2)); //k2
//    examplesList.add(new FactorizationCase("g1", 2)); //k2
//    examplesList.add(new FactorizationCase("cd.txt", 1)); //prime
//    examplesList.add(new FactorizationCase("g3", 1)); //prime
//don't use    examplesList.add(new FactorizationCase("newEx.txt", -1));
//    examplesList.add(new FactorizationCase("newExCart.txt", 2)); //nk2
    examplesList.add(new FactorizationCase("newExCart-no_diag.txt", 2)); //nk2
//    examplesList.add(new FactorizationCase("newExCart-mod.txt", 1)); //prime
//    examplesList.add(new FactorizationCase("przyklad.txt", 1)); //prime
//don't use    examplesList.add(new FactorizationCase("easyPartialCube2.txt", -1));
//    examplesList.add(new FactorizationCase("simpleExample.txt", 1)); //prime
//    examplesList.add(new FactorizationCase("example.txt", 1)); //prime
//    examplesList.add(new FactorizationCase("exampleOfCartesianProduct.txt", 2)); //k2
//    examplesList.add(new FactorizationCase("exampleOfCartesianProduct3.txt", 3)); //k2
//    examplesList.add(new FactorizationCase("victory.txt", 3));
  }

  GraphReader graphReader = new GraphReader();

  @Test
  public void singleFactorFactorizationAfterVertexRemoval()
  {

    GraphPreparer graphPreparer = new GraphPreparer();
    for (FactorizationCase factorizationCase : examplesList)
    {
      List<Vertex> orgGraphVertices = graphReader.readGraph(factorizationCase.getFileName());
      LinearFactorization orgLinearFactorization = new LinearFactorization(factorizationCase.getFileName());
      Graph orgGraph = orgLinearFactorization.factorizeWithPreparation(orgGraphVertices, orgGraphVertices.get(6));
      for (int i = 0; i < orgGraphVertices.size(); i++)
      {
        List<Vertex> graphVertices = graphReader.readGraph(factorizationCase.getFileName());
        graphPreparer.removeVertex(graphVertices, i);
        LinearFactorization linearFactorization = new LinearFactorization(factorizationCase.getFileName());
        Graph resultGraph = linearFactorization.factorizeWithPreparation(graphVertices, graphVertices.get(6));
        if (resultGraph != null)
        {
          int amountOfFactors = resultGraph.getGraphColoring().getActualColors().size();
          assertThat(factorizationCase.getFileName(), amountOfFactors, is(1));
        }

        if (resultGraph != null)
        {
          changeNumberingForModifiedGraph(resultGraph, i);
          analyze(factorizationCase.getFileName(), resultGraph.getGraphColoring().getOriginalColorsAmount(), resultGraph.getRoot().getVertexNo(), i, resultGraph.getAnalyzeData());
        }
      }
    }
  }

  private void changeNumberingForModifiedGraph(Graph graph, int removedVertexNo)
  {
    for (Vertex vertex : graph.getVertices())
    {
      if (vertex.getVertexNo() >= removedVertexNo)
      {
        vertex.setVertexNo(vertex.getVertexNo() + 1);
      }
    }

  }

  private void analyze(String fileName, int originalColorsAmount, int rootNo, int removedVertexNo, AnalyzeData analyzeData)
  {
    Graph bfsGraph = buildBfsGraphFromRoot(fileName, rootNo);
    Vertex removedVertex = bfsGraph.getVertices().get(removedVertexNo);
    clearBfsColors(bfsGraph);
    calculateDistancesFromGivenVertex(removedVertex);

    List<AnalyzeData.MergeOperation> mergeOperations = analyzeData.getMergeOperations();
    AnalyzeResult analyzeResult = new AnalyzeResult();
    analyzeResult.setMergeOperationsTotal(mergeOperations.size());
    analyzeResult.setRemovedVertexNo(removedVertexNo);
    analyzeResult.setOriginalColorsAmount(originalColorsAmount);
    System.out.println(fileName);
    for (int i = mergeOperations.size() - 1; i >= 0; i--)
    {
      AnalyzeData.MergeOperation mergeOperation = mergeOperations.get(i);
      List<Edge> edgesByMerge = mergeOperation.getEdgesByMerge();
      for (Edge edgeByMerge : edgesByMerge)
      {
        Vertex origin = edgeByMerge.getOrigin();
        Vertex endpoint = edgeByMerge.getEndpoint();
        Vertex originCorrespondingVertex = getCorrespondingVertex(bfsGraph, origin, removedVertexNo);
        Vertex endpointCorrespondingVertex = getCorrespondingVertex(bfsGraph, endpoint, removedVertexNo);

        updateAnalyzeResult(originCorrespondingVertex, i, mergeOperation, removedVertexNo, analyzeResult);
        updateAnalyzeResult(endpointCorrespondingVertex, i, mergeOperation, removedVertexNo, analyzeResult);
      }
    }
    if (mergeOperations.size() != 0)
    {
      System.out.println("\t" + analyzeResult);
    }
    else
    {
      System.out.println("\tNo merges for: " + fileName);
    }
  }

  private Vertex getCorrespondingVertex(Graph graph, Vertex v, int removedVertexNo)
  {
    int vertexNo = v.getVertexNo();
    return graph.getVertices().get(vertexNo);
  }

  private Graph buildBfsGraphFromRoot(String fileName, int rootNo)
  {
    List<Vertex> graphVertices = graphReader.readGraph(fileName);
    Vertex root = graphVertices.get(rootNo);
    GraphPreparer graphPreparer = new GraphPreparer();
    Graph bfsGraph = graphPreparer.prepareToLinearFactorization(graphVertices, root);
    graphPreparer.finalizeFactorization(bfsGraph);
    return bfsGraph;
  }

  private void clearBfsColors(Graph graph)
  {
    for (Vertex vertex : graph.getVertices())
    {
      vertex.setColor(Color.WHITE);
    }

  }

  private void calculateDistancesFromGivenVertex(Vertex root)
  {
    root.setColor(Color.GRAY);
    root.setBfsLayer(0);
    Queue<Vertex> queue = new LinkedList<Vertex>();
    queue.add(root);
    while (!queue.isEmpty())
    {
      Vertex u = queue.poll();
      for (Edge e : u.getEdges())
      {
        Vertex v = e.getEndpoint();
        if (v.getColor() == Color.WHITE)
        {
          v.setColor(Color.GRAY);
          v.setBfsLayer(u.getBfsLayer() + 1);
          queue.add(v);
        }
      }
      u.setColor(Color.BLACK);
    }
  }

  private void updateAnalyzeResult(Vertex v, int mergeOperationNo, AnalyzeData.MergeOperation mergeOperation, int removedVertexNo, AnalyzeResult analyzeResult)
  {
    if (v.getBfsLayer() < analyzeResult.getDistance())
    {
      analyzeResult.setDistance(v.getBfsLayer());
      analyzeResult.setClosestVertex(v);
      analyzeResult.setClosestMergeOperation(mergeOperation);
      analyzeResult.setClosestMergeOperationNo(mergeOperationNo);
    }
  }

  class AnalyzeResult
  {
    int mergeOperationsTotal;
    int closestMergeOperationNo;
    AnalyzeData.MergeOperation closestMergeOperation;
    Vertex closestVertex;
    int removedVertexNo;
    int originalColorsAmount;
    int distance = Integer.MAX_VALUE;

    int getDistance()
    {
      return distance;
    }

    void setDistance(int distance)
    {
      this.distance = distance;
    }

    void setMergeOperationsTotal(int mergeOperationsTotal)
    {
      this.mergeOperationsTotal = mergeOperationsTotal;
    }

    void setClosestMergeOperationNo(int closestMergeOperationNo)
    {
      this.closestMergeOperationNo = closestMergeOperationNo;
    }

    void setClosestMergeOperation(AnalyzeData.MergeOperation closestMergeOperation)
    {
      this.closestMergeOperation = closestMergeOperation;
    }

    void setClosestVertex(Vertex closestVertex)
    {
      this.closestVertex = closestVertex;
    }

    void setRemovedVertexNo(int removedVertexNo)
    {
      this.removedVertexNo = removedVertexNo;
    }

    void setOriginalColorsAmount(int originalColorsAmount)
    {
      this.originalColorsAmount = originalColorsAmount;
    }

    @Override
    public String toString()
    {
      return removedVertexNo + "->" + closestVertex.getVertexNo() + " d=" + distance +
              ", " + closestMergeOperation.getMergeTag() + "(" + (closestMergeOperationNo + 1) + "/" + mergeOperationsTotal + ") + initial colors: " + originalColorsAmount;

    }
  }
}
