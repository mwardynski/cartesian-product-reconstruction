package at.ac.unileoben.mat.dissertation.structure;

import java.util.List;

/**
 * Created by mwardynski on 27/11/16.
 */
public class ReconstructionEntryData
{
  List<Edge> inconsistentEdges;
  Vertex sourceVertex;
  EdgeType edgeType;

  public ReconstructionEntryData(List<Edge> inconsistentEdges, Vertex sourceVertex, EdgeType edgeType)
  {
    this.inconsistentEdges = inconsistentEdges;
    this.sourceVertex = sourceVertex;
    this.edgeType = edgeType;
  }

  public List<Edge> getInconsistentEdges()
  {
    return inconsistentEdges;
  }

  public Vertex getSourceVertex()
  {
    return sourceVertex;
  }

  public EdgeType getEdgeType()
  {
    return edgeType;
  }
}
