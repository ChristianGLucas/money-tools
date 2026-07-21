package nodes;

import axiom.AxiomContext;
import gen.Messages.AllocateInput;
import gen.Messages.AllocateResult;
import gen.Messages.MoneyAmount;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

public class AllocateTest {

    private static MoneyAmount money(String amount, String currency) {
        return MoneyAmount.newBuilder().setAmount(amount).setCurrency(currency).build();
    }

    @Test
    public void splitFiveCentsThreeWays_theClassicCase_handComputed() {
        // The textbook "split $0.05 three ways" remainder-distribution
        // problem: floor(5/3)=1 cent each (3 allocated), 2 cents leftover,
        // distributed to the first two (equal remainders, tie broken by
        // ascending index) -> [0.02, 0.02, 0.01].
        AxiomContext ax = TestSupport.newContext();
        AllocateInput input = AllocateInput.newBuilder()
            .setAmount(money("0.05", "USD"))
            .addRatios(1).addRatios(1).addRatios(1)
            .build();
        AllocateResult result = Allocate.allocate(ax, input);
        assertEquals("", result.getError().getCode());
        assertEquals(3, result.getPartsCount());
        assertEquals("0.02", result.getParts(0).getAmount());
        assertEquals("0.02", result.getParts(1).getAmount());
        assertEquals("0.01", result.getParts(2).getAmount());
    }

    @Test
    public void twoToOneRatioSplit_handComputed_sumsExactly() {
        // 10.00 split 2:1 -> floor(2000/3)=666 rem2, floor(1000/3)=333 rem1;
        // leftover 1 cent goes to the larger remainder (index 0) ->
        // [6.67, 3.33]. Independently verifiable: 6.67 + 3.33 = 10.00.
        AxiomContext ax = TestSupport.newContext();
        AllocateInput input = AllocateInput.newBuilder()
            .setAmount(money("10.00", "USD"))
            .addRatios(2).addRatios(1)
            .build();
        AllocateResult result = Allocate.allocate(ax, input);
        assertEquals("6.67", result.getParts(0).getAmount());
        assertEquals("3.33", result.getParts(1).getAmount());

        BigDecimal sum = new BigDecimal(result.getParts(0).getAmount())
            .add(new BigDecimal(result.getParts(1).getAmount()));
        assertEquals(0, sum.compareTo(new BigDecimal("10.00")),
            "parts must sum to exactly the original amount — the whole point of Allocate");
    }

    @Test
    public void evenSplit_noRemainder() {
        AxiomContext ax = TestSupport.newContext();
        AllocateInput input = AllocateInput.newBuilder()
            .setAmount(money("9.00", "USD"))
            .addRatios(1).addRatios(1).addRatios(1)
            .build();
        AllocateResult result = Allocate.allocate(ax, input);
        assertEquals("3.00", result.getParts(0).getAmount());
        assertEquals("3.00", result.getParts(1).getAmount());
        assertEquals("3.00", result.getParts(2).getAmount());
    }

    @Test
    public void negativeAmount_returnsStructuredError_ambiguousConventionRejected() {
        AxiomContext ax = TestSupport.newContext();
        AllocateInput input = AllocateInput.newBuilder()
            .setAmount(money("-10.00", "USD"))
            .addRatios(1).addRatios(1)
            .build();
        AllocateResult result = Allocate.allocate(ax, input);
        assertEquals("INVALID_ARGUMENT", result.getError().getCode());
    }

    @Test
    public void zeroRatio_returnsStructuredError() {
        AxiomContext ax = TestSupport.newContext();
        AllocateInput input = AllocateInput.newBuilder()
            .setAmount(money("10.00", "USD"))
            .addRatios(1).addRatios(0)
            .build();
        AllocateResult result = Allocate.allocate(ax, input);
        assertEquals("INVALID_ARGUMENT", result.getError().getCode());
    }

    @Test
    public void emptyRatios_returnsStructuredError() {
        AxiomContext ax = TestSupport.newContext();
        AllocateInput input = AllocateInput.newBuilder()
            .setAmount(money("10.00", "USD"))
            .build();
        AllocateResult result = Allocate.allocate(ax, input);
        assertEquals("INVALID_ARGUMENT", result.getError().getCode());
    }
}
