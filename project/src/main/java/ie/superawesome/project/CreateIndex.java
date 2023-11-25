package ie.superawesome.project;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.file.Files;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;


class Query
{
    int num;
    String desc, narr, title;
    public Query(int num, String desc, String narr, String title){
        this.num = num;
        this.desc = desc;
        this.narr = narr;
        this.title = title;
    }
}

public class CreateIndex
{

    private static String INDEX_DIRECTORY = "./index";
    public static void main(String[] args) throws IOException
    {
        try {
            List<Query> queries = topicsToQueries(Paths.get("..", "topics", "topics").toString());


            Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
            Analyzer analyzer = new EnglishAnalyzer();
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
            indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
            CollectionIndexer.Index(indexWriter);
            indexWriter.close();
            directory.close();
            


            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(INDEX_DIRECTORY)));
            IndexSearcher searcher = new IndexSearcher(reader);
            Similarity similarity = new BM25Similarity();
            

            // Delete previous results
            File file = new File("results/query_results.txt");
            file.delete();
            // set up file writer to write results to
            FileWriter fileWriter = new FileWriter("results/query_results.txt");
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            // initialise the individual queries
            StringBuilder queryResultBuilder = new StringBuilder();


            searcher.setSimilarity(similarity);
            for (Query queryDoc : queries) {
                HashMap<String, Float> boosts = new HashMap<String, Float>();
                boosts.put("title", 0.1f);
                boosts.put("text", 1.0f);
                MultiFieldQueryParser multiFieldQP = new MultiFieldQueryParser(new String[] { "text", "title" }, analyzer, boosts);
                org.apache.lucene.search.Query queryDesc = multiFieldQP.parse(QueryParser.escape(queryDoc.desc));
                org.apache.lucene.search.Query queryTitle = multiFieldQP.parse(QueryParser.escape(queryDoc.title));


                String[] narr = splitNarrative(queryDoc.narr);
                String relNarr = narr[0];
                org.apache.lucene.search.Query queryNarr = null;
                if (relNarr.length() > 0) {
                    queryNarr = multiFieldQP.parse(QueryParser.escape(relNarr));
                }

                String irrelNarr = narr[1];
                org.apache.lucene.search.Query queryNarrIrrel = null;
                if (irrelNarr.length() > 0) {
                    queryNarrIrrel = multiFieldQP.parse(QueryParser.escape(irrelNarr));
                }


                BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();

                booleanQuery.add(new BoostQuery(queryTitle, 4.5f), BooleanClause.Occur.SHOULD);
                booleanQuery.add(new BoostQuery(queryDesc, 1.8f), BooleanClause.Occur.SHOULD);

                if (queryNarr != null) {
                    booleanQuery.add(new BoostQuery(queryNarr, 0.8f), BooleanClause.Occur.SHOULD);
                }

                if (queryNarrIrrel != null) {
                    //booleanQuery.add(new BoostQuery(queryNarrIrrel, 0.05f), BooleanClause.Occur.MUST_NOT);
                }

                ScoreDoc[] results = searcher.search(booleanQuery.build(), 1000).scoreDocs;

                int rank = 1;
                for (ScoreDoc scoreDoc : results) {
                    Document document = searcher.doc(scoreDoc.doc);
                    String resultLine = queryDoc.num + " Q0 " + document.get("docid") + " "+rank+" " + scoreDoc.score + " VMS\n";
                    queryResultBuilder.append(resultLine);
                    rank++;
                    
                }

            }
            bufferedWriter.write(queryResultBuilder.toString());
            bufferedWriter.close();
            
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
            String title = retrieve("<title>(.+?)<desc>", topic).toLowerCase();

            String desc = retrieve("<desc> Description:(.+?)<narr>",topic).replace("\n"," ").toLowerCase();

            String narr = retrieve("<narr> Narrative:((.|\n)*)", topic).replace("\n"," ").toLowerCase();

        
            queries.add(new Query(num,desc,narr, title));
        }
        return queries;
    }

    // Return narrivate split into relevant and irrevelant sentences 
    private static String[] splitNarrative(String narrative) {
        String[] splitNarrative = new String[2];
        String relevant = "";
        String irrelevant = "";
        java.text.BreakIterator iterator = java.text.BreakIterator.getSentenceInstance(java.util.Locale.US);
        iterator.setText(narrative);
        int start = iterator.first();
        for (int end = iterator.next(); end != java.text.BreakIterator.DONE; start = end, end = iterator.next()) {
            String sentence = narrative.substring(start,end).replaceAll("[\n\r]", "");

            if (sentence.contains("unless")) {
                continue;
            }
            // split sentence at "even" and keep first half
            String[] splitSentence = sentence.split("even");
            if (splitSentence.length > 1) {
                sentence = splitSentence[0];
            }
            //System.out.println(sentence);
            boolean isIrrelevant = false;
            for (String irrel : irrelevantArr) {
                if (sentence.contains(irrel)) {
                    isIrrelevant = true;
                    sentence = sentence.replaceAll(irrel, "");
                    for (String gen : generalArr) {
                        sentence = sentence.replaceAll(gen, "");
                    }
                    irrelevant += sentence + " ";
                    break;
                } 
            }
            if (isIrrelevant) {
                continue;
            }
            for (String rel : relevantArr) {
                sentence = sentence.replaceAll(rel, "");
                for (String gen : generalArr) {
                    sentence = sentence.replaceAll(gen, "");
                }
            }
            relevant += sentence + " ";
        }
        splitNarrative[0] = relevant;
        splitNarrative[1] = irrelevant;
        return splitNarrative;
    }

    // Phrases to identify relevant sentences and to remove them from the narrative
    private static String[] relevantArr = new String[] {
        "are relevant",
        "are all relevant",
        "are also relevant",
        "are all of interest",
        "are considered relevant",
        "relevant items could also",
        "relevant items include",
        "relevant documents will discuss",
        "relevant documents must cite",
        "relevant documents will contain any information about",
        "relevant documents will contain",
        "relevant documents may also include",
        "a relevant document must show",
        "a relevant document must include mention",
        "a relevant document must contain",
        "a relevant document must describe",
        "a relevant document may include",
        "a relevant document identifies",
        "a relevant document provides",
        "a relevant document must discuss",
        "a relevant document could identify",
        "a relevant document will discuss",
        "a relevant document will provide information on",
        "a relevant document will provide information regarding",
        "a relevant document will provide",
        "a relevant document will contain information",
        "a relevant document will focus",
        "a relevant document will",
        "is relevant",
        "is also relevant",
        "is relevant when tied in with",
        "also deemed relevant is",
        "to be relevant, a document will",
        "to be relevant, a document must discuss",
        "to be relevant, a document must indicate that"
    };

    // Phrases to identify irrelevant sentences and to remove them from the narrative
    private static String[] irrelevantArr = new String[] {
        "is not relevant",
        "are also not relevant",
        "are not relevant",
        "not relevant",
    };

    // General phrases that can be removed from the narrative
    private static String[] generalArr = new String[] {
        "documents that address",
        "documents that note",
        "documents that give",
        "documents that discuss",
        "documents that indicate",
        "documents that describe",
        "documents that identify",
        "documents that",
        "documents pertaining to",
        "documents discussing",
        "documents mentioning",
        "documents describing any",
        "documents describing",
        "any discussion of",
        "any mention of",
        "discussions of",
        "a general mention",
        "the intent of this query is to",
        "all references to"
    };
}
