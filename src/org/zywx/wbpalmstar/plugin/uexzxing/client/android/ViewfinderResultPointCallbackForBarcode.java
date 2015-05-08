/*
 * Copyright (C) 2009 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.zywx.wbpalmstar.plugin.uexzxing.client.android;

import org.zywx.wbpalmstar.plugin.uexzxing.ResultPoint;
import org.zywx.wbpalmstar.plugin.uexzxing.ResultPointCallback;

final class ViewfinderResultPointCallbackForBarcode implements ResultPointCallback {

  private final ViewfinderViewForBarcode viewfinderView;

  ViewfinderResultPointCallbackForBarcode(ViewfinderViewForBarcode viewfinderView) {
    this.viewfinderView = viewfinderView;
  }

  public void foundPossibleResultPoint(ResultPoint point) {
    viewfinderView.addPossibleResultPoint(point);
  }

}
