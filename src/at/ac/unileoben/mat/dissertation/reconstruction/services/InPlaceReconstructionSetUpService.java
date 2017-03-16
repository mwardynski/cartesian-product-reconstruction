package at.ac.unileoben.mat.dissertation.reconstruction.services;

/**
 * Created by Marcin on 16.03.2017.
 */
public interface InPlaceReconstructionSetUpService
{
  boolean isInPlaceReconstructionToBeStarted();

  void setUpReconstructionInPlace();
}
