package ie.superawesome.project;

import java.io.IOException;
import java.io.*;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.lucene.document.Document;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;


public class CreateIndex
{
    private static String INDEX_DIRECTORY = "./index";
    public static void main(String[] args) throws IOException


    {
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
        Analyzer analyzer = new StandardAnalyzer();

        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
        BufferedReader objReader = null;

        File f = new File("./data/latimes");
        File[] files = f.listFiles();

        // Display the names of the files
            try {
                for (int i = 0; i < files.length - 2; i++) {
                    objReader = new BufferedReader(new FileReader("./data/latimes/" + files[i].getName()));

                    while (objReader.readLine() != null) {
                        String strCurrentLine = objReader.readLine();
                        String text = "";
                        String byline = "";
                        String headline = "";
                        Document doc = new Document();

                        //IndexDocument(indexWriter, "Path to file or whatever");
                        if (strCurrentLine.contains("<DOCNO>")) {
                            strCurrentLine = strCurrentLine.substring(8, 21);
                            //System.out.println(strCurrentLine);
                            doc.add(new TextField("docno", strCurrentLine, Field.Store.YES));
                        }
                        if (strCurrentLine.contains("<DOCID>")) {
                            strCurrentLine = strCurrentLine.substring(8, strCurrentLine.indexOf("/") - 2);
                            //System.out.println(strCurrentLine);
                            doc.add(new TextField("docid", strCurrentLine, Field.Store.YES));
                        }
                        if (strCurrentLine.contains("<DATE>")) {
                            strCurrentLine = objReader.readLine();
                            strCurrentLine = objReader.readLine(); //it says stuff like home edition but there is no explanation on the readme
                            //System.out.println(strCurrentLine);
                            //SimpleDateFormat sdf = new SimpleDateFormat("MMMM, F, yyyy");
                            //String date = sdf.format(new Date(strCurrentLine));
                            doc.add(new TextField("date", strCurrentLine, Field.Store.YES));
                        }
                        if (strCurrentLine.contains("<SECTION>")) {
                            strCurrentLine = objReader.readLine();
                            strCurrentLine = objReader.readLine(); //it says stuff like home edition but there is no explanation on the readme
                            //System.out.println(strCurrentLine);
                            doc.add(new TextField("section", strCurrentLine, Field.Store.YES));
                        }

                        if (strCurrentLine.contains("<LENGTH>")) {
                            strCurrentLine = objReader.readLine();
                            strCurrentLine = objReader.readLine();
                            if (strCurrentLine.contains("word")) {
                                strCurrentLine = (strCurrentLine.substring(0, strCurrentLine.indexOf("w") - 1));
                            }
                            //System.out.println(strCurrentLine);
                            doc.add(new TextField("length", strCurrentLine, Field.Store.YES));
                        }

                        if (strCurrentLine.contains("<HEADLINE>")) {
                            strCurrentLine = objReader.readLine();//<P>
                            strCurrentLine = objReader.readLine();//prima headline
                            headline = strCurrentLine;
                            strCurrentLine = objReader.readLine();//</P>
                            if (objReader.readLine().contains("<P>")) {
                                strCurrentLine = objReader.readLine();
                                headline += strCurrentLine;
                                do {
                                    headline += objReader.readLine();
                                } while ((!objReader.readLine().equals("</P>")));
                            }
                            //System.out.println(headline);
                            doc.add(new TextField("headline", headline, Field.Store.YES));
                        }

                        if (strCurrentLine.contains("<BYLINE>")) {
                            if (objReader.readLine().contains("<P>")) {
                                strCurrentLine = objReader.readLine();
                                byline += strCurrentLine;
                                while ((!strCurrentLine.startsWith("</P>"))) {
                                    byline += strCurrentLine;
                                    strCurrentLine = objReader.readLine();
                                }
                            }
                            //System.out.println(byline);
                            doc.add(new TextField("byline", byline, Field.Store.YES));
                        }


                        if (strCurrentLine.contains("<TEXT>")) {
                            if (objReader.readLine().contains("<P>")) {
                                strCurrentLine = objReader.readLine();
                                text += strCurrentLine;
                                while ((!strCurrentLine.startsWith("</TEXT>") && !strCurrentLine.startsWith("<TAB"))) {
                                    text += strCurrentLine;
                                    strCurrentLine = objReader.readLine();
                                }
                            }
                            text = text.replaceAll("<P>", "");
                            text = text.replaceAll("</P>", "");
                            text = text.replaceAll("<GRAPHIC>", "");
                            text = text.replaceAll("</GRAPHIC>", "");
                            //System.out.println(text);
                            doc.add(new TextField("text", text, Field.Store.YES));
                        }
                        indexWriter.addDocument(doc);
                    }
                }
                indexWriter.close();
                directory.close();
            } catch (Exception e) {
                System.out.println("Error: " + e.toString());
            }
        }
    }


