package com.insider.driver;

import com.thoughtworks.gauge.AfterScenario;
import com.thoughtworks.gauge.BeforeScenario;
import com.thoughtworks.gauge.BeforeStep;
import com.thoughtworks.gauge.ExecutionContext;
import com.insider.imp.LoggerImps;
import lombok.extern.log4j.Log4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Log4j
public class BaseTest {
    private final DriverManager driverManager = DriverManager.getInstance();

    @BeforeScenario
    public void setup(ExecutionContext context) {
        driverManager.initializeDriver(context);
    }

    @AfterScenario
    public void tearDown(ExecutionContext context) {
        driverManager.afterScenario(context);
    }

    @BeforeStep
    public void beforeStep(ExecutionContext context) {
        driverManager.beforeEachStep(context);
    }

    @Test
    void testInstance() {
        Assertions.assertNotNull(driverManager);
        LoggerImps.getInstance(log).info("BaseTest classı için instance başarılı ile oluşturulmuştur.");
    }
}