package at.ac.unileoben.mat.dissertation.reconstruction.services;

import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.EdgeType;
import at.ac.unileoben.mat.dissertation.structure.Vertex;

import java.util.List;

/**
 * Created by mwardynski on 26/11/16.
 */
public interface ReconstructionService
{
  void clearReconstructionData();

  boolean isReconstructionSuitableByConsistencyCheck();

  boolean isReconstructionSuitableByLabeling(int currentLayerNo);

  boolean isNewVertex(Vertex vertex);

  boolean isCorrespondingEdgesCheckForUpEdgesReasonable();

  boolean addEdgesToReconstruction(List<Edge> inconsistentEdges, Vertex baseVertex, EdgeType edgeType);

  void reconstructWithCollectedData();

  boolean isTopVertexMissingByReconstruction(int currentLayerNo);

  void prepareTopVertexReconstruction(List<Vertex> currentLayer);
}
