package at.ac.unileoben.mat.dissertation.linearfactorization.impl;

import at.ac.unileoben.mat.dissertation.common.FactorizationCase;
import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.config.FactorizationConfig;
import at.ac.unileoben.mat.dissertation.linearfactorization.LinearFactorization;
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
 * User: marcin
 * Date: 13-12-04
 * Time: 18:07
 * To change this template use File | Settings | File Templates.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {FactorizationConfig.class})
public class FactorizationTest
{
  private final static List<FactorizationCase> examplesList = new LinkedList<FactorizationCase>();
  @Autowired
  GraphHelper graphHelper;
  @Autowired
  LinearFactorization linearFactorization;

  @Test
  public void checkExamples()
  {
    for (FactorizationCase factorizationCase : examplesList)
    {
      try
      {
        List<Vertex> vertices = graphHelper.parseGraph(factorizationCase.getFileName());
        Vertex root = null;
        if (factorizationCase.getRootVertexNo() != null)
        {
          root = vertices.get(factorizationCase.getRootVertexNo());
        }
        Graph resultGraph = linearFactorization.factorize(vertices, root);
        int amountOfFactors = resultGraph.getGraphColoring().getActualColors().size();
        assertThat(factorizationCase.getFileName(), amountOfFactors, is(factorizationCase.getAmountOfFactors()));
        System.out.println("OK - file: " + factorizationCase.getFileName());
      }
      catch (IllegalArgumentException | IllegalStateException e)
      {
      }
    }
  }

  static
  {
    examplesList.add(new FactorizationCase("additionalVertex.txt", 2));
    examplesList.add(new FactorizationCase("breakExample.txt", 2));
    examplesList.add(new FactorizationCase("breakExample2.txt", 2));
    examplesList.add(new FactorizationCase("breakExample3.txt", 3));
    examplesList.add(new FactorizationCase("breakExample4.txt", 2));
    examplesList.add(new FactorizationCase("c.txt", 3));
    examplesList.add(new FactorizationCase("cartFactExample.txt", 2));
    examplesList.add(new FactorizationCase("CartesianProductWithCrossEdges.txt", 2));
    examplesList.add(new FactorizationCase("g1", 2));
    examplesList.add(new FactorizationCase("cd.txt", 1));
    examplesList.add(new FactorizationCase("g3", 1));
//    examplesList.add(new FactorizationCase("newEx.txt", -1));
    examplesList.add(new FactorizationCase("newExCart.txt", 2));
    examplesList.add(new FactorizationCase("newExCart-mod.txt", 1));
    examplesList.add(new FactorizationCase("przyklad.txt", 1));
//    examplesList.add(new FactorizationCase("easyPartialCube2.txt", -1));
    examplesList.add(new FactorizationCase("simpleExample.txt", 1));
    examplesList.add(new FactorizationCase("example.txt", 1));
    examplesList.add(new FactorizationCase("exampleOfCartesianProduct.txt", 2));
    examplesList.add(new FactorizationCase("exampleOfCartesianProduct3.txt", 3));
    examplesList.add(new FactorizationCase("notAllEdgesLabeled-root_v3.txt", 2, 3));
    examplesList.add(new FactorizationCase("crossToCross.txt", 2));
    examplesList.add(new FactorizationCase("victory.txt", 3));
  }
}
