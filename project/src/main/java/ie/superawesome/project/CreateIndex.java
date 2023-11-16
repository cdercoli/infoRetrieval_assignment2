package ie.superawesome.project;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.file.Files;
import java.io.File;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;


class Query
{
    int num;
    String include, ignore;
    public Query(int num, String include, String ignore){
        this.num = num;
        this.include = include;
        this.ignore = ignore;
    }
}

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
            CollectionIndexer.Index(indexWriter);
            indexWriter.close();
            directory.close();
            


            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(INDEX_DIRECTORY)));
            IndexSearcher searcher = new IndexSearcher(reader);
            Similarity similarity = new ClassicSimilarity();
            List<Query> queries = topicsToQueries(Paths.get("..", "topics", "topics").toString());




            searcher.setSimilarity(similarity);
            for (Query queryDoc : queries) {
                System.out.println("ID: " + queryDoc.num +" Query: " + queryDoc.include);
                HashMap<String, Float> boosts = new HashMap<String, Float>();
                boosts.put("text", 1.0f);
                MultiFieldQueryParser multiFieldQP = new MultiFieldQueryParser(new String[] { "text" }, analyzer, boosts);
                org.apache.lucene.search.Query query = multiFieldQP.parse(queryDoc.include);
                ScoreDoc[] results = searcher.search(query, 1000).scoreDocs;

                int rank = 1;
                int limit = 3;
                for (ScoreDoc scoreDoc : results) {
                    if (limit-- == 0) {
                        break;
                    }
                    Document document = searcher.doc(scoreDoc.doc);
                    System.out.println(document.get("docid"));
                    //writer.write(queryDoc.num + " Q0 " + document.get("id") + " "+rank+" " + scoreDoc.score + "VMS\n");
                    rank++;
                    
                }
            }

 
        }catch (Exception e){
            System.out.println("Error: " + e.toString());
        }
    }

    private static String retrieve(String regex,String doc)
    {
        Matcher matcher = Pattern.compile(regex,Pattern.DOTALL).matcher(doc);
        if(matcher.find()){
            return matcher.group(1).trim();
        }
        else return "";
    }

    private static Pattern tagRegex(String tag){
        return Pattern.compile("<"+tag+">(.+?)</"+tag+">", Pattern.DOTALL);
    }

    private static List<Query> topicsToQueries(String pathString) throws IOException
    {
        File file = new File(pathString);
        String content = new String(Files.readAllBytes(Paths.get(file.getPath())));
        List<String> topics = new ArrayList<String>();
        Matcher matcher = tagRegex("top").matcher(content);
        while(matcher.find()){
            topics.add(matcher.group(1));
        }
        List<Query> queries = new ArrayList<Query>();
        for(String topic : topics){
            topic = topic.trim();
            int num = Integer.parseInt(topic.substring(14,17));
            String title = retrieve("<title>(.+?)<desc>", topic);

            String desc = retrieve("<desc> Description:(.+?)<narr>",topic).replace("\n"," ");

            String narr = retrieve("<narr> Narrative:((.|\n)*)", topic).replace("\n"," ");

            String[] sentences = narr.split("\\.|;");
            String ignoreable = "";
            for (String sentence : sentences){
                if( sentence.contains("not relevant")){
                    ignoreable = ignoreable + " "+ sentence.trim();
                }
            }
            ignoreable = ignoreable.replace(" not relevant", "").trim();
            queries.add(new Query(num,title+' '+desc,ignoreable));
        }
        return queries;
    }
}
