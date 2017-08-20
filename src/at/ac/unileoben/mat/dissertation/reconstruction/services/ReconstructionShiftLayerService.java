package at.ac.unileoben.mat.dissertation.reconstruction.services;

/**
 * Created by Marcin on 20.08.2017.
 */
public interface ReconstructionShiftLayerService
{
  boolean isVertexToShiftAvailable();

  void shiftVertex();
}
