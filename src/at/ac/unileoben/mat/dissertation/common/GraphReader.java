package at.ac.unileoben.mat.dissertation.common;

import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.Vertex;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 13-11-29
 * Time: 15:23
 * To change this template use File | Settings | File Templates.
 */
@Component
public class GraphReader
{
  public static int MIN_NEIGHBOURS_ACCOUNT = Integer.MAX_VALUE;
  public static int MAX_NEIGHBOURS_ACCOUNT = Integer.MIN_VALUE;
  private static final String SPLIT_SIGN = " <==> ";
  private static final String INPUT_DIR = "resources/";
  private static int MAX_NUMBER_OF_NEIGHBORS = Integer.MIN_VALUE;

  public List<Vertex> readGraph(String fileName)
  {

    FileReader fr;
    try
    {
      File f = new File(INPUT_DIR);
      fr = new FileReader(INPUT_DIR + fileName);
      BufferedReader br = new BufferedReader(fr);
      String s;

      int size = 0;
      while ((s = br.readLine()) != null)
      {
        size++;
      }

      br.close();
      fr.close();

      Vertex[] graph = new Vertex[size];
      for (int i = 0; i < size; i++)
      {
        graph[i] = new Vertex(i, new ArrayList<Edge>(size));
      }

      fr = new FileReader(INPUT_DIR + fileName);
      br = new BufferedReader(fr);

      while ((s = br.readLine()) != null)
      {
        String[] firstSplit = s.split(SPLIT_SIGN);
        String[] neighbors = firstSplit[1].split(" ");
        int vertexNo = Integer.parseInt(firstSplit[0]);
        for (String neighborNoStr : neighbors)
        {
          int neighborNo = Integer.parseInt(neighborNoStr);
          if (neighborNo >= vertexNo)
          {
            Edge e1 = new Edge(graph[vertexNo], graph[neighborNo]);
            Edge e2 = new Edge(graph[neighborNo], graph[vertexNo]);
            e1.setOpposite(e2);
            e2.setOpposite(e1);
            graph[vertexNo].getEdges().add(e1);
            graph[neighborNo].getEdges().add(e2);
          }
        }
        setMinMax(neighbors.length);
      }
      br.close();
      fr.close();
      return new ArrayList<Vertex>(Arrays.asList(graph));
    }
    catch (FileNotFoundException e)
    {
      e.printStackTrace();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    return null;
  }

  private void setMinMax(int size)
  {
    if (MIN_NEIGHBOURS_ACCOUNT > size)
    {
      MIN_NEIGHBOURS_ACCOUNT = size;
    }
    if (MAX_NEIGHBOURS_ACCOUNT < size)
    {
      MAX_NEIGHBOURS_ACCOUNT = size;
      MAX_NUMBER_OF_NEIGHBORS = MAX_NEIGHBOURS_ACCOUNT + 2;
    }
  }

}
