package at.ac.unileoben.mat.dissertation.common.impl;

import at.ac.unileoben.mat.dissertation.common.ReconstructionHelper;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.LabelUtils;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.EdgeService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static at.ac.unileoben.mat.dissertation.structure.EdgeType.*;

/**
 * Created by mwardynski on 26/11/16.
 */
@Component
public class ReconstructionHelperImpl implements ReconstructionHelper
{

  @Autowired
  Graph graph;

  @Autowired
  ReconstructionData reconstructionData;

  @Autowired
  EdgeService edgeService;

  @Autowired
  LabelUtils labelUtils;

  @Override
  public void clearReconstructionData()
  {
    reconstructionData.setCurrentFactorization(null);
    reconstructionData.setResultFactorization(null);
  }

  @Override
  public boolean addEdgesToReconstruction(List<Edge> inconsistentEdges, Edge baseEdge, EdgeType edgeType)
  {
    if (edgeType == UP && inconsistentEdges.size() > 1)
    {
      throw new IllegalArgumentException("there shouldn't be more than one inconsistent Up-Edge");
    }


    ReconstructionEntryData reconstructionEntry = new ReconstructionEntryData(inconsistentEdges, baseEdge.getOrigin(), edgeType);
    reconstructionData.getReconstructionEntries().add(reconstructionEntry);
    return true;
  }

  @Override
  public void reconstructWithCollectedData()
  {
    Set<EdgeType> reconstructedEdgeTypes = EnumSet.noneOf(EdgeType.class);

    Queue<ReconstructionEntryData> reconstructionEntries = reconstructionData.getReconstructionEntries();
    while (!reconstructionEntries.isEmpty())
    {
      ReconstructionEntryData reconstructionEntry = reconstructionEntries.poll();

      createNewVertexIfNeeded(reconstructionEntry.getSourceVertex());
      reconstructedEdgeTypes.add(reconstructionEntry.getEdgeType());

      reconstructionEntry.getInconsistentEdges()
              .forEach(edge -> addEdgeToMissingVertex(edge, reconstructionEntry.getSourceVertex(), reconstructionEntry.getEdgeType()));
    }

    arrangeNewVertexEdges(reconstructedEdgeTypes);
  }

  private void createNewVertexIfNeeded(Vertex baseVertex)
  {
    Vertex newVertex = reconstructionData.getNewVertex();
    if (newVertex == null)
    {
      int newVertexNo = graph.getVertices().size();
      int newVertexLayer = baseVertex.getBfsLayer() + 1;

      newVertex = new Vertex(newVertexNo, new LinkedList<>());
      newVertex.setBfsLayer(newVertexLayer);
      newVertex.setDownEdges(new EdgesGroup(new ArrayList<>(newVertexNo)));
      newVertex.setCrossEdges(new EdgesGroup(new ArrayList<>(newVertexNo)));
      newVertex.setUpEdges(new EdgesGroup(new ArrayList<>(newVertexNo)));
      graph.getVertices().add(newVertex);
      graph.getLayers().get(newVertexLayer).add(newVertex);
      reconstructionData.setNewVertex(newVertex);
    }
  }

  private void addEdgeToMissingVertex(Edge inconsistentEdge, Vertex origin, EdgeType edgeType)
  {
    Vertex endpoint = reconstructionData.getNewVertex();

    Edge newEdge = new Edge(origin, endpoint);
    newEdge.setEdgeType(edgeType);
    edgeService.addLabel(newEdge, inconsistentEdge.getLabel().getColor(), inconsistentEdge.getLabel().getName(), inconsistentEdge, new LabelOperationDetail.Builder(LabelOperationEnum.RECONSTRUCTION).build());

    Edge inconsistentEdgeOpposite = inconsistentEdge.getOpposite();
    Edge newEdgeOpposite = new Edge(endpoint, origin);
    newEdgeOpposite.setEdgeType(getOppositeEdgeType(edgeType));
    edgeService.addLabel(newEdgeOpposite, inconsistentEdgeOpposite.getLabel().getColor(), inconsistentEdgeOpposite.getLabel().getName(), inconsistentEdgeOpposite, new LabelOperationDetail.Builder(LabelOperationEnum.RECONSTRUCTION).build());

    newEdge.setOpposite(newEdgeOpposite);
    newEdgeOpposite.setOpposite(newEdge);

    reconstructionData.getNewVertex().getEdges().add(newEdgeOpposite);
    switch (edgeType)
    {
      case UP:
        reconstructionData.getNewVertex().getDownEdges().getEdges().add(newEdgeOpposite);
        insertEdgeIntoExistingEdgesGroup(newEdge, origin.getUpEdges());
        break;
      case CROSS:
        reconstructionData.getNewVertex().getCrossEdges().getEdges().add(newEdgeOpposite);
        insertEdgeIntoExistingEdgesGroup(newEdge, origin.getCrossEdges());
        break;
      case DOWN:
        reconstructionData.getNewVertex().getUpEdges().getEdges().add(newEdgeOpposite);
        insertEdgeIntoExistingEdgesGroup(newEdge, origin.getDownEdges());
        break;
    }
  }

