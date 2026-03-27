package org.acme.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.quarkiverse.langchain4j.RegisterAiService;

public final class OrderRiskAgent {

    private OrderRiskAgent() {
    }

    public record OrderRiskAssessment(String risk) {
    }

    public record FormattedOrder(
            String orderId,
            double amount,
            String customerId,
            String risk,
            String status
    ) {
    }

    @RegisterAiService
    public interface RiskClassifierAgent {

        @SystemMessage("""
                You classify order risk using only the provided amount.

                Rules:
                - amount < 100 => LOW
                - 100 <= amount < 1000 => MEDIUM
                - amount >= 1000 => HIGH

                Return only valid JSON:
                {"risk":"LOW"}
                """)
        @UserMessage("""
                Order id: {orderId}
                Customer id: {customerId}
                Amount: {amount}
                """)
        OrderRiskAssessment classify(
                @V("orderId") String orderId,
                @V("amount") double amount,
                @V("customerId") String customerId
        );
    }

    @RegisterAiService
    public interface FormatterAgent {

        @SystemMessage("""
                You produce the final JSON response.

                Rules:
                - Copy values exactly
                - status must be COMPLETED
                - output only valid JSON
                - do not output markdown
                """)
        @UserMessage("""
                orderId={orderId}
                amount={amount}
                customerId={customerId}
                risk={risk}

                Return:
                {
                  "orderId": "...",
                  "amount": ...,
                  "customerId": "...",
                  "risk": "...",
                  "status": "COMPLETED"
                }
                """)
        FormattedOrder format(
                @V("orderId") String orderId,
                @V("amount") double amount,
                @V("customerId") String customerId,
                @V("risk") String risk
        );
    }
}
