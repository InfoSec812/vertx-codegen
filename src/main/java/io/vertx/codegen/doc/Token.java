package io.vertx.codegen.doc;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A comment token.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public abstract class Token {

  private static final Pattern TOKEN_SPLITTER = Pattern.compile("(\\{@\\p{Alpha}[^\\}]*\\})|(\\r?\\n)");
  private static final Pattern INLINE_TAG_PATTERN = Pattern.compile("\\{@([^\\p{javaWhitespace}]+)((?:.|\\n)*)\\}");

  /**
   * Tokenize the string.
   *
   * @param s the string to tokenize
   * @return the tokens after analysis
   */
  public static List<Token> tokenize(String s) {
    ArrayList<Token> events = new ArrayList<>();
    parse(s, 0, TOKEN_SPLITTER.matcher(s), events);
    return events;
  }

  private static void parse(String s, int prev, Matcher matcher, ArrayList<Token> events) {
    if (matcher.find()) {
      if (matcher.start() > prev) {
        events.add(new Token.Text(s.substring(prev, matcher.start())));
      }
      String value = s.substring(matcher.start(), matcher.end());
      if (matcher.group(1) != null) {
        Matcher tagMatcher = INLINE_TAG_PATTERN.matcher(value);
        if (!tagMatcher.matches()) {
          // If we are here, it means the INLINE_TAG_PATTERN matches less then TOKEN_SPLITTER and this is a bug.
          // so we should know at least the value that raised it
          throw new AssertionError("bug -->" + value + "<--");
        }
        events.add(new Token.InlineTag(value, new Tag(tagMatcher.group(1), tagMatcher.group(2))));
      } else {
        events.add(new Token.LineBreak(value));
      }
      parse(s, matcher.end(), matcher, events);
    } else {
      if (prev < s.length()) {
        events.add(new Token.Text(s.substring(prev, s.length())));
      }
    }
  }

  final String value;

  public Token(String value) {
    this.value = value;
  }

  /**
   * @return true if the token is text
   */
  public boolean isText() {
    return false;
  }

  /**
   * @return true if the token is an inline tag
   */
  public boolean isInlineTag() {
    return false;
  }

  /**
   * @return true if the token is line break
   */
  public boolean isLineBreak() {
    return false;
  }

  /**
   * @return the token text
   */
  public String getValue() {
    return value;
  }

  public static class Text extends Token {
    public Text(String value) {
      super(value);
    }

    @Override
    public boolean isText() {
      return true;
    }
  }

  public static class LineBreak extends Token {
    public LineBreak(String value) {
      super(value);
    }

    @Override
    public boolean isLineBreak() {
      return true;
    }
  }

  public static class InlineTag extends Token {

    private final Tag tag;

    public InlineTag(String value, Tag tag) {
      super(value);
      this.tag = tag;
    }

    @Override
    public boolean isInlineTag() {
      return true;
    }

    /**
     * @return the parsed tag
     */
    public Tag getTag() {
      return tag;
    }
  }
}
