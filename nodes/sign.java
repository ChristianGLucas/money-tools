package nodes;

import axiom.AxiomContext;
import gen.Messages.SignInput;
import gen.Messages.SignResult;

import java.util.Map;

public class Sign {

    /**
     * Classify one amount's sign: zero, positive, or negative, plus a
     * signum (-1/0/+1).
     */
    public static SignResult sign(AxiomContext ax, SignInput input) {
        ax.log().info("sign handling", Map.of());
        try {
            MoneyUtil.Parsed amount = MoneyUtil.parse(input.getAmount(), "amount");
            int signum = amount.amount.signum();
            return SignResult.newBuilder()
                .setIsZero(signum == 0)
                .setIsPositive(signum > 0)
                .setIsNegative(signum < 0)
                .setSignum(signum)
                .build();
        } catch (MoneyUtil.MoneyOpException e) {
            return SignResult.newBuilder().setError(MoneyUtil.toError(e)).build();
        }
    }
}
