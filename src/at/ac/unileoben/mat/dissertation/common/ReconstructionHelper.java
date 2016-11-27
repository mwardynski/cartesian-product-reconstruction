package at.ac.unileoben.mat.dissertation.common;

import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.EdgeType;

import java.util.List;

/**
 * Created by mwardynski on 26/11/16.
 */
public interface ReconstructionHelper
{
  void clearReconstructionData();

  boolean addEdgesToReconstruction(List<Edge> inconsistentEdges, Edge baseEdge, EdgeType edgeType);

  void reconstructWithCollectedData();
}
