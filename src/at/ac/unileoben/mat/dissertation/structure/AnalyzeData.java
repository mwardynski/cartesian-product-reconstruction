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

  public void addMergeOperation(MergeOperation mergeOperation)
  {
    mergeOperations.add(mergeOperation);
  }

  public List<MergeOperation> getMergeOperations()
  {
    return mergeOperations;
  }
}


