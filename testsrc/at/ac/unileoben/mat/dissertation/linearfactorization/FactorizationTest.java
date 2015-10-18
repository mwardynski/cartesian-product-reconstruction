package at.ac.unileoben.mat.dissertation.linearfactorization;

import at.ac.unileoben.mat.dissertation.structure.Graph;
import org.junit.Test;

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
public class FactorizationTest
{

  private final static List<FactorizationCase> examplesList = new LinkedList<FactorizationCase>();

  static
  {
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
    examplesList.add(new FactorizationCase("newExCart-mod.txt", 2));
    examplesList.add(new FactorizationCase("przyklad.txt", 1));
//    examplesList.add(new FactorizationCase("easyPartialCube2.txt", -1));
    examplesList.add(new FactorizationCase("simpleExample.txt", 1));
    examplesList.add(new FactorizationCase("example.txt", 1));
    examplesList.add(new FactorizationCase("exampleOfCartesianProduct.txt", 2));
    examplesList.add(new FactorizationCase("exampleOfCartesianProduct3.txt", 3));
    examplesList.add(new FactorizationCase("victory.txt", 3));
  }

  @Test
  public void checkExamples()
  {
    for (FactorizationCase factorizationCase : examplesList)
    {
      LinearFactorization linearFactorization = new LinearFactorization(factorizationCase.getFileName());
      Graph resultGraph = linearFactorization.factorizeWithPreparation();
      if (resultGraph != null)
      {
        int amountOfFactors = resultGraph.getGraphColoring().getActualColors().size();
        assertThat(factorizationCase.getFileName(), amountOfFactors, is(factorizationCase.getAmountOfFactors()));
      }
    }
  }
}
