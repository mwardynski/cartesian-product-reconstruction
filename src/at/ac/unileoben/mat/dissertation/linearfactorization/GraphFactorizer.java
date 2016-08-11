package at.ac.unileoben.mat.dissertation.linearfactorization;

import at.ac.unileoben.mat.dissertation.structure.FactorizationData;

/**
 * Created with IntelliJ IDEA.
 * User: Marcin
 * Date: 20.12.15
 * Time: 19:27
 * To change this template use File | Settings | File Templates.
 */
public interface GraphFactorizer
{
  void factorize();

  void factorizeSingleLayer(int currentLayerNo, FactorizationData factorizationData);
}
