package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.linearfactorization.label.LabelUtils;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.EdgeService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.ReconstructionService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static at.ac.unileoben.mat.dissertation.structure.EdgeType.*;

/**
 * Created by mwardynski on 26/11/16.
 */
@Component
public class ReconstructionServiceImpl implements ReconstructionService
{

  @Autowired
  Graph graph;

  @Autowired
  ReconstructionData reconstructionData;

  @Autowired
  EdgeService edgeService;

  @Autowired
  LabelUtils labelUtils;

  @Autowired
  ColoringService coloringService;

  @Override
  public void clearReconstructionData()
  {
    reconstructionData.setCurrentFactorization(null);
    reconstructionData.setResultFactorization(null);
    reconstructionData.setNewVertex(null);
    reconstructionData.getReconstructionEntries().clear();
  }

  @Override
  public boolean isReconstructionSuitableByConsistencyCheck()
  {
    return reconstructionData.getOperationOnGraph() == OperationOnGraph.IN_PLACE_RECONSTRUCTION
            && (reconstructionData.getCurrentLayerNo() > reconstructionData.getResultFactorization().getMaxConsistentLayerNo()
            || (reconstructionData.getCurrentLayerNo() == reconstructionData.getResultFactorization().getMaxConsistentLayerNo()
            && !reconstructionData.getResultFactorization().isAfterConsistencyCheck()));
  }

  @Override
  public boolean isReconstructionSuitableByLabeling(int currentLayerNo)
  {
    return reconstructionData.getOperationOnGraph() == OperationOnGraph.IN_PLACE_RECONSTRUCTION
            && reconstructionData.getNewVertex() != null
            && reconstructionData.getNewVertex().getBfsLayer() == currentLayerNo - 1;
  }

  @Override
  public boolean isNewVertex(Vertex vertex)
  {
    return reconstructionData.getNewVertex() == vertex;
  }

  @Override
  public boolean isCorrespondingEdgesCheckForUpEdgesReasonable()
  {
    return reconstructionData.getOperationOnGraph() != OperationOnGraph.IN_PLACE_RECONSTRUCTION
            || (reconstructionData.getOperationOnGraph() == OperationOnGraph.IN_PLACE_RECONSTRUCTION
            && reconstructionData.getNewVertex() == null
            || (reconstructionData.getNewVertex() != null
            && CollectionUtils.isNotEmpty(reconstructionData.getNewVertex().getUpEdges().getEdges())));
  }

  @Override
  public boolean addEdgesToReconstruction(List<Edge> inconsistentEdges, Vertex baseVertex, EdgeType edgeType)
  {
    if (edgeType == UP && inconsistentEdges.size() > 1)
    {
      throw new IllegalArgumentException("there shouldn't be more than one inconsistent Up-Edge");
    }

    ReconstructionEntryData reconstructionEntry = new ReconstructionEntryData(inconsistentEdges, baseVertex, edgeType);
    reconstructionData.getReconstructionEntries().add(reconstructionEntry);
    return true;
  }

  @Override
  public void reconstructWithCollectedData()
  {
    Set<EdgeType> reconstructedEdgeTypes = EnumSet.noneOf(EdgeType.class);

    boolean isReconstructionAfterConsistencyTest = true;
    Queue<ReconstructionEntryData> reconstructionEntries = reconstructionData.getReconstructionEntries();
    while (!reconstructionEntries.isEmpty())
    {
      ReconstructionEntryData reconstructionEntry = reconstructionEntries.poll();

      createNewVertexIfNeeded(reconstructionEntry.getSourceVertex());

      EdgeType currentEdgeType = reconstructionEntry.getEdgeType();
      if (!isCorrectEdgeTypeToAdd(reconstructionEntry.getSourceVertex().getBfsLayer(), currentEdgeType))
      {
        continue;
      }
      reconstructedEdgeTypes.add(currentEdgeType);
      for (Edge edge : reconstructionEntry.getInconsistentEdges())
      {
        if (edge.getLabel() == null)
        {
          isReconstructionAfterConsistencyTest = false;
        }
        addEdgeToMissingVertex(edge, reconstructionEntry.getSourceVertex(), currentEdgeType);
      }
    }
    if (isReconstructionAfterConsistencyTest)
    {
      arrangeNewVertexEdges(reconstructedEdgeTypes);
    }
  }

