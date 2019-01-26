package at.ac.unileoben.mat.dissertation.structure;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class TestCaseContext
{
  private Set<Integer> removedVertexNeighbors;
  List<Vertex> verticesToRemoveForResult;
  List<Vertex> removedVerticesWithCorrectResult;
  Edge removedEdge;
  private boolean correctResult;

  public Set<Integer> getRemovedVertexNeighbors()
  {
    return removedVertexNeighbors;
  }

  public void setRemovedVertexNeighbors(Set<Integer> removedVertexNeighbors)
  {
    this.removedVertexNeighbors = removedVertexNeighbors;
  }

  public List<Vertex> getVerticesToRemoveForResult()
  {
    return verticesToRemoveForResult;
  }

  public void setVerticesToRemoveForResult(List<Vertex> verticesToRemoveForResult)
  {
    this.verticesToRemoveForResult = verticesToRemoveForResult;
  }

  public List<Vertex> getRemovedVerticesWithCorrectResult()
  {
    return removedVerticesWithCorrectResult;
  }

  public void setRemovedVerticesWithCorrectResult(List<Vertex> removedVerticesWithCorrectResult)
  {
    this.removedVerticesWithCorrectResult = removedVerticesWithCorrectResult;
  }

  public Edge getRemovedEdge()
  {
    return removedEdge;
  }

  public void setRemovedEdge(Edge removedEdge)
  {
    this.removedEdge = removedEdge;
  }

  public boolean isCorrectResult()
  {
    return correctResult;
  }

  public void setCorrectResult(boolean correctResult)
  {
    this.correctResult = correctResult;
  }
}
