import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringReplace {

  private static final String literalLeftBrace = "\\{";
  private static final String literalRightBrace = "\\}";
  private static final String literalComma = "\\,";
  private static final String or = "|";
  private static final String leftParenthesis = "(";
  private static final String rightParenthesis = ")";
  private static final String whiteSpace = "\\s";
  private static final String literalQuote = "\\\"";
  private static final String literalSemicolon = "\\:";
  private static final String literalExclamationMark = "\\!";
  private static final String zeroOrMore = "*";
  private static final String zeroOrOne = "?";
  private static final String oneOrMore = "+";

  private static String not(String input) {
    return "[^"+input+"]";
  }

  private static String zeroOrMore(String input) {
    return input.concat(zeroOrMore);
  }

  private static String zeroOrOne(String input) {
    return input.concat(zeroOrOne);
  }

  private static String oneOrMore(String input) {
    return input.concat(oneOrMore);
  }

  private static String group(String input) {
    return leftParenthesis.concat(input).concat(rightParenthesis);
  }

  private static String or(String first, String second) {
    return first + or + second;
  }

  private static String betweenLiteralBraces(String input) {
    return literalLeftBrace+input+literalRightBrace;
  }

  private static String betweenQuotes(String input) {
    return literalQuote+input+literalQuote;
  }

  private static final String stringInQuotes = group(
      zeroOrMore(whiteSpace)+betweenQuotes(oneOrMore(not(literalQuote))) + zeroOrMore(whiteSpace)
  );

  private static final String keyValue = stringInQuotes + literalSemicolon + stringInQuotes;
  private static final String keyValueGroup = group(keyValue);
  private static final String commaThenKeyValueGroup = group(literalComma + keyValue);

  private static final String zeroOrOneEntryDictionary = betweenLiteralBraces(zeroOrOne(keyValueGroup));
  private static final String moreThanOneEntryDictionary = betweenLiteralBraces(keyValueGroup + zeroOrMore(commaThenKeyValueGroup));


  private static String validatorRegex =
      or( zeroOrOneEntryDictionary, moreThanOneEntryDictionary);

  public static void main(String[] args) {
    System.out.println(populate_template(args[0], args[1]));
  }

  private static String populate_template(String template, String dictionary) {
    Map<String, String> processedDictionary = createDictionary(dictionary);

    String hash = group(zeroOrMore(not(or(literalRightBrace,literalLeftBrace))));

    Pattern pattern = Pattern.compile(betweenLiteralBraces(literalExclamationMark + hash));
    Matcher matcher = pattern.matcher(template);

    /* Code from StackOverflow */
    StringBuffer sb = new StringBuffer();
    while(matcher.find()) {
      String key = matcher.group(1);
      String value = processedDictionary.get(key);
      if(value == null) {
        throw new RuntimeException("Hash does not exist");
      }
      matcher.appendReplacement(sb, value);
    }
    matcher.appendTail(sb);
    return sb.toString();
    /* Code from StackOverflow */
  }


  private static Map<String, String> createDictionary(String dictionary) {
    boolean isValid = isDictionaryValid(dictionary);
    if (isValid) {
      return createEntries(dictionary);
    } else {
      throw new RuntimeException("Dictionary is not valid");
    }
  }

  private static boolean isDictionaryValid(String dictionary) {
    Pattern validator = Pattern.compile(validatorRegex);

    Matcher validatorMatcher = validator.matcher(dictionary);

    return validatorMatcher.matches();
  }

  private static Map<String, String> createEntries(String dictionary) {
    Pattern entryPattern = Pattern.compile(keyValueGroup);
    Matcher entryMatcher = entryPattern.matcher(dictionary);
    Map<String, String> replacements = new HashMap<>();
    while (entryMatcher.find()) {
      String key = trimAndRemoveQuotes(entryMatcher.group(2));
      String value = trimAndRemoveQuotes(entryMatcher.group(3));
      replacements.put(key, value);
    }
    return replacements;
  }

  private static String trimAndRemoveQuotes(String input) {
    String trimmed = input.trim();
    return trimmed.substring(1, trimmed.length() - 1);
  }
}