  private void createNewVertexIfNeeded(Vertex baseVertex)
  {
    Vertex newVertex = reconstructionData.getNewVertex();
    if (newVertex == null)
    {
      int newVertexNo;
      if (graph.getLayers().size() - baseVertex.getBfsLayer() > 2)
      {
        newVertexNo = graph.getLayers().get(baseVertex.getBfsLayer() + 2).get(0).getVertexNo();
        for (int i = newVertexNo; i < graph.getVertices().size(); i++)
        {
          Vertex vertex = graph.getVertices().get(i);
          vertex.setVertexNo(vertex.getVertexNo() + 1);
        }
      }
      else
      {
        newVertexNo = graph.getVertices().size();
      }
      updateReverseReindexArray(newVertexNo);


      newVertex = new Vertex(newVertexNo, new LinkedList<>());

      int newVertexLayer = baseVertex.getBfsLayer() + 1;
      newVertex.setBfsLayer(newVertexLayer);
      if (newVertexLayer == 1)
      {
        newVertex.setUnitLayer(true);
      }

      newVertex.setDownEdges(new EdgesGroup(new ArrayList<>(graph.getVertices().size())));
      newVertex.setCrossEdges(new EdgesGroup(new ArrayList<>(graph.getVertices().size())));
      assignEmptyEdgesRefToEdgesGroup(newVertex.getCrossEdges());
      newVertex.setUpEdges(new EdgesGroup(new ArrayList<>(graph.getVertices().size())));

      graph.getVertices().add(newVertexNo, newVertex);
      if (graph.getLayers().size() == newVertexLayer)
      {
        graph.getLayers().add(new ArrayList<>(1));
      }
      graph.getLayers().get(newVertexLayer).add(newVertex);
      reconstructionData.setNewVertex(newVertex);
    }
  }

  private void assignEmptyEdgesRefToEdgesGroup(EdgesGroup edgesGroup)
  {
    EdgesRef crossEdgesRef = new EdgesRef();
    coloringService.setColorAmounts(crossEdgesRef, new int[graph.getGraphColoring().getOriginalColorsAmount()]);
    edgesGroup.setEdgesRef(crossEdgesRef);
  }

  private void updateReverseReindexArray(int newVertexNo)
  {
    Integer[] reverseReindexArray = graph.getReverseReindexArray();
    Integer[] updatedReverseReindexArray = Arrays.copyOf(reverseReindexArray, reverseReindexArray.length + 1);
    for (int i = reverseReindexArray.length - 1; i >= newVertexNo; i--)
    {
      updatedReverseReindexArray[i + 1] = updatedReverseReindexArray[i];
    }
    updatedReverseReindexArray[newVertexNo] = graph.getVertices().size();
    graph.setReverseReindexArray(updatedReverseReindexArray);
  }

  private boolean isCorrectEdgeTypeToAdd(int sourceVertexLayer, EdgeType currentEdgeType)
  {
    return (sourceVertexLayer == reconstructionData.getNewVertex().getBfsLayer()
            && currentEdgeType == CROSS)
            || (sourceVertexLayer == reconstructionData.getNewVertex().getBfsLayer() + 1
            && currentEdgeType == DOWN)
            || (sourceVertexLayer == reconstructionData.getNewVertex().getBfsLayer() - 1
            && currentEdgeType == UP);
  }

