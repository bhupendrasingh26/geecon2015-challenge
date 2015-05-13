package pl.allegro.promo.geecon2015.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.allegro.promo.geecon2015.domain.stats.FinancialStatisticsRepository;
import pl.allegro.promo.geecon2015.domain.stats.FinancialStats;
import pl.allegro.promo.geecon2015.domain.transaction.TransactionRepository;
import pl.allegro.promo.geecon2015.domain.transaction.UserTransaction;
import pl.allegro.promo.geecon2015.domain.transaction.UserTransactions;
import pl.allegro.promo.geecon2015.domain.user.User;
import pl.allegro.promo.geecon2015.domain.user.UserRepository;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class ReportGenerator {

    private final FinancialStatisticsRepository financialStatisticsRepository;

    private final UserRepository userRepository;

    private final TransactionRepository transactionRepository;

    @Autowired
    public ReportGenerator(FinancialStatisticsRepository financialStatisticsRepository,
                           UserRepository userRepository,
                           TransactionRepository transactionRepository) {
        this.financialStatisticsRepository = financialStatisticsRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    public Report generate(ReportRequest request) {
        Report report = new Report();
        FinancialStats uuids = getUsersForReport(request);
        for (UUID uuid : uuids) {
            User user = getUserDetails(uuid);
            BigDecimal userTransactionSum = getTransactionsSumForUser(uuid);
            report.add(createReportedUser(uuid, user, userTransactionSum));
        }
        return report;
    }

    private FinancialStats getUsersForReport(ReportRequest request) {
        return financialStatisticsRepository.listUsersWithMinimalIncome(request.getMinimalIncome(),
                request.getUsersToCheck());
    }

    private User getUserDetails(UUID uuid) {
        return userRepository.detailsOf(uuid);
    }

    private BigDecimal getTransactionsSumForUser(UUID uuid) {
        UserTransactions userTransactions = transactionRepository.transactionsOf(uuid);

        BigDecimal userTransactionSum;
        if (userTransactions != null) {
            userTransactionSum = userTransactions.getTransactions().stream()
                    .map(UserTransaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } else {
            userTransactionSum = null;
        }
        return userTransactionSum;
    }

    private ReportedUser createReportedUser(UUID uuid, User user, BigDecimal userTransactionSum) {
        return new ReportedUser(uuid, user.getName(), userTransactionSum);
    }

}
