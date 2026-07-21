package nodes;

import axiom.AxiomContext;
import gen.Messages.GetCurrencyMetadataInput;
import gen.Messages.CurrencyMetadata;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GetCurrencyMetadataTest {

    @Test
    public void usd_independentOracle_publicIso4217Facts() {
        // USD's default fraction digits (2), numeric code (840), and symbol
        // ($) are public ISO 4217 facts, verifiable independent of this
        // code (e.g. iso.org's ISO 4217 currency table).
        AxiomContext ax = TestSupport.newContext();
        CurrencyMetadata result = GetCurrencyMetadata.getCurrencyMetadata(ax,
            GetCurrencyMetadataInput.newBuilder().setCurrency("USD").build());
        assertEquals("", result.getError().getCode());
        assertEquals("USD", result.getCurrency());
        assertEquals(2, result.getDefaultFractionDigits());
        assertEquals(840, result.getNumericCode());
        assertEquals("$", result.getSymbol());
    }

    @Test
    public void jpy_zeroFractionDigits_independentOracle() {
        // JPY famously has 0 minor units (no cents) — numeric code 392.
        AxiomContext ax = TestSupport.newContext();
        CurrencyMetadata result = GetCurrencyMetadata.getCurrencyMetadata(ax,
            GetCurrencyMetadataInput.newBuilder().setCurrency("JPY").build());
        assertEquals(0, result.getDefaultFractionDigits());
        assertEquals(392, result.getNumericCode());
        assertEquals("¥", result.getSymbol());
        // Regression guard (caught by review): default_fraction_digits is a
        // proto3 `optional int32` specifically so a genuine 0 (JPY) is still
        // PRESENT on the wire — a plain int32 field silently vanishes from
        // JSON at its zero default, which would drop this exact value for
        // this exact currency. hasX() only exists because the field is
        // `optional`; reverting that in messages.proto breaks this build.
        assertTrue(result.hasDefaultFractionDigits());
        assertTrue(result.hasNumericCode());
    }

    @Test
    public void caseInsensitive_normalizesToUppercase() {
        AxiomContext ax = TestSupport.newContext();
        CurrencyMetadata result = GetCurrencyMetadata.getCurrencyMetadata(ax,
            GetCurrencyMetadataInput.newBuilder().setCurrency("usd").build());
        assertEquals("USD", result.getCurrency());
        assertEquals("", result.getError().getCode());
    }

    @Test
    public void unknownCurrency_returnsStructuredError_notCrash() {
        AxiomContext ax = TestSupport.newContext();
        CurrencyMetadata result = GetCurrencyMetadata.getCurrencyMetadata(ax,
            GetCurrencyMetadataInput.newBuilder().setCurrency("ZZZZZ").build());
        assertEquals("UNKNOWN_CURRENCY", result.getError().getCode());
    }
}
