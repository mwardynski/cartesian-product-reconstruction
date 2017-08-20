package at.ac.unileoben.mat.dissertation.linearfactorization.impl;

import at.ac.unileoben.mat.dissertation.linearfactorization.ConsistencyChecker;
import at.ac.unileoben.mat.dissertation.linearfactorization.GraphFactorizer;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.impl.CrossEdgesLabeler;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.impl.DownEdgesLabeler;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.impl.UpEdgesLabeler;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.VertexService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.DetermineFactorsService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.ReconstructionBackupLayerService;
import at.ac.unileoben.mat.dissertation.structure.Graph;
import at.ac.unileoben.mat.dissertation.structure.OperationOnGraph;
import at.ac.unileoben.mat.dissertation.structure.ReconstructionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

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
  ReconstructionData reconstructionData;

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
  DetermineFactorsService determineFactorsService;

  @Autowired
  ReconstructionBackupLayerService reconstructionBackupLayerService;

  @Override
  public void factorize()
  {
    int layersAmount = graph.getLayers().size();
    for (int currentLayerNo = 2; currentLayerNo < layersAmount; currentLayerNo++)
    {
      reconstructionData.setCurrentLayerNo(currentLayerNo);
      reconstructionBackupLayerService.storeCurrentLayerBackup();
      factorizeSingleLayer(currentLayerNo);
      if (reconstructionData.getLayerNoToRefactorizeFromOptional().isPresent())
      {
        currentLayerNo = reconstructionData.getLayerNoToRefactorizeFromOptional().get() - 1;
        reconstructionData.setLayerNoToRefactorizeFromOptional(Optional.empty());
        layersAmount = graph.getLayers().size();
      }
    }
  }

  @Override
  public void factorizeSingleLayer(int currentLayerNo)
  {
    downEdgesLabeler.labelEdges(currentLayerNo);
    crossEdgesLabeler.labelEdges(currentLayerNo);
    upEdgesLabeler.labelEdges(currentLayerNo);

    if (reconstructionData.getOperationOnGraph() != OperationOnGraph.RECONSTRUCT)
    {
      consistencyChecker.checkConsistency(currentLayerNo);
    }
    else
    {
      determineFactorsService.findReconstructionComponents(currentLayerNo, false);
      consistencyChecker.checkConsistency(currentLayerNo);
      determineFactorsService.findReconstructionComponents(currentLayerNo, true);
    }
  }
}
