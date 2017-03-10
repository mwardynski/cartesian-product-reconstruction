package at.ac.unileoben.mat.dissertation.reconstruction.services;

import at.ac.unileoben.mat.dissertation.structure.Vertex;

/**
 * Created by Marcin on 10.03.2017.
 */
public interface ReconstructionBackupLayerService
{
  void addNewVertexToLayerBackup(Vertex v);

  void storeCurrentLayerBackup();

  void recoverAfterCompleteMerge();
}
