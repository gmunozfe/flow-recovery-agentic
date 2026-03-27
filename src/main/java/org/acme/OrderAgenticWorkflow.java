package org.acme;

import static io.serverlessworkflow.fluent.func.FuncWorkflowBuilder.workflow;
import static io.serverlessworkflow.fluent.func.dsl.FuncDSL.function;

import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.acme.agent.OrderRiskAgent.FormattedOrder;
import org.acme.agent.OrderRiskAgent.FormatterAgent;
import org.acme.agent.OrderRiskAgent.OrderRiskAssessment;
import org.acme.agent.OrderRiskAgent.RiskClassifierAgent;
import org.jboss.logging.Logger;

import io.quarkiverse.flow.Flow;
import io.serverlessworkflow.api.types.Workflow;

@ApplicationScoped
public class OrderAgenticWorkflow extends Flow {

    private static final Logger LOG = Logger.getLogger(OrderAgenticWorkflow.class);

    @Inject
    RiskClassifierAgent riskClassifierAgent;

    @Inject
    FormatterAgent formatterAgent;

    @Inject
    CrashOnceService crashOnceService;

    @Override
    public Workflow descriptor() {
        return workflow("order-agentic-workflow")
                .tasks(
                    function("riskAgent", this::classifyStep, Map.class),
                    function("crashOnce", this::crashOnceStep, Map.class),
                    function("formatterAgent", this::formatStep, Map.class)
                )
                .build();
    }

    public Map<String, Object> classifyStep(Map<String, Object> input) {
        String orderId = String.valueOf(input.get("orderId"));
        double amount = ((Number) input.get("amount")).doubleValue();
        String customerId = String.valueOf(input.get("customerId"));

        OrderRiskAssessment result = riskClassifierAgent.classify(orderId, amount, customerId);

        Map<String, Object> out = Map.of(
                "orderId", orderId,
                "amount", amount,
                "customerId", customerId,
                "risk", result.risk(),
                "status", "RISK_ASSESSED"
        );

        LOG.infov("[STEP 1 - RiskAgent] output={0}", out);
        return out;
    }

    public Map<String, Object> crashOnceStep(Map<String, Object> input) {
        String orderId = String.valueOf(input.get("orderId"));

        LOG.infov("[STEP 2 - CrashOnce] recoveredInput={0}", input);

        if (crashOnceService.shouldCrash(orderId)) {
            LOG.infov("[STEP 2 - CrashOnce] crashing once for orderId={0}", orderId);
            crashOnceService.markCrashed();
            Runtime.getRuntime().halt(137);
        }

        LOG.infov("[STEP 2 - CrashOnce] continuing with input={0}", input);
        return input;
    }

    public Map<String, Object> formatStep(Map<String, Object> input) {
        String orderId = String.valueOf(input.get("orderId"));
        double amount = ((Number) input.get("amount")).doubleValue();
        String customerId = String.valueOf(input.get("customerId"));
        String risk = String.valueOf(input.get("risk"));

        FormattedOrder result = formatterAgent.format(orderId, amount, customerId, risk);

        Map<String, Object> out = Map.of(
                "orderId", result.orderId(),
                "amount", result.amount(),
                "customerId", result.customerId(),
                "risk", result.risk(),
                "status", result.status()
        );

        LOG.infov("[STEP 3 - FormatterAgent] recoveredInput={0}", input);
        LOG.infov("[STEP 3 - FormatterAgent] output={0}", out);

        return out;
    }
}
