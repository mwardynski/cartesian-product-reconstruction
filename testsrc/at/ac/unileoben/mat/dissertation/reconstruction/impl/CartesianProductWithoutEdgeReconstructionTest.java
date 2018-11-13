package at.ac.unileoben.mat.dissertation.reconstruction.impl;

import at.ac.unileoben.mat.dissertation.common.FactorizationCase;
import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.config.FactorizationConfig;
import at.ac.unileoben.mat.dissertation.linearfactorization.LinearFactorization;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {FactorizationConfig.class})
public class CartesianProductWithoutEdgeReconstructionTest
{

  @Autowired
  Graph graph;

  @Autowired
  ReconstructionData reconstructionData;

  @Autowired
  GraphHelper graphHelper;

  @Autowired
  LinearFactorization linearFactorization;

  private final static List<FactorizationCase> examplesList = new LinkedList<FactorizationCase>();

  @Test
  public void checkAllReconstructionCases()
  {
    reconstructionData.setOperationOnGraph(OperationOnGraph.EDGE_RECONSTRUCTION);
    for (FactorizationCase factorizationCase : examplesList)
    {
      List<Vertex> originalVertices = graphHelper.parseGraph(factorizationCase.getFileName());
      for (Vertex edgeToRemoveOrigin : originalVertices)
      {
        for (Edge edgeToRemove : edgeToRemoveOrigin.getEdges())
        {
          if (edgeToRemove.getEndpoint().getVertexNo() < edgeToRemoveOrigin.getVertexNo())
          {
            continue;
          }

          if (edgeToRemoveOrigin.getVertexNo() != 0 || edgeToRemove.getEndpoint().getVertexNo() != 8)
          {
            continue;
          }

          List<Vertex> vertices = graphHelper.parseGraph(factorizationCase.getFileName());
          removeEdge(edgeToRemove.getOrigin().getVertexNo(), edgeToRemove.getEndpoint().getVertexNo(), vertices);
          Vertex root = findRoot(vertices);
          reconstructionData.setCompleteMergeLayerNo(0);
          reconstructionData.setCompleteMergeOperation(null);

          Graph resultGraph = linearFactorization.factorize(vertices, root);
          int amountOfFactors = resultGraph.getGraphColoring().getActualColors().size();

          assertThat(factorizationCase.getFileName(), amountOfFactors, is(1));
          int edgeToRemoveOriginBfsLayer = graph.getVertices().get(edgeToRemove.getOrigin().getVertexNo()).getBfsLayer();
          int edgeToRemoveEndpointBfsLayer = graph.getVertices().get(edgeToRemove.getEndpoint().getVertexNo()).getBfsLayer();
          System.out.println(String.format("OK - file: %s, root: %d, removed edge: %d(L%d)-%d(L%d), complete merge: %d - diff:%d, Ldiff:%d, mergeOp:%s",
                  factorizationCase.getFileName(), root.getVertexNo(),
                  edgeToRemove.getOrigin().getVertexNo(), edgeToRemoveOriginBfsLayer,
                  edgeToRemove.getEndpoint().getVertexNo(), edgeToRemoveEndpointBfsLayer,
                  reconstructionData.getCompleteMergeLayerNo(),
                  reconstructionData.getCompleteMergeLayerNo() - Math.max(edgeToRemoveOriginBfsLayer, edgeToRemoveEndpointBfsLayer),
                  Math.abs(edgeToRemoveOriginBfsLayer - edgeToRemoveEndpointBfsLayer),
                  reconstructionData.getCompleteMergeOperation().getMergeTag()));
        }
      }
    }
  }

  private void removeEdge(int edgeToRemoveOriginNumber, int edgeToRemoveEntpointNumber, List<Vertex> vertices)
  {
    Vertex edgeToRemoveOrigin = vertices.get(edgeToRemoveOriginNumber);
    Iterator<Edge> edgeItertor = edgeToRemoveOrigin.getEdges().iterator();
    while (edgeItertor.hasNext())
    {
      Edge edge = edgeItertor.next();
      if (edge.getEndpoint().getVertexNo() == edgeToRemoveEntpointNumber)
      {
        edgeItertor.remove();
        break;
      }
    }

    Vertex edgeToRemoveEndpoinnt = vertices.get(edgeToRemoveEntpointNumber);
    edgeItertor = edgeToRemoveEndpoinnt.getEdges().iterator();
    while (edgeItertor.hasNext())
    {
      Edge edge = edgeItertor.next();
      if (edge.getEndpoint().getVertexNo() == edgeToRemoveOriginNumber)
      {
        edgeItertor.remove();
        break;
      }
    }
  }

  private Vertex findRoot(List<Vertex> vertices)
  {
    Vertex root = vertices.get(0);
    for (Vertex vertex : vertices)
    {
      if (vertex.getEdges().size() > root.getEdges().size())
      {
        root = vertex;
      }
    }
    return root;
  }

  static
  {
//    examplesList.add(new FactorizationCase("P3xK2.txt", 2));
//    examplesList.add(new FactorizationCase("K23xK2-mirrored.txt", 2));
//    examplesList.add(new FactorizationCase("notAllEdgesLabeled-root_v3.txt", 2, 3));
//    examplesList.add(new FactorizationCase("cartFactExample.txt", 2));
//    examplesList.add(new FactorizationCase("K4-ExK2.txt", 2));
//    examplesList.add(new FactorizationCase("C3xK2xK2.txt", 3));
//    examplesList.add(new FactorizationCase("C3xK2.txt", 2));
//    examplesList.add(new FactorizationCase("C4-ExC4-E.txt", 2));
    examplesList.add(new FactorizationCase("handP2.txt", 2));
//    examplesList.add(new FactorizationCase("S2xK2xK2.txt", 3));
//    examplesList.add(new FactorizationCase("S2xK2.txt", 2));
//    examplesList.add(new FactorizationCase("hxh.txt", 2, 0));
//    examplesList.add(new FactorizationCase("cube-vxcube-v.txt", 2, 0));
//    examplesList.add(new FactorizationCase("cube-VxP3.txt", 2, 0));
//    examplesList.add(new FactorizationCase("S6xP3.txt", 2, 0));
//    examplesList.add(new FactorizationCase("S6xP4.txt", 2, 0));
//    examplesList.add(new FactorizationCase("S3xS3.txt", 2, 0));
//    examplesList.add(new FactorizationCase("bP3xP6mVbxP3.txt", 2, 0));
//    examplesList.add(new FactorizationCase("bP3xP6mVbxP4.txt", 2, 0));
//    examplesList.add(new FactorizationCase("hxP3.txt", 2, 0));
//    examplesList.add(new FactorizationCase("P6xP4.txt", 2, 0));
//    examplesList.add(new FactorizationCase("P6xP3.txt", 2, 0));
//    examplesList.add(new FactorizationCase("bP3xK2xK2bmVxP3.txt", 2, 0));
//    examplesList.add(new FactorizationCase("YxP3xP3.txt", 2, 0));
//    examplesList.add(new FactorizationCase("P3xP4.txt", 2, 0));
//    examplesList.add(new FactorizationCase("K4-ExS2.txt", 2, 10));
//    examplesList.add(new FactorizationCase("K3pExP3.txt", 2));
//    examplesList.add(new FactorizationCase("bP3xK2bpExP3.txt", 2, 0));
//    examplesList.add(new FactorizationCase("bP3xP3bpExP3.txt", 2, 4));
//    examplesList.add(new FactorizationCase("C3xS2.txt", 2));
//    examplesList.add(new FactorizationCase("C4xS2.txt", 2));
//    examplesList.add(new FactorizationCase("C4pExS2.txt", 2));
//    examplesList.add(new FactorizationCase("C6xS2.txt", 2));
//    examplesList.add(new FactorizationCase("K23xP3.txt", 2, 13));
//    examplesList.add(new FactorizationCase("handP3.txt", 2));
//    examplesList.add(new FactorizationCase("hxhxh.txt", 2, 0));
//    examplesList.add(new FactorizationCase("victory.txt", 2));

  }
}
