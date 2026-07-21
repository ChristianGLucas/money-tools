package nodes;

import axiom.AxiomContext;
import gen.Messages.CompareInput;
import gen.Messages.CompareResult;

import java.util.Map;

public class Compare {

    /**
     * Order two same-currency amounts by numeric value (2.0 and 2.00 are
     * equal, regardless of how many decimal digits either was written
     * with). Currency mismatch returns CURRENCY_MISMATCH rather than
     * crashing.
     */
    public static CompareResult compare(AxiomContext ax, CompareInput input) {
        ax.log().info("compare handling", Map.of());
        try {
            MoneyUtil.Parsed a = MoneyUtil.parse(input.getA(), "a");
            MoneyUtil.Parsed b = MoneyUtil.parse(input.getB(), "b");
            MoneyUtil.requireSameCurrency(a.currency, b.currency);
            int cmp = a.amount.compareTo(b.amount);
            int normalized = Integer.signum(cmp);
            return CompareResult.newBuilder()
                .setComparison(normalized)
                .setEqual(normalized == 0)
                .setGreater(normalized > 0)
                .setLess(normalized < 0)
                .build();
        } catch (MoneyUtil.MoneyOpException e) {
            return CompareResult.newBuilder().setError(MoneyUtil.toError(e)).build();
        }
    }
}
