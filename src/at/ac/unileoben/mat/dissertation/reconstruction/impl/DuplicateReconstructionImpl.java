package at.ac.unileoben.mat.dissertation.reconstruction.impl;

import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.common.impl.GraphHelperImpl;
import at.ac.unileoben.mat.dissertation.config.FactorizationConfig;
import at.ac.unileoben.mat.dissertation.linearfactorization.GraphFactorizationPreparer;
import at.ac.unileoben.mat.dissertation.linearfactorization.GraphFactorizer;
import at.ac.unileoben.mat.dissertation.linearfactorization.LinearFactorization;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.reconstruction.DuplicateReconstruction;
import at.ac.unileoben.mat.dissertation.reconstruction.services.ReconstructionService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

/**
 * Created by mwardynski on 24/04/16.
 */
@Component
public class DuplicateReconstructionImpl implements DuplicateReconstruction
{

  @Autowired
  Graph graph;

  @Autowired
  GraphHelper graphHelper;

  @Autowired
  GraphFactorizationPreparer graphFactorizationPreparer;

  @Autowired
  GraphFactorizer graphFactorizer;

  @Autowired
  ColoringService coloringService;

  @Autowired
  ReconstructionService reconstructionService;

  @Autowired
  LinearFactorization linearFactorization;


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


