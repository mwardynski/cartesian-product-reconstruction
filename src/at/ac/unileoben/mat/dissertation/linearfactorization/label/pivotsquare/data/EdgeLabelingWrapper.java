package at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.data;

import at.ac.unileoben.mat.dissertation.structure.Edge;

import java.util.Optional;

/**
 * Created by Marcin on 16.07.2017.
 */
public class EdgeLabelingWrapper
{
  Edge edge;
  Optional<EdgeLabelingReconstruction> scnd;

  public EdgeLabelingWrapper(Edge edge, Optional<EdgeLabelingReconstruction> scnd)
  {
    this.edge = edge;
    this.scnd = scnd;
  }

  public Edge getEdge()
  {
    return edge;
  }

  public Optional<EdgeLabelingReconstruction> getScnd()
  {
    return scnd;
  }
}
