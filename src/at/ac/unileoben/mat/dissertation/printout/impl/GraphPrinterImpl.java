package at.ac.unileoben.mat.dissertation.printout.impl;

import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.VertexService;
import at.ac.unileoben.mat.dissertation.printout.EdgeColorsGenerator;
import at.ac.unileoben.mat.dissertation.printout.GraphPrinter;
import at.ac.unileoben.mat.dissertation.printout.data.EdgeData;
import at.ac.unileoben.mat.dissertation.printout.data.EdgeStyleDefinition;
import at.ac.unileoben.mat.dissertation.printout.data.VertexData;
import at.ac.unileoben.mat.dissertation.printout.utils.EdgeStyleEnum;
import at.ac.unileoben.mat.dissertation.printout.utils.LabelUtils;
import at.ac.unileoben.mat.dissertation.printout.utils.VertexColorEnum;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * User: Marcin
 * Date: 18.10.15
 * Time: 12:40
 * To change this template use File | Settings | File Templates.
 */
@Component
public class GraphPrinterImpl implements GraphPrinter
{
  public static final double EDGE_LENGTH = 1.0;
  public static final String LAYER_BEFORE_CONSISTENCY_CHECK = "LAYER - BEFORE CONSISTENCY CHECK";
  public static final String LAYER_DONE = "LAYER - DONE";
  public static final String LABEL_PREFIX = "LABEL - ";
  public static final String MERGE_PREFIX = "MERGE - ";
  public static final String RECONSTRUCTION_RECOVERY = "RECONSTRUCTION RECOVERY";

  private static final String COLOR_PREFIX = "color";
  private static final String ORIGINAL_COLORS_PROFILE = "origColors";
  private static final String FINDING_SQUARE_HEADER = "FINDING SQUARE";
  private static final String COLORING_SQUARE_HEADER = "COLORING SQUARE";
  private static final String COLORING_EDGES_WITHOUT_SQUARE_HEADER = "COLORING EDGES WITHOUT SQUARE";

  @Autowired
  Environment environment;

  @Autowired
  Graph graph;

  @Autowired
  EdgeColorsGenerator edgeColorsGenerator;

  @Autowired
  ColoringService coloringService;

  @Autowired
  VertexService vertexService;

  List<String> steps;

  public GraphPrinterImpl()
  {
    Velocity.init();
    this.steps = new LinkedList<>();
  }

  @Override
  public void printFactorization()
  {
    VelocityContext context = new VelocityContext();
    context.put("steps", steps);
    storeEdgeColorsInContext(context);


    try (PrintWriter printWriter = new PrintWriter("out/tex/factorization.tex", StandardCharsets.UTF_8.name()))
    {
      Velocity.mergeTemplate("resources/vm/factorization.vm", StandardCharsets.UTF_8.name(), context, printWriter);
      printWriter.flush();
    }
    catch (FileNotFoundException | UnsupportedEncodingException e)
    {
      throw new RuntimeException(e);
    }
  }

  private void storeEdgeColorsInContext(VelocityContext context)
  {
    List<String> colors = edgeColorsGenerator.generateColors(graph.getGraphColoring().getColorsMapping().size()).stream()
            .map(color -> color.toString())
            .collect(Collectors.toList());
    context.put("colors", colors);
  }

  @Override
  public void createLayerSnapshot(String label)
  {
    createSnapshot(label, () -> prepareEdges(Collections.emptyList()));
  }

  @Override
  public void createMergeSnapshot(List<Edge> edges, MergeTagEnum mergeTag)
  {
    EdgeStyleDefinition edgeStyleDefinition = new EdgeStyleDefinition(edges, EdgeStyleEnum.DASHED.toString());
    createSnapshot(MERGE_PREFIX + mergeTag.toString(), () -> prepareEdges(Collections.singletonList(edgeStyleDefinition)));
  }

