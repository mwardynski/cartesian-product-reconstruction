package at.ac.unileoben.mat.dissertation.reconstruction.services;

import at.ac.unileoben.mat.dissertation.structure.ResultMissingSquaresData;

public interface ReconstructionResultVerifier
{
  void compareFoundMissingVertexWithCorrectResult(ResultMissingSquaresData resultMissingSquaresData);
}
