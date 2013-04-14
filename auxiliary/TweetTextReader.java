package auxiliary;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Author: mi
 * Date: 4/3/13
 * Time: 12:47 PM
 */
public class TweetTextReader implements ITweetReader {
    private BufferedReader in;

    public TweetTextReader(String filename) throws FileNotFoundException {
        in = new BufferedReader(new FileReader(filename));
    }

    @Override
    public String nextTweet() throws IOException {
        if (hasMoreTweets()) {
            return in.readLine();
        }
        return null;
    }

    @Override
    public boolean hasMoreTweets() throws IOException {
        return in.ready();
    }
}
