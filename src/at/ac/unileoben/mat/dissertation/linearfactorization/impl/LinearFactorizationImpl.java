package at.ac.unileoben.mat.dissertation.linearfactorization.impl;

import at.ac.unileoben.mat.dissertation.common.GraphCorrectnessChecker;
import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.common.impl.GraphHelperImpl;
import at.ac.unileoben.mat.dissertation.config.FactorizationConfig;
import at.ac.unileoben.mat.dissertation.linearfactorization.GraphFactorizationPreparer;
import at.ac.unileoben.mat.dissertation.linearfactorization.GraphFactorizer;
import at.ac.unileoben.mat.dissertation.linearfactorization.LinearFactorization;
import at.ac.unileoben.mat.dissertation.structure.Graph;
import at.ac.unileoben.mat.dissertation.structure.OperationOnGraph;
import at.ac.unileoben.mat.dissertation.structure.ReconstructionData;
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
public class LinearFactorizationImpl implements LinearFactorization
{
  @Autowired
  Graph graph;

  @Autowired
  ReconstructionData reconstructionData;

  @Autowired
  GraphHelper graphHelper;

  @Autowired
  GraphFactorizationPreparer graphFactorizationPreparer;

  @Autowired
  GraphFactorizer graphFactorizer;

  @Autowired
  GraphCorrectnessChecker graphCorrectnessChecker;


  public static void main(String... args)
  {
    if (args.length < 1)
    {
      System.err.println("Wrong number of arguments.\n"
              + "Please put at least one argument with a path to the input file");
      System.exit(-1);
    }

    ApplicationContext applicationContext = new AnnotationConfigApplicationContext(FactorizationConfig.class);

    GraphHelper graphHelper = applicationContext.getBean(GraphHelperImpl.class);
    List<Vertex> vertices = graphHelper.parseGraph(args[0]);

    Vertex root = null;
    if (args.length > 1)
    {
      root = vertices.get(Integer.parseInt(args[1]));
    }

    LinearFactorization linearFactorization = applicationContext.getBean(LinearFactorizationImpl.class);
    Graph resultGraph = linearFactorization.factorize(vertices, root);
    int amountOfFactors = resultGraph.getGraphColoring().getActualColors().size();
    System.out.println(amountOfFactors);
  }


  @Override
  public Graph factorize(List<Vertex> vertices, Vertex root)
  {
    prepare(vertices, root);
    if (reconstructionData.getOperationOnGraph() == null
            || reconstructionData.getOperationOnGraph() == OperationOnGraph.RECONSTRUCT)
    {
      reconstructionData.setOperationOnGraph(OperationOnGraph.FACTORIZE);
    }
    factorizeGraph();
    return graph;
  }

  private void prepare(List<Vertex> vertices, Vertex root)
  {
    checkGraphCorrectness(vertices);
    graphHelper.prepareGraphBfsStructure(vertices, root);
    graphFactorizationPreparer.arrangeFirstLayerEdges();
    if (graph.getLayers().size() < 3)
    {
      throw new IllegalStateException(graphCorrectnessChecker.NOT_HIGH_ENOUGH);
    }
  }

  private void factorizeGraph()
  {
    graphFactorizer.factorize();
    graphHelper.revertGraphBfsStructure();
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