    DuplicateReconstruction duplicateReconstruction = applicationContext.getBean(DuplicateReconstruction.class);
    Graph resultGraph = duplicateReconstruction.reconstruct(vertices);
    int amountOfFactors = resultGraph.getGraphColoring().getActualColors().size();
    System.out.println(amountOfFactors);
  }

  @Override
  public Graph reconstruct(List<Vertex> vertices)
  {
    FactorizationData factorizationData = findFactors(vertices);

    graphHelper.prepareGraphBfsStructure(vertices, factorizationData.getRootVertex());
    List<List<Vertex>> factors = factorizationData.getFactors().stream()
            .map(factor -> graphHelper.getFactorForTopVertices(factor.getTopVertices(), vertices))
            .collect(toList());

    Iterator<List<Vertex>> factorsIterator = factors.iterator();
    List<Vertex> resultProduct = factorsIterator.next();
    while (factorsIterator.hasNext())
    {
      List<Vertex> secondFactor = factorsIterator.next();

      resultProduct = multiplyFactors(resultProduct, secondFactor, vertices);
      reindexResultProduct(resultProduct);
    }


    return linearFactorization.factorize(resultProduct, null);
  }

  private ArrayList<Vertex> multiplyFactors(List<Vertex> resultProduct, List<Vertex> secondFactor, List<Vertex> vertices)
  {
    List<List<Vertex>> product = new ArrayList<>();

    product.add(resultProduct);
    IntStream.range(1, secondFactor.size()).forEach(i -> product.add(duplicateFactor(resultProduct, vertices)));

    linkDuplicatedFactors(secondFactor, product);

    return product.stream().flatMap(productVertices -> productVertices.stream()).collect(toCollection(ArrayList::new));
  }

  private void reindexResultProduct(List<Vertex> resultProduct)
  {
    IntStream.range(0, resultProduct.size()).forEach(i -> resultProduct.get(i).setVertexNo(i));
  }

  private List<Vertex> duplicateFactor(List<Vertex> factor, List<Vertex> vertices)
  {
    List<Vertex> factorCopy = IntStream.range(0, factor.size())
            .mapToObj(i -> new Vertex(i, new ArrayList<Edge>(vertices.size())))
            .collect(toList());
    adjustFactorEdges(factor,
            edge ->
            {
              Vertex originVertexCopy = factorCopy.get(edge.getOrigin().getVertexNo());
              Vertex endpointVertexCopy = factorCopy.get(edge.getEndpoint().getVertexNo());
              graphHelper.createEdgeBetweenVertices(originVertexCopy, endpointVertexCopy);
            });
    return factorCopy;
  }

  private void linkDuplicatedFactors(List<Vertex> secondFactor, List<List<Vertex>> product)
  {
    adjustFactorEdges(secondFactor,
            edge ->
            {
              int originVertexNo = edge.getOrigin().getVertexNo();
              int endpointVertexNo = edge.getEndpoint().getVertexNo();
              IntStream.range(0, product.get(0).size()).forEach(i ->
              {
                Vertex productEdgeOriginVertex = product.get(originVertexNo).get(i);
                Vertex productEdgeEndpointVertex = product.get(endpointVertexNo).get(i);
                graphHelper.createEdgeBetweenVertices(productEdgeOriginVertex, productEdgeEndpointVertex);
              });
            });
  }

  private void adjustFactorEdges(List<Vertex> factorVertices, Consumer<Edge> consumer)
  {
    factorVertices.stream()
            .flatMap(vertex -> vertex.getEdges().stream())
            .filter(edge -> edge.getOrigin().getVertexNo() < edge.getEndpoint().getVertexNo())
            .forEach(edge -> consumer.accept(edge));
  }

  @Override
  public FactorizationData findFactors(List<Vertex> vertices)
  {
    FactorizationResultData factorizationResultData = new FactorizationResultData();
    for (Vertex vertex : vertices)
    {
      findFactorsForRoot(vertices, vertex, factorizationResultData);
      graphHelper.revertGraphBfsStructure();
    }
    return factorizationResultData.getResultFactorization();
  }

  private void findFactorsForRoot(List<Vertex> vertices, Vertex root, FactorizationResultData factorizationResultData)
  {
    clearVerticesAndEdges(vertices);
    graphHelper.prepareGraphBfsStructure(vertices, root);
    graph.setOperationOnGraph(OperationOnGraph.RECONSTRUCT);
    graphFactorizationPreparer.arrangeFirstLayerEdges();

    int layersAmount = graph.getLayers().size();
    FactorizationData factorizationData = new FactorizationData(layersAmount - 1, root);
    factorizationResultData.setCurrentFactorization(factorizationData);

    collectFirstLayerFactors(vertices, root, factorizationResultData);
    findFactorsForPreparedGraph(factorizationResultData);
  }

  private void clearVerticesAndEdges(List<Vertex> vertices)
  {
    for (Vertex vertex : vertices)
    {
      vertex.setUnitLayer(false);
      for (Edge edge : vertex.getEdges())
      {
        edge.setLabel(null);
      }
    }
  }

  private void collectFirstLayerFactors(List<Vertex> vertices, Vertex root, FactorizationResultData factorizationResultData)
  {
    List<List<Vertex>> topUnitLayerVertices = reconstructionService.createTopVerticesList(graph.getGraphColoring().getOriginalColorsAmount());
    int[] unitLayerVerticesAmountPerColor = new int[graph.getGraphColoring().getOriginalColorsAmount()];
    for (Edge e : root.getUpEdges().getEdges())
    {
      int arbitraryEdgeColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), e.getLabel().getColor());
      Vertex v = e.getEndpoint();
      topUnitLayerVertices.get(arbitraryEdgeColor).add(v);
      unitLayerVerticesAmountPerColor[arbitraryEdgeColor]++;
    }
    int graphSizeWithFoundFactors = 1;
    for (int edgesAmount : unitLayerVerticesAmountPerColor)
    {
      if (edgesAmount != 0)
      {
        graphSizeWithFoundFactors *= (edgesAmount + 1);
      }
    }
    if (vertices.size() + 1 == graphSizeWithFoundFactors)
    {
      FactorizationData currentFactorization = factorizationResultData.getCurrentFactorization();
      reconstructionService.collectFactors(currentFactorization, topUnitLayerVertices);
      currentFactorization.setMaxConsistentLayerNo(1);
      currentFactorization.setFactorizationCompleted(true);
    }
  }

  private void findFactorsForPreparedGraph(FactorizationResultData factorizationResultData)
  {
    int layersAmount = graph.getLayers().size();
    for (int currentLayerNo = 2; currentLayerNo < layersAmount; currentLayerNo++)
    {
      if (graph.getOperationOnGraph() == OperationOnGraph.RECONSTRUCT && breakProcessing(currentLayerNo, factorizationResultData))
      {
        break;
      }
      else
      {
        graphFactorizer.factorizeSingleLayer(currentLayerNo, factorizationResultData);
      }
    }
  }

  private boolean breakProcessing(int currentLayer, FactorizationResultData factorizationResultData)
  {

    boolean breakProcessing = isSingleFactor() || isLastIncompleteLayer(currentLayer, factorizationResultData);
    if (breakProcessing)
    {
      reconstructionService.updateFactorizationResult(factorizationResultData);
    }
    return breakProcessing;
  }

  private boolean isSingleFactor()
  {
    return graph.getGraphColoring().getActualColors().size() == 1;
  }

  private boolean isLastIncompleteLayer(int currentLayer, FactorizationResultData factorizationResultData)
  {
    boolean lastIncompleteLayer = false;
    FactorizationData currentFactorizationData = factorizationResultData.getCurrentFactorization();
    if (currentLayer == graph.getLayers().size() - 1)
    {
      OptionalInt maxTopVerticesSizeOptional = currentFactorizationData.getFactors().stream().mapToInt(factorData -> factorData.getTopVertices().size()).max();
      if (maxTopVerticesSizeOptional.isPresent() && graph.getLayers().get(currentLayer).size() < maxTopVerticesSizeOptional.getAsInt())
      {
        lastIncompleteLayer = true;

      }
    }
    return lastIncompleteLayer;
  }

}
