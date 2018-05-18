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
import java.util.LinkedList;
import java.util.List;

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

    linearFactorization.prepare(vertices, root, false);

    Graph originalGraph = new Graph(graph);

    List<Vertex> originalGraphVertices = new LinkedList(originalGraph.getVertices());
    Collections.reverse(originalGraphVertices);

    boolean reconstructed = false;
    for (Vertex vertex : originalGraphVertices)
    {
      if (vertex.getBfsLayer() > 1)
      {
        boolean foundNotPrimeInterval = findNotPrimeInterval(vertex, vertices, originalGraph);
        if (foundNotPrimeInterval)
        {

        }
      }
      if (vertex.getBfsLayer() == 1 || reconstructed)
      {
        break;
      }
    }

    return null;
  }

  private boolean findNotPrimeInterval(Vertex topVertex, List<Vertex> vertices, Graph originalGraph)
  {
    SubgraphData subgraph = graphHelper.getSubgraphForTopVertices(Collections.singletonList(topVertex), vertices);
    List<Vertex> subgraphVertices = subgraph.getVertices();
    linearFactorization.factorize(subgraphVertices, subgraphVertices.get(subgraphVertices.size() - 1));
    if (!graphHelper.isMoreThanOneColorLeft(graph))
    {
      return false;
    }

    reindexSubgraphToOriginalGraph(subgraph);

    transferColorsFromSubGraphToOriginalGraph(originalGraph);

    return true;
  }

  private void reindexSubgraphToOriginalGraph(SubgraphData subgraph)
  {
    for (Vertex v : graph.getVertices())
    {
      v.setVertexNo(subgraph.getReverseReindexArray()[v.getVertexNo()]);
    }
  }

  private void transferColorsFromSubGraphToOriginalGraph(Graph originalGraph)
  {
    List<Vertex> originalGraphVertices = originalGraph.getVertices();

    for (Vertex vSub : graph.getVertices())
    {

      Vertex vOrig = originalGraphVertices.get(vSub.getVertexNo());

      Edge[] vOrigEdges = new Edge[originalGraphVertices.size()];
      vOrig.getEdges().stream().forEach(e -> vOrigEdges[e.getEndpoint().getVertexNo()] = e);

      vSub.getEdges().stream()
              .forEach(e -> vOrigEdges[e.getEndpoint().getVertexNo()].setLabel(new Label(e.getLabel().getColor(), -1)));
    }
  }
}