package at.ac.unileoben.mat.dissertation.linearfactorization.label.impl;

import at.ac.unileoben.mat.dissertation.linearfactorization.label.EdgesLabeler;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.LabelUtils;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.strategies.PivotSquareFinderStrategy;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.FactorizationStepService;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.VertexService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 2/4/14
 * Time: 9:03 PM
 * To change this template use File | Settings | File Templates.
 */
@Component
public class CrossEdgesLabeler implements EdgesLabeler
{
  @Autowired
  Graph graph;

  @Autowired
  FactorizationStepService factorizationStepService;

  @Autowired
  VertexService vertexService = new VertexService();

  @Autowired
  PivotSquareFinderStrategy crossEdgesPivotSquareFinderStrategy;

  @Autowired
  LabelUtils labelUtils;

  @Override
  public void labelEdges(int currentLayerNo)
  {
    List<Vertex> currentLayer = vertexService.getGraphLayer(currentLayerNo);
    List<Vertex> previousLayer = vertexService.getGraphLayer(currentLayerNo - 1);


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

    FactorizationStep findSquareFirstPhase = factorizationSteps.getFindSquareFirstPhase();
    labelUtils.singleFindPivotSquarePhase(crossEdgesPivotSquareFinderStrategy, findSquareFirstPhase, null);
  }
}
