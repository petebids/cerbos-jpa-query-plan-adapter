package com.github.petebids;


import dev.cerbos.sdk.CerbosBlockingClient;
import dev.cerbos.sdk.CerbosClientBuilder;
import dev.cerbos.sdk.PlanResourcesResult;
import dev.cerbos.sdk.builders.Principal;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@Testcontainers
@SpringBootTest
public class QueryPlanAdapterTest {

    @Autowired
    private ResourceRepository resourceRepository;

    private static CerbosBlockingClient cerbosBlockingClient;

    @Container
    private static final PostgreSQLContainer<?> database = new PostgreSQLContainer<>("postgres:latest")
            .withPassword("postgres")
            .withUsername("postgres")
            .withExposedPorts(5432)
            .withInitScript("init.sql")
            .waitingFor(new LogMessageWaitStrategy().withRegEx(".*database system is ready to accept connections.*"));

    @Container
    private static final GenericContainer<?> cerbos = new GenericContainer<>("cerbos/cerbos:0.36.0")
            .withExposedPorts(3593)
            .withCopyFileToContainer(MountableFile.forClasspathResource("resource.yaml"), "/policies/resource.yaml")
            .withCopyFileToContainer(MountableFile.forClasspathResource("cerbos_config.yaml"), "config.yaml")
            .withCommand("server", "--config=config.yaml")
            .waitingFor(new LogMessageWaitStrategy()
                    .withRegEx(".*Starting gRPC server.*")
                    .withStartupTimeout(Duration.of(10, ChronoUnit.SECONDS)));

    @BeforeAll
    static void setUp() throws CerbosClientBuilder.InvalidClientConfigurationException {
        database.start();
        cerbos.start();
        cerbosBlockingClient = new CerbosClientBuilder(String.format("localhost:%d",
                cerbos.getMappedPort(3593))).withInsecure().withPlaintext().buildBlockingClient();
    }


    @DynamicPropertySource
    public static void registerDynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", database::getJdbcUrl);
        registry.add("spring.datasource.username", database::getUsername);
        registry.add("spring.datasource.password", database::getPassword);
        registry.add("spring.datasource.driver-class-name", database::getDriverClassName);
        registry.add("spring.jpa.databasePlatform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("spring.jpa.properties.hibernate.format_sql", () -> "true");
        registry.add("spring.jpa.properties.hibernate.show_sql", () -> "true");
    }

    @AfterAll
    static void tearDown() {
        database.stop();
        cerbos.stop();
    }


    static Stream<TestCase> testData() {
        return Stream.of(
                //     new TestCase("always-allow", "1", 3, resources -> true),
                new TestCase("equal", "1", 2, resources -> true),
                new TestCase("and", "1", 1, resources -> true),
                new TestCase("nand", "1", 2, resources -> true),
                new TestCase("ne", "1", 2, resources -> true),
                new TestCase("or", "1", 3, resources -> true),
                new TestCase("lte", "1", 2, resources -> true),
                new TestCase("nor", "0", 3, resources -> true),
                new TestCase("in", "1", 3, resources -> true),
                new TestCase("gt", "1", 3, resources -> true),
                new TestCase("lt", "1", 3, resources -> true),
                new TestCase("gte", "1", 3, resources -> true),
                new TestCase("relation-some", "1", 3, resources -> true),
                new TestCase("relation-none", "1", 3, resources -> true),
                new TestCase("relation-is", "1", 3, resources -> true)


        );
    }


    @ParameterizedTest
    @MethodSource("testData")
    void adapt(TestCase testCase) {

        assertEquals(3, (long) resourceRepository.findAll().size());

        final QueryPlanAdapter<Resource> adapter = new QueryPlanAdapter<>(Collections.emptyMap());

        final PlanResourcesResult plan = cerbosBlockingClient.plan(Principal.newInstance(testCase.principal()).withRoles("USER"),
                dev.cerbos.sdk.builders.Resource.newInstance("resource"), testCase.action());

        if (plan.hasValidationErrors() || plan.isAlwaysAllowed() || plan.isAlwaysDenied() || plan.getCondition().isEmpty()) {
            throw new RuntimeException();
        }

        log.info("test case {}", testCase);
        final List<Resource> resources = resourceRepository.findAll(adapter.adapt(plan.getCondition().get()));

        assertEquals(testCase.expectedResultCount(), resources.size());
        assertTrue(testCase.validator().apply(resources));


    }
}