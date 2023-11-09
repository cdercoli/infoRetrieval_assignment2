package ie.superawesome.project;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.index.IndexWriter;

public abstract class Collection {
    public void IndexFolder(IndexWriter indexWriter, String dir) throws IOException {
        File[] files = new File(dir).listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                IndexFolder(indexWriter, file.getAbsolutePath());
            } else {
                IndexFile(indexWriter, file.getAbsolutePath());
            }
        }
    }

    abstract protected void IndexFile(IndexWriter indexWriter, String filename) throws IOException;
}
