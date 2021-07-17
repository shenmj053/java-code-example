package xyz.shenmj.tools;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

/**
 * Jackson 帮助类
 *
 * @author SHEN Minjiang
 */
public class Jackson {
    private Jackson() {
    }

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
            .enable(JsonParser.Feature.ALLOW_COMMENTS)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private static final ObjectWriter writer = objectMapper.writer();
    private static final ObjectWriter prettyWriter = objectMapper.writerWithDefaultPrettyPrinter();

    public static String toJsonPrettyString(Object value) {
        try {
            return prettyWriter.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static String toJsonString(Object value) {
        try {
            return writer.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static <T> T fromJsonString(String json, Class<T> clazz) {
        if (json == null)
            return null;
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parse Json String.", e);
        }
    }

    public static <T> List<T> fromJsonArrayStringToList(String json) {
        if (json == null)
            return null;
        try {
            return objectMapper.readValue(json, new TypeReference<List<T>>(){});
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parse Json String.", e);
        }
    }

    public static <K, V> Map<K, V> fromJsonStringToMap(String json) {
        if (json == null)
            return null;
        try {
            return objectMapper.readValue(json, new TypeReference<Map<K, V>>(){});
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parse Json String.", e);
        }
    }

    public static JsonNode jsonNodeOf(String json) {
        return fromJsonString(json, JsonNode.class);
    }

    public static JsonGenerator jsonGeneratorOf(Writer writer) throws IOException {
        return new JsonFactory().createGenerator(writer);
    }

    public static <T> T loadFrom(File file, Class<T> clazz) throws IOException {
        try {
            return objectMapper.readValue(file, clazz);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public static ObjectWriter getWriter() {
        return writer;
    }

    public static ObjectWriter getPrettyWriter() {
        return prettyWriter;
    }

    public static void main(String[] args) throws IOException {
    }

    public static class PolymorphicExample {
        @JsonTypeInfo(
                use = JsonTypeInfo.Id.NAME,
                include = As.EXISTING_PROPERTY,
                property = "typeName")
        @JsonSubTypes({
                @JsonSubTypes.Type(value = Dog.class),
                @JsonSubTypes.Type(value = Cat.class)
        })
        @Data
        @SuperBuilder(toBuilder = true)
        @AllArgsConstructor
        public static abstract class Animal {
            public abstract String getTypeName();
        }

        @JsonTypeName(Dog.TYPE_NAME)
        @Data
        @SuperBuilder(toBuilder = true)
        @EqualsAndHashCode(callSuper = true)
        @AllArgsConstructor
        @NoArgsConstructor
        @ToString(callSuper = true)
        public static class Dog extends Animal {
            public static final String TYPE_NAME = "dog";
            private double barkVolume;
            private Age age;
            enum Age {
                X,Y,Z
            }
            public String getTypeName() {
                return TYPE_NAME;
            }
        }

        @JsonTypeName(Cat.TYPE_NAME)
        @Data
        @SuperBuilder(toBuilder = true)
        @EqualsAndHashCode(callSuper = true)
        @AllArgsConstructor
        @NoArgsConstructor
        @ToString(callSuper = true)
        public static class Cat extends Animal {
            public static final String TYPE_NAME = "cat";
            private boolean likesCream;
            private int lives;

            public String getTypeName() {
                return TYPE_NAME;
            }
        }

        public static void main(String[] args) {
//            Dog dog = Dog.builder().name("lacy").barkVolume(2.0).age(Dog.Age.Z).build();
            Dog dog = Dog.builder().barkVolume(2.0).age(Dog.Age.Z).build();
            String result = Jackson.toJsonString(dog);
            System.out.println(result);
            Dog dog1 = Jackson.fromJsonString(result, Dog.class);
            System.out.println(dog1);
            System.out.println(dog1.getTypeName());
        }
    }
}