  private void addEdgeToMissingVertex(Edge inconsistentEdge, Vertex origin, EdgeType edgeType)
  {
    Vertex endpoint = reconstructionData.getNewVertex();

    Edge newEdge = new Edge(origin, endpoint);
    newEdge.setEdgeType(edgeType);


    Edge newEdgeOpposite = new Edge(endpoint, origin);
    newEdgeOpposite.setEdgeType(getOppositeEdgeType(edgeType));

    if (inconsistentEdge.getLabel() != null)
    {
      if (inconsistentEdge.getOrigin().getVertexNo() != Integer.MIN_VALUE)
      {
        Edge inconsistentEdgeOpposite = inconsistentEdge.getOpposite();
        edgeService.addLabel(newEdge, inconsistentEdge.getLabel().getColor(), inconsistentEdge.getLabel().getName(), inconsistentEdge, new LabelOperationDetail.Builder(LabelOperationEnum.RECONSTRUCTION).build());
        edgeService.addLabel(newEdgeOpposite, inconsistentEdgeOpposite.getLabel().getColor(), inconsistentEdgeOpposite.getLabel().getName(), inconsistentEdgeOpposite, new LabelOperationDetail.Builder(LabelOperationEnum.RECONSTRUCTION).build());
      }
      else
      {
        edgeService.addLabel(newEdge, inconsistentEdge.getLabel().getColor(), inconsistentEdge.getLabel().getName(), null, new LabelOperationDetail.Builder(LabelOperationEnum.RECONSTRUCTION).build());
        edgeService.addLabel(newEdgeOpposite, inconsistentEdge.getLabel().getColor(), inconsistentEdge.getLabel().getName(), null, new LabelOperationDetail.Builder(LabelOperationEnum.RECONSTRUCTION).build());
      }
    }

    newEdge.setOpposite(newEdgeOpposite);
    newEdgeOpposite.setOpposite(newEdge);

    boolean isInsertionSuccessful = true;
    switch (edgeType)
    {
      case UP:
        isInsertionSuccessful = insertEdgeIntoExistingEdgesGroup(newEdge, origin.getUpEdges());
        if (isInsertionSuccessful)
        {
          reconstructionData.getNewVertex().getDownEdges().getEdges().add(newEdgeOpposite);
        }
        break;
      case CROSS:
        isInsertionSuccessful = insertEdgeIntoExistingEdgesGroup(newEdge, origin.getCrossEdges());
        if (isInsertionSuccessful)
        {
          reconstructionData.getNewVertex().getCrossEdges().getEdges().add(newEdgeOpposite);
        }
        break;
      case DOWN:
        isInsertionSuccessful = insertEdgeIntoExistingEdgesGroup(newEdge, origin.getDownEdges());
        if (isInsertionSuccessful)
        {
          reconstructionData.getNewVertex().getUpEdges().getEdges().add(newEdgeOpposite);
        }
        break;
    }
    if (isInsertionSuccessful)
    {
      reconstructionData.getNewVertex().getEdges().add(newEdgeOpposite);
      origin.getEdges().add(newEdge);
    }
  }

  private EdgeType getOppositeEdgeType(EdgeType edgeType)
  {
    return edgeType == CROSS ? CROSS : edgeType == DOWN ? UP : DOWN;
  }

  private boolean insertEdgeIntoExistingEdgesGroup(Edge newEdge, EdgesGroup edgesGroup)
  {
    boolean isInsertionSuccessful = true;
    EdgesRef edgesRef = edgesGroup.getEdgesRef();
    if (edgesRef == null)
    {
      edgesGroup.getEdges().add(newEdge);
      return isInsertionSuccessful;
    }

    List<ColorGroupLocation> colorPositions = edgesRef.getColorPositions();

    List<Edge> edges = edgesGroup.getEdges();
    int currentEdgesSize = edges.size();

    int edgeColor = newEdge.getLabel().getColor();
    ColorGroupLocation colorGroupLocationForEdgeColor = colorPositions.get(edgeColor);

    if (colorGroupLocationForEdgeColor != null)
    {
      Optional<Integer> newEdgeIndexOptional = calculateIndexForNewEdge(newEdge, edges, colorGroupLocationForEdgeColor);
      if (newEdgeIndexOptional.isPresent())
      {
        int newEdgeIndex = newEdgeIndexOptional.get();
        colorGroupLocationForEdgeColor.setLength(colorGroupLocationForEdgeColor.getLength() + 1);
        edges.add(newEdgeIndex, newEdge);

        incrementColorGroupLocationsAfterNewColor(edgeColor, colorPositions, currentEdgesSize);
      }
      else
      {
        isInsertionSuccessful = false;
      }
    }
    else
    {
      ColorGroupLocation newColorGroupLocation = getColorGroupLocationForNewColor(edgeColor, colorPositions, currentEdgesSize);
      colorPositions.set(edgeColor, newColorGroupLocation);
      edges.add(newColorGroupLocation.getIndex(), newEdge);

      incrementColorGroupLocationsAfterNewColor(edgeColor, colorPositions, currentEdgesSize);
    }
    return isInsertionSuccessful;
  }

