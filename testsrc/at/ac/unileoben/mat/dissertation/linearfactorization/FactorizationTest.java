package at.ac.unileoben.mat.dissertation.linearfactorization;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 13-12-04
 * Time: 18:07
 * To change this template use File | Settings | File Templates.
 */
public class FactorizationTest
{

  private final static List<String> examplesList = new LinkedList<String>();

  static
  {
    examplesList.add("c.txt");
    examplesList.add("cartFactExample.txt");
    examplesList.add("CartesianProductWithCrossEdges.txt");
    examplesList.add("g1");
    examplesList.add("cd.txt");
    examplesList.add("g3");
    examplesList.add("newEx.txt");
    examplesList.add("newExCart.txt");
    examplesList.add("przyklad.txt");
    examplesList.add("easyPartialCube2.txt");
    examplesList.add("simpleExample.txt");
    examplesList.add("example.txt");
    examplesList.add("exampleOfCartesianProduct.txt");
    examplesList.add("exampleOfCartesianProduct3.txt");
    examplesList.add("victory.txt");
  }

  @Test
  public void checkExamples()
  {
    for (String exampleFilePath : examplesList)
    {
      System.out.print(exampleFilePath + ": ");
      LinearFactorization linearFactorization = new LinearFactorization(exampleFilePath);
      linearFactorization.run();
    }
  }
}
