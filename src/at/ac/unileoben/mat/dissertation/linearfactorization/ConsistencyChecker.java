package at.ac.unileoben.mat.dissertation.linearfactorization;

/**
 * Created with IntelliJ IDEA.
 * User: Marcin
 * Date: 20.12.15
 * Time: 19:27
 * To change this template use File | Settings | File Templates.
 */
public interface ConsistencyChecker
{
  void checkConsistency(int currentLayerNo);


  boolean checkConsistencyDuringReconstruction(int currentLayerNo);
}
