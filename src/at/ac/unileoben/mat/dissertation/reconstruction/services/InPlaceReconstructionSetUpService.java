package at.ac.unileoben.mat.dissertation.reconstruction.services;

import at.ac.unileoben.mat.dissertation.structure.InconsistentEdge;
import at.ac.unileoben.mat.dissertation.structure.Vertex;

import java.util.List;
import java.util.Optional;

/**
 * Created by Marcin on 16.03.2017.
 */
public interface InPlaceReconstructionSetUpService
{
  boolean isInPlaceReconstructionToBeStarted();

  void setUpReconstructionInPlace();

  Optional<Vertex> findCorrespondingVertexToMissingVertexToBeCreatedLater(List<InconsistentEdge> uvInconsistentEdges, List<InconsistentEdge> uwInconsistentEdges);

  void reconstructMissingVertexToBeCreatedLater(Vertex correspondingVertex);
}
