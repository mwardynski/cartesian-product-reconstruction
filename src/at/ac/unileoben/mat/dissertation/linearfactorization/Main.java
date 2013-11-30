package at.ac.unileoben.mat.dissertation.linearfactorization;

import at.ac.unileoben.mat.dissertation.common.GraphCorrectnessChecker;
import at.ac.unileoben.mat.dissertation.common.GraphReader;
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

  String graphFilePath;
  GraphReader graphReader;
  GraphCorrectnessChecker graphCorrectnessChecker;
  GraphPreparer graphPreparer;


  Main(String graphFilePath)
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

    Main main = new Main(args[0]);
    main.run();
  }

  private void run()
  {
    List<Vertex> graph = graphReader.readGraph(graphFilePath);
    if (!checkGraphCorrectness(graph))
    {
      System.exit(-1);
    }
    graphPreparer.prepareToLinearFactorization(graph);

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