  @Override
  public void createLabelSnapshot(Edge edge, int color, int name, LabelOperationDetail labelOperationDetail)
  {
    List<EdgeStyleDefinition> edgeStyleDefinitions = new LinkedList<>();
    if (labelOperationDetail.getSameColorEdge() != null)
    {
      edgeStyleDefinitions.add(new EdgeStyleDefinition(Collections.singletonList(labelOperationDetail.getSameColorEdge()), EdgeStyleEnum.DOUBLE.toString()));
    }
    if (labelOperationDetail.getPivotSquareFirstEdge() != null)
    {
      edgeStyleDefinitions.add(new EdgeStyleDefinition(Collections.singletonList(labelOperationDetail.getPivotSquareFirstEdge()), EdgeStyleEnum.DASHDOTTED.toString()));
    }
    if (labelOperationDetail.getPivotSquareFirstEdgeCounterpart() != null)
    {
      edgeStyleDefinitions.add(new EdgeStyleDefinition(Collections.singletonList(labelOperationDetail.getPivotSquareFirstEdgeCounterpart()), EdgeStyleEnum.LOOSELY_DOTTED.toString()));
    }
    edgeStyleDefinitions.add(new EdgeStyleDefinition(Collections.singletonList(edge), EdgeStyleEnum.DOUBLE.toString()));
    createSnapshot(LABEL_PREFIX + labelOperationDetail.getType().toString(), () -> prepareEdges(edgeStyleDefinitions));
  }

  @Override
  public void createFindingSquareSnapshot(Edge baseEdge, Edge otherEdge)
  {
    EdgeStyleDefinition edgeStyleDefinition = new EdgeStyleDefinition(Arrays.asList(baseEdge, otherEdge), EdgeStyleEnum.DASHED.toString());
    createSnapshot(FINDING_SQUARE_HEADER, () -> prepareEdges(Collections.singletonList(edgeStyleDefinition)));

    System.out.println(String.format("%d. FINDING - %d-%d, %d-%d", steps.size(),
            baseEdge.getOrigin().getVertexNo(), baseEdge.getEndpoint().getVertexNo(),
            otherEdge.getOrigin().getVertexNo(), otherEdge.getEndpoint().getVertexNo()));
  }

  @Override
  public void createColoringSquareSnapshot(Edge baseEdge, Edge squareEdge, Edge otherColorBaseEdge, SquareReconstructionData squareReconstructionData)
  {
    List<EdgeStyleDefinition> edgeStyleDefinitions = new LinkedList<>();
    edgeStyleDefinitions.add(new EdgeStyleDefinition(Arrays.asList(baseEdge, squareEdge), EdgeStyleEnum.DOUBLE.toString()));
    edgeStyleDefinitions.add(new EdgeStyleDefinition(Collections.singletonList(otherColorBaseEdge), EdgeStyleEnum.DASHDOTTED.toString()));

    SquareMatchingEdgeData[][] squareMatchingEdgesByEdge = squareReconstructionData.getSquareMatchingEdgesByEdge();
    SquareMatchingEdgeData squareMatchingEdgeData = squareMatchingEdgesByEdge[otherColorBaseEdge.getOrigin().getVertexNo()][otherColorBaseEdge.getEndpoint().getVertexNo()];
    if (squareMatchingEdgeData != null)
    {
      List<Edge> squareMatchingEdges = Arrays.stream(squareMatchingEdgeData.getEdgesByColors())
              .filter(Objects::nonNull)
              .flatMap(edges -> edges.stream())
              .collect(Collectors.toList());

      edgeStyleDefinitions.add(new EdgeStyleDefinition(squareMatchingEdges, EdgeStyleEnum.LOOSELY_DOTTED.toString()));
    }

    createSnapshot(COLORING_SQUARE_HEADER, () -> prepareEdges(edgeStyleDefinitions));

    System.out.println(String.format("%d. COLORING - %d-%d, %d-%d", steps.size(),
            baseEdge.getOrigin().getVertexNo(), baseEdge.getEndpoint().getVertexNo(),
            squareEdge.getOrigin().getVertexNo(), squareEdge.getEndpoint().getVertexNo()));
  }

