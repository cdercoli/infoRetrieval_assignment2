package ie.superawesome.project;

import java.util.Set;

import org.apache.lucene.index.IndexWriter;
import org.reflections.Reflections;

public class CollectionIndexer {
    public static void Index(IndexWriter indexWriter) {
        Reflections reflections = new Reflections("ie.superawesome.project");
        Set<Class<? extends Collection>> collectionClasses = reflections.getSubTypesOf(Collection.class);
        for (Class<? extends Collection> klass : collectionClasses) {
            CollectionLocation collectionLocation = klass.getAnnotation(CollectionLocation.class);
            if (collectionLocation == null) {
                System.out.println("Class " + klass.getName() + " does not have CollectionLocation annotation, if you want to index this collection please add it.");
                continue;
            }
            String path = collectionLocation.path();
            try {
                System.out.println("Indexing collection " + klass.getName() + " at path " + path);
                klass.getConstructor().newInstance().IndexFolder(indexWriter, path);
            } catch (Exception e) {
                System.out.println("Error indexing collection " + klass.getName() + " at path " + path);
                e.printStackTrace();
                continue;
            }       
            System.out.println("Finished indexing collection " + klass.getName() + " at path " + path);
        }
    }
}
