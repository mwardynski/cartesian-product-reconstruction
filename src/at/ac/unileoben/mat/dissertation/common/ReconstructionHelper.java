package at.ac.unileoben.mat.dissertation.common;

import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.EdgeType;
import at.ac.unileoben.mat.dissertation.structure.Vertex;

import java.util.List;

/**
 * Created by mwardynski on 26/11/16.
 */
public interface ReconstructionHelper
{
  void clearReconstructionData();

  boolean isReconstructionSuitableByConsistancyCheck();

  boolean isReconstructionSuitableByLabeling(int currentLayerNo);

  boolean addEdgesToReconstruction(List<Edge> inconsistentEdges, Vertex baseVertex, EdgeType edgeType);

  void reconstructWithCollectedData();

  boolean isTopVertexMissingByReconstruction(int currentLayerNo);

  void prepareTopVertexReconstruction(List<Vertex> currentLayer);
}
