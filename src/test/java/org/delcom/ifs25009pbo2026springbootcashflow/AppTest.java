package org.delcom.ifs25009pbo2026springbootcashflow;

import org.delcom.ifs25009pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25009pbo2026springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25009pbo2026springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25009pbo2026springbootcashflow.usecase.FinanceUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mockStatic;

/**
 * Smoke test Spring Boot: memastikan wiring bean Clean Architecture berjalan
 * dan App.main mendelegasikan ke SpringApplication.run.
 */
@SpringBootTest
class AppTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoadsAndWiresCleanArchitectureBeans() {
        ITransactionRepository repository = context.getBean(ITransactionRepository.class);
        assertInstanceOf(TransactionRepository.class, repository);
        assertNotNull(context.getBean(FinanceUseCase.class));
        assertNotNull(context.getBean(FinancePresenter.class));
    }

    @Test
    void mainDelegatesToSpringApplicationRun() throws Exception {
        String[] args = {"--spring.main.web-application-type=none"};
        try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
            mocked.when(() -> SpringApplication.run(App.class, args)).thenReturn(null);

            App.main(args);

            mocked.verify(() -> SpringApplication.run(App.class, args));
        }
    }
}
