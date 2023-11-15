package ie.superawesome.project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;

public class LATimes extends Collection {
    @Override
    protected void IndexFile(IndexWriter indexWriter, String filename) throws IOException {
        BufferedReader objReader = new BufferedReader(new FileReader(filename));

        while (objReader.readLine() != null) {
            String strCurrentLine = objReader.readLine();
            if(strCurrentLine == null){
                break;
            }
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
                doc.add(new StringField("docid", strCurrentLine, Field.Store.YES));
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

    @Override
    protected String Path() {
        return Paths.get("..", "collection", "latimes").toString();
    }

}
