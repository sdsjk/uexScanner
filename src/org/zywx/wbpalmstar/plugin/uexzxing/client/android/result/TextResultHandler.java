package org.zywx.wbpalmstar.plugin.uexzxing.client.android.result;

import org.zywx.wbpalmstar.plugin.uexzxing.client.result.ParsedResult;

import android.app.Activity;

public final class TextResultHandler extends ResultHandler {

  private static final int[] buttons = {};

  public TextResultHandler(Activity activity, ParsedResult result) {
    super(activity, result);
  }

  @Override
  public int getButtonCount() {
    return buttons.length;
  }

  @Override
  public int getButtonText(int index) {
    return buttons[index];
  }

  @Override
  public void handleButtonPress(int index) {

    switch (index) {
      case 0:
        
        break;
      case 1:
          
          break;
    }
  }

  @Override
  public int getDisplayTitle() {
    return 0;
  }
}
