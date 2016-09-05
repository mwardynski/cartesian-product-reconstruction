package at.ac.unileoben.mat.dissertation.reconstruction.impl;

import at.ac.unileoben.mat.dissertation.common.FactorizationCase;
import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.config.FactorizationConfig;
import at.ac.unileoben.mat.dissertation.linearfactorization.GraphFactorizationPreparer;
import at.ac.unileoben.mat.dissertation.reconstruction.DuplicateReconstruction;
import at.ac.unileoben.mat.dissertation.structure.FactorizationData;
import at.ac.unileoben.mat.dissertation.structure.Vertex;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.StopWatch;

import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by mwardynski on 24/04/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {FactorizationConfig.class})
public class DuplicateReconstructionTest
{

  private final static List<FactorizationCase> examplesList = new LinkedList<FactorizationCase>();

  @Autowired
  GraphHelper graphHelper;

  @Autowired
  DuplicateReconstruction duplicateReconstruction;

  @Autowired
  GraphFactorizationPreparer graphFactorizationPreparer;

  @Test
  public void checkExamples()
  {
    for (FactorizationCase factorizationCase : examplesList)
    {
      try
      {
        List<Vertex> vertices = graphHelper.parseGraph(factorizationCase.getFileName());
        for (int vertexNumberToRemove = 0; vertexNumberToRemove < vertices.size(); vertexNumberToRemove++)
        {
          StopWatch stopWatch = new StopWatch();
          stopWatch.start();
          List<Vertex> incompleteVertices = graphHelper.parseGraph(factorizationCase.getFileName());
          graphFactorizationPreparer.removeVertex(incompleteVertices, vertexNumberToRemove);

          try
          {
            FactorizationData factorizationData = duplicateReconstruction.findFactors(incompleteVertices);
            assertThat("file: " + factorizationCase.getFileName() + ", removed vertex no: " + vertexNumberToRemove,
                    factorizationData.getFactors().size(), is(factorizationCase.getAmountOfFactors()));
            System.out.print("OK - file: " + factorizationCase.getFileName() + ", removed vertex no: " + vertexNumberToRemove);
          }
          catch (Exception e)
          {
            System.out.print("EXCEPTION " + e + ", file: " + factorizationCase.getFileName() + ", removed vertex no: " + vertexNumberToRemove);
            e.printStackTrace();
          }
          stopWatch.stop();
          System.out.println(" [" + stopWatch.getTotalTimeSeconds() + "s]");
        }
      }
      catch (IllegalArgumentException | IllegalStateException e)
      {
      }
    }
  }

  static
  {

    examplesList.add(new FactorizationCase("newExCart.txt", 2));
    examplesList.add(new FactorizationCase("additionalVertex.txt", 2));
    examplesList.add(new FactorizationCase("breakExample2.txt", 2));
    examplesList.add(new FactorizationCase("breakExample3.txt", 3));
    examplesList.add(new FactorizationCase("breakExample4.txt", 2));
    examplesList.add(new FactorizationCase("g1", 2));
    examplesList.add(new FactorizationCase("exampleOfCartesianProduct.txt", 2));
    examplesList.add(new FactorizationCase("notAllEdgesLabeled-root_v3.txt", 2, 3));
    //working
    examplesList.add(new FactorizationCase("c.txt", 3));
    examplesList.add(new FactorizationCase("cartFactExample.txt", 2));
    examplesList.add(new FactorizationCase("CartesianProductWithCrossEdges.txt", 2));
    examplesList.add(new FactorizationCase("exampleOfCartesianProduct3.txt", 3));
    examplesList.add(new FactorizationCase("breakExample.txt", 2));
    //not needed:
//    examplesList.add(new FactorizationCase("victory.txt", 3));
//    examplesList.add(new FactorizationCase("cd.txt", 1));
//    examplesList.add(new FactorizationCase("g3", 1));
//    examplesList.add(new FactorizationCase("newExCart-mod.txt", 1));
//    examplesList.add(new FactorizationCase("przyklad.txt", 1));
//    examplesList.add(new FactorizationCase("simpleExample.txt", 1));
//    examplesList.add(new FactorizationCase("example.txt", 1));

  }
}
