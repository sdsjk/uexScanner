
package org.zywx.wbpalmstar.plugin.uexzxing.multi;


import java.util.Hashtable;

import org.zywx.wbpalmstar.plugin.uexzxing.BinaryBitmap;
import org.zywx.wbpalmstar.plugin.uexzxing.NotFoundException;
import org.zywx.wbpalmstar.plugin.uexzxing.Result;

public interface MultipleBarcodeReader {

  Result[] decodeMultiple(BinaryBitmap image) throws NotFoundException;

  @SuppressWarnings("rawtypes")
Result[] decodeMultiple(BinaryBitmap image, Hashtable hints) throws NotFoundException;

}
