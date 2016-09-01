package at.ac.unileoben.mat.dissertation.reconstruction.services;

import at.ac.unileoben.mat.dissertation.structure.FactorizationData;
import at.ac.unileoben.mat.dissertation.structure.FactorizationResultData;
import at.ac.unileoben.mat.dissertation.structure.Vertex;

import java.util.List;

/**
 * Created by mwardynski on 12/06/16.
 */
public interface ReconstructionService
{
  List<List<Vertex>> createTopVerticesList(int originalColorsAmount);

  void updateFactorizationResult(FactorizationResultData factorizationResultData);

  void findReconstructionComponents(int currentLayerNo, FactorizationResultData factorizationResultData, boolean afterConsistencyCheck);

  void collectFactors(FactorizationData factorizationData, List<List<Vertex>> topUnitLayerVertices);
}
