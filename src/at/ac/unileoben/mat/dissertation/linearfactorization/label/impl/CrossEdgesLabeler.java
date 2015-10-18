package at.ac.unileoben.mat.dissertation.linearfactorization.label.impl;

import at.ac.unileoben.mat.dissertation.linearfactorization.label.EdgesLabeler;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.LabelUtils;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.strategies.PivotSquareFinderStrategy;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.strategies.impl.CrossEdgesPivotSquareFinderStrategy;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.FactorizationStepService;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.VertexService;
import at.ac.unileoben.mat.dissertation.structure.*;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 2/4/14
 * Time: 9:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class CrossEdgesLabeler implements EdgesLabeler
{
  FactorizationStepService factorizationStepService = new FactorizationStepService();
  VertexService vertexService = new VertexService();

  private Graph graph;

  public CrossEdgesLabeler(Graph graph)
  {
    this.graph = graph;
  }

  @Override
  public void labelEdges(int currentLayerNo)
  {
    List<Vertex> currentLayer = vertexService.getLayer(graph, currentLayerNo);
    List<Vertex> previousLayer = vertexService.getLayer(graph, currentLayerNo - 1);


    FactorizationSteps factorizationSteps = new FactorizationSteps(previousLayer);

    for (Vertex u : currentLayer)
    {
      List<Edge> uDownEdges = u.getDownEdges().getEdges();
      Edge uw = uDownEdges.get(0);
      u.setFirstEdge(uw);
      u.setEdgeWithColorToLabel(uw);
      Vertex w = uw.getEndpoint();
      factorizationStepService.initialVertexInsertForCrossEdges(factorizationSteps, u, w);
    }

    PivotSquareFinderStrategy pivotSquareFinderStrategy = new CrossEdgesPivotSquareFinderStrategy();
    FactorizationStep findSquareFirstPhase = factorizationSteps.getFindSquareFirstPhase();
    LabelUtils.singleFindPivotSquarePhase(graph, pivotSquareFinderStrategy, findSquareFirstPhase, null);
  }
}
