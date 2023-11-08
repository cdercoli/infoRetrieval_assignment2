package ie.superawesome.project;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.file.Files;
import java.io.File;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;

public class CreateIndex
{

    private static String INDEX_DIRECTORY = "./index";
    public static void main(String[] args) throws IOException
    {
        try {
            Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
            Analyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
            indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);

            IndexDocument(indexWriter, "../../infoweb/Assignment Two/fbis");
            
            indexWriter.close();
            directory.close();
        }catch (Exception e){
            System.out.println("Error: " + e.toString());
        }
    }

    public static Pattern tagRegex(String tag){
        return Pattern.compile("<"+tag+">(.+?)</"+tag+">", Pattern.DOTALL);
    }

    public static void IndexDocument(IndexWriter writer, String pathString) throws IOException, ParseException
    {
        File path = new File(pathString);
        File[] files = path.listFiles();
        ArrayList<Document> docs = new ArrayList<Document>();
        for (File file : files) {
            if(!file.getName().contains("read")){
                String content = new String(Files.readAllBytes(Paths.get(file.getPath())));
                List<String> docTexts = new ArrayList<String>();
                Matcher matcher = tagRegex("DOC").matcher(content);
                while(matcher.find()){
                    docTexts.add(matcher.group(1));
                }
                for(String docText : docTexts){
                    Document doc = new Document();
                    matcher = tagRegex("DOCNO").matcher(docText);
                    matcher.find();
                    String docNo = matcher.group(1);
                    docNo = docNo.trim();
                    doc.add(new StringField("DOCNO",docNo,Field.Store.YES));


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
                    if(DateNum == 19700101){
                        System.out.println(file.getName()+", "+docNo);
                    }
                    doc.add(new LongPoint("DATE",DateNum));

                    matcher = tagRegex("TI").matcher(docText);
                    matcher.find();
                    String title = matcher.group(1);
                    title = title.trim();
                    doc.add(new TextField("TITLE", title, Field.Store.YES));

                    System.out.println(file.getName()+" - "+docNo+": "+title);

                    matcher = tagRegex("TEXT").matcher(docText);
                    matcher.find();
                    String text = matcher.group(1);
                    text = text.replaceAll("(<F.+?>)|(</F>)|(</?H[0-9]>)|(Language:.*)|(Article Type:.*)","");
                    text = text.trim();
                    doc.add(new TextField("TEXT",text,Field.Store.YES));

                    docs.add(doc);
                }
            }
        }
        writer.addDocuments(docs);
        writer.close();
    }
}
