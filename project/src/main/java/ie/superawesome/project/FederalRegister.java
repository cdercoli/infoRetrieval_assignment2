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

public class FederalRegister extends Collection {
    @Override
    protected void IndexFile(IndexWriter indexWriter, String filename) throws IOException {
        Document file = Jsoup.parse(new File(filename));
        Elements elements = file.select("DOC");
        org.apache.lucene.document.Document document;
        String title, id, content;
        for (Element elem : elements) {
            title = elem.select("DOCTITLE").text();
            id = elem.select("DOCNO").text();
            // Additionally we can remove elements here if we don't want them in the text tag
            // elem.select("blah").remove()

            elem.select("ACTION").remove();
            elem.select("RINDOCK").remove();
            elem.select("SIGNJOB").remove();
            elem.select("FRFILING").remove();
            elem.select("BILLING").remove();



            content = elem.select("TEXT").text();
            // Index relevant fields
            // System.out.printf("ID: %s, Title: %s, ContentLength: %d\n", id, title, content.length());
            document = new org.apache.lucene.document.Document();
            document.add(new StringField("docid", id, Field.Store.YES));
            document.add(new TextField("title", title, Field.Store.YES));
            document.add(new TextField("text", content, Field.Store.YES));
            indexWriter.addDocument(document);
        }
    }

    @Override
    protected String Path() {
        return Paths.get("..", "collection", "fr94").toString();
    }
    
}
