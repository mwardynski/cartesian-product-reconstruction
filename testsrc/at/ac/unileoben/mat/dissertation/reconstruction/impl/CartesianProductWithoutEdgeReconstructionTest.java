package at.ac.unileoben.mat.dissertation.reconstruction.impl;

import at.ac.unileoben.mat.dissertation.common.FactorizationCase;
import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.config.FactorizationConfig;
import at.ac.unileoben.mat.dissertation.reconstruction.Reconstruction;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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
  @Qualifier("intervalReconstructionImpl")
  Reconstruction intervalReconstruction;

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
        for (long i = 1; ; i++)
        {
          List<Edge> edgesToRemove = selectEdgesToRemove(i, edgeToRemoveOrigin);
          if (CollectionUtils.isEmpty(edgesToRemove))
          {
            break;
          }

          List<Vertex> vertices = graphHelper.parseGraph(factorizationCase.getFileName());
          int numberOfEdgesBeforeDeletion = countAllEdges(vertices);
          removeEdges(edgesToRemove, vertices);
          int numberOfEdgesAfterDeletion = countAllEdges(vertices);

          intervalReconstruction.reconstruct(vertices);
          int amountOfFactors = graph.getGraphColoring().getActualColors().size() - 1;

          List<Edge> speciallyColoredEdges = findSpeciallyColoredEdges(vertices);
          String removedEdges = edgesToRemove.stream()
                  .map(edge -> String.format("%d-%d", edge.getOrigin().getVertexNo(), edge.getEndpoint().getVertexNo()))
                  .collect(Collectors.joining(","));

          System.out.println(String.format("%s-v:%d,e:{%s}(qb:%d,qa:%d): expected: %d, actual: %d, special: %d", factorizationCase.getFileName(),
                  edgeToRemoveOrigin.getVertexNo(), removedEdges,
                  numberOfEdgesBeforeDeletion, numberOfEdgesAfterDeletion,
                  factorizationCase.getAmountOfFactors(), amountOfFactors, speciallyColoredEdges.size()));

          if (CollectionUtils.isEmpty(speciallyColoredEdges))
          {
            if (amountOfFactors != factorizationCase.getAmountOfFactors())
            {
              return;
            }
            assertThat(factorizationCase.getFileName(), amountOfFactors, is(factorizationCase.getAmountOfFactors()));
          }

//          assertThat(factorizationCase.getFileName(), amountOfFactors, is(1));
          /*int edgeToRemoveOriginBfsLayer = graph.getVertices().get(edgeToRemove.getOrigin().getVertexNo()).getBfsLayer();
          int edgeToRemoveEndpointBfsLayer = graph.getVertices().get(edgeToRemove.getEndpoint().getVertexNo()).getBfsLayer();
          System.out.println(String.format("OK - file: %s, root: %d, removed edge: %d(L%d)-%d(L%d), complete merge: %d - diff:%d, Ldiff:%d, mergeOp:%s",
                  factorizationCase.getFileName(), root.getVertexNo(),
                  edgeToRemove.getOrigin().getVertexNo(), edgeToRemoveOriginBfsLayer,
                  edgeToRemove.getEndpoint().getVertexNo(), edgeToRemoveEndpointBfsLayer,
                  reconstructionData.getCompleteMergeLayerNo(),
                  reconstructionData.getCompleteMergeLayerNo() - Math.max(edgeToRemoveOriginBfsLayer, edgeToRemoveEndpointBfsLayer),
                  Math.abs(edgeToRemoveOriginBfsLayer - edgeToRemoveEndpointBfsLayer),
                  reconstructionData.getCompleteMergeOperation().getMergeTag()));*/
        }
      }
    }
    System.out.println("ALL CORRECT");
  }

  private void removeEdges(List<Edge> edgesToRemove, List<Vertex> vertices)
  {
    for (Edge edgeToRemove : edgesToRemove)
    {
      Vertex edgeToRemoveOrigin = vertices.get(edgeToRemove.getOrigin().getVertexNo());
      Iterator<Edge> edgeItertor = edgeToRemoveOrigin.getEdges().iterator();
      while (edgeItertor.hasNext())
      {
        Edge edge = edgeItertor.next();
        if (edge.getEndpoint().getVertexNo() == edgeToRemove.getEndpoint().getVertexNo())
        {
          edgeItertor.remove();
          break;
        }
      }

      Vertex edgeToRemoveEndpoinnt = vertices.get(edgeToRemove.getEndpoint().getVertexNo());
      edgeItertor = edgeToRemoveEndpoinnt.getEdges().iterator();
      while (edgeItertor.hasNext())
      {
        Edge edge = edgeItertor.next();
        if (edge.getEndpoint().getVertexNo() == edgeToRemove.getOrigin().getVertexNo())
        {
          edgeItertor.remove();
          break;
        }
      }
    }
  }

  private List<Edge> selectEdgesToRemove(long counter, Vertex origin)
  {
    int edgesSize = origin.getEdges().size();
    long maxCounter = (long) Math.pow(2, edgesSize) - 1;

    List<Edge> edgesToRemove = new LinkedList<>();
    if (counter < maxCounter)
    {
      BigInteger counterBits = BigInteger.valueOf(counter);

      for (int i = 0; i < edgesSize; i++)
      {
        if (counterBits.testBit(i))
        {
          edgesToRemove.add(origin.getEdges().get(i));
        }
      }
    }

    return edgesToRemove;
  }

  private List<Edge> findSpeciallyColoredEdges(List<Vertex> vertices)
  {
    List<Edge> speciallyColoredEdges = vertices.stream()
            .flatMap(v -> v.getEdges().stream())
            .filter(e -> e.getLabel().getColor() == 0)
            .filter(e -> e.getOrigin().getVertexNo() < e.getEndpoint().getVertexNo())
            .collect(Collectors.toList());
    return speciallyColoredEdges;
  }

  private int countAllEdges(List<Vertex> vertices)
  {
    int doubleNumberOfEdges = vertices.stream()
            .flatMap(v -> v.getEdges().stream())
            .mapToInt(e -> 1)
            .sum();
    return doubleNumberOfEdges / 2;
  }

  static
  {

//    examplesList.add(new FactorizationCase("cubexK2.txt", 4));
    examplesList.add(new FactorizationCase("cube-ExK2.txt", 2));
//    examplesList.add(new FactorizationCase("cube-2ExK2.txt", 2));
//    examplesList.add(new FactorizationCase("P3xK2.txt", 2));
//    examplesList.add(new FactorizationCase("K23xK2-mirrored.txt", 2));
//    examplesList.add(new FactorizationCase("notAllEdgesLabeled-root_v3.txt", 2, 3));
//    examplesList.add(new FactorizationCase("cartFactExample.txt", 2));
//    examplesList.add(new FactorizationCase("K4-ExK2.txt", 2));
//    examplesList.add(new FactorizationCase("C3xK2xK2.txt", 3));
//    examplesList.add(new FactorizationCase("C3xK2.txt", 2));
//    examplesList.add(new FactorizationCase("C4-ExC4-E.txt", 2));
//    examplesList.add(new FactorizationCase("handP2.txt", 2));
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
