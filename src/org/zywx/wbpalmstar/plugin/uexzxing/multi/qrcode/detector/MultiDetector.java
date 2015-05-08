package org.zywx.wbpalmstar.plugin.uexzxing.multi.qrcode.detector;


import java.util.Hashtable;
import java.util.Vector;

import org.zywx.wbpalmstar.plugin.uexzxing.NotFoundException;
import org.zywx.wbpalmstar.plugin.uexzxing.ReaderException;
import org.zywx.wbpalmstar.plugin.uexzxing.common.BitMatrix;
import org.zywx.wbpalmstar.plugin.uexzxing.common.DetectorResult;
import org.zywx.wbpalmstar.plugin.uexzxing.qrcode.detector.Detector;
import org.zywx.wbpalmstar.plugin.uexzxing.qrcode.detector.FinderPatternInfo;


public final class MultiDetector extends Detector {

  private static final DetectorResult[] EMPTY_DETECTOR_RESULTS = new DetectorResult[0];

  public MultiDetector(BitMatrix image) {
    super(image);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
public DetectorResult[] detectMulti(Hashtable hints) throws NotFoundException {
    BitMatrix image = getImage();
    MultiFinderPatternFinder finder = new MultiFinderPatternFinder(image);
    FinderPatternInfo[] info = finder.findMulti(hints);

    if (info == null || info.length == 0) {
      throw NotFoundException.getNotFoundInstance();
    }

    Vector result = new Vector();
    for (int i = 0; i < info.length; i++) {
      try {
        result.addElement(processFinderPatternInfo(info[i]));
      } catch (ReaderException e) {
        // ignore
      }
    }
    if (result.isEmpty()) {
      return EMPTY_DETECTOR_RESULTS;
    } else {
      DetectorResult[] resultArray = new DetectorResult[result.size()];
      for (int i = 0; i < result.size(); i++) {
        resultArray[i] = (DetectorResult) result.elementAt(i);
      }
      return resultArray;
    }
  }

}
