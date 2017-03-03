package at.ac.unileoben.mat.dissertation.reconstruction.impl;

import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.common.impl.GraphHelperImpl;
import at.ac.unileoben.mat.dissertation.config.FactorizationConfig;
import at.ac.unileoben.mat.dissertation.linearfactorization.LinearFactorization;
import at.ac.unileoben.mat.dissertation.reconstruction.ReconstructionAfterFindingAllFactors;
import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.FactorizationData;
import at.ac.unileoben.mat.dissertation.structure.Graph;
import at.ac.unileoben.mat.dissertation.structure.Vertex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

/**
 * Created by mwardynski on 24/04/16.
 */
@Component
public class DuplicateReconstructionAfterFindingAllFactorsImpl extends AbstractReconstructionAfterFindingAllFactors
{

  @Autowired
  GraphHelper graphHelper;

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


    ReconstructionAfterFindingAllFactors reconstruction = applicationContext.getBean("duplicateReconstructionAfterFindingAllFactorsImpl", ReconstructionAfterFindingAllFactors.class);
    Graph resultGraph = reconstruction.reconstruct(vertices);
    int amountOfFactors = resultGraph.getGraphColoring().getActualColors().size();
    System.out.println(amountOfFactors);
  }

  @Override
  public Graph reconstruct(List<Vertex> vertices)
  {
    FactorizationData factorizationData = findFactors(vertices);

    return reconstructWithFoundFactors(vertices, factorizationData);
  }

  private Graph reconstructWithFoundFactors(List<Vertex> vertices, FactorizationData factorizationData)
  {
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
}
