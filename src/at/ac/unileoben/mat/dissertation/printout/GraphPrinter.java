package at.ac.unileoben.mat.dissertation.printout;

import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.VertexService;
import at.ac.unileoben.mat.dissertation.printout.data.EdgeData;
import at.ac.unileoben.mat.dissertation.printout.data.VertexData;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * User: Marcin
 * Date: 18.10.15
 * Time: 12:40
 * To change this template use File | Settings | File Templates.
 */
@Component
public class GraphPrinter
{
  public static final double EDGE_LENGTH = 2.0;

  @Autowired
  Graph graph;

  @Autowired
  ColoringService coloringService;

  @Autowired
  VertexService vertexService;

  List<String> steps;
  String[] edgeColors = {"red", "green", "blue", "yellow"};

  public GraphPrinter()
  {
    Velocity.init();
    this.steps = new LinkedList<>();
  }

  public void printFactorization()
  {
    VelocityContext context = new VelocityContext();
    context.put("steps", steps);

    try (PrintWriter printWriter = new PrintWriter("out/tex/factorization.tex", StandardCharsets.UTF_8.name()))
    {
      Velocity.mergeTemplate("resources/vm/factorization.vm", StandardCharsets.UTF_8.name(), context, printWriter);
      printWriter.flush();
    }
    catch (FileNotFoundException | UnsupportedEncodingException e)
    {
      e.printStackTrace();
    }
  }

  public void createGraphSnapshot()
  {
    VelocityContext context = new VelocityContext();

    List<VertexData> vertices = prepareVertices();
    List<EdgeData> edges = prepareEdges();
    context.put("vertices", vertices);
    context.put("edges", edges);

    StringWriter stringWriter = new StringWriter();

    Velocity.mergeTemplate("resources/vm/step.vm", StandardCharsets.UTF_8.name(), context, stringWriter);
    steps.add(stringWriter.toString());
  }

  private List<VertexData> prepareVertices()
  {
    List<VertexData> vertices = new LinkedList<>();

    for (int layerNo = 0; layerNo < graph.getLayers().size(); layerNo++)
    {
      List<Vertex> layer = vertexService.getGraphLayer(layerNo);

      for (int vertexInLayerNo = 0; vertexInLayerNo < layer.size(); vertexInLayerNo++)
      {
        Vertex vertex = layer.get(vertexInLayerNo);
        VertexData vertexData = new VertexData();
        vertexData.setVertexNo(vertex.getVertexNo());
        vertexData.setOrigVertexNo(graph.getReindexArray()[vertex.getVertexNo()]);

        double posX = -layer.size() / (2.0 / EDGE_LENGTH) + (0.5 * EDGE_LENGTH) + (vertexInLayerNo * EDGE_LENGTH);
        vertexData.setPosX(posX);
        vertexData.setPosY(layerNo * EDGE_LENGTH);
        vertices.add(vertexData);
      }
    }
    return vertices;
  }

  private List<EdgeData> prepareEdges()
  {
    List<EdgeData> edges = new LinkedList<>();

    for (Vertex vertex : graph.getVertices())
    {
      GraphColoring graphColoring = graph.getGraphColoring();

      List<EdgeData> downEdges = vertex.getDownEdges().getEdges().stream().map(edge -> mapSingleEdge(edge, graphColoring)).collect(Collectors.toList());
      edges.addAll(downEdges);

      List<EdgeData> crossEdges = vertex.getCrossEdges().getEdges().stream()
              .filter(edge -> edge.getOrigin().getVertexNo() < edge.getEndpoint().getVertexNo())
              .map(edge -> mapSingleEdge(edge, graphColoring)).collect(Collectors.toList());
      edges.addAll(crossEdges);
    }
    return edges;
  }

  private EdgeData mapSingleEdge(Edge edge, GraphColoring graphColoring)
  {
    EdgeData edgeData = new EdgeData();
    edgeData.setOriginNo(edge.getOrigin().getVertexNo());
    edgeData.setEndpointNo(edge.getEndpoint().getVertexNo());

    Label label = edge.getLabel();
    if (label != null && label.getColor() < edgeColors.length)
    {
      int colorNumber = coloringService.getCurrentColorMapping(graphColoring, label.getColor());
      edgeData.setColor(edgeColors[colorNumber]);
    }
    return edgeData;
  }
}
