package com.syndicated_loan.syndicated_loan.common;

import com.syndicated_loan.syndicated_loan.common.testutil.TestDataBuilder;
import com.syndicated_loan.syndicated_loan.common.testutil.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public abstract class BaseServiceTest {
    @Autowired
    protected TestDataBuilder testDataBuilder;

    protected TestData testData;

    @BeforeEach
    void baseSetUp() {
        testData = testDataBuilder.createBasicTestData();
    }
}
