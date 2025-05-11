package net.puffish.skillsmod.experience;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ExperienceCurveTest {

	@Test
	public void testRequiredSimple() {
		var curve = ExperienceCurve.create(x -> x * x + 1, Integer.MAX_VALUE);

		Assertions.assertEquals(17, curve.getRequired(4));
		Assertions.assertEquals(1, curve.getRequired(0));
		Assertions.assertEquals(37, curve.getRequired(6));
		Assertions.assertEquals(5, curve.getRequired(2));
	}

	@Test
	public void testRequiredTotalSimple() {
		var curve = ExperienceCurve.create(x -> x * x + 1, Integer.MAX_VALUE);

		Assertions.assertEquals(
				1 + 2 + 5 + 10 + 17,
				curve.getRequiredTotal(4));
		Assertions.assertEquals(
				1,
				curve.getRequiredTotal(0));
		Assertions.assertEquals(
				1 + 2 + 5 + 10 + 17 + 26 + 37,
				curve.getRequiredTotal(6));
		Assertions.assertEquals(
				1 + 2 + 5,
				curve.getRequiredTotal(2));
	}

	@Test
	public void testProgressSimple() {
		var curve = ExperienceCurve.create(x -> x * x + 1, Integer.MAX_VALUE);

		Assertions.assertEquals(
				new ExperienceCurve.Progress(2, 0, 5),
				curve.getProgress(3));
		Assertions.assertEquals(
				new ExperienceCurve.Progress(1, 1, 2),
				curve.getProgress(2));
		Assertions.assertEquals(
				new ExperienceCurve.Progress(3, 0, 10),
				curve.getProgress(8));
		Assertions.assertEquals(
				new ExperienceCurve.Progress(2, 4, 5),
				curve.getProgress(7));
		Assertions.assertEquals(
				new ExperienceCurve.Progress(0, 0, 1),
				curve.getProgress(0));
		Assertions.assertEquals(
				new ExperienceCurve.Progress(1, 0, 2),
				curve.getProgress(1));
	}

	@Test
	public void testProgressZeros() {
		var curve = ExperienceCurve.create(List.of(0, 1, 0, 0, 0, 0, 2, 3)::get, Integer.MAX_VALUE);

		Assertions.assertEquals(
				new ExperienceCurve.Progress(6, 0, 2),
				curve.getProgress(1));
		Assertions.assertEquals(
				new ExperienceCurve.Progress(1, 0, 1),
				curve.getProgress(0));
		Assertions.assertEquals(
				new ExperienceCurve.Progress(6, 1, 2),
				curve.getProgress(2));
	}

	@Test
	public void testRequiredLimit() {
		var curve = ExperienceCurve.create(x -> x * x + 1, 4);

		Assertions.assertEquals(0, curve.getRequired(4));
		Assertions.assertEquals(1, curve.getRequired(0));
		Assertions.assertEquals(0, curve.getRequired(6));
		Assertions.assertEquals(5, curve.getRequired(2));
	}



	@Test
	public void testRequiredTotalLimit() {
		var curve = ExperienceCurve.create(x -> x * x + 1, 4);

		Assertions.assertEquals(
				1 + 2 + 5 + 10,
				curve.getRequiredTotal(4));
		Assertions.assertEquals(
				1,
				curve.getRequiredTotal(0));
		Assertions.assertEquals(
				1 + 2 + 5 + 10,
				curve.getRequiredTotal(6));
		Assertions.assertEquals(
				1 + 2 + 5,
				curve.getRequiredTotal(2));
	}

	@Test
	public void testProgressLimit() {
		var curve = ExperienceCurve.create(x -> x * x + 1, 2);

		Assertions.assertEquals(
				new ExperienceCurve.Progress(2, 0, 0),
				curve.getProgress(3));
		Assertions.assertEquals(
				new ExperienceCurve.Progress(1, 1, 2),
				curve.getProgress(2));
		Assertions.assertEquals(
				new ExperienceCurve.Progress(2, 0, 0),
				curve.getProgress(8));
		Assertions.assertEquals(
				new ExperienceCurve.Progress(2, 0, 0),
				curve.getProgress(7));
		Assertions.assertEquals(
				new ExperienceCurve.Progress(0, 0, 1),
				curve.getProgress(0));
		Assertions.assertEquals(
				new ExperienceCurve.Progress(1, 0, 2),
				curve.getProgress(1));
	}

}
