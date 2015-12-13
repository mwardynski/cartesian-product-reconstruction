package at.ac.unileoben.mat.dissertation.linearfactorization;

import at.ac.unileoben.mat.dissertation.config.FactorizationConfig;
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
 * Date: 13.12.15
 * Time: 20:39
 * To change this template use File | Settings | File Templates.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {FactorizationConfig.class})
public class FactorizationIncompleteGraphTest
{
  @Autowired
  LinearFactorization linearFactorization;

  private final static List<String> examplesList = new LinkedList<>();

  static
  {
    examplesList.add("breakExample.txt");
    examplesList.add("breakExample2.txt");
    examplesList.add("breakExample3.txt");
    examplesList.add("breakExample4.txt");
    examplesList.add("c.txt");
    examplesList.add("cartFactExample.txt");
    examplesList.add("CartesianProductWithCrossEdges.txt");
    examplesList.add("g1");
    examplesList.add("cd.txt");
    examplesList.add("g3");
//    examplesList.add("newEx.txt");
    examplesList.add("newExCart.txt");
    examplesList.add("newExCart-mod.txt");
    examplesList.add("przyklad.txt");
//    examplesList.add("easyPartialCube2.txt");
    examplesList.add("simpleExample.txt");
    examplesList.add("example.txt");
    examplesList.add("exampleOfCartesianProduct.txt");
    examplesList.add("exampleOfCartesianProduct3.txt");
//    examplesList.add("victory.txt");
  }

  @Test
  public void checkExamples()
  {
    for (String factorizationCase : examplesList)
    {
      try
      {
        List<Vertex> vertices = linearFactorization.parseGraph(factorizationCase);

        for (int vertexNumberToRemove = 0; vertexNumberToRemove < vertices.size(); vertexNumberToRemove++)
        {
          for (int rootNumber = 0; rootNumber < vertices.size(); rootNumber++)
          {
            if (rootNumber == vertexNumberToRemove)
            {
              continue;
            }
            List<Vertex> singleRunVertices = linearFactorization.parseGraph(factorizationCase);
            Graph resultGraph = linearFactorization.factorizeIncompleteGraph(singleRunVertices, vertexNumberToRemove, rootNumber);
            int amountOfFactors = resultGraph.getGraphColoring().getActualColors().size();
            assertThat("more then one factor for incomplete product: " + factorizationCase
                    + " after removing vertex number: " + vertexNumberToRemove
                    + " and setting as root: " + rootNumber,
                    amountOfFactors, is(1));
          }
        }
      }
      catch (IllegalArgumentException | IllegalStateException e)
      {
      }
    }
  }
}
