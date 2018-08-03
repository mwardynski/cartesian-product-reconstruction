package at.ac.unileoben.mat.dissertation.reconstruction.impl;


import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.common.ReconstructionCase;
import at.ac.unileoben.mat.dissertation.config.FactorizationConfig;
import at.ac.unileoben.mat.dissertation.linearfactorization.GraphFactorizationPreparer;
import at.ac.unileoben.mat.dissertation.linearfactorization.LinearFactorization;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.EdgeService;
import at.ac.unileoben.mat.dissertation.structure.Graph;
import at.ac.unileoben.mat.dissertation.structure.Vertex;
import at.ac.unileoben.mat.dissertation.structure.exception.CompleteMergeException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.LinkedList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {FactorizationConfig.class})
public class MissingVertexToRootDistanceTest
{

  @Autowired
  Graph graph;

  @Autowired
  GraphHelper graphHelper;

  @Autowired
  GraphFactorizationPreparer graphFactorizationPreparer;

  @Autowired
  LinearFactorization linearFactorization;

  @Autowired
  EdgeService edgeService;

  private final static List<ReconstructionCase> examplesList = new LinkedList<ReconstructionCase>();

  static
  {

    examplesList.add(new ReconstructionCase("K3pExP3.txt", 2));
    examplesList.add(new ReconstructionCase("bP3xK2bpExP3.txt", 2));
    examplesList.add(new ReconstructionCase("K3xK3.txt", 2));
    examplesList.add(new ReconstructionCase("K4-ExS2.txt", 2));
    examplesList.add(new ReconstructionCase("C6xS2.txt", 2));
    examplesList.add(new ReconstructionCase("cartFactExample.txt", 2));
    examplesList.add(new ReconstructionCase("C4-ExC4-E.txt", 2));
    examplesList.add(new ReconstructionCase("handP3.txt", 2));

    //xK2
    examplesList.add(new ReconstructionCase("K23xK2.txt", 2));
    examplesList.add(new ReconstructionCase("S2xK2.txt", 2));
    examplesList.add(new ReconstructionCase("S2xK2xK2.txt", 3));
    examplesList.add(new ReconstructionCase("P3xK2.txt", 2));
    examplesList.add(new ReconstructionCase("K23xK2-mirrored.txt", 2));
    examplesList.add(new ReconstructionCase("K4-ExK2.txt", 2));
    examplesList.add(new ReconstructionCase("C3xK2xK2.txt", 3));
    examplesList.add(new ReconstructionCase("C3xK2.txt", 2));
    examplesList.add(new ReconstructionCase("handP2.txt", 2));

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
          linearFactorization.factorize(incompleteVertices, vertex);
        }
        catch (CompleteMergeException completeMergeException)
        {
          graphHelper.revertGraphBfsStructure();
          logDistanceToMissingVertex(graphName, vertexNumberToRemove, distanceVector, completeMergeException);
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

  private void clearVerticesAndEdges(List<Vertex> vertices)
  {
    for (Vertex vertex : vertices)
    {
      vertex.setUnitLayer(false);
      vertex.getEdges().stream().forEach(edgeService::clearEdgeLabeling);
    }
  }

  private void logDistanceToMissingVertex(String graphPath, int vertexNumberToRemove, int[] distanceVector, CompleteMergeException completeMergeException)
  {
    Integer rootNo = graph.getRoot().getVertexNo();
    int rootCorrectedNo = rootNo >= vertexNumberToRemove ? rootNo + 1 : rootNo;

    int distanceToRemovedVertex = distanceVector[rootCorrectedNo];

    System.out.println(String.format("%s, m:%d r:%d(%d) - expected: layer %d, actual layer: %d(%b), diff=%d",
            graphPath, vertexNumberToRemove, rootNo, rootCorrectedNo, distanceToRemovedVertex,
            completeMergeException.getLayerNo(), completeMergeException.getAfterConsistencyCheck(),
            distanceToRemovedVertex - completeMergeException.getLayerNo()));
  }
}


