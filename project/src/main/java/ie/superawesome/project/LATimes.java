package ie.superawesome.project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class LATimes extends Collection {
    @Override
    protected void IndexFile(IndexWriter indexWriter, String filename) throws IOException {
        Document file = Jsoup.parse(new File(filename));
        Elements elements = file.select("DOC");
        for (Element elem : elements) {
            elem.select("SECTION").remove();
            elem.select("TYPE").remove();

            String content = elem.select("TEXT").text() + " " + elem.select("GRAPHIC").text();
            
            String docno = elem.select("DOCNO").text();
            String date = elem.select("DATE").text();
            String section = elem.select("SECTION").text();
            String length = elem.select("LENGTH").text();
            String headline = elem.select("HEADLINE").text();
            String byline = elem.select("BYLINE").text();

            org.apache.lucene.document.Document document = new org.apache.lucene.document.Document();
            document.add(new StringField("docid", docno, Field.Store.YES));
            document.add(new TextField("text", headline, Field.Store.YES));
            document.add(new TextField("text", content, Field.Store.YES));
            indexWriter.addDocument(document);

            // Index relevant fields
            // System.out.printf("ID: %s, Title: %s, ContentLength: %d\n", id, title, content.length());
        }
    }

    @Override
    protected String Path() {
        return Paths.get("..", "collection", "latimes").toString();
    }

}