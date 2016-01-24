package at.ac.unileoben.mat.dissertation.reconstruction.impl;

import at.ac.unileoben.mat.dissertation.common.GraphCorrectnessChecker;
import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.common.impl.GraphHelperImpl;
import at.ac.unileoben.mat.dissertation.config.FactorizationConfig;
import at.ac.unileoben.mat.dissertation.reconstruction.ProductReconstructor;
import at.ac.unileoben.mat.dissertation.reconstruction.Reconstruction;
import at.ac.unileoben.mat.dissertation.structure.Graph;
import at.ac.unileoben.mat.dissertation.structure.Vertex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Marcin
 * Date: 23.01.16
 * Time: 11:42
 * To change this template use File | Settings | File Templates.
 */
@Component
public class ReconstructionImpl implements Reconstruction
{
  @Autowired
  Graph graph;

  @Autowired
  GraphCorrectnessChecker graphCorrectnessChecker;

  @Autowired
  ProductReconstructor productReconstructor;

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

    Reconstruction reconstruction = applicationContext.getBean(ReconstructionImpl.class);
    Graph resultGraph = reconstruction.reconstruct(vertices);
    int amountOfFactors = resultGraph.getGraphColoring().getActualColors().size();
    System.out.println(amountOfFactors);
  }

  @Override
  public Graph reconstruct(List<Vertex> vertices)
  {
    checkGraphCorrectness(vertices);
    productReconstructor.reconstructProduct(vertices);
    return graph;
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
