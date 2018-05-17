package at.ac.unileoben.mat.dissertation.reconstruction.impl;

import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.common.impl.GraphHelperImpl;
import at.ac.unileoben.mat.dissertation.config.FactorizationConfig;
import at.ac.unileoben.mat.dissertation.linearfactorization.LinearFactorization;
import at.ac.unileoben.mat.dissertation.reconstruction.Reconstruction;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class IntervalReconstructionImpl extends AbstractReconstruction implements Reconstruction
{

  @Autowired
  LinearFactorization linearFactorization;

  @Autowired
  ReconstructionData reconstructionData;

  @Autowired
  GraphHelper graphHelper;

  @Autowired
  Graph graph;

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

    Reconstruction intervalReconstruction = (Reconstruction) applicationContext.getBean("intervalReconstructionImpl");
    Graph resultGraph = intervalReconstruction.reconstruct(vertices, root);
    int amountOfFactors = resultGraph.getGraphColoring().getActualColors().size();
    System.out.println(amountOfFactors);
  }


  @Override
  public Graph reconstruct(List<Vertex> vertices, Vertex root)
  {
    reconstructionData.setOperationOnGraph(OperationOnGraph.FINDING_INTERVAL);
    reconstructionData.setIntervalData(new IntervalData(vertices.size()));

    linearFactorization.prepare(vertices, root);

    Graph graphCopy = new Graph(graph);

    List<Vertex> intervalTopVertices = IntStream.range(2, graph.getLayers().size())
            .mapToObj(i -> graphCopy.getLayers().get(i))
            .flatMap(layer -> layer.stream())
            .filter(v -> isTopVertexOfInterval(v, vertices))
            .collect(Collectors.toList());

    List<Integer> mappedTopVertices = intervalTopVertices.stream()
            .map(v -> graphCopy.getReverseReindexArray()[v.getVertexNo()])
            .collect(Collectors.toList());

    return null;
  }

  private boolean isTopVertexOfInterval(Vertex topVertex, List<Vertex> vertices)
  {
    List<Vertex> subgraph = graphHelper.getSubgraphForTopVertices(Collections.singletonList(topVertex), vertices);

    linearFactorization.factorize(subgraph, null);

    return graph.getGraphColoring().getActualColors().size() != 1;
  }
}