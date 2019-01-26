package at.ac.unileoben.mat.dissertation.reconstruction.impl;

import at.ac.unileoben.mat.dissertation.common.FactorizationCase;
import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.config.FactorizationConfig;
import at.ac.unileoben.mat.dissertation.reconstruction.Reconstruction;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {FactorizationConfig.class})
public class CartesianProductWithoutSingleEdgeReconstructionTest extends AbstractCartesianProductEdgesReconstructionTest
{

  @Autowired
  ReconstructionData reconstructionData;

  @Autowired
  TestCaseContext testCaseContext;

  @Autowired
  @Qualifier("intervalReconstructionImpl")
  Reconstruction intervalReconstruction;

  @Autowired
  GraphHelper graphHelper;

  private final static List<FactorizationCase> examplesList = new LinkedList<>();

  @Test
  public void checkAllReconstructionCases()
  {
    reconstructionData.setOperationOnGraph(OperationOnGraph.SINGLE_EDGE_RECONSTRUCTION);
    boolean allCorrect = true;
    for (FactorizationCase factorizationCase : examplesList)
    {
      List<Vertex> originalVertices = graphHelper.parseGraph(factorizationCase.getFileName());
      for (Vertex edgeToRemoveOrigin : originalVertices)
      {
        for (Edge edgeToRemove : edgeToRemoveOrigin.getEdges())
        {
          if (edgeToRemove.getOrigin().getVertexNo() > edgeToRemove.getEndpoint().getVertexNo())
          {
            continue;
          }

//          edgeToRemove = new Edge(originalVertices.get(3), originalVertices.get(11));

          testCaseContext.setRemovedEdge(edgeToRemove);
          testCaseContext.setCorrectResult(false);

          List<Vertex> vertices = graphHelper.parseGraph(factorizationCase.getFileName());
          removeEdges(Collections.singletonList(edgeToRemove), vertices);

          System.out.println(String.format("%s-v:%d,e:%d-%d", factorizationCase.getFileName(),
                  edgeToRemoveOrigin.getVertexNo(), edgeToRemove.getOrigin().getVertexNo(), edgeToRemove.getEndpoint().getVertexNo()));

          intervalReconstruction.reconstruct(vertices);


          if (testCaseContext.isCorrectResult())
          {
            System.out.println("OK!");
          }
          else
          {
            throw new IllegalStateException("no result!!");
          }
        }
      }
    }
    if (allCorrect)
    {
      System.out.println("ALL CORRECT");
    }
  }

  static
  {
    //K2
    examplesList.add(new FactorizationCase("hxK2.txt", 2));
    examplesList.add(new FactorizationCase("cubexK2.txt", 4));
    examplesList.add(new FactorizationCase("cube-ExK2.txt", 2));
    examplesList.add(new FactorizationCase("cube-2ExK2.txt", 2));
    examplesList.add(new FactorizationCase("K23xK2-mirrored.txt", 2));
    examplesList.add(new FactorizationCase("K4-ExK2.txt", 2));
    examplesList.add(new FactorizationCase("C3xK2xK2.txt", 3));
    examplesList.add(new FactorizationCase("C3xK2.txt", 2));
    examplesList.add(new FactorizationCase("S2xK2xK2.txt", 3));
    examplesList.add(new FactorizationCase("handP2.txt", 2));
    examplesList.add(new FactorizationCase("cubep2ExK2.txt", 2, 0));
    examplesList.add(new FactorizationCase("cubep2EdiagxK2.txt", 2, 0));
    //minP3
    examplesList.add(new FactorizationCase("hxP3.txt", 2, 0));
    examplesList.add(new FactorizationCase("hxP4.txt", 2, 0));
    examplesList.add(new FactorizationCase("cube-ExP3.txt", 2));
    examplesList.add(new FactorizationCase("cube-2ExP3.txt", 2));
    examplesList.add(new FactorizationCase("notAllEdgesLabeled-root_v3.txt", 2, 3));
    examplesList.add(new FactorizationCase("cartFactExample.txt", 2));
    examplesList.add(new FactorizationCase("C4-ExC4-E.txt", 2));
    examplesList.add(new FactorizationCase("cube-vxcube-v.txt", 2, 0));
    examplesList.add(new FactorizationCase("cube-VxP3.txt", 2, 0));
    examplesList.add(new FactorizationCase("S6xP4.txt", 2, 0));
    examplesList.add(new FactorizationCase("S3xS3.txt", 2, 0));
    examplesList.add(new FactorizationCase("bP3xP6mVbxP3.txt", 2, 0));
    examplesList.add(new FactorizationCase("bP3xP6mVbxP4.txt", 2, 0));
    examplesList.add(new FactorizationCase("P6xP4.txt", 2, 0));
    examplesList.add(new FactorizationCase("P6xP3.txt", 2, 0));
    examplesList.add(new FactorizationCase("YxP3xP3.txt", 2, 0));
    examplesList.add(new FactorizationCase("P3xP4.txt", 2, 0));
    examplesList.add(new FactorizationCase("K4-ExS2.txt", 2, 10));
    examplesList.add(new FactorizationCase("K3pExP3.txt", 2));
    examplesList.add(new FactorizationCase("bP3xK2xK2bmVxP3.txt", 2, 0));
    examplesList.add(new FactorizationCase("bP3xP3bpExP3.txt", 2, 4));
    examplesList.add(new FactorizationCase("bP3xK2bpExP3.txt", 2, 0));
    examplesList.add(new FactorizationCase("C3xS2.txt", 2));
    examplesList.add(new FactorizationCase("C4xS2.txt", 2));
    examplesList.add(new FactorizationCase("C4pExS2.txt", 2));
    examplesList.add(new FactorizationCase("C6xS2.txt", 2));
    examplesList.add(new FactorizationCase("K23xP3.txt", 2, 13));
    examplesList.add(new FactorizationCase("handP3.txt", 2));
    examplesList.add(new FactorizationCase("hxh.txt", 2, 0));
    //LONG
    //    examplesList.add(new FactorizationCase("hxhxh.txt", 2, 0));
//    examplesList.add(new FactorizationCase("victory.txt", 2));


    //no square
//    examplesList.add(new FactorizationCase("P3xK2.txt", 2));
//    examplesList.add(new FactorizationCase("S2xK2.txt", 2));
//    examplesList.add(new FactorizationCase("S6xP3.txt", 2, 0));


  }
}
