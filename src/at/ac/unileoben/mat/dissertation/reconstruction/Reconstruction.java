package at.ac.unileoben.mat.dissertation.reconstruction;

import at.ac.unileoben.mat.dissertation.structure.Graph;
import at.ac.unileoben.mat.dissertation.structure.Vertex;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Marcin
 * Date: 23.01.16
 * Time: 11:41
 * To change this template use File | Settings | File Templates.
 */
public interface Reconstruction
{
  Graph reconstruct(List<Vertex> vertices);
}
