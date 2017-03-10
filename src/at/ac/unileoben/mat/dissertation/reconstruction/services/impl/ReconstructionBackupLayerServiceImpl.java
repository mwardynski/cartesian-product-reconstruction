package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.reconstruction.services.ReconstructionBackupLayerService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by Marcin on 10.03.2017.
 */
@Component
public class ReconstructionBackupLayerServiceImpl implements ReconstructionBackupLayerService
{

  @Autowired
  Graph graph;

  @Autowired
  ReconstructionData reconstructionData;

  @Override
  public void addNewVertexToLayerBackup(Vertex v)
  {
    reconstructionData.getCurrentLayerBackup().getNewUnitLayerVertices().add(v);
  }

  @Override
  public void storeCurrentLayerBackup()
  {
    if (reconstructionData.getCurrentBackupLayerNo() < reconstructionData.getCurrentLayerNo())
    {
      reconstructionData.setCurrentBackupLayerNo(reconstructionData.getCurrentLayerNo());
      reconstructionData.setPrevLayerBackup(reconstructionData.getCurrentLayerBackup());
      reconstructionData.setCurrentLayerBackup(new LayerBackupReconstructionData(new GraphColoring(graph.getGraphColoring())));
    }
  }

  @Override
  public void recoverAfterCompleteMerge()
  {
    LayerBackupReconstructionData currentLayerBackup = reconstructionData.getCurrentLayerBackup();
    currentLayerBackup.getNewUnitLayerVertices().forEach(v -> v.setUnitLayer(false));
    reconstructionData.setCurrentLayerBackup(new LayerBackupReconstructionData(currentLayerBackup.getGraphColoring()));

    LayerBackupReconstructionData prevLayerBackup = reconstructionData.getPrevLayerBackup();
    if (prevLayerBackup != null)
    {
      prevLayerBackup.getNewUnitLayerVertices().forEach(v -> v.setUnitLayer(false));
      reconstructionData.setPrevLayerBackup(new LayerBackupReconstructionData(prevLayerBackup.getGraphColoring()));
      graph.setGraphColoring(prevLayerBackup.getGraphColoring());
    }
    else
    {
      graph.setGraphColoring(currentLayerBackup.getGraphColoring());
    }
  }
}
