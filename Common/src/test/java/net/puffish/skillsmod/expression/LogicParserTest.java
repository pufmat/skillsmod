package net.puffish.skillsmod.expression;

import net.puffish.skillsmod.api.utils.Failure;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

class LogicParserTest {

	@Test
	public void testValidExpressions() {
		testValid(false, "a & b", Map.ofEntries(
				Map.entry("a", true),
				Map.entry("b", false)
		));
		testValid(false, "a &b", Map.ofEntries(
				Map.entry("a", false),
				Map.entry("b", true)
		));
		testValid(true, "a& b", Map.ofEntries(
				Map.entry("a", true),
				Map.entry("b", true)
		));
		testValid(true, "a | b", Map.ofEntries(
				Map.entry("a", true),
				Map.entry("b", false)
		));
		testValid(true, "a |b", Map.ofEntries(
				Map.entry("a", false),
				Map.entry("b", true)
		));
		testValid(false, "a| b", Map.ofEntries(
				Map.entry("a", false),
				Map.entry("b", false)
		));
		testValid(false, "!a & !b", Map.ofEntries(
				Map.entry("a", true),
				Map.entry("b", false)
		));
		testValid(false, "!a &!b", Map.ofEntries(
				Map.entry("a", false),
				Map.entry("b", true)
		));
		testValid(true, "!a& !b", Map.ofEntries(
				Map.entry("a", false),
				Map.entry("b", false)
		));
		testValid(false, "!a | !b", Map.ofEntries(
				Map.entry("a", true),
				Map.entry("b", true)
		));
		testValid(true, "a |b& c", Map.ofEntries(
				Map.entry("a", true),
				Map.entry("b", false),
				Map.entry("c", false)
		));
		testValid(false, "(a |b) & c", Map.ofEntries(
				Map.entry("a", true),
				Map.entry("b", false),
				Map.entry("c", false)
		));
		testValid(false, "a | b & c& d | e", Map.ofEntries(
				Map.entry("a", false),
				Map.entry("b", true),
				Map.entry("c", false),
				Map.entry("d", true),
				Map.entry("e", false)
		));
		testValid(true, "((a|b))", Map.ofEntries(
				Map.entry("a", false),
				Map.entry("b", true)
		));
		testValid(true, "a", Map.ofEntries(
				Map.entry("a", true)
		));
	}

	@Test
	public void testInvalidExpressions() {
		testInvalid(Failure.message("Invalid expression"), "");
		testInvalid(Failure.message("Invalid expression"), "|");
		testInvalid(Failure.message("Invalid expression"), "a &", Map.ofEntries(
				Map.entry("a", false)
		));
		testInvalid(Failure.message("Invalid expression"), "a b", Map.ofEntries(
				Map.entry("a", false),
				Map.entry("b", true)
		));
		testInvalid(Failure.message("Invalid expression"), "(a | b", Map.ofEntries(
				Map.entry("a", true),
				Map.entry("b", false)
		));
		testInvalid(Failure.message("Invalid expression"), "a & b)", Map.ofEntries(
				Map.entry("a", true),
				Map.entry("b", true)
		));
		testInvalid(Failure.message("Unknown variable `a`"), "a");
		testInvalid(Failure.message("Unknown variable `b`"), "a | b", Map.ofEntries(
				Map.entry("a", false)
		));
	}

	private void testValid(boolean expected, String expression) {
		testValid(expected, expression, Map.of());
	}

	private void testValid(boolean expected, String expression, Map<String, Boolean> parameters) {
		var success = LogicParser.parse(expression, parameters.keySet()).getSuccess();
		Assertions.assertTrue(success.isPresent(), "Unexpected failure: " + expression);
		Assertions.assertEquals(expected, success.orElseThrow().eval(parameters), expression);
	}

	private void testInvalid(Failure expected, String expression) {
		testInvalid(expected, expression, Map.of());
	}

	private void testInvalid(Failure expected, String expression, Map<String, Boolean> parameters) {
		var failure = LogicParser.parse(expression, parameters.keySet()).getFailure();
		Assertions.assertTrue(failure.isPresent(), "Unexpected success: " + expression);
		Assertions.assertEquals(expected.getMessages(), failure.orElseThrow().getMessages(), expression);
	}

}