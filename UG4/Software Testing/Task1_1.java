package st;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class Task1_1_FunctionalTest {

	private Parser parser;

	/** Setup as required **/
	@Before
	public void setup() {
		parser = new Parser();
	}

	// Bug 1
	@Test
	public void emptyShortcutExistsTest() {
		parser.addOption(new Option("option", Type.INTEGER));
		assert (!parser.shortcutExists(""));
	}

	// Bug 2
	@Test
	public void getBooleanEmptyValue() {
		parser.addOption(new Option("option", Type.BOOLEAN), "o");
		parser.parse("--option=");
		assertEquals(parser.getBoolean("option"), false);
	}

	// Bug 3
	@Test
	public void LongBooleanInput() {
		parser.addOption(new Option("option", Type.BOOLEAN), "o");
		parser.parse("--option=VeryLongInputAsBoolean");
		assertEquals(parser.getInteger("option"), 1);
	}

	// Bug 4
	@Test
	public void LongShortcutTest() {
		parser.addOption(new Option("option", Type.STRING),
				"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		assert (parser.shortcutExists("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
	}

	// Bug 5
	// This test throws an ArithmeticException on the jar file because the
	// getInteger method of the jar file cannot handle negative numbers
	@Test
	public void checkNegativeIntegerInput() {
		parser.addOption(new Option("option", Type.INTEGER), "o");
		parser.parse("--option=-12");
		assertEquals(parser.getInteger("option"), -12);
	}

	// Bug 6
	@Test
	public void optionsNotEqualsTest() {
		Option opt1 = new Option("", Type.STRING);
		Option opt2 = new Option("option", Type.STRING);
		assert (!opt1.equals(opt2));
	}

	// Bug 7
	@Test
	public void IntegerAsStringContaining5() {
		parser.addOption(new Option("option", Type.STRING), "o");
		parser.parse("--option=5");
		assertEquals(parser.getInteger("option"), 5);
	}

	// Bug 8
	@Test
	public void sameOptionNameShortcutsTest() {
		parser.addOption(new Option("option", Type.STRING), "o1");
		parser.addOption(new Option("option", Type.INTEGER), "o2");
		assert (parser.shortcutExists("o2"));
	}

	// Bug 9
	@Test
	public void WhiteSpaceParseTest() {
		parser.addOption(new Option("option", Type.STRING), "o");
		assertEquals(parser.parse("  "), 0);
	}

	// Bug 10
	@Test
	public void EmptyCharacterInputTest() {
		parser.addOption(new Option("option", Type.STRING), "o");
		parser.parse("--option=");
		assertEquals(parser.getCharacter("option"), '\0');
	}

	// Bug 11
	// this expects an exception # is not a valid character in the name
	@Test(expected = IllegalArgumentException.class)
	public void optionValidRegexTest() {
		parser.addOption(new Option("a#", Type.STRING), "o");
		assert (parser.optionExists("a#"));
	}

	// Bug 12
	@Test
	public void shortcutReplaceBySymbolTest() {
		parser.addOption(new Option("opt1", Type.STRING), "o1");
		parser.addOption(new Option("opt2", Type.STRING), "o2");
		parser.parse("--opt1=OldText --opt2=OldText2");
		parser.replace("-o1", "Old", "New");
		assertEquals(parser.getString("opt1"), "NewText");
	}

	// Bug 13
	@Test
	public void singleQuotesEqualsAndSpacetAssignmentTest() {
		parser.addOption(new Option("option", Type.STRING), "o");
		parser.addOption(new Option("option2", Type.STRING), "o2");
		parser.parse("-o '-o=7' --option2 =test");
		assertEquals(parser.getString("o2"), "");
	}

	// Bug 14
	@Test
	public void DoubleBackLashNewlineInputTest() {
		parser.addOption(new Option("option", Type.STRING), "o");
		parser.parse("--option=test\\n");
		assertEquals(parser.getString("option"), "test\\n");
	}

	// Bug 15
	@Test
	public void IntegerOutOfRangeTest() {
		parser.addOption(new Option("option", Type.STRING), "o");
		parser.parse("--option=9999999999");
		assertEquals(parser.getInteger("option"), 0);
	}

	// Bug 16
	// expects null pointer exception as should not be able to getString for null option
	@Test(expected = NullPointerException.class)
	public void nullOptionAndShortcutExists() {
		parser.addOption(new Option("option", Type.STRING), "");
		parser.getString(null);
	}

	// Bug 17
	// Note: this also displays Bug 1 due to the lack of shortcut
	@Test
	public void LongOptionNameTestWithoutShortcut() {
		parser.addOption(new Option("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Type.STRING));
		assert (parser.optionExists("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
	}

	// Bug 18
	@Test
	public void WhiteSpaceReplaceTest() {
		parser.addOption(new Option("opt1", Type.STRING), "o1");
		parser.addOption(new Option("opt2", Type.STRING), "o2");
		parser.parse("--opt1=OldText --opt2=OldText2");
		parser.replace("opt1   opt2", "Old", "New");
		assertEquals(parser.getString("opt1"), "NewText");
	}

	// Bug 19
	// expects exception as options should not be defined for key
	@Test(expected = RuntimeException.class)
	public void doubleQuotesShortcutAssignmentTest() {
		parser.addOption(new Option("option", Type.STRING), "o");
		parser.addOption(new Option("option2", Type.STRING), "o2");
		parser.parse("-o2=\"-o2\"=test");
	}

	// Bug 20
	@Test
	public void multipleAlphaNumericValuesParseTest() {
		parser.addOption(new Option("option", Type.STRING), "o");
		parser.addOption(new Option("option2", Type.STRING), "o2");
		parser.parse("-o '\"-o7'\" --option2 =test");
		assertEquals(parser.getString("o"), "'\"-o7'\"");
	}

}
