package at.ac.unileoben.mat.dissertation.reconstruction.impl;

import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.common.impl.GraphHelperImpl;
import at.ac.unileoben.mat.dissertation.config.FactorizationConfig;
import at.ac.unileoben.mat.dissertation.reconstruction.ReconstructionAfterFindingAllFactors;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by mwardynski on 26/11/16.
 */
@Component
public class InPlaceReconstructionAfterFindingAllFactorsImpl extends AbstractReconstructionAfterFindingAllFactors
{

  @Autowired
  ReconstructionData reconstructionData;

  @Autowired
  GraphHelper graphHelper;

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


    ReconstructionAfterFindingAllFactors reconstruction = applicationContext.getBean(InPlaceReconstructionAfterFindingAllFactorsImpl.class);
    Graph resultGraph = reconstruction.reconstruct(vertices);
    int amountOfFactors = resultGraph.getGraphColoring().getActualColors().size();
    System.out.println(amountOfFactors);
  }

  @Override
  public Graph reconstruct(List<Vertex> vertices)
  {
    FactorizationData factorizationData = findFactors(vertices);
    Graph graph = reconstructWithFoundFactors(vertices, factorizationData);
    graphHelper.revertGraphBfsStructure();
    return graph;
  }

  private Graph reconstructWithFoundFactors(List<Vertex> vertices, FactorizationData factorizationData)
  {
    reconstructionData.setOperationOnGraph(OperationOnGraph.IN_PLACE_RECONSTRUCTION);
    return findFactorsForRoot(vertices, factorizationData.getRootVertex());
  }
}
