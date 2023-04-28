package net.puffish.skillsmod.expression;

import net.puffish.skillsmod.utils.error.Error;
import net.puffish.skillsmod.utils.error.SingleError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

class ArithmeticParserTest {

	@Test
	public void testValidExpressions() {
		testValid(0, "0");
		testValid(3.5, "1.5 + 2");
		testValid(1.5, "5- 3.5");
		testValid(-8, "-2 * 4");
		testValid(-4, "8 / -2");
		testValid(68, "2 + 3 *4 * 5 + 6");
		testValid(262144, "4 ^ 3 ^ 2");
		testValid(128, "4 ^ 3 * 2");
		testValid(36, "4 * 3 ^ 2");
		testValid(2621440, "5 * 4 ^3 ^2 * 2");
		testValid(-64, "-4 ^ 3");
		testValid(-16, "128* -2 ^ -3");
		testValid(64, "16 ^ 1.5");
		testValid(3, "+2 ++1");
		testValid(1, "+2 + -1");
		testValid(3, "- -2 - -1");
		testValid(9, "(1+ 2) * 3");
		testValid(3, "((1 + 2))");
		testValid(-4, "min(2, -4)");
		testValid(23, "max(12, -6, 23, 1)");
		testValid(12, "abs(12)");
		testValid(Double.MAX_VALUE, "min()");
		testValid(-Double.MAX_VALUE, "max()");
		testValid(8, "aa_aaa", Map.ofEntries(
				Map.entry("aa_aaa", 8.0)
		));
		testValid(29, "a + bbb * _c_ + dd_d", Map.ofEntries(
				Map.entry("a", 3.0),
				Map.entry("bbb", 4.0),
				Map.entry("_c_", 5.0),
				Map.entry("dd_d", 6.0)
		));
		testValid(10, "abs_ijk + abs(-7)", Map.ofEntries(
				Map.entry("abs_ijk", 3.0)
		));
	}

	@Test
	public void testInvalidExpressions() {
		testInvalid(SingleError.of("Invalid expression"), "");
		testInvalid(SingleError.of("Invalid expression"), "+");
		testInvalid(SingleError.of("Invalid expression"), "1 *");
		testInvalid(SingleError.of("Invalid expression"), "1 2");
		testInvalid(SingleError.of("Invalid expression"), "(1 / 2");
		testInvalid(SingleError.of("Invalid expression"), "1 - 2)");
		testInvalid(SingleError.of("Invalid expression"), "abs(");
		testInvalid(SingleError.of("Invalid expression"), "abs(5");
		testInvalid(SingleError.of("Invalid expression"), "abs(3,");
		testInvalid(SingleError.of("Invalid expression"), "abs(1, 2)");
		testInvalid(SingleError.of("Invalid expression"), "abs()");
		testInvalid(SingleError.of("Unknown variable `abs`"), "abs");
		testInvalid(SingleError.of("Unknown variable `a`"), "a");
		testInvalid(SingleError.of("Unknown variable `a`"), "3 * a + 2");
		testInvalid(SingleError.of("Unknown variable `2.3.4`"), "2.3.4");
	}

	private void testValid(double expected, String expression) {
		testValid(expected, expression, Map.of());
	}

	private void testValid(double expected, String expression, Map<String, Double> parameters) {
		var success = ArithmeticParser.parse(expression, parameters.keySet()).getSuccess();
		Assertions.assertTrue(success.isPresent(), "Unexpected failure: " + expression);
		Assertions.assertEquals(expected, success.orElseThrow().eval(parameters), expression);
	}

	private void testInvalid(Error expected, String expression) {
		testInvalid(expected, expression, Map.of());
	}

	private void testInvalid(Error expected, String expression, Map<String, Double> parameters) {
		var failure = ArithmeticParser.parse(expression, parameters.keySet()).getFailure();
		Assertions.assertTrue(failure.isPresent(), "Unexpected success: " + expression);
		Assertions.assertEquals(expected.getMessages(), failure.orElseThrow().getMessages(), expression);
	}
}