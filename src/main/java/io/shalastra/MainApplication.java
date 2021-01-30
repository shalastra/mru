package io.shalastra;

import picocli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Properties;
import java.util.function.Consumer;

public class MainApplication {

    @Property("mru.version")
    private String val;

    public static void main(String[] args) throws IOException {
        Properties properties = new Properties();
        InputStream resourceAsStream = MainApplication.class.getResourceAsStream("/application.properties");
        properties.load(resourceAsStream);
        MainApplication mainApplication = new MainApplication();
        bindProperties(properties, mainApplication);

        int exitCode = new CommandLine(new GitUpdate()).execute(args);
        System.exit(exitCode);
    }

    private static void bindProperties(Properties properties, Object object) {
        Arrays.stream(object.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Property.class))
                .forEach(getFieldConsumer(properties, object));
    }

    private static Consumer<Field> getFieldConsumer(Properties properties, Object object) {
        return field ->  extracted(properties, object, field);
    }

    private static void extracted(Properties properties, Object object, Field field) {
        try {
            Property property = field.getAnnotation(Property.class);
            String key = property.value();
            String propertyValue = properties.getProperty(key);
            field.setAccessible(true);
            field.set(object, propertyValue);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }

    public String getVal() {
        return val;
    }
}

