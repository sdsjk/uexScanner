package org.zywx.wbpalmstar.plugin.uexzxing.client.android.result;

import org.zywx.wbpalmstar.plugin.uexzxing.Result;
import org.zywx.wbpalmstar.plugin.uexzxing.client.result.ParsedResult;
import org.zywx.wbpalmstar.plugin.uexzxing.client.result.ResultParser;

import android.app.Activity;

public final class ResultHandlerFactory {
  private ResultHandlerFactory() {
  }

  public static ResultHandler makeResultHandler(Activity activity, Result rawResult) {
    ParsedResult result = parseResult(rawResult);
     return new TextResultHandler(activity, result);
  }

  private static ParsedResult parseResult(Result rawResult) {
    return ResultParser.parseResult(rawResult);
  }
}
