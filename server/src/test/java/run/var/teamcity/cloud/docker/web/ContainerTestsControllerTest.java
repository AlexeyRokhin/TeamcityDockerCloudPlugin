package run.var.teamcity.cloud.docker.web;

import jetbrains.buildServer.serverSide.WebLinks;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import run.var.teamcity.cloud.docker.DockerCloudClientConfig;
import run.var.teamcity.cloud.docker.DockerImageConfig;
import run.var.teamcity.cloud.docker.client.DockerClientConfig;
import run.var.teamcity.cloud.docker.test.TestAtmosphereFrameworkFacade;
import run.var.teamcity.cloud.docker.test.TestBuildAgentManager;
import run.var.teamcity.cloud.docker.test.TestDockerClient;
import run.var.teamcity.cloud.docker.test.TestDockerClientFactory;
import run.var.teamcity.cloud.docker.test.TestDockerImageResolver;
import run.var.teamcity.cloud.docker.test.TestPluginDescriptor;
import run.var.teamcity.cloud.docker.test.TestRootUrlHolder;
import run.var.teamcity.cloud.docker.test.TestSBuildServer;
import run.var.teamcity.cloud.docker.test.TestUtils;
import run.var.teamcity.cloud.docker.test.TestWebControllerManager;
import run.var.teamcity.cloud.docker.util.Node;
import run.var.teamcity.cloud.docker.web.TestContainerStatusMsg.Phase;
import run.var.teamcity.cloud.docker.web.TestContainerStatusMsg.Status;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static run.var.teamcity.cloud.docker.test.TestUtils.waitUntil;

/**
 * {@link ContainerTestsController} test suite.
 */
@Test
public class ContainerTestsControllerTest {

    private long testMaxIdleTime = ContainerTestsController.TEST_DEFAULT_IDLE_TIME_SEC;
    private long cleanupRateSec = ContainerTestsController.CLEANUP_DEFAULT_TASK_RATE_SEC;

    private TestDockerClientFactory dockerClientFactory;
    private DockerCloudClientConfig clientConfig;
    private DockerImageConfig imageConfig;

    @BeforeMethod
    public void init() {
        dockerClientFactory = new TestDockerClientFactory() {
            @Override
            public void configureClient(TestDockerClient dockerClient) {
                dockerClient.knownImage("resolved-image", "1.0");
            }
        };


        DockerClientConfig dockerClientConfig = new DockerClientConfig(TestDockerClient.TEST_CLIENT_URI);
        clientConfig = new DockerCloudClientConfig(TestUtils.TEST_UUID, dockerClientConfig,
                false);

        Node containerSpec = Node.EMPTY_OBJECT.editNode().put("Image", "test-image").saveNode();
        imageConfig = new DockerImageConfig("test", containerSpec, true, false, 1);
    }

    public void fullTest() {

        ContainerTestsController ctrl = createController();

        TestContainerStatusMsg statusMsg = ctrl.doAction(ContainerTestsController.Action.CREATE, null, clientConfig,
                imageConfig);

        TestDockerClient dockerClient = dockerClientFactory.getClient();

        dockerClient.lock();

        UUID testUuid = statusMsg.getTaskUuid();
        Status status = statusMsg.getStatus();

        assertThat(status).isSameAs(Status.PENDING);

        dockerClient.unlock();

        queryUntilSuccess(ctrl, testUuid, Phase.CREATE);

        queryUntilSuccess(ctrl, testUuid, Phase.CREATE);

        ctrl.doAction(ContainerTestsController.Action.DISPOSE, testUuid, null, null);

        queryUntilSuccess(ctrl, testUuid, Phase.DISPOSE, Phase.STOP);
    }

    public void autoDispose() {

        cleanupRateSec = 2;
        testMaxIdleTime = 3;

        ContainerTestsController ctrl = createController();

        TestContainerStatusMsg statusMsg = ctrl.doAction(ContainerTestsController.Action.CREATE, null, clientConfig,
                imageConfig);

        UUID testUuid = statusMsg.getTaskUuid();

        queryUntilSuccess(ctrl, testUuid);

        TestUtils.waitSec(5);

        assertThatExceptionOfType(ContainerTestsController.ActionException.class).isThrownBy( () -> ctrl.doAction
                (ContainerTestsController.Action.QUERY, testUuid, null, null));
    }

    private void queryUntilSuccess(ContainerTestsController ctrl, UUID testUuid, Phase... allowedPhases) {
        waitUntil(() -> {
            TestContainerStatusMsg queryMsg = ctrl.doAction(ContainerTestsController.Action.QUERY, testUuid, null,
                    null);
            Status status = queryMsg.getStatus();
            assertThat(status).isNotSameAs(Status.FAILURE);
            if (allowedPhases != null && allowedPhases.length > 0) {
                assertThat(queryMsg.getPhase()).isIn((Object[]) allowedPhases);
            }
            return status == Status.SUCCESS;
        });
    }

    private ContainerTestsController createController() {
        return new ContainerTestsController(new TestAtmosphereFrameworkFacade(),
                dockerClientFactory, new TestDockerImageResolver("resolved-image:1.0"),
                new TestSBuildServer(), new TestPluginDescriptor(), new TestWebControllerManager(),
                new TestBuildAgentManager(), new WebLinks(new TestRootUrlHolder()), testMaxIdleTime, cleanupRateSec);
    }
}