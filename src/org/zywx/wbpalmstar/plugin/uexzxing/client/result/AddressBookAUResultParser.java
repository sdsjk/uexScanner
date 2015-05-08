package org.zywx.wbpalmstar.plugin.uexzxing.client.result;


import java.util.Vector;

import org.zywx.wbpalmstar.plugin.uexzxing.Result;


final class AddressBookAUResultParser extends ResultParser {

  public static AddressBookParsedResult parse(Result result) {
    String rawText = result.getText();
    // MEMORY is mandatory; seems like a decent indicator, as does end-of-record separator CR/LF
    if (rawText == null || rawText.indexOf("MEMORY") < 0 || rawText.indexOf("\r\n") < 0) {
      return null;
    }

    // NAME1 and NAME2 have specific uses, namely written name and pronunciation, respectively.
    // Therefore we treat them specially instead of as an array of names.
    String name = matchSinglePrefixedField("NAME1:", rawText, '\r', true);
    String pronunciation = matchSinglePrefixedField("NAME2:", rawText, '\r', true);

    String[] phoneNumbers = matchMultipleValuePrefix("TEL", 3, rawText, true);
    String[] emails = matchMultipleValuePrefix("MAIL", 3, rawText, true);
    String note = matchSinglePrefixedField("MEMORY:", rawText, '\r', false);
    String address = matchSinglePrefixedField("ADD:", rawText, '\r', true);
    String[] addresses = address == null ? null : new String[] {address};
    return new AddressBookParsedResult(maybeWrap(name), pronunciation, phoneNumbers, emails, note,
        addresses, null, null, null, null);
  }

  private static String[] matchMultipleValuePrefix(String prefix, int max, String rawText,
      boolean trim) {
    Vector<String> values = null;
    for (int i = 1; i <= max; i++) {
      String value = matchSinglePrefixedField(prefix + i + ':', rawText, '\r', trim);
      if (value == null) {
        break;
      }
      if (values == null) {
        values = new Vector<String>(max); // lazy init
      }
      values.addElement(value);
    }
    if (values == null) {
      return null;
    }
    return toStringArray(values);
  }

}
