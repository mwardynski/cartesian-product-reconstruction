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
    examplesList.add("cartesianProductWithCrossEdges.txt");
  }

  @Test
  public void checkExamples()
  {
    for (String exampleFilePath : examplesList)
    {
      Main main = new Main(exampleFilePath);
      main.run();
    }
  }
}
