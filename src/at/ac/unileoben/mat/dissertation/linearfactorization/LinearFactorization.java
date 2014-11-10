package at.ac.unileoben.mat.dissertation.linearfactorization;

import at.ac.unileoben.mat.dissertation.common.GraphCorrectnessChecker;
import at.ac.unileoben.mat.dissertation.common.GraphReader;
import at.ac.unileoben.mat.dissertation.structure.Graph;
import at.ac.unileoben.mat.dissertation.structure.Vertex;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 13-11-29
 * Time: 15:17
 * To change this template use File | Settings | File Templates.
 */
public class LinearFactorization
{

  private String graphFilePath;
  private GraphReader graphReader;
  private GraphCorrectnessChecker graphCorrectnessChecker;
  private GraphPreparer graphPreparer;


  LinearFactorization(String graphFilePath)
  {
    this.graphFilePath = graphFilePath;
    graphReader = new GraphReader();
    graphCorrectnessChecker = new GraphCorrectnessChecker();
    graphPreparer = new GraphPreparer();
  }

  public static void main(String... args)
  {
    if (args.length != 1)
    {
      System.err.println("Wrong number of arguments.\n"
              + "Please put only one argument with a path to the input file");
      System.exit(-1);
    }

    LinearFactorization linearFactorization = new LinearFactorization(args[0]);
    Graph resultGraph = linearFactorization.factorizeWithPreparation();
    int amountOfFactors = resultGraph.getGraphColoring().getActualColors().size();
    System.out.println(amountOfFactors);
  }


  Graph factorizeWithPreparation()
  {
    List<Vertex> vertices = graphReader.readGraph(graphFilePath);
    Graph preparedGraph = prepare(vertices, null);
    if (preparedGraph == null)
    {
      return null;
    }
    return factorizeGivenGraph(preparedGraph);
  }

  Graph factorizeWithPreparation(List<Vertex> vertices, Vertex root)
  {
    Graph preparedGraph = prepare(vertices, root);
    if (preparedGraph == null)
    {
      return null;
    }
    return factorizeGivenGraph(preparedGraph);
  }

  private Graph prepare(List<Vertex> vertices, Vertex root)
  {
    if (!checkGraphCorrectness(vertices))
    {
      return null;
    }
    Graph graph = graphPreparer.prepareToLinearFactorization(vertices, root);
    if (graph.getLayersAmount() < 3)
    {
      return null;
    }
    return graph;
  }

  private Graph factorizeGivenGraph(Graph graph)
  {

    GraphFactorizer graphFactorizer = new GraphFactorizer(graph);
    graphFactorizer.factorize(graph);
    graphPreparer.finalizeFactorization(graph);
    return graph;
  }


  private boolean checkGraphCorrectness(List<Vertex> graph)
  {
    if (!graphCorrectnessChecker.isSimple(graph))
    {
      System.out.println(graphCorrectnessChecker.NOT_SIMPLE);
      return false;
    }
    else if (!graphCorrectnessChecker.isConnected(graph))
    {
      System.out.println(graphCorrectnessChecker.NOT_CONNECTED);
      return false;
    }
    return true;
  }
}