  private Optional<Integer> calculateIndexForNewEdge(Edge newEdge, List<Edge> edges, ColorGroupLocation colorGroupLocationForEdgeColor)
  {
    int firstEdgeIndex = colorGroupLocationForEdgeColor.getIndex();
    int newEdgeIndex = colorGroupLocationForEdgeColor.getIndex() + colorGroupLocationForEdgeColor.getLength();
    for (int i = firstEdgeIndex; i < newEdgeIndex; i++)
    {
      Edge currentEdge = edges.get(i);
      if (currentEdge.getOrigin() == newEdge.getOrigin() && currentEdge.getEndpoint() == newEdge.getEndpoint())
      {
        return Optional.empty();
      }
      int currentEdgeName = currentEdge.getLabel().getName();
      int newEdgeName = newEdge.getLabel().getName();
      if (newEdgeName < currentEdgeName)
      {
        newEdgeIndex = i;
      }
      else if (newEdgeName == currentEdgeName)
      {
        throw new IllegalStateException("shouldn't happen");
      }
    }
    return Optional.of(newEdgeIndex);
  }

  private ColorGroupLocation getColorGroupLocationForNewColor(int edgeColor, List<ColorGroupLocation> colorPositions, int currentEdgesSize)
  {
    ColorGroupLocation newColorGroupLocation = null;
    if (currentEdgesSize > 0)
    {
      for (int i = edgeColor - 1; i >= 0 && newColorGroupLocation == null; i--)
      {
        ColorGroupLocation colorGroupLocation = colorPositions.get(i);
        if (colorGroupLocation != null)
        {
          newColorGroupLocation = new ColorGroupLocation(colorGroupLocation.getIndex() + colorGroupLocation.getLength(), 1);
        }
      }
    }
    if (newColorGroupLocation == null)
    {
      newColorGroupLocation = new ColorGroupLocation(0, 1);
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

  @Override
  public boolean isTopVertexMissingByReconstruction(int currentLayerNo)
  {
    return reconstructionData.getNewVertex() == null
            && CollectionUtils.isEmpty(reconstructionData.getReconstructionEntries())
            && currentLayerNo == graph.getLayers().size() - 1;
  }

  @Override
  public void prepareTopVertexReconstruction(List<Vertex> currentLayer)
  {
    for (Vertex u : currentLayer)
    {
      Edge uv = u.getDownEdges().getEdges().get(0);
      int uvMappedColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), uv.getLabel().getColor());
      Edge uw = edgeService.getEdgeOfDifferentColor(u, uvMappedColor, graph.getGraphColoring());
      Edge referenceEdge = findReferenceEdgeForMissingTopVertex(uv)
              .orElseGet(() -> findReferenceEdgeForMissingTopVertex(uw)
                      .orElseThrow(() -> new IllegalStateException("there must be at lease one up edge for missing layer vertex")));
      addEdgesToReconstruction(Collections.singletonList(referenceEdge), uv.getOrigin(), EdgeType.UP);
    }
  }

  private Optional<Edge> findReferenceEdgeForMissingTopVertex(Edge uv)
  {
    Vertex v = uv.getEndpoint();
    List<List<Edge>> vDifferentThanUv = edgeService.getAllEdgesOfDifferentColor(v, uv.getLabel().getColor(), graph.getGraphColoring(), EdgeType.UP);

    Optional<Edge> resultEdgeOptional = Optional.empty();
    for (List<Edge> edges : vDifferentThanUv)
    {
      if (edges.size() == 1)
      {
        if (!resultEdgeOptional.isPresent())
        {
          resultEdgeOptional = Optional.of(edges.get(0));
        }
        else
        {
          throw new IllegalStateException("there could be only one reference edge pro neighbor for missing top layer vertex");
        }
      }
      else if (edges.size() > 1)
      {
        throw new IllegalStateException("there could be only one missing edge pro neighbor for missing top layer vertex");
      }
    }
    return resultEdgeOptional;
  }

}
