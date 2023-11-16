package ie.superawesome.project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;

public class ForeignBroadcastInformationService extends Collection {
    private static Pattern tagRegex(String tag){
        return Pattern.compile("<"+tag+">(.+?)</"+tag+">", Pattern.DOTALL);
    }

    @Override
    protected void IndexFile(IndexWriter indexWriter, String filename) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(filename)));
        List<String> docTexts = new ArrayList<String>();
        Matcher matcher = tagRegex("DOC").matcher(content);
        while(matcher.find()){
            docTexts.add(matcher.group(1));
        }
        for(String docText : docTexts){
            try{
            Document doc = new Document();
            matcher = tagRegex("DOCNO").matcher(docText);
            matcher.find();
            String docNo = matcher.group(1);
            docNo = docNo.trim();
            doc.add(new StringField("docid",docNo,Field.Store.YES));


            matcher = tagRegex("DATE1").matcher(docText);
            matcher.find();
            String dateText = matcher.group(1).trim();
            SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy");
            Date date = formatter.parse("1 January 1970");
            try {
                date = formatter.parse(dateText);
            } catch (ParseException _e) {
                try {
                    date = new SimpleDateFormat("MMMM dd yyyy").parse(dateText);
                } catch (ParseException __e) {
                    try {
                        date = formatter.parse(dateText + " 1970");
                    } catch (ParseException ___e) {
                        try {
                            date = formatter.parse("01 January "+dateText.substring(dateText.length()-4,dateText.length()));
                        } catch (ParseException ____e) {
                            ____e.printStackTrace();
                        }
                    }
                }
            }
            int DateNum = Integer.parseInt(new SimpleDateFormat("yyyyMMdd").format(date));
            doc.add(new LongPoint("DATE",DateNum));

            matcher = tagRegex("TI").matcher(docText);
            matcher.find();
            String title = matcher.group(1);
            title = title.trim();
            doc.add(new TextField("title", title, Field.Store.YES));



            matcher = tagRegex("TEXT").matcher(docText);
            matcher.find();
            String text = matcher.group(1);
            text = text.replaceAll("(<F.+?>)|(</F>)|(</?H[0-9]>)|(Language:.*)|(Article Type:.*)","");
            text = text.trim();
            doc.add(new TextField("text",text,Field.Store.YES));

            indexWriter.addDocument(doc);
            } catch (Exception e) {
                System.out.println("Error indexing document " + filename);
                e.printStackTrace();
            }
        }
    }

    @Override
    protected String Path() {
        return Paths.get("..", "collection", "fbis").toString();
    }
    
}
