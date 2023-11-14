package ie.tcd.macgioca;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

public class CustomAnalyzer extends Analyzer {

    private static String STOPWORDS_ENGLISH = "../lists/stop_words_english.txt";

    public CustomAnalyzer() {
        super();
        System.out.println("CustomAnalyzer created");
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer tokenizer = new StandardTokenizer();
        TokenStream tokenStream = new LowerCaseFilter(tokenizer);
        tokenStream = new PorterStemFilter(tokenStream);
        tokenStream = new PunctuationFilter(tokenStream);
        // Stop words from https://countwordsfree.com/stopwords
        List<String> stopWords;
        try {
            stopWords = Files.readAllLines(Paths.get(STOPWORDS_ENGLISH), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            stopWords = Arrays.asList();
        }
        tokenStream = new StopFilter(tokenStream, new CharArraySet(stopWords, true));
        return new TokenStreamComponents(tokenizer, tokenStream);

    }
}
