package at.ac.unileoben.mat.dissertation.printout.utils;

import at.ac.unileoben.mat.dissertation.structure.Label;
import org.apache.commons.lang.StringUtils;

/**
 * Created by mwardynski on 05/06/16.
 */
public class LabelUtils
{
  public static final String formatLabel(Label label)
  {
    if (label != null)
    {
      return label.getColor() + "," + label.getName();
    }
    else
    {
      return StringUtils.EMPTY;
    }
  }
}
