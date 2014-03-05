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
    linearFactorization.run();
  }

  void run()
  {
    List<Vertex> vvv = graphReader.readGraph(graphFilePath);
    int vertexIndexToRemove = vvv.size() - 1;
    while (true)
    {
      List<Vertex> vertices = graphReader.readGraph(graphFilePath);
      if (vertices.size() > vertexIndexToRemove)
      {
//        graphPreparer.removeVertex(vertices, vertexIndexToRemove);
//        System.out.println("removed vertex: " + vertexIndexToRemove);
        vertexIndexToRemove++;
      }
      else
      {
        break;
      }
      if (!checkGraphCorrectness(vertices))
      {
        continue;
      }
      int[] reindexArray = new int[vertices.size()];
      Graph graph = graphPreparer.prepareToLinearFactorization(vertices, reindexArray);
      GraphFactorizer graphFactorizer = new GraphFactorizer(graph);
      graphFactorizer.factorize(graph);
      graphPreparer.finalizeFactorization(graph, reindexArray);
      System.out.println(graph.getGraphColoring().getActualColors().size());
    }
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
