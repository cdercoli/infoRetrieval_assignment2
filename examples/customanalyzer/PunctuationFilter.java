package ie.tcd.macgioca;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class PunctuationFilter extends TokenFilter {

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

    public PunctuationFilter(TokenStream input) {
        super(input);
    }

    @Override
    public boolean incrementToken() throws IOException {
        while (input.incrementToken()) {
            char[] buffer = termAtt.buffer();
            int length = termAtt.length();
            int newLength = 0;

            for (int i = 0; i < length; i++) {
                char c = buffer[i];
                if (Character.isLetterOrDigit(c)) {
                    buffer[newLength++] = c;
                }
            }

            termAtt.setLength(newLength);
            return true;
        }

        return false;
    }
    
}
