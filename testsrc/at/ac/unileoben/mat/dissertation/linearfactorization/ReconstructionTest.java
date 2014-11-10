package at.ac.unileoben.mat.dissertation.linearfactorization;

import at.ac.unileoben.mat.dissertation.common.GraphReader;
import at.ac.unileoben.mat.dissertation.structure.Graph;
import at.ac.unileoben.mat.dissertation.structure.Vertex;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 14-11-09
 * Time: 13:16
 * To change this template use File | Settings | File Templates.
 */
public class ReconstructionTest
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
    examplesList.add(new FactorizationCase("newExCart-mod.txt", 1));
    examplesList.add(new FactorizationCase("przyklad.txt", 1));
//    examplesList.add(new FactorizationCase("easyPartialCube2.txt", -1));
    examplesList.add(new FactorizationCase("simpleExample.txt", 1));
    examplesList.add(new FactorizationCase("example.txt", 1));
    examplesList.add(new FactorizationCase("exampleOfCartesianProduct.txt", 2));
    examplesList.add(new FactorizationCase("exampleOfCartesianProduct3.txt", 3));
//    takes too long examplesList.add(new FactorizationCase("victory.txt", 3));
  }

  GraphReader graphReader = new GraphReader();

  @Test
  public void singleFactorFactorizationAfterVertexRemoval()
  {

    GraphPreparer graphPreparer = new GraphPreparer();
    for (FactorizationCase factorizationCase : examplesList)
    {
      List<Vertex> tmpGraphVertices = graphReader.readGraph(factorizationCase.getFileName());
      for (int i = 0; i < tmpGraphVertices.size(); i++)
      {
        List<Vertex> graphVertices = graphReader.readGraph(factorizationCase.getFileName());
        graphPreparer.removeVertex(graphVertices, i);
        LinearFactorization linearFactorization = new LinearFactorization(factorizationCase.getFileName());
        Graph resultGraph = linearFactorization.factorizeWithPreparation(graphVertices, null);
        if(resultGraph != null)
        {
          int amountOfFactors = resultGraph.getGraphColoring().getActualColors().size();
          assertThat(factorizationCase.getFileName(), amountOfFactors, is(1));
        }
      }
    }
  }

  private Graph factorizeGraphWithAllVertices(String fileName, int removedVertexNo)
  {
    List<Vertex> graphVertices = graphReader.readGraph(fileName);
    Vertex removedVertex = graphVertices.get(removedVertexNo);
    LinearFactorization linearFactorization = new LinearFactorization(fileName);
    return linearFactorization.factorizeWithPreparation(graphVertices, removedVertex);
  }
}
