package run.var.teamcity.cloud.docker.util;

import jetbrains.buildServer.serverSide.AgentDescription;
import org.testng.annotations.Test;
import run.var.teamcity.cloud.docker.test.TestSBuildAgent;
import run.var.teamcity.cloud.docker.test.TestInputStream;
import run.var.teamcity.cloud.docker.test.TestUtils;

import java.io.IOException;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SuppressWarnings("ConstantConditions")
@Test
public class DockerCloudUtilsTest {

    public void requireNotNull() {
        String errorMsg = "Blah blah";
        DockerCloudUtils.requireNonNull(new Object(), errorMsg);
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> DockerCloudUtils.requireNonNull(null,
                errorMsg)).withMessage(errorMsg);
    }

    public void getClientId() {
        AgentDescription description = new TestSBuildAgent().
                environmentVariable(DockerCloudUtils.ENV_CLIENT_ID, TestUtils.TEST_UUID.toString());
        assertThat(DockerCloudUtils.getClientId(description)).isEqualTo(TestUtils.TEST_UUID);
    }

    public void getClientIdWithNoEnvVariable() {
        AgentDescription description = new TestSBuildAgent();
        assertThat(DockerCloudUtils.getClientId(description)).isNull();
    }

    public void getClientIdWithInvalidUUID() {
        AgentDescription description = new TestSBuildAgent().
                environmentVariable(DockerCloudUtils.ENV_CLIENT_ID, "Not an UUID");
        assertThat(DockerCloudUtils.getClientId(description)).isNull();
    }

    public void getClientIdWithNullDescription() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> DockerCloudUtils.getClientId(null));
    }

    public void getImageId() {
        AgentDescription description = new TestSBuildAgent().
                environmentVariable(DockerCloudUtils.ENV_IMAGE_ID, TestUtils.TEST_UUID.toString());
        assertThat(DockerCloudUtils.getImageId(description)).isEqualTo(TestUtils.TEST_UUID);
    }

    public void getImageIdWithNoEnvVariable() {
        AgentDescription description = new TestSBuildAgent();
        assertThat(DockerCloudUtils.getImageId(description)).isNull();
    }

    public void getImageIdWithInvalidUUID() {
        AgentDescription description = new TestSBuildAgent().
                environmentVariable(DockerCloudUtils.ENV_IMAGE_ID, "Not an UUID");
        assertThat(DockerCloudUtils.getImageId(description)).isNull();
    }

    public void getImageIdWithNullDescription() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> DockerCloudUtils.getImageId(null));
    }

    public void getInstanceId() {
        AgentDescription description = new TestSBuildAgent().
                environmentVariable(DockerCloudUtils.ENV_INSTANCE_ID, TestUtils.TEST_UUID.toString());
        assertThat(DockerCloudUtils.getInstanceId(description)).isEqualTo(TestUtils.TEST_UUID);
    }

    public void getInstanceIdWithNoEnvVariable() {
        AgentDescription description = new TestSBuildAgent();
        assertThat(DockerCloudUtils.getInstanceId(description)).isNull();
    }

    public void getInstanceIdWithInvalidUUID() {
        AgentDescription description = new TestSBuildAgent().
                environmentVariable(DockerCloudUtils.ENV_IMAGE_ID, "Not an UUID");
        assertThat(DockerCloudUtils.getInstanceId(description)).isNull();
    }

    public void getInstanceIdWithNullDescription() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> DockerCloudUtils.getInstanceId(null));
    }

    public void tryParseAsUUID() {
        assertThat(DockerCloudUtils.tryParseAsUUID(TestUtils.TEST_UUID.toString())).isEqualTo(TestUtils.TEST_UUID);
    }

    public void tryParseAsUUIDInvalidInput() {
        assertThat(DockerCloudUtils.tryParseAsUUID("Not an UUID")).isNull();
    }

    public void tryParseAsUUIDWithNullInput() {
        assertThat(DockerCloudUtils.tryParseAsUUID(null)).isNull();
    }

    public void toShortId() {
        assertThat(DockerCloudUtils.toShortId("1abe11edbeef0000000000000000000000")).isEqualTo("1abe11edbeef");
    }

    public void toShortIdUnderflow() {
        assertThat(DockerCloudUtils.toShortId("beef")).isEqualTo("beef");
        assertThat(DockerCloudUtils.toShortId("")).isEqualTo("");
    }

    public void toShortIdWithNullInput() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> DockerCloudUtils.toShortId(null));
    }

    public void getLogger() {
        assertThat(DockerCloudUtils.getLogger(Object.class)).isNotNull();
    }

    public void getLoggerWithNullClass() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> DockerCloudUtils.getLogger(null));
    }

    public void getEnvParameter() {
        AgentDescription description = new TestSBuildAgent().
                environmentVariable("ABC", "123").
                environmentVariable("CDF", "456");

        assertThat(DockerCloudUtils.getEnvParameter(description, "ABC")).isEqualTo("123");
        assertThat(DockerCloudUtils.getEnvParameter(description, "CDF")).isEqualTo("456");
        assertThat(DockerCloudUtils.getEnvParameter(description, "EFG")).isNull();
    }

    public void getEnvParameterWithNullArguments() {
        AgentDescription description = new TestSBuildAgent().
                environmentVariable("ABC", "123").
                environmentVariable("CDF", "456");

        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() ->
                DockerCloudUtils.getEnvParameter(description, null));
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> DockerCloudUtils.getEnvParameter(null,
                "ABC"));
    }

    public void readUTF8String() throws IOException {
        String bigText = bigAsciiText();
        TestInputStream stream = TestInputStream.withUTF8String(bigText);
        stream.mark(0);
        assertThat(DockerCloudUtils.readUTF8String(stream)).isEqualTo(bigText);
        stream.reset();
        assertThat(DockerCloudUtils.readUTF8String(stream, -1)).isEqualTo(bigText);
    }

    public void readUTF8AsciiStringWithLengthCap() throws IOException {
        // Ascii text: 1 byte per character, so no risk to truncate a character.
        String bigText = bigAsciiText();
        TestInputStream stream = TestInputStream.withUTF8String(bigText);
        assertThat(DockerCloudUtils.readUTF8String(stream, 5)).
                isEqualTo(bigText.substring(0, 5));
    }

    public void readUTF8AsciiStringWithLengthCapAndTextUnderflow() throws IOException {
        // Ascii text: 1 byte per character, so no risk to truncate a character.
        String bigText = bigAsciiText();
        TestInputStream stream = TestInputStream.withUTF8String("ABC");
        assertThat(DockerCloudUtils.readUTF8String(stream, 1000)).isEqualTo("ABC");
        assertThat(DockerCloudUtils.readUTF8String(TestInputStream.empty(), 1000)).isEqualTo("");
    }

    public void readUTF8MultiByteStringWithLengthCap() throws IOException {
        String textWithTwoBytesChars = "πππππ";
        TestInputStream stream = TestInputStream.withUTF8String(textWithTwoBytesChars);
        // We are cutting in half a string composed of two-bytes characters and uneven length. We are expecting to find
        // back the first half of the string terminated with a single replacement character.
        assertThat(DockerCloudUtils.readUTF8String(stream, 5)).isEqualTo(textWithTwoBytesChars.substring(0, 2) + "�");
    }

    public void readUTF8StringShouldNotCloseStream() throws IOException {
        TestInputStream stream = TestInputStream.empty();
        DockerCloudUtils.readUTF8String(stream);
        assertThat(stream.isClosed()).isFalse();
    }

    public void readUTF8StringInvalidCap() throws IOException {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> DockerCloudUtils.readUTF8String
                (TestInputStream.withUTF8String("hello"), -2));
    }

    public void readUTF8StringWithNullInput() throws IOException {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> DockerCloudUtils.readUTF8String(null));
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> DockerCloudUtils.readUTF8String(null,
                -1));
    }

    public void hasImageTag() {
        assertThat(DockerCloudUtils.hasImageTag("my-image")).isFalse();
        assertThat(DockerCloudUtils.hasImageTag("my-image:1.0")).isTrue();
        assertThat(DockerCloudUtils.hasImageTag("my-repo/my-image")).isFalse();
        assertThat(DockerCloudUtils.hasImageTag("my-repo/my-image:1.0")).isTrue();
        assertThat(DockerCloudUtils.hasImageTag("localhost:5000/my-repo/my-image")).isFalse();
        assertThat(DockerCloudUtils.hasImageTag("localhost:5000/my-repo/my-image:1.0")).isTrue();
    }

    public void hasImageTagWithInvalidInput() {
        assertThat(DockerCloudUtils.hasImageTag("")).isFalse();
        assertThat(DockerCloudUtils.hasImageTag("my-image:")).isFalse();
    }

    public void hasImageTagWithNullInput() throws IOException {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> DockerCloudUtils.hasImageTag(null));
    }

    public void toUnsignedLong() {
        assertThat(DockerCloudUtils.toUnsignedLong(0)).isEqualTo(0L);
        assertThat(DockerCloudUtils.toUnsignedLong(1)).isEqualTo(1L);
        assertThat(DockerCloudUtils.toUnsignedLong(Integer.MAX_VALUE)).isEqualTo(Integer.MAX_VALUE);
        assertThat(DockerCloudUtils.toUnsignedLong(Integer.MIN_VALUE)).isEqualTo((long) Integer.MAX_VALUE + 1);
        assertThat(DockerCloudUtils.toUnsignedLong(-1)).isEqualTo(((long) Integer.MAX_VALUE) * 2 + 1);
    }

    public void getStackTrace() {
        assertThat(DockerCloudUtils.getStackTrace(new RuntimeException("test"))).
                isNotEmpty();
    }

    public void getStackTraceWithNullInput() {
        assertThat(DockerCloudUtils.getStackTrace(null)).isNull();
    }

    private String bigAsciiText() {
        StringBuilder bigText = new StringBuilder();
        IntStream.range(0, 8192).forEach(i -> bigText.append('a'));
        return bigText.toString();
    }
}