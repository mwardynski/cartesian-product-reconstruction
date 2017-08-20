package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.reconstruction.services.ReconstructionBackupLayerService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;

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
    reconstructionData.setMergeTags(new LinkedList<>());
    if (reconstructionData.getPrevLayerBackup() == null || reconstructionData.getPrevLayerBackup().getLayerNo() < reconstructionData.getCurrentLayerNo())
    {
      reconstructionData.setPrevLayerBackup(reconstructionData.getCurrentLayerBackup());
    }
    else
    {
      reconstructionData.setPrevLayerBackup(null);
    }
    reconstructionData.setCurrentLayerBackup(new LayerBackupReconstructionData(reconstructionData.getCurrentLayerNo(), new GraphColoring(graph.getGraphColoring())));
  }

  @Override
  public void recoverAfterCompleteMerge()
  {
    LayerBackupReconstructionData currentLayerBackup = reconstructionData.getCurrentLayerBackup();
    currentLayerBackup.getNewUnitLayerVertices().forEach(v -> v.setUnitLayer(false));
    reconstructionData.setCurrentLayerBackup(new LayerBackupReconstructionData(currentLayerBackup.getLayerNo(), currentLayerBackup.getGraphColoring()));

    LayerBackupReconstructionData prevLayerBackup = reconstructionData.getPrevLayerBackup();
    if (prevLayerBackup != null)
    {
      prevLayerBackup.getNewUnitLayerVertices().forEach(v -> v.setUnitLayer(false));
      reconstructionData.setPrevLayerBackup(new LayerBackupReconstructionData(prevLayerBackup.getLayerNo(), prevLayerBackup.getGraphColoring()));
      graph.setGraphColoring(new GraphColoring(prevLayerBackup.getGraphColoring()));
    }
    else
    {
      graph.setGraphColoring(new GraphColoring(currentLayerBackup.getGraphColoring()));
    }
  }
}
