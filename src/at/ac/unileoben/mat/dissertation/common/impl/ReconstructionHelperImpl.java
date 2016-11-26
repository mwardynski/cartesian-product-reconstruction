package at.ac.unileoben.mat.dissertation.common.impl;

import at.ac.unileoben.mat.dissertation.common.ReconstructionHelper;
import at.ac.unileoben.mat.dissertation.structure.ReconstructionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by mwardynski on 26/11/16.
 */
@Component
public class ReconstructionHelperImpl implements ReconstructionHelper
{

  @Autowired
  ReconstructionData reconstructionData;

  @Override
  public void clearReconstructionData()
  {
    reconstructionData.setCurrentFactorization(null);
    reconstructionData.setResultFactorization(null);
  }
}
