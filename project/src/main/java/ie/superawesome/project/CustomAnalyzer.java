package ie.superawesome.project;

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
import org.tartarus.snowball.ext.EnglishStemmer;
import org.apache.lucene.analysis.ngram.NGramTokenFilter;

import org.apache.lucene.analysis.en.EnglishMinimalStemmer;
import org.apache.lucene.analysis.miscellaneous.TrimFilter;

import org.apache.lucene.analysis.en.KStemFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;

import org.apache.lucene.analysis.miscellaneous.CapitalizationFilter;
import org.apache.lucene.analysis.ngram.NGramTokenizer;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

    public class CustomAnalyzer extends Analyzer {

        private static String STOPWORDS_ENGLISH = "stopwords/stop_words_english.txt";

        public CustomAnalyzer() {
            super();
            System.out.println("CustomAnalyzer created");
        }

        @Override
        protected TokenStreamComponents createComponents(String fieldName) {
            Tokenizer tokenizer = new StandardTokenizer();
//             Tokenizer tokenizer = new NGramTokenizer(1, 3);

            TokenStream tokenStream = new LowerCaseFilter(tokenizer);
//            tokenStream = new TrimFilter(tokenStream);
//            tokenStream = new PorterStemFilter(tokenStream);
//            tokenStream = new KStemFilter(tokenStream);

            // tokenStream = new ShingleFilter(tokenStream, 3);
            //tokenStream = new PunctuationFilter(tokenStream);
            // Stop words from https://countwordsfree.com/stopwords
            List<String> stopWords;
            try {
                stopWords = Files.readAllLines(Paths.get(STOPWORDS_ENGLISH), StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
                stopWords = Arrays.asList();
            }
            tokenStream = new StopFilter(tokenStream, new CharArraySet(stopWords, true));
            tokenStream = new SnowballFilter(tokenStream, new EnglishStemmer());

            System.out.println("Custom Analyzer Completed");
            return new TokenStreamComponents(tokenizer, tokenStream);

        }
//        protected TokenStreamComponents createComponents(String fieldName) {
//            Tokenizer tokenizer = new StandardTokenizer();
//            TokenStream tokenStream = new LowerCaseFilter(tokenizer);
//            // Stop words from https://countwordsfree.com/stopwords
//            List<String> stopWords;
//            try {
//                stopWords = Files.readAllLines(Paths.get(STOPWORDS_ENGLISH), StandardCharsets.UTF_8);
//            } catch (IOException e) {
//                e.printStackTrace();
//                stopWords = Arrays.asList();
//            }
//            tokenStream = new StopFilter(tokenStream, new CharArraySet(stopWords, true));
//            tokenStream = new PorterStemFilter(tokenStream);
//
//            return new TokenStreamComponents(tokenizer, tokenStream);
//
//        }
    }