package at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.data;

import at.ac.unileoben.mat.dissertation.structure.Edge;

import java.util.Optional;

/**
 * Created by Marcin on 16.07.2017.
 */
public class EdgeLabelingWrapper
{
  Edge edge;
  Optional<EdgeLabelingReconstruction> potentialEdgeReconstruction;

  public EdgeLabelingWrapper(Edge edge, Optional<EdgeLabelingReconstruction> potentialEdgeReconstruction)
  {
    this.edge = edge;
    this.potentialEdgeReconstruction = potentialEdgeReconstruction;
  }

  public Edge getEdge()
  {
    return edge;
  }

  public Optional<EdgeLabelingReconstruction> getPotentialEdgeReconstruction()
  {
    return potentialEdgeReconstruction;
  }
}
