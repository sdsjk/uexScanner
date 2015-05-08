package org.zywx.wbpalmstar.plugin.uexzxing.client.result;


import java.util.Hashtable;

import org.zywx.wbpalmstar.plugin.uexzxing.Result;


final class EmailAddressResultParser extends ResultParser {

  public static EmailAddressParsedResult parse(Result result) {
    String rawText = result.getText();
    if (rawText == null) {
      return null;
    }
    String emailAddress;
    if (rawText.startsWith("mailto:") || rawText.startsWith("MAILTO:")) {
      // If it starts with mailto:, assume it is definitely trying to be an email address
      emailAddress = rawText.substring(7);
      int queryStart = emailAddress.indexOf('?');
      if (queryStart >= 0) {
        emailAddress = emailAddress.substring(0, queryStart);
      }
      Hashtable<?, ?> nameValues = parseNameValuePairs(rawText);
      String subject = null;
      String body = null;
      if (nameValues != null) {
        if (emailAddress.length() == 0) {
          emailAddress = (String) nameValues.get("to");
        }
        subject = (String) nameValues.get("subject");
        body = (String) nameValues.get("body");
      }
      return new EmailAddressParsedResult(emailAddress, subject, body, rawText);
    } else {
      if (!EmailDoCoMoResultParser.isBasicallyValidEmailAddress(rawText)) {
        return null;
      }
      emailAddress = rawText;
      return new EmailAddressParsedResult(emailAddress, null, null, "mailto:" + emailAddress);
    }
  }

}