package at.ac.unileoben.mat.dissertation.reconstruction.strategies;

import at.ac.unileoben.mat.dissertation.structure.Graph;
import at.ac.unileoben.mat.dissertation.structure.Vertex;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Marcin
 * Date: 24.01.16
 * Time: 11:35
 * To change this template use File | Settings | File Templates.
 */
public interface C8BasedReconstructionStrategy
{
  Graph reconstruct(List<Vertex> vertices);
}
