package at.ac.unileoben.mat.dissertation.structure;

import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 14-11-06
 * Time: 19:49
 * To change this template use File | Settings | File Templates.
 */
public class AnalyzeData
{

  private List<MergeOperation> mergeOperations = new LinkedList<MergeOperation>();

  public void addMergeOperation(List<Edge> edges, MergeTagEnum mergeTag)
  {
    MergeOperation mergeOperation = new MergeOperation(edges, mergeTag);
    mergeOperations.add(mergeOperation);
  }

  public List<MergeOperation> getMergeOperations()
  {
    return mergeOperations;
  }

  public static class MergeOperation
  {
    int colorsLeft;
    List<Edge> edgesByMerge;
    MergeTagEnum mergeTag;

    public MergeOperation(List<Edge> edgesByMerge, MergeTagEnum mergeTag)
    {
      this.edgesByMerge = edgesByMerge;
      this.mergeTag = mergeTag;
    }


    public List<Edge> getEdgesByMerge()
    {
      return edgesByMerge;
    }

    public MergeTagEnum getMergeTag()
    {
      return mergeTag;
    }
  }
}


