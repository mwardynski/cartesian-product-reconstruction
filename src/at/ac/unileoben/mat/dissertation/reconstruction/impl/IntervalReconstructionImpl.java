package at.ac.unileoben.mat.dissertation.reconstruction.impl;

import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.common.impl.GraphHelperImpl;
import at.ac.unileoben.mat.dissertation.config.FactorizationConfig;
import at.ac.unileoben.mat.dissertation.linearfactorization.LinearFactorization;
import at.ac.unileoben.mat.dissertation.reconstruction.Reconstruction;
import at.ac.unileoben.mat.dissertation.reconstruction.services.SingleSquareReconstructionService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.SquareHandlingStrategy;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class IntervalReconstructionImpl extends AbstractReconstruction implements Reconstruction
{

  @Autowired
  Graph graph;

  @Autowired
  LinearFactorization linearFactorization;

  @Autowired
  ReconstructionData reconstructionData;

  @Autowired
  GraphHelper graphHelper;

  @Autowired
  SingleSquareReconstructionService singleSquareReconstructionService;

  @Autowired
  SquareHandlingStrategy squareHandlingStrategy;

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
    Edge[][] adjacencyMatrix = graphHelper.createAdjacencyMatrix();
    graph.setAdjacencyMatrix(adjacencyMatrix);
    reconstructionData.setMergeOperations(new LinkedList<>());

    SquareMatchingEdgeData[][] squareMatchingEdgesByEdgeAndColor1 = new SquareMatchingEdgeData[vertices.size()][vertices.size()];
    singleSquareReconstructionService.reconstructUsingSquares(squareMatchingEdgesByEdgeAndColor1);

    if (true)
    {
      return null;
    }

    Graph originalGraph = new Graph(graph);

    List<Vertex> originalGraphVertices = new LinkedList(originalGraph.getVertices());
//    Collections.reverse(originalGraphVertices);

    boolean reconstructed = false;
    for (Vertex vertex : originalGraphVertices)
    {
      if (vertex.getBfsLayer() > 1)
      {
        //FIXME replace third vertices.size() with max grade
        SquareMatchingEdgeData[][] squareMatchingEdgesByEdgeAndColor = new SquareMatchingEdgeData[vertices.size()][vertices.size()];

        Vertex intervalRoot = findNotPrimeInterval(vertex, vertices, originalGraph, squareMatchingEdgesByEdgeAndColor);
        if (intervalRoot != null)
        {
          graphHelper.overrideGlobalGraph(originalGraph);
          singleSquareReconstructionService.reconstructUsingSquares(squareMatchingEdgesByEdgeAndColor);
//          factorsFromIntervalReconstructionService.reconstructUsingIntervalFactors(intervalRoot);
          return null;
        }
      }
//      if (vertex.getBfsLayer() == 1 || reconstructed)
//      {
//        break;
//      }
    }

    return null;
  }

  private Vertex findNotPrimeInterval(Vertex topVertex, List<Vertex> vertices, Graph originalGraph, SquareMatchingEdgeData[][] squareMatchingEdges)
  {
    SubgraphData subgraph = graphHelper.getSubgraphForTopVertices(Collections.singletonList(topVertex), vertices, false);
    List<Vertex> subgraphVertices = subgraph.getVertices();
    linearFactorization.factorize(subgraphVertices, subgraphVertices.get(subgraphVertices.size() - 1));
    if (!graphHelper.isMoreThanOneColorLeft(graph))
    {
      return null;
    }

    Vertex subgraphRoot = graph.getRoot();

    reindexSubgraphToOriginalGraph(subgraph);

    collectMatchingSquareEdges(squareMatchingEdges, originalGraph);

    transferColorsFromSubGraphToOriginalGraph(originalGraph);

    return subgraphRoot;
  }

  private void reindexSubgraphToOriginalGraph(SubgraphData subgraph)
  {
    for (Vertex v : graph.getVertices())
    {
      v.setVertexNo(subgraph.getReverseReindexArray()[v.getVertexNo()]);
    }
  }

  private void collectMatchingSquareEdges(SquareMatchingEdgeData[][] squareMatchingEdgesByEdge, Graph originalGraph)
  {
    List<Integer> subGraphColors = graph.getRoot().getEdges().stream()
            .mapToInt(e -> e.getLabel().getColor())
            .distinct()
            .mapToObj(Integer::valueOf)
            .collect(Collectors.toList());

    graph.getVertices().stream()
            .forEach(v -> v.getEdges().stream()
                    .filter(e -> e.getSquareMatchingEdge() != null)
                    .forEach(edge ->
                            {
                              int otherColor = edge.getLabel().getColor() == subGraphColors.get(0) ? subGraphColors.get(1) : subGraphColors.get(0);

                              Edge squareMatchingEdge = edge.getSquareMatchingEdge();

                              Edge[][] originalGraphAdjacencyMatrix = originalGraph.getAdjacencyMatrix();
                              Edge originalGraphEdge = originalGraphAdjacencyMatrix[edge.getOrigin().getVertexNo()][edge.getEndpoint().getVertexNo()];
                              Edge originalGraphSquareMatchingEdge = originalGraphAdjacencyMatrix[squareMatchingEdge.getOrigin().getVertexNo()][squareMatchingEdge.getEndpoint().getVertexNo()];

                              int originalGraphSize = originalGraph.getVertices().size();
                              squareHandlingStrategy.storeSingleSquareMatchingEdge(originalGraphEdge, originalGraphSquareMatchingEdge, otherColor, originalGraphSize, squareMatchingEdgesByEdge);
                              squareHandlingStrategy.storeSingleSquareMatchingEdge(originalGraphEdge.getOpposite(), originalGraphSquareMatchingEdge.getOpposite(), otherColor, originalGraphSize, squareMatchingEdgesByEdge);
                              squareHandlingStrategy.storeSingleSquareMatchingEdge(originalGraphSquareMatchingEdge, originalGraphEdge, otherColor, originalGraphSize, squareMatchingEdgesByEdge);
                              squareHandlingStrategy.storeSingleSquareMatchingEdge(originalGraphSquareMatchingEdge.getOpposite(), originalGraphEdge.getOpposite(), otherColor, originalGraphSize, squareMatchingEdgesByEdge);
                            }
                    ));
  }

  private void transferColorsFromSubGraphToOriginalGraph(Graph originalGraph)
  {
    originalGraph.setGraphColoring(graph.getGraphColoring());
    Edge[][] originalGraphAdjacencyMatrix = originalGraph.getAdjacencyMatrix();

    graph.getVertices().stream().forEach(vSub -> vSub.getEdges().stream()
            .forEach(e ->
            {
              Edge originalGraphEdge = originalGraphAdjacencyMatrix[e.getOrigin().getVertexNo()][e.getEndpoint().getVertexNo()];
              originalGraphEdge.setLabel(new Label(e.getLabel().getColor(), -1));
            }));
  }
}