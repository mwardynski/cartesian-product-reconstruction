package at.ac.unileoben.mat.dissertation.structure;

/**
 * Created by Marcin on 19.08.2017.
 */
public class InconsistentEdge
{
  private Edge edge;
  private InconsistentEdgeTag inconsistentEdgeTag;
  private ReconstructionEntryData reconstructionEntry;

  public InconsistentEdge(Edge edge, ReconstructionEntryData reconstructionEntry, InconsistentEdgeTag inconsistentEdgeTag)
  {
    this.edge = edge;
    this.reconstructionEntry = reconstructionEntry;
    this.inconsistentEdgeTag = inconsistentEdgeTag;
  }

  public Edge getEdge()
  {
    return edge;
  }

  public ReconstructionEntryData getReconstructionEntry()
  {
    return reconstructionEntry;
  }

  public InconsistentEdgeTag getInconsistentEdgeTag()
  {
    return inconsistentEdgeTag;
  }
}
