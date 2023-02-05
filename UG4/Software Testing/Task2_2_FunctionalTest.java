package st;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class Task2_2_FunctionalTest {

	private Parser parser;

	/** Setup as required **/
	@Before
	public void setup() {
		parser = new Parser();
	}

	///////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////Tests from Task1_1///////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////
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

		///////////////////////////////////////////////////////////////////////////////////
		//////////////////////////////Tests from Task1_1///////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////
		
	// optionOrShortcutExists test
	@Test
	public void OptionOrShortcutExistsOptionTest() {
		parser.addOption(new Option("option", Type.STRING), "o");
		assert(parser.optionOrShortcutExists("option"));
	}
	
	// optionOrShortcutExists test
	@Test
	public void OptionOrShortcutExistsShortcutTest() {
		parser.addOption(new Option("option", Type.STRING), "o");
		assert(parser.optionOrShortcutExists("o"));
	}
	
	// optionOrShortcutExists test
	@Test
	public void OptionOrShortcutNotExistsShortcutTest() {
		parser.addOption(new Option("option", Type.STRING), "o");
		assert(!parser.optionOrShortcutExists("test"));
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
	public void getIntegerCatchBigIntExcpetion() {
		parser.addOption(new Option("option", Type.INTEGER), "o");
		parser.parse("--option=test");
		assertEquals(parser.getInteger("option"), 0);
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
	public void parseBooleanFalseStringTest() {
		parser.addOption(new Option("option", Type.BOOLEAN), "o");
		parser.parse("--option=false");
		assertEquals(parser.getString("option"), "");
	}

	// parse test
	@Test
	public void parseBackslashAtStart() {
		parser.addOption(new Option("option", Type.STRING), "o");
		parser.parse("-o=\"test");
		assertEquals(parser.getString("option"), "\"test");
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
		assertEquals(parser.getString("o"),"test");
	}
	
	// setShortcut test
	@Test(expected = RuntimeException.class)
	public void setShortcutonNullOptionTest() {
		parser.addOption(new Option("option", Type.STRING));
		parser.setShortcut(null, "o");
		parser.parse("-o=test");
	}
	
	// parse test
	@Test
	public void emptyCommandLine() {
		parser.addOption(new Option("option", Type.STRING),"o");
		assertEquals(parser.parse(""),-2);
	}
	
	// parse test
	@Test
	public void nullCommandLine() {
		parser.addOption(new Option("option", Type.STRING),"o");
		assertEquals(parser.parse(null),-1);
	}
	
	//toString test
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
	
	@Test(expected = RuntimeException.class)
	public void shortcutAndOptionNotDefinedForKey() {
		parser.addOption(new Option("opt1", Type.STRING), "o1");
		parser.getString("o2");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void isOptionValidShortcutNull() {
		parser.addOption(new Option("opt1", Type.STRING), null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void isOptionValidNull() {
		parser.addOption(new Option(null, Type.STRING), "o1");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void isOptionValidEmpty() {
		parser.addOption(new Option("", Type.STRING), "o1");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void isOptionValidShortcutNotEmptyAndNotMatching() {
		parser.addOption(new Option("test", Type.STRING), "#");
	}
	
	//equals method test
	@Test
	public void OptionEqualsDiffTypeTest() {
		Option opt1 = new Option("option", Type.INTEGER);
		Option opt2 = new Option("option", Type.STRING);
		assert (!opt1.equals(opt2));
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
	public void OptionEqualsNull() {
		Option opt1 = new Option("opt1", Type.STRING);
		assert (!opt1.equals(null));
	}
	
	// equals method test
	@Test
	public void OptionEqualsItself() {
		Option opt1 = new Option("opt1", Type.STRING);
		assert (opt1.equals(opt1));
	}

	// equals method test
	@Test(expected = NullPointerException.class)
	public void OptionEqualsBothNamesNull() {
		Option opt1 = new Option(null, Type.STRING);
		Option opt2 = new Option(null, Type.STRING);
		assert (opt1.equals(opt2));
	}

	// equals method test
	@Test
	public void OptionEqualsOtherNameNull() {
		Option opt1 = new Option(null, Type.STRING);
		Option opt2 = new Option("opt", Type.STRING);
		assert (!opt1.equals(opt2));
	}

	// setName test
	@Test
	public void setOptionNameTest() {
		Option opt1 = new Option("opt", Type.STRING);
		opt1.setName("opt2");
		assertEquals(opt1.getName(),"opt2");
	}

	// getBoolean test
	@Test
	public void getBooleanIsZero() {
		parser.addOption(new Option("opt1", Type.STRING), "o1");
		parser.parse("--opt1=0");
		assert(!parser.getBoolean("opt1"));
	}
	
	// getBoolean test
	@Test
	public void getBooleanFalse() {
		parser.addOption(new Option("opt1", Type.STRING), "o1");
		parser.parse("--opt1=false");
		assert(!parser.getBoolean("opt1"));
	}
	
	//store method test
	@Test(expected = IllegalArgumentException.class)
	public void storeNoTypeTest() {
		parser.addOption(new Option("opt", Type.NOTYPE), "o");
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
