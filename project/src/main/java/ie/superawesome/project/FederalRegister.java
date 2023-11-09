package ie.superawesome.project;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.index.IndexWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class FederalRegister extends Collection {

    @Override
    protected void IndexFile(IndexWriter indexWriter, String filename) throws IOException {
        Document file = Jsoup.parse(new File(filename));
        Elements elements = file.select("DOC");
        for (Element elem : elements) {
            String title = elem.select("DOCTITLE").text();
            String id = elem.select("DOCNO").text();
            String content = elem.select("TEXT").text();
            // Index relevant fields
            System.out.printf("ID: %s, Title: %s, ContentLength: %d\n", id, title, content.length());
        }
    }
    
}
