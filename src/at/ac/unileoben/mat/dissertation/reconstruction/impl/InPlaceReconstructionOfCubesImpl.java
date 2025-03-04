package at.ac.unileoben.mat.dissertation.reconstruction.impl;

import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.common.impl.GraphHelperImpl;
import at.ac.unileoben.mat.dissertation.config.FactorizationConfig;
import at.ac.unileoben.mat.dissertation.linearfactorization.LinearFactorization;
import at.ac.unileoben.mat.dissertation.reconstruction.Reconstruction;
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
 * Created by Marcin on 16.03.2017.
 */
@Component
public class InPlaceReconstructionOfCubesImpl extends AbstractReconstruction implements Reconstruction
{

  @Autowired
  LinearFactorization linearFactorization;

  @Autowired
  ReconstructionData reconstructionData;

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

    Reconstruction inPlaceReconstructionOfCubes = (Reconstruction) applicationContext.getBean("inPlaceReconstructionOfCubesImpl");
    Graph resultGraph = inPlaceReconstructionOfCubes.reconstruct(vertices, root);
    int amountOfFactors = resultGraph.getGraphColoring().getActualColors().size();
    System.out.println(amountOfFactors);
  }


  @Override
  public Graph reconstruct(List<Vertex> vertices, Vertex root)
  {
    reconstructionData.setOperationOnGraph(OperationOnGraph.PRE_IN_PLACE_RECONSTRUCTION);
    return linearFactorization.factorize(vertices, root);
  }
}
