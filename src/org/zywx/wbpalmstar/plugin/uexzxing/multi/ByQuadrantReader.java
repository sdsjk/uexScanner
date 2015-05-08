
package org.zywx.wbpalmstar.plugin.uexzxing.multi;


import java.util.Hashtable;

import org.zywx.wbpalmstar.plugin.uexzxing.BinaryBitmap;
import org.zywx.wbpalmstar.plugin.uexzxing.ChecksumException;
import org.zywx.wbpalmstar.plugin.uexzxing.FormatException;
import org.zywx.wbpalmstar.plugin.uexzxing.NotFoundException;
import org.zywx.wbpalmstar.plugin.uexzxing.Reader;
import org.zywx.wbpalmstar.plugin.uexzxing.Result;


public final class ByQuadrantReader implements Reader {

  private final Reader delegate;

  public ByQuadrantReader(Reader delegate) {
    this.delegate = delegate;
  }

  public Result decode(BinaryBitmap image)
      throws NotFoundException, ChecksumException, FormatException {
    return decode(image, null);
  }

  @SuppressWarnings("rawtypes")
public Result decode(BinaryBitmap image, Hashtable hints)
      throws NotFoundException, ChecksumException, FormatException {

    int width = image.getWidth();
    int height = image.getHeight();
    int halfWidth = width / 2;
    int halfHeight = height / 2;

    BinaryBitmap topLeft = image.crop(0, 0, halfWidth, halfHeight);
    try {
      return delegate.decode(topLeft, hints);
    } catch (NotFoundException re) {
      // continue
    }

    BinaryBitmap topRight = image.crop(halfWidth, 0, halfWidth, halfHeight);
    try {
      return delegate.decode(topRight, hints);
    } catch (NotFoundException re) {
      // continue
    }

    BinaryBitmap bottomLeft = image.crop(0, halfHeight, halfWidth, halfHeight);
    try {
      return delegate.decode(bottomLeft, hints);
    } catch (NotFoundException re) {
      // continue
    }

    BinaryBitmap bottomRight = image.crop(halfWidth, halfHeight, halfWidth, halfHeight);
    try {
      return delegate.decode(bottomRight, hints);
    } catch (NotFoundException re) {
      // continue
    }

    int quarterWidth = halfWidth / 2;
    int quarterHeight = halfHeight / 2;
    BinaryBitmap center = image.crop(quarterWidth, quarterHeight, halfWidth, halfHeight);
    return delegate.decode(center, hints);
  }

  public void reset() {
    delegate.reset();
  }

}
