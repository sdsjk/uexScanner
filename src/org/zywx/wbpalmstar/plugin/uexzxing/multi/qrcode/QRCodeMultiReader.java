package org.zywx.wbpalmstar.plugin.uexzxing.multi.qrcode;


import java.util.Hashtable;
import java.util.Vector;

import org.zywx.wbpalmstar.plugin.uexzxing.BarcodeFormat;
import org.zywx.wbpalmstar.plugin.uexzxing.BinaryBitmap;
import org.zywx.wbpalmstar.plugin.uexzxing.NotFoundException;
import org.zywx.wbpalmstar.plugin.uexzxing.ReaderException;
import org.zywx.wbpalmstar.plugin.uexzxing.Result;
import org.zywx.wbpalmstar.plugin.uexzxing.ResultMetadataType;
import org.zywx.wbpalmstar.plugin.uexzxing.ResultPoint;
import org.zywx.wbpalmstar.plugin.uexzxing.common.DecoderResult;
import org.zywx.wbpalmstar.plugin.uexzxing.common.DetectorResult;
import org.zywx.wbpalmstar.plugin.uexzxing.multi.MultipleBarcodeReader;
import org.zywx.wbpalmstar.plugin.uexzxing.multi.qrcode.detector.MultiDetector;
import org.zywx.wbpalmstar.plugin.uexzxing.qrcode.QRCodeReader;

public final class QRCodeMultiReader extends QRCodeReader implements MultipleBarcodeReader {

  private static final Result[] EMPTY_RESULT_ARRAY = new Result[0];

  public Result[] decodeMultiple(BinaryBitmap image) throws NotFoundException {
    return decodeMultiple(image, null);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
public Result[] decodeMultiple(BinaryBitmap image, Hashtable hints) throws NotFoundException {
    Vector results = new Vector();
    DetectorResult[] detectorResult = new MultiDetector(image.getBlackMatrix()).detectMulti(hints);
    for (int i = 0; i < detectorResult.length; i++) {
      try {
        DecoderResult decoderResult = getDecoder().decode(detectorResult[i].getBits());
        ResultPoint[] points = detectorResult[i].getPoints();
        Result result = new Result(decoderResult.getText(), decoderResult.getRawBytes(), points,
            BarcodeFormat.QR_CODE);
        if (decoderResult.getByteSegments() != null) {
          result.putMetadata(ResultMetadataType.BYTE_SEGMENTS, decoderResult.getByteSegments());
        }
        if (decoderResult.getECLevel() != null) {
          result.putMetadata(ResultMetadataType.ERROR_CORRECTION_LEVEL, decoderResult.getECLevel().toString());
        }
        results.addElement(result);
      } catch (ReaderException re) {
        // ignore and continue 
      }
    }
    if (results.isEmpty()) {
      return EMPTY_RESULT_ARRAY;
    } else {
      Result[] resultArray = new Result[results.size()];
      for (int i = 0; i < results.size(); i++) {
        resultArray[i] = (Result) results.elementAt(i);
      }
      return resultArray;
    }
  }

}
