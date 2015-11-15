package at.ac.unileoben.mat.dissertation.printout.data;

import at.ac.unileoben.mat.dissertation.structure.Edge;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Marcin
 * Date: 15.11.15
 * Time: 18:50
 * To change this template use File | Settings | File Templates.
 */
public class EdgeStyleDefinition
{
  private List<Edge> edges;
  private String style;

  public EdgeStyleDefinition(List<Edge> edges, String style)
  {
    this.edges = edges;
    this.style = style;
  }

  public List<Edge> getEdges()
  {
    return edges;
  }

  public String getStyle()
  {
    return style;
  }
}
