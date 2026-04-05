package fr.fumbus.blackflash.configurations;

import org.junit.jupiter.api.Test;
import tools.jackson.core.JsonParser;
import tools.jackson.core.ObjectReadContext;
import tools.jackson.core.json.JsonFactory;

import java.io.StringReader;
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
    void jsonMapper_isNotNull() {
        assertThat(objectMapperConfiguration.jsonMapper()).isNotNull();
    }

    @Test
    void jsonMapper_trimsLeadingAndTrailingWhitespace() {
        String json = "{\"value\": \"  hello world  \"}";

        TestStringDto result = objectMapperConfiguration.jsonMapper().readValue(json, TestStringDto.class);

        assertThat(result.value()).isEqualTo("hello world");
    }

    @Test
    void jsonMapper_trimsOnlyWhitespaceStringToEmpty() {
        String json = "{\"value\": \"   \"}";

        TestStringDto result = objectMapperConfiguration.jsonMapper().readValue(json, TestStringDto.class);

        assertThat(result.value()).isEmpty();
    }

    @Test
    void jsonMapper_handlesNullStringValue() {
        String json = "{\"value\": null}";

        TestStringDto result = objectMapperConfiguration.jsonMapper().readValue(json, TestStringDto.class);

        assertNull(result.value());
    }

    @Test
    void jsonMapper_doesNotFailOnUnknownProperties() {
        String json = "{\"value\": \"test\", \"unknown_field\": \"ignored\"}";

        assertDoesNotThrow(() -> objectMapperConfiguration.jsonMapper().readValue(json, TestStringDto.class));
    }

    @Test
    void jsonMapper_excludesNullFieldsFromSerialization() {
        TestStringDto dto = new TestStringDto(null);

        String json = objectMapperConfiguration.jsonMapper().writeValueAsString(dto);

        assertThat(json).doesNotContain("value");
    }

    @Test
    void jsonMapper_includesNonNullFieldsInSerialization() {
        TestStringDto dto = new TestStringDto("hello");

        String json = objectMapperConfiguration.jsonMapper().writeValueAsString(dto);

        assertThat(json).contains("hello");
    }

    @Test
    void jsonMapper_serialisesFieldsWithSnakeCase() {
        TestSnakeCaseDto dto = new TestSnakeCaseDto("test");

        String json = objectMapperConfiguration.jsonMapper().writeValueAsString(dto);

        assertThat(json)
                .contains("my_field")
                .doesNotContain("myField");
    }

    @Test
    void jsonMapper_deserialisesSnakeCaseFieldNames() {
        String json = "{\"my_field\": \"value\"}";

        TestSnakeCaseDto result = objectMapperConfiguration.jsonMapper().readValue(json, TestSnakeCaseDto.class);

        assertThat(result.myField()).isEqualTo("value");
    }

    @Test
    void jsonMapper_deserialisesJsr310LocalDate() {
        String json = "{\"date\": \"2024-01-15\"}";

        TestDateDto result = objectMapperConfiguration.jsonMapper().readValue(json, TestDateDto.class);

        assertThat(result.date()).isEqualTo(LocalDate.of(2024, 1, 15));
    }

    @Test
    void jsonMapper_serialisesJsr310LocalDate() {
        TestDateDto dto = new TestDateDto(LocalDate.of(2024, 1, 15));

        String json = objectMapperConfiguration.jsonMapper().writeValueAsString(dto);

        assertThat(json).contains("{\"date\":\"2024-01-15\"}");
    }

    // -------------------------------------------------------------------------
    // StringTrimmingDeserializer
    // -------------------------------------------------------------------------

    @Test
    void stringTrimmingDeserializer_trimsLeadingAndTrailingWhitespace() {
        JsonParser parser = createStringParser("  trimmed  ");

        String result = deserializer.deserialize(parser, null);

        assertThat(result).isEqualTo("trimmed");
    }

    @Test
    void stringTrimmingDeserializer_returnsNullWhenValueIsNull() {
        JsonParser parser = createNullParser();

        String result = deserializer.deserialize(parser, null);

        assertNull(result);
    }

    @Test
    void stringTrimmingDeserializer_trimsToEmptyWhenOnlyWhitespace() {
        JsonParser parser = createStringParser("   ");

        String result = deserializer.deserialize(parser, null);

        assertThat(result).isEmpty();
    }

    @Test
    void stringTrimmingDeserializer_doesNotAlterAlreadyTrimmedString() {
        JsonParser parser = createStringParser("hello");

        String result = deserializer.deserialize(parser, null);

        assertThat(result).isEqualTo("hello");
    }

    private JsonParser createStringParser(String value) {
        JsonParser parser = jsonFactory.createParser(ObjectReadContext.empty(), new StringReader("\"" + value + "\""));
        parser.nextToken();
        return parser;
    }

    private JsonParser createNullParser() {
        JsonParser parser = jsonFactory.createParser(ObjectReadContext.empty(), new StringReader("null"));
        parser.nextToken();
        return parser;
    }

    // -------------------------------------------------------------------------
    // Inner test records
    // -------------------------------------------------------------------------

    record TestStringDto(String value) {
    }

    record TestSnakeCaseDto(String myField) {
    }

    record TestDateDto(LocalDate date) {
    }
}


