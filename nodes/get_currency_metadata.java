package nodes;

import axiom.AxiomContext;
import gen.Messages.GetCurrencyMetadataInput;
import gen.Messages.CurrencyMetadata;

import javax.money.CurrencyUnit;
import java.util.Map;

public class GetCurrencyMetadata {

    /**
     * Look up structural (non-locale-specific) ISO 4217 metadata for a
     * currency code: default fraction digits (the conventional minor-unit
     * scale), numeric code, and the JDK's fixed-locale (Locale.US) symbol.
     * For a LOCALIZED display name/symbol under a specific CLDR locale, use
     * christiangeorgelucas/locale-tools' GetCurrencyInfo instead.
     */
    public static CurrencyMetadata getCurrencyMetadata(AxiomContext ax, GetCurrencyMetadataInput input) {
        ax.log().info("getCurrencyMetadata handling", Map.of());
        try {
            CurrencyUnit cu = MoneyUtil.currency(input.getCurrency());
            return CurrencyMetadata.newBuilder()
                .setCurrency(cu.getCurrencyCode())
                .setDefaultFractionDigits(cu.getDefaultFractionDigits())
                .setNumericCode(cu.getNumericCode())
                .setSymbol(MoneyUtil.symbolFor(cu.getCurrencyCode()))
                .build();
        } catch (MoneyUtil.MoneyOpException e) {
            return CurrencyMetadata.newBuilder().setError(MoneyUtil.toError(e)).build();
        }
    }
}
