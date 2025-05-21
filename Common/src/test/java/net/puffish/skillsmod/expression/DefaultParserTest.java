package net.puffish.skillsmod.expression;

import net.puffish.skillsmod.api.util.Problem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

class DefaultParserTest {

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
		testValid(1, "4> 2");
		testValid(0, "1 + 3< 2");
		testValid(1, "4 >= 2 *2");
		testValid(1, "2^ 3<= 2* 4");
		testValid(0, "1 +1 = 1");
		testValid(1, "3* 2 <>1");
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

		testValid(0, "a & b", Map.ofEntries(
				Map.entry("a", 1.0),
				Map.entry("b", 0.0)
		));
		testValid(0, "a &b", Map.ofEntries(
				Map.entry("a", 0.0),
				Map.entry("b", 1.0)
		));
		testValid(1, "a& b", Map.ofEntries(
				Map.entry("a", 1.0),
				Map.entry("b", 1.0)
		));
		testValid(1, "a | b", Map.ofEntries(
				Map.entry("a", 1.0),
				Map.entry("b", 0.0)
		));
		testValid(1, "a |b", Map.ofEntries(
				Map.entry("a", 0.0),
				Map.entry("b", 1.0)
		));
		testValid(0, "a| b", Map.ofEntries(
				Map.entry("a", 0.0),
				Map.entry("b", 0.0)
		));
		testValid(0, "!a & !b", Map.ofEntries(
				Map.entry("a", 1.0),
				Map.entry("b", 0.0)
		));
		testValid(0, "!a &!b", Map.ofEntries(
				Map.entry("a", 0.0),
				Map.entry("b", 1.0)
		));
		testValid(1, "!a& !b", Map.ofEntries(
				Map.entry("a", 0.0),
				Map.entry("b", 0.0)
		));
		testValid(0, "!a | !b", Map.ofEntries(
				Map.entry("a", 1.0),
				Map.entry("b", 1.0)
		));
		testValid(1, "a |b& c", Map.ofEntries(
				Map.entry("a", 1.0),
				Map.entry("b", 0.0),
				Map.entry("c", 0.0)
		));
		testValid(0, "(a |b) & c", Map.ofEntries(
				Map.entry("a", 1.0),
				Map.entry("b", 0.0),
				Map.entry("c", 0.0)
		));
		testValid(0, "a | b & c& d | e", Map.ofEntries(
				Map.entry("a", 0.0),
				Map.entry("b", 1.0),
				Map.entry("c", 0.0),
				Map.entry("d", 1.0),
				Map.entry("e", 0.0)
		));
		testValid(1, "((a|b))", Map.ofEntries(
				Map.entry("a", 0.0),
				Map.entry("b", 1.0)
		));
		testValid(1, "a", Map.ofEntries(
				Map.entry("a", 1.0)
		));
		testValid(Math.E, "e");
		testValid(Math.PI, "pi");
		testValid(Math.PI * 2, "tau");
		testValid(8.0, "e", Map.ofEntries(
				Map.entry("e", 8.0)
		));
	}

	@Test
	public void testInvalidExpressions() {
		testInvalid(Problem.message("Invalid expression"), "");
		testInvalid(Problem.message("Invalid expression"), "+");
		testInvalid(Problem.message("Invalid expression"), "1 *");
		testInvalid(Problem.message("Invalid expression"), "1 2");
		testInvalid(Problem.message("Invalid expression"), "(1 / 2");
		testInvalid(Problem.message("Invalid expression"), "1 - 2)");
		testInvalid(Problem.message("Invalid expression"), "abs(");
		testInvalid(Problem.message("Invalid expression"), "abs(5");
		testInvalid(Problem.message("Invalid expression"), "abs(3,");
		testInvalid(Problem.message("Invalid expression"), "abs(1, 2)");
		testInvalid(Problem.message("Invalid expression"), "abs()");
		testInvalid(Problem.message("Unknown variable `abs`"), "abs");
		testInvalid(Problem.message("Unknown variable `a`"), "a");
		testInvalid(Problem.message("Unknown variable `a`"), "3 * a + 2");
		testInvalid(Problem.message("Unknown variable `2.3.4`"), "2.3.4");

		testInvalid(Problem.message("Invalid expression"), "");
		testInvalid(Problem.message("Invalid expression"), "|");
		testInvalid(Problem.message("Invalid expression"), "a &", Map.ofEntries(
				Map.entry("a", 0.0)
		));
		testInvalid(Problem.message("Invalid expression"), "a b", Map.ofEntries(
				Map.entry("a", 0.0),
				Map.entry("b", 1.0)
		));
		testInvalid(Problem.message("Invalid expression"), "(a | b", Map.ofEntries(
				Map.entry("a", 1.0),
				Map.entry("b", 0.0)
		));
		testInvalid(Problem.message("Invalid expression"), "a & b)", Map.ofEntries(
				Map.entry("a", 1.0),
				Map.entry("b", 1.0)
		));
		testInvalid(Problem.message("Unknown variable `a`"), "a");
		testInvalid(Problem.message("Unknown variable `b`"), "a | b", Map.ofEntries(
				Map.entry("a", 0.0)
		));
	}

	private void testValid(double expected, String expression) {
		testValid(expected, expression, Map.of());
	}

	private void testValid(double expected, String expression, Map<String, Double> variables) {
		var success = DefaultParser.parse(expression, variables.keySet()).getSuccess();
		Assertions.assertTrue(success.isPresent(), "Unexpected failure: " + expression);
		Assertions.assertEquals(expected, success.orElseThrow().eval(variables), expression);
	}

	private void testInvalid(Problem expected, String expression) {
		testInvalid(expected, expression, Map.of());
	}

	private void testInvalid(Problem expected, String expression, Map<String, Double> variables) {
		var failure = DefaultParser.parse(expression, variables.keySet()).getFailure();
		Assertions.assertTrue(failure.isPresent(), "Unexpected success: " + expression);
		Assertions.assertEquals(expected.toString(), failure.orElseThrow().toString(), expression);
	}
}