package at.ac.unileoben.mat.dissertation.reconstruction;

import at.ac.unileoben.mat.dissertation.structure.FactorizationData;
import at.ac.unileoben.mat.dissertation.structure.Vertex;

import java.util.List;

/**
 * Created by mwardynski on 24/04/16.
 */
public interface DuplicateReconstruction extends Reconstruction
{
  FactorizationData findFactors(List<Vertex> vertices);
}
