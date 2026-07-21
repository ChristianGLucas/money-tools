package nodes;

import axiom.AxiomContext;
import gen.Messages.AllocateInput;
import gen.Messages.AllocateResult;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

public class Allocate {

    /**
     * Split a non-negative amount into len(ratios) parts proportional to
     * ratios, with the exact remainder distributed one minor unit at a time
     * (largest fractional remainder first, ties broken by ascending index)
     * so the parts always sum to the original amount exactly — no rounding
     * loss, no leftover unit. E.g. 0.05 USD split [1,1,1] -> [0.02, 0.02,
     * 0.01] USD.
     */
    public static AllocateResult allocate(AxiomContext ax, AllocateInput input) {
        ax.log().info("allocate handling", Map.of());
        try {
            MoneyUtil.Parsed amount = MoneyUtil.parse(input.getAmount(), "amount");
            int scale = amount.amount.scale();
            BigInteger unscaledTotal = amount.amount.unscaledValue();
            long[] ratios = new long[input.getRatiosCount()];
            for (int i = 0; i < ratios.length; i++) {
                ratios[i] = input.getRatios(i);
            }
            BigInteger[] parts = MoneyUtil.allocateUnscaled(unscaledTotal, ratios);

            AllocateResult.Builder result = AllocateResult.newBuilder();
            for (BigInteger part : parts) {
                BigDecimal partAmount = new BigDecimal(part, scale);
                result.addParts(MoneyUtil.build(partAmount, amount.currency));
            }
            return result.build();
        } catch (MoneyUtil.MoneyOpException e) {
            return AllocateResult.newBuilder().setError(MoneyUtil.toError(e)).build();
        }
    }
}
