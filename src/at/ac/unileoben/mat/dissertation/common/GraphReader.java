package at.ac.unileoben.mat.dissertation.common;

import at.ac.unileoben.mat.dissertation.structure.Vertex;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 13-11-29
 * Time: 15:23
 * To change this template use File | Settings | File Templates.
 */
public interface GraphReader
{
  List<Vertex> readGraph(String fileName);
}
