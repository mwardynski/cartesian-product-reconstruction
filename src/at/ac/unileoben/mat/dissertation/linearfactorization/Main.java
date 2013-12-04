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
public class Main
{

  private String graphFilePath;
  private GraphReader graphReader;
  private GraphCorrectnessChecker graphCorrectnessChecker;
  private GraphPreparer graphPreparer;
  private GraphFactorizer graphFactorizer;


  Main(String graphFilePath)
  {
    this.graphFilePath = graphFilePath;
    graphReader = new GraphReader();
    graphCorrectnessChecker = new GraphCorrectnessChecker();
    graphPreparer = new GraphPreparer();
    graphFactorizer = new GraphFactorizer();
  }

  public static void main(String... args)
  {
    if (args.length != 1)
    {
      System.err.println("Wrong number of arguments.\n"
              + "Please put only one argument with a path to the input file");
      System.exit(-1);
    }

    Main main = new Main(args[0]);
    main.run();
  }

  void run()
  {
    List<Vertex> vertices = graphReader.readGraph(graphFilePath);
    if (!checkGraphCorrectness(vertices))
    {
      System.exit(-1);
    }
    Graph graph = graphPreparer.prepareToLinearFactorization(vertices);
    graphFactorizer.factorize(graph);
  }

  private boolean checkGraphCorrectness(List<Vertex> graph)
  {
    if (!graphCorrectnessChecker.isSimple(graph))
    {
      System.err.println(graphCorrectnessChecker.NOT_SIMPLE);
      return false;
    }
    return true;
  }
}