  private EdgeType getOppositeEdgeType(EdgeType edgeType)
  {
    return edgeType == CROSS ? CROSS : edgeType == DOWN ? UP : DOWN;
  }

  private void insertEdgeIntoExistingEdgesGroup(Edge newEdge, EdgesGroup edgesGroup)
  {
    EdgesRef edgesRef = edgesGroup.getEdgesRef();


    List<ColorGroupLocation> colorPositions = edgesRef.getColorPositions();

    List<Edge> edges = edgesGroup.getEdges();
    int currentEdgesSize = edges.size();

    int edgeColor = newEdge.getLabel().getColor();
    ColorGroupLocation colorGroupLocationForEdgeColor = colorPositions.get(edgeColor);

    if (colorGroupLocationForEdgeColor != null)
    {
      int newEdgeIndex = calculateIndexForNewEdge(newEdge, edges, colorGroupLocationForEdgeColor);
      colorGroupLocationForEdgeColor.setLength(colorGroupLocationForEdgeColor.getLength() + 1);
      edges.add(newEdgeIndex, newEdge);

      incrementColorGroupLocationsAfterNewColor(edgeColor, colorPositions, currentEdgesSize);
    }
    else
    {
      ColorGroupLocation newColorGroupLocation = getColorGroupLocationForNewColor(edgeColor, colorPositions, currentEdgesSize);
      colorPositions.set(edgeColor, newColorGroupLocation);
      edges.add(newColorGroupLocation.getIndex(), newEdge);

      incrementColorGroupLocationsAfterNewColor(edgeColor, colorPositions, currentEdgesSize);
    }

  }

  private int calculateIndexForNewEdge(Edge newEdge, List<Edge> edges, ColorGroupLocation colorGroupLocationForEdgeColor)
  {
    int firstEdgeIndex = colorGroupLocationForEdgeColor.getIndex();
    int lastEdgeIndex = colorGroupLocationForEdgeColor.getIndex() + colorGroupLocationForEdgeColor.getLength();
    for (int i = firstEdgeIndex; i < lastEdgeIndex; i++)
    {
      int currentEdgeName = edges.get(i).getLabel().getName();
      int newEdgeName = newEdge.getLabel().getName();
      if (newEdgeName < currentEdgeName)
      {
        lastEdgeIndex = i;
      }
      else if (newEdgeName == currentEdgeName)
      {
        System.out.println("shouldn't happen");
      }
    }
    return lastEdgeIndex;
  }

  private ColorGroupLocation getColorGroupLocationForNewColor(int edgeColor, List<ColorGroupLocation> colorPositions, int currentEdgesSize)
  {
    ColorGroupLocation newColorGroupLocation = null;
    if (currentEdgesSize > 0)
    {
      for (int i = edgeColor + 1; i < colorPositions.size() && newColorGroupLocation == null; i++)
      {
        ColorGroupLocation colorGroupLocation = colorPositions.get(i);
        if (colorGroupLocation != null)
        {
          newColorGroupLocation = new ColorGroupLocation(colorGroupLocation.getIndex(), 1);
        }
      }
    }
    else
    {
      newColorGroupLocation = new ColorGroupLocation(currentEdgesSize, 1);
    }
    return newColorGroupLocation;
  }

  private void incrementColorGroupLocationsAfterNewColor(int edgeColor, List<ColorGroupLocation> colorPositions, int currentEdgesSize)
  {
    if (currentEdgesSize > 0)
    {
      for (int i = edgeColor + 1; i < colorPositions.size(); i++)
      {
        ColorGroupLocation colorGroupLocation = colorPositions.get(i);
        if (colorGroupLocation != null)
        {
          colorGroupLocation.setIndex(colorGroupLocation.getIndex() + 1);
        }
      }
    }
  }

  private void arrangeNewVertexEdges(Set<EdgeType> reconstructedEdgeTypes)
  {
    Vertex newVertex = reconstructionData.getNewVertex();

    Iterator<EdgeType> edgeTypesIt = reconstructedEdgeTypes.iterator();
    while (edgeTypesIt.hasNext())
    {
      EdgeType edgeType = edgeTypesIt.next();

      switch (edgeType)
      {
        case UP:
          labelUtils.sortEdgesAccordingToLabels(newVertex.getDownEdges(), graph.getGraphColoring());
          break;
        case CROSS:
          labelUtils.sortEdgesAccordingToLabels(newVertex.getCrossEdges(), graph.getGraphColoring());
          break;
        case DOWN:
          labelUtils.sortEdgesAccordingToLabels(newVertex.getUpEdges(), graph.getGraphColoring());
          break;
      }
      edgeTypesIt.remove();
    }
  }

}
