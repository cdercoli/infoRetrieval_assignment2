package ie.superawesome.project;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CollectionLocation {
    public String path() default "";

}