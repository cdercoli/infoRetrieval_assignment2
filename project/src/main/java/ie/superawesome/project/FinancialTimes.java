package ie.superawesome.project;

import java.io.File;
import java.io.IOException;

import java.nio.file.Paths;

import org.apache.lucene.index.IndexWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class FinancialTimes extends Collection {
    @Override
    protected void IndexFile(IndexWriter indexWriter, String filename) throws IOException {
        Document file = Jsoup.parse(new File(filename));
        Elements elements = file.select("DOC");
        for (Element elem : elements) {
            String title = elem.select("HEADLINE").text();
            String id = elem.select("DOCNO").text();
            // Additionally we can remove elements here if we don't want them in the text tag
            // elem.select("blah").remove();
            String content = elem.select("TEXT").text();
            // Index relevant fields
            // System.out.printf("ID: %s, Title: %s, ContentLength: %d\n", id, title, content.length());
        }
    }

    @Override
    protected String Path() {
        return Paths.get("..", "collection", "ft").toString();
    }

}