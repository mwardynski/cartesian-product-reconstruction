package at.ac.unileoben.mat.dissertation.linearfactorization;

import at.ac.unileoben.mat.dissertation.common.GraphCorrectnessChecker;
import at.ac.unileoben.mat.dissertation.common.GraphReader;
import at.ac.unileoben.mat.dissertation.config.FactorizationConfig;
import at.ac.unileoben.mat.dissertation.structure.Graph;
import at.ac.unileoben.mat.dissertation.structure.Vertex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 13-11-29
 * Time: 15:17
 * To change this template use File | Settings | File Templates.
 */
@Component
public class LinearFactorization
{
  @Autowired
  Graph graph;

  @Autowired
  GraphPreparer graphPreparer;

  @Autowired
  GraphFactorizer graphFactorizer;

  @Autowired
  GraphReader graphReader;

  @Autowired
  GraphCorrectnessChecker graphCorrectnessChecker;


  public static void main(String... args)
  {
    if (args.length < 1)
    {
      System.err.println("Wrong number of arguments.\n"
              + "Please put argument with a path to the input file");
      System.exit(-1);
    }

    ApplicationContext applicationContext = new AnnotationConfigApplicationContext(FactorizationConfig.class);

    LinearFactorization linearFactorization = applicationContext.getBean(LinearFactorization.class);
    List<Vertex> vertices = linearFactorization.parseGraph(args[0]);
    Graph resultGraph;
    if (args.length == 1)
    {
      resultGraph = linearFactorization.factorize(vertices);
    }
    else if (args.length == 2)
    {
      resultGraph = linearFactorization.factorizeIncompleteGraph(vertices, Integer.parseInt(args[1]), null);
    }
    else
    {
      resultGraph = linearFactorization.factorizeIncompleteGraph(vertices, Integer.parseInt(args[1]), Integer.parseInt(args[2]));
    }
    int amountOfFactors = resultGraph.getGraphColoring().getActualColors().size();
    System.out.println(amountOfFactors);
  }


  List<Vertex> parseGraph(String graphFilePath)
  {
    return graphReader.readGraph(graphFilePath);
  }

  Graph factorize(List<Vertex> vertices)
  {
    prepare(vertices, null);
    factorizeGraph();
    return graph;
  }

  Graph factorizeIncompleteGraph(List<Vertex> vertices, Integer vertexNumberToRemove, Integer rootNumber)
  {
    Vertex root = null;
    if (rootNumber != null)
    {
      root = vertices.get(rootNumber);
    }
    graphPreparer.removeVertex(vertices, vertexNumberToRemove);
    prepare(vertices, root);
    factorizeGraph();
    return graph;
  }

  private void prepare(List<Vertex> vertices, Vertex root)
  {
    checkGraphCorrectness(vertices);
    graphPreparer.prepareToLinearFactorization(vertices, root);
    if (graph.getLayers().size() < 3)
    {
      throw new IllegalStateException(graphCorrectnessChecker.NOT_HIGH_ENOUGH);
    }
  }

  private void factorizeGraph()
  {
    graphFactorizer.factorize();
    graphPreparer.finalizeFactorization();
  }


  private void checkGraphCorrectness(List<Vertex> vertices)
  {
    if (!graphCorrectnessChecker.isSimple(vertices))
    {
      throw new IllegalArgumentException(graphCorrectnessChecker.NOT_SIMPLE);
    }
    else if (!graphCorrectnessChecker.isConnected(vertices))
    {
      throw new IllegalArgumentException(graphCorrectnessChecker.NOT_CONNECTED);
    }
  }
}
