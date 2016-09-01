package at.ac.unileoben.mat.dissertation.linearfactorization.impl;

import at.ac.unileoben.mat.dissertation.linearfactorization.ConsistencyChecker;
import at.ac.unileoben.mat.dissertation.linearfactorization.GraphFactorizer;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.impl.CrossEdgesLabeler;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.impl.DownEdgesLabeler;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.impl.UpEdgesLabeler;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.VertexService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.ReconstructionService;
import at.ac.unileoben.mat.dissertation.structure.FactorizationResultData;
import at.ac.unileoben.mat.dissertation.structure.Graph;
import at.ac.unileoben.mat.dissertation.structure.OperationOnGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 14-01-29
 * Time: 20:15
 * To change this template use File | Settings | File Templates.
 */
@Component
public class GraphFactorizerImpl implements GraphFactorizer
{
  @Autowired
  Graph graph;

  @Autowired
  DownEdgesLabeler downEdgesLabeler;

  @Autowired
  CrossEdgesLabeler crossEdgesLabeler;

  @Autowired
  UpEdgesLabeler upEdgesLabeler;

  @Autowired
  ConsistencyChecker consistencyChecker;
  @Autowired
  VertexService vertexService;
  @Autowired
  ColoringService coloringService;

  @Autowired
  ReconstructionService reconstructionService;

  @Override
  public void factorize()
  {
    int layersAmount = graph.getLayers().size();
    for (int currentLayerNo = 2; currentLayerNo < layersAmount; currentLayerNo++)
    {
      factorizeSingleLayer(currentLayerNo, null);
    }
  }

  @Override
  public void factorizeSingleLayer(int currentLayerNo, FactorizationResultData factorizationResultData)
  {
    downEdgesLabeler.labelEdges(currentLayerNo);
    crossEdgesLabeler.labelEdges(currentLayerNo);
    upEdgesLabeler.labelEdges(currentLayerNo);

    if (graph.getOperationOnGraph() != OperationOnGraph.RECONSTRUCT)
    {
      consistencyChecker.checkConsistency(currentLayerNo);
    }
    else
    {
      reconstructionService.findReconstructionComponents(currentLayerNo, factorizationResultData, false);
      consistencyChecker.checkConsistency(currentLayerNo);
      reconstructionService.findReconstructionComponents(currentLayerNo, factorizationResultData, true);
    }
  }
}
