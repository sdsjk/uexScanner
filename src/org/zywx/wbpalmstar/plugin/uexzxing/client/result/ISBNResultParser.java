package org.zywx.wbpalmstar.plugin.uexzxing.client.result;

import org.zywx.wbpalmstar.plugin.uexzxing.BarcodeFormat;
import org.zywx.wbpalmstar.plugin.uexzxing.Result;

/**
 * Parses strings of digits that represent a ISBN.
 * 
 * @author jbreiden@google.com (Jeff Breidenbach)
 */
public class ISBNResultParser extends ResultParser {

  private ISBNResultParser() {
  }

  // ISBN-13 For Dummies 
  // http://www.bisg.org/isbn-13/for.dummies.html
  public static ISBNParsedResult parse(Result result) {
    BarcodeFormat format = result.getBarcodeFormat();
    if (!BarcodeFormat.EAN_13.equals(format)) {
      return null;
    }
    String rawText = result.getText();
    if (rawText == null) {
      return null;
    }
    int length = rawText.length();
    if (length != 13) {
      return null;
    }
    if (!rawText.startsWith("978") && !rawText.startsWith("979")) {
      return null;
    }
   
    return new ISBNParsedResult(rawText);
  }

}
