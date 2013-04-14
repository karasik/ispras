package auxiliary;

import artifacts.JsonTweet;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Author: mi
 * Date: 4/12/13
 * Time: 9:07 PM
 */
public class TweetJsonReader implements ITweetReader {
    private BufferedReader in;
    private Gson g;

    public TweetJsonReader(String filename) throws FileNotFoundException {
        in = new BufferedReader(new FileReader(filename));
        g = new Gson();
    }

    @Override
    public String nextTweet() throws IOException {
        String s = in.readLine();
        JsonTweet t = g.fromJson(in.readLine(), JsonTweet.class);
        return t.norm1_text;
    }

    @Override
    public boolean hasMoreTweets() throws IOException {
        return in.ready();
    }
}
