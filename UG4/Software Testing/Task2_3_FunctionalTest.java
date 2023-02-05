package st;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class Task2_3_FunctionalTest {

	private Parser parser;

	/** Setup as required **/
	@Before
	public void setup() {
		parser = new Parser();
	}

	/////////////Reused tests from part1///////////////////
	@Test
	public void sameOptionNameOverrideTest() {
		parser.addOption(new Option("option", Type.STRING), "o1");
		parser.addOption(new Option("option", Type.INTEGER), "o2");
		assert (parser.shortcutExists("o2"));
	}

	@Test
	public void IntegerAsString() {
		parser.addOption(new Option("option", Type.STRING), "o");
		parser.parse("--option=5");
		assertEquals(parser.getInteger("option"), 5);
	}

	@Test
	public void VeryLargeIntegerTest() {
		parser.addOption(new Option("option", Type.STRING), "o");
		parser.parse("--option=9999999999");
		assertEquals(parser.getInteger("option"), 0); 
	}

	@Test
	public void bugTest13() {
		parser.addOption(new Option("option", Type.STRING), "o");
		parser.addOption(new Option("option2", Type.STRING), "o2");
		parser.parse("-o '-o=7' --option2 =test");
		assertEquals(parser.getString("o2"), "");
	}


	@Test
	public void LongOptionNameTestWithoutShortcut() {
		parser.addOption(new Option("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Type.STRING));
		assert (parser.optionExists("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
	}

	@Test
	public void TripleWhiteSpaceReplaceTest() {
		parser.addOption(new Option("opt1", Type.STRING), "o1");
		parser.addOption(new Option("opt2", Type.STRING), "o2");
		parser.parse("--opt1=OldText --opt2=OldText2");
		parser.replace("opt1   opt2", "Old", "New");
		assertEquals(parser.getString("opt1"), "NewText");
	}

	@Test
	public void shortcutReplaceTest() {
		parser.addOption(new Option("opt1", Type.STRING), "o1");
		parser.addOption(new Option("opt2", Type.STRING), "o2");
		parser.parse("--opt1=OldText --opt2=OldText2");
		parser.replace("-o1", "Old", "New");
		assertEquals(parser.getString("opt1"), "NewText");
	}

	@Test(expected = IllegalArgumentException.class)
	public void regexTest() {
		parser.addOption(new Option("a#", Type.STRING), "o");
		assert (parser.optionExists("a#"));
	}

	////////////Reused tests from part1///////////////////


	// optionOrShortcutExists test
	@Test
	public void OptionOrShortcutExistsShortcutTest() {
		parser.addOption(new Option("option", Type.STRING), "o");
		assert (parser.optionOrShortcutExists("o"));
	}


	// getInteger test
	@Test
	public void getIntegerTypeCharacter() {
		parser.addOption(new Option("option", Type.CHARACTER), "o");
		parser.parse("-o=a");
		assertEquals(parser.getInteger("option"), 'a');
	}


	// getInteger test
	@Test
	public void getIntegerBooleanZero() {
		parser.addOption(new Option("option", Type.BOOLEAN), "o");
		parser.parse("--option=0");
		assertEquals(parser.getInteger("option"), 0);
	}


	// parse test
	@Test
	public void parseBackslashAtStartAndEnd() {
		parser.addOption(new Option("option", Type.STRING), "o");
		parser.parse("-o=\"test\"");
		assertEquals(parser.getString("option"), "test");
	}

	// setShortcut test
	@Test
	public void setShortcutTest() {
		parser.addOption(new Option("option", Type.STRING));
		parser.setShortcut("option", "o");
		parser.parse("-o=test");
		assertEquals(parser.getString("o"), "test");
	}


	// toString test
	@Test
	public void parserToStringTest() {
		parser.addOption(new Option("option", Type.STRING), "o");
		parser.parse("--option=test");
		assertEquals(parser.toString(), "Options Map: \n" + "{option=Option[name:option, value:test, type:STRING]}\n"
				+ "Shortcuts Map:\n" + "{o=Option[name:option, value:test, type:STRING]}");
	}

	// replace method test
	@Test
	public void optionReplaceTest() {
		parser.addOption(new Option("opt1", Type.STRING), "o1");
		parser.parse("--opt1=OldText");
		parser.replace("--opt1", "Old", "New");
		assertEquals(parser.getString("opt1"), "NewText");
	}

	// replace method test
	@Test
	public void shortcutNoDashReplaceTest() {
		parser.addOption(new Option("opt1", Type.STRING), "o1");
		parser.parse("--opt1=OldText");
		parser.replace("o1", "Old", "New");
		assertEquals(parser.getString("opt1"), "NewText");
	}

	@Test(expected = RuntimeException.class)
	public void optionNotDefinedForKey() {
		parser.addOption(new Option("opt1", Type.STRING), "o1");
		parser.getString("--opt2");
	}

	@Test(expected = RuntimeException.class)
	public void shortcutNotDefinedForKey() {
		parser.addOption(new Option("opt1", Type.STRING), "o1");
		parser.getString("-o2");
	}


	// equals method test
	@Test
	public void OptionEquals() {
		Option opt1 = new Option("opt1", Type.STRING);
		Option opt2 = new Option("opt1", Type.STRING);
		assert (opt1.equals(opt2));
	}


	// equals method test
	@Test
	public void OptionEqualsOtherNameNull() {
		Option opt1 = new Option(null, Type.STRING);
		Option opt2 = new Option("opt", Type.STRING);
		assert (!opt1.equals(opt2));
	}


	// store method test
	@Test(expected = RuntimeException.class)
	public void storeEmptyShortcutTest() {
		parser.addOption(new Option("opt1", Type.STRING), "o1");
		parser.addOption(new Option("opt1", Type.STRING), "");
		parser.parse("-=test");
		assertEquals(parser.getString(""), "test");
	}
}
