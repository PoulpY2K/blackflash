package fr.fumbus.blackflash.configurations;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;

class ObjectMapperConfigurationTests {

    private final ObjectMapperConfiguration objectMapperConfiguration = new ObjectMapperConfiguration();
    private final ObjectMapperConfiguration.StringTrimmingDeserializer deserializer =
            new ObjectMapperConfiguration.StringTrimmingDeserializer();
    private final JsonFactory jsonFactory = new JsonFactory();

    @Test
    void objectMapper_isNotNull() {
        assertThat(objectMapperConfiguration.objectMapper()).isNotNull();
    }

    @Test
    void objectMapper_trimsLeadingAndTrailingWhitespace() throws Exception {
        String json = "{\"value\": \"  hello world  \"}";

        TestStringDto result = objectMapperConfiguration.objectMapper().readValue(json, TestStringDto.class);

        assertThat(result.value()).isEqualTo("hello world");
    }

    @Test
    void objectMapper_trimsOnlyWhitespaceStringToEmpty() throws Exception {
        String json = "{\"value\": \"   \"}";

        TestStringDto result = objectMapperConfiguration.objectMapper().readValue(json, TestStringDto.class);

        assertThat(result.value()).isEmpty();
    }

    @Test
    void objectMapper_handlesNullStringValue() throws Exception {
        String json = "{\"value\": null}";

        TestStringDto result = objectMapperConfiguration.objectMapper().readValue(json, TestStringDto.class);

        assertNull(result.value());
    }

    @Test
    void objectMapper_doesNotFailOnUnknownProperties() {
        String json = "{\"value\": \"test\", \"unknown_field\": \"ignored\"}";

        assertDoesNotThrow(() -> objectMapperConfiguration.objectMapper().readValue(json, TestStringDto.class));
    }

    @Test
    void objectMapper_excludesNullFieldsFromSerialization() throws Exception {
        TestStringDto dto = new TestStringDto(null);

        String json = objectMapperConfiguration.objectMapper().writeValueAsString(dto);

        assertThat(json).doesNotContain("value");
    }

    @Test
    void objectMapper_includesNonNullFieldsInSerialization() throws Exception {
        TestStringDto dto = new TestStringDto("hello");

        String json = objectMapperConfiguration.objectMapper().writeValueAsString(dto);

        assertThat(json).contains("hello");
    }

    @Test
    void objectMapper_serialisesFieldsWithSnakeCase() throws Exception {
        TestSnakeCaseDto dto = new TestSnakeCaseDto("test");

        String json = objectMapperConfiguration.objectMapper().writeValueAsString(dto);

        assertThat(json)
                .contains("my_field")
                .doesNotContain("myField");
    }

    @Test
    void objectMapper_deserialisesSnakeCaseFieldNames() throws Exception {
        String json = "{\"my_field\": \"value\"}";

        TestSnakeCaseDto result = objectMapperConfiguration.objectMapper().readValue(json, TestSnakeCaseDto.class);

        assertThat(result.myField()).isEqualTo("value");
    }

    @Test
    void objectMapper_deserialisesJsr310LocalDate() throws Exception {
        String json = "{\"date\": \"2024-01-15\"}";

        TestDateDto result = objectMapperConfiguration.objectMapper().readValue(json, TestDateDto.class);

        assertThat(result.date()).isEqualTo(LocalDate.of(2024, 1, 15));
    }

    @Test
    void objectMapper_serialisesJsr310LocalDate() throws Exception {
        TestDateDto dto = new TestDateDto(LocalDate.of(2024, 1, 15));

        String json = objectMapperConfiguration.objectMapper().writeValueAsString(dto);

        assertThat(json).contains("{\"date\":[2024,1,15]}");
    }

    // -------------------------------------------------------------------------
    // StringTrimmingDeserializer
    // -------------------------------------------------------------------------

    @Test
    void stringTrimmingDeserializer_trimsLeadingAndTrailingWhitespace() throws IOException {
        JsonParser parser = createStringParser("  trimmed  ");

        String result = deserializer.deserialize(parser, null);

        assertThat(result).isEqualTo("trimmed");
    }

    @Test
    void stringTrimmingDeserializer_returnsNullWhenValueIsNull() throws IOException {
        JsonParser parser = createNullParser();

        String result = deserializer.deserialize(parser, null);

        assertNull(result);
    }

    @Test
    void stringTrimmingDeserializer_trimsToEmptyWhenOnlyWhitespace() throws IOException {
        JsonParser parser = createStringParser("   ");

        String result = deserializer.deserialize(parser, null);

        assertThat(result).isEmpty();
    }

    @Test
    void stringTrimmingDeserializer_doesNotAlterAlreadyTrimmedString() throws IOException {
        JsonParser parser = createStringParser("hello");

        String result = deserializer.deserialize(parser, null);

        assertThat(result).isEqualTo("hello");
    }

    private JsonParser createStringParser(String value) throws IOException {
        JsonParser parser = jsonFactory.createParser('"' + value + '"');
        parser.nextToken();
        return parser;
    }

    private JsonParser createNullParser() throws IOException {
        JsonParser parser = jsonFactory.createParser("null");
        parser.nextToken();
        return parser;
    }

    // -------------------------------------------------------------------------
    // Inner test records
    // -------------------------------------------------------------------------

    record TestStringDto(String value) {}

    record TestSnakeCaseDto(String myField) {}

    record TestDateDto(LocalDate date) {}
}


