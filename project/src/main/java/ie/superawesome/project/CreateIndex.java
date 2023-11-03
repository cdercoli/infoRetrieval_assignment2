package ie.superawesome.project;

import java.io.IOException;
import java.nio.file.Paths;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
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

            //IndexDocument(indexWriter, "Path to file or whatever");
            
            indexWriter.close();
            directory.close();
        }catch (Exception e){
            System.out.println("Error: " + e.toString());
        }
    }
}
