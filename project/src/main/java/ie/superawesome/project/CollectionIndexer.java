package ie.superawesome.project;

import java.util.Set;

import org.apache.lucene.index.IndexWriter;
import org.reflections.Reflections;

public class CollectionIndexer {
    public static void Index(IndexWriter indexWriter) {
        Reflections reflections = new Reflections("ie.superawesome.project");
        Set<Class<? extends Collection>> collectionClasses = reflections.getSubTypesOf(Collection.class);
        for (Class<? extends Collection> klass : collectionClasses) {
            try {
                Collection instance = klass.getConstructor().newInstance();
                System.out.println("Indexing collection " + klass.getName() + " at path " + instance.Path());
                instance.IndexFolder(indexWriter, instance.Path());
                System.out.println("Finished indexing collection " + klass.getName() + " at path " + instance.Path());
            } catch (Exception e) {
                System.out.println("Error indexing collection " + klass.getName());
                e.printStackTrace();
            }       
        }
    }
}
