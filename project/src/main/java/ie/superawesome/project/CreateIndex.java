package ie.superawesome.project;

import java.io.IOException;
import java.nio.file.Paths;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;

import java.io.*;

import java.util.ArrayList;

import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.File;
import java.nio.file.Path;

import java.util.Scanner;

import org.apache.lucene.analysis.CharArraySet;

import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.DirectoryReader;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.ParseException;

import java.nio.file.Path;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.BooleanSimilarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.util.Set;

public class CreateIndex
{
    private static String DATA = "Assignment Two/ft/ft911/ft911_1";
    private static String TOPFOLDER = "Assignment Two/ft/";
    private static String INDEX_DIRECTORY = "index";

    public static void goThroughFolders(File directory, IndexWriterConfig config, Analyzer analyzer, IndexWriter iwriter) throws IOException{
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    System.out.println("Directory: " + file.getPath());
                    goThroughFolders(file, config, analyzer, iwriter);
                }
                else {
                    System.out.println("File: " + file.getPath());
                    createIndex(file.getPath(), config, analyzer, iwriter);
                }
            }
        }
    }

    public static void createIndex(String PATH, IndexWriterConfig config, Analyzer analyzer, IndexWriter iwriter) throws IOException {
        // logic for importing
        BufferedReader lineReader = new BufferedReader(new FileReader(PATH));
        String line;
        Document doc = new Document();

        while ((line = lineReader.readLine()) != null) {
//            System.out.println(line);
            if (line.startsWith("<DOC>")) {
                doc = new Document();
            }
            else if (line.startsWith("<DOCNO>")) {
                String docNo = line.replace("<DOCNO>", "").replace("</DOCNO>", "");
                doc.add(new TextField("DocNo", docNo, Field.Store.YES));
            }
            else if (line.startsWith("<DATE>")) {
                // change this to a number
                String date = line.replace("<DATE>", "").trim();
                int date_num = Integer.parseInt(date);
                doc.add(new IntPoint("date", date_num));
            }
            else if (line.startsWith("<HEADLINE>")) {
                StringBuilder headline = new StringBuilder();
                while (!line.startsWith("</HEADLINE>")) {
                    headline.append(line);
                    line = lineReader.readLine();
//                    System.out.println(line);
                }
                doc.add(new TextField("headline", headline.toString(), Field.Store.YES));
            }
            else if (line.startsWith("<BYLINE>")) {
                StringBuilder byline = new StringBuilder();
                while (!line.startsWith("</BYLINE>")) {
                    byline.append(line);
                    line = lineReader.readLine();
//                    System.out.println(line);
                }
                doc.add(new TextField("byline", byline.toString(), Field.Store.YES));
            }
            else if (line.startsWith("<PROFILE>")) {
                String profile = line.replace("<PROFILE>", "").replace("</PROFILE>", "");
                doc.add(new StringField("date", profile, Field.Store.YES));
            }
            else if (line.startsWith("<TEXT>")) {
                StringBuilder text = new StringBuilder();
                while (!line.startsWith("</TEXT>")) {
                    text.append(line);
                    line = lineReader.readLine();
//                    System.out.println(line);
                }
                doc.add(new TextField("text", text.toString(), Field.Store.YES));
            }
            else if (line.startsWith("<PUB>")) {
                String pub = line.replace("<PUB>", "").replace("</PUB>", "").trim();
                doc.add(new StringField("publication", pub, Field.Store.YES));
            }
            else if (line.startsWith("<PAGE>")) {
                StringBuilder page = new StringBuilder();
                while (!line.startsWith("</PAGE>")) {
                    page.append(line);
                    line = lineReader.readLine();
//                    System.out.println(line);
                }
                doc.add(new TextField("page", page.toString(), Field.Store.YES));
            }
            else if (line.startsWith("</DOC>")) {
                iwriter.addDocument(doc);
            }
        }
//        iwriter.close();
//        directory.close();

    }

    public static void main(String[] args) throws IOException
    {
        try {
            Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
            Analyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
            indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);

            File dir = new File(TOPFOLDER);
            goThroughFolders(dir, indexWriterConfig, analyzer, indexWriter);
            // createIndex(DATA, indexWriterConfig, analyzer, indexWriter);
            //IndexDocument(indexWriter, "Path to file or whatever");
//            createIndex(indexWriterConfig, analyzer, indexWriter);
            
            indexWriter.close();
            directory.close();
        }catch (Exception e){
            System.out.println("Error: " + e.toString());
        }
    }
}
