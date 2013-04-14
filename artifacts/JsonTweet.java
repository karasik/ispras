package artifacts;

/**
 * Author: mi
 * Date: 3/18/13
 * Time: 10:11 PM
 */
public class JsonTweet {
    public String author;
    public String norm1_text;

    private JsonTweet() {

    }
    @Override
    public String toString() {
        return "Text: " + norm1_text;
    }
}