  @Override
  public void createColoringEdgesWithoutSquareSnapshot(List<Edge> edgesWithoutSquare)
  {
    EdgeStyleDefinition edgeStyleDefinition = new EdgeStyleDefinition(edgesWithoutSquare, EdgeStyleEnum.DASHED.toString());
    createSnapshot(COLORING_EDGES_WITHOUT_SQUARE_HEADER, () -> prepareEdges(Collections.singletonList(edgeStyleDefinition)));

    String formattedEdgesWithoutSquare = edgesWithoutSquare.stream()
            .map(e -> String.format("%d-%d", e.getOrigin().getVertexNo(), e.getEndpoint().getVertexNo()))
            .collect(Collectors.joining(", "));
    System.out.println(String.format("%d. NO SQUARE - %s", steps.size(), formattedEdgesWithoutSquare));
  }

  private void createSnapshot(String figureTitle, Supplier<List<EdgeData>> edgesSupplier)
  {
    VelocityContext context = new VelocityContext();

    context.put("figureTitle", figureTitle);

    List<VertexData> vertices = prepareVertices();
    List<EdgeData> edges = edgesSupplier.get();
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
        vertexData.setOrigVertexNo(vertex.getVertexNo() < graph.getReverseReindexArray().length ? graph.getReverseReindexArray()[vertex.getVertexNo()] : vertex.getVertexNo());
        if (vertex.isUnitLayer())
        {
          vertexData.setColor(VertexColorEnum.ORANGE.toString());
        }

        double posX = -layer.size() / (2.0 / EDGE_LENGTH) + (0.5 * EDGE_LENGTH) + (vertexInLayerNo * EDGE_LENGTH);
        vertexData.setPosX(posX);
        vertexData.setPosY(layerNo * EDGE_LENGTH);
        vertices.add(vertexData);
      }
    }
    return vertices;
  }

  private List<EdgeData> prepareEdges(List<EdgeStyleDefinition> edgeStyleDefinitions)
  {
    List<EdgeData> edges = new LinkedList<>();

    for (Vertex vertex : graph.getVertices())
    {
      List<EdgeData> downEdges = vertex.getDownEdges().getEdges().stream().map(edge -> mapSingleEdge(edge, edgeStyleDefinitions)).collect(Collectors.toList());
      edges.addAll(downEdges);

      List<EdgeData> crossEdges = vertex.getCrossEdges().getEdges().stream()
              .filter(edge -> edge.getOrigin().getVertexNo() < edge.getEndpoint().getVertexNo())
              .map(edge -> mapSingleEdge(edge, edgeStyleDefinitions)).collect(Collectors.toList());
      edges.addAll(crossEdges);
    }
    return edges;
  }

  private EdgeData mapSingleEdge(Edge edge, List<EdgeStyleDefinition> edgeStyleDefinitions)
  {
    EdgeData edgeData = new EdgeData();
    edgeData.setOriginNo(edge.getOrigin().getVertexNo());
    edgeData.setEndpointNo(edge.getEndpoint().getVertexNo());
    edgeData.setEdgeType(edge.getEdgeType().name());

    if (edge.getLabel() != null && edge.getLabel().getName() != -1)
    {
      edgeData.setBackwardEdgeLabel(LabelUtils.formatLabel(edge.getLabel()));

      if (edge.getOpposite() != null && edge.getOpposite().getLabel() != null)
      {
        edgeData.setForwardEdgeLabel(LabelUtils.formatLabel(edge.getOpposite().getLabel()));
      }
    }

    Label label = edge.getLabel();
    if (label != null)
    {
      int colorNumber = coloringService.getCurrentColorMapping(graph.getGraphColoring(), label.getColor());
      if (isProfileActive(ORIGINAL_COLORS_PROFILE))
      {
        colorNumber = label.getColor();
      }

      edgeData.setColor(COLOR_PREFIX + colorNumber);
    }

    for (EdgeStyleDefinition edgeStyleDefinition : edgeStyleDefinitions)
    {
      List<Edge> styledEdges = edgeStyleDefinition.getEdges();
      if (styledEdges.contains(edge) || styledEdges.contains(edge.getOpposite()))
      {
        edgeData.setStyle(edgeStyleDefinition.getStyle());
      }
    }
    return edgeData;
  }

  private boolean isProfileActive(String profileName)
  {
    return Arrays.stream(environment.getActiveProfiles())
            .filter(profile -> profile.equals(profileName))
            .findAny().isPresent();
  }
}
