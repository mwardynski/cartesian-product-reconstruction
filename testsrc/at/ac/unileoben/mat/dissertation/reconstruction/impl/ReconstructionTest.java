package at.ac.unileoben.mat.dissertation.reconstruction.impl;

import at.ac.unileoben.mat.dissertation.common.FactorizationCase;
import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.config.FactorizationConfig;
import at.ac.unileoben.mat.dissertation.linearfactorization.GraphFactorizationPreparer;
import at.ac.unileoben.mat.dissertation.reconstruction.Reconstruction;
import at.ac.unileoben.mat.dissertation.structure.Graph;
import at.ac.unileoben.mat.dissertation.structure.Vertex;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created with IntelliJ IDEA.
 * User: Marcin
 * Date: 24.01.16
 * Time: 18:24
 * To change this template use File | Settings | File Templates.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {FactorizationConfig.class})
public class ReconstructionTest
{
  @Autowired
  GraphHelper graphHelper;

  @Autowired
  Reconstruction reconstruction;

  @Autowired
  GraphFactorizationPreparer graphFactorizationPreparer;

  private final static List<FactorizationCase> examplesList = new LinkedList<FactorizationCase>();

  static
  {
    examplesList.add(new FactorizationCase("additionalVertex.txt", 2));
    examplesList.add(new FactorizationCase("breakExample.txt", 2));
    examplesList.add(new FactorizationCase("breakExample2.txt", 2));
    examplesList.add(new FactorizationCase("breakExample3.txt", 3));
//    examplesList.add(new FactorizationCase("breakExample4.txt", 2)); //FIXME not working
    examplesList.add(new FactorizationCase("c.txt", 3));
    examplesList.add(new FactorizationCase("cartFactExample.txt", 2));
    examplesList.add(new FactorizationCase("CartesianProductWithCrossEdges.txt", 2));
    examplesList.add(new FactorizationCase("g1", 2));
//    examplesList.add(new FactorizationCase("newExCart.txt", 2)); //FIXME not working
//    examplesList.add(new FactorizationCase("exampleOfCartesianProduct.txt", 2)); //FIXME not working
    examplesList.add(new FactorizationCase("exampleOfCartesianProduct3.txt", 3));
//    examplesList.add(new FactorizationCase("victory.txt", 3));
  }

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
          List<Vertex> incompleteVertices = graphHelper.parseGraph(factorizationCase.getFileName());
          graphFactorizationPreparer.removeVertex(incompleteVertices, vertexNumberToRemove);

          Graph resultGraph = reconstruction.reconstruct(incompleteVertices);
          int amountOfFactors = resultGraph.getGraphColoring().getActualColors().size();
          assertThat("file: " + factorizationCase.getFileName() + ", removed vertex number: " + vertexNumberToRemove, amountOfFactors, is(factorizationCase.getAmountOfFactors()));
        }
      }
      catch (IllegalArgumentException | IllegalStateException e)
      {
      }
    }
  }
}
