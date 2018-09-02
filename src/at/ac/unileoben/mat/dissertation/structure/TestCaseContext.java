package at.ac.unileoben.mat.dissertation.structure;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class TestCaseContext
{
  private Set<Integer> removedVertexNeighbors;

  public Set<Integer> getRemovedVertexNeighbors()
  {
    return removedVertexNeighbors;
  }

  public void setRemovedVertexNeighbors(Set<Integer> removedVertexNeighbors)
  {
    this.removedVertexNeighbors = removedVertexNeighbors;
  }
}
