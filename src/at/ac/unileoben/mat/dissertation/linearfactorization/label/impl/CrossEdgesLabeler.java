package at.ac.unileoben.mat.dissertation.linearfactorization.label.impl;

import at.ac.unileoben.mat.dissertation.linearfactorization.label.EdgesLabeler;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.LabelUtils;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.strategies.PivotSquareFinderStrategy;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.EdgeService;
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
  FactorizationStepService factorizationStepService;

  @Autowired
  VertexService vertexService;

  @Autowired
  PivotSquareFinderStrategy crossEdgesPivotSquareFinderStrategyImpl;

  @Autowired
  LabelUtils labelUtils;

  @Autowired
  EdgeService edgeService;

  @Override
  public void labelEdges(int currentLayerNo)
  {
    List<Vertex> currentLayer = vertexService.getGraphLayer(currentLayerNo);
    List<Vertex> previousLayer = vertexService.getGraphLayer(currentLayerNo - 1);


    FactorizationSteps factorizationSteps = new FactorizationSteps(previousLayer, vertexService.getGraphSize());

    for (Vertex u : currentLayer)
    {
      Edge uw = edgeService.getFirstEdge(u, EdgeType.DOWN);
      factorizationStepService.initialVertexInsertForCrossEdges(factorizationSteps, uw);
    }

    FactorizationStep findSquareFirstPhase = factorizationSteps.getFindSquareFirstPhase();
    labelUtils.singleFindPivotSquarePhase(crossEdgesPivotSquareFinderStrategyImpl, findSquareFirstPhase, null, null);
  }
}
