package auxiliary;

import java.io.IOException;

/**
 * Author: mi
 * Date: 4/3/13
 * Time: 12:46 PM
 */
public interface ITweetReader {
    public String nextTweet() throws IOException;
    public boolean hasMoreTweets() throws IOException;
}
