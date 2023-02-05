package st;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class Task3_TDD1 {
	
	private Task3_Parser parser;

	/** Setup as required **/
	@Before
	public void setup() {
		parser = new Task3_Parser();
	}
	
	//convertStringTypeToEnum Helper Method tests
	
	@Test
	public void stringToEnumType() {
		assertEquals(parser.convertStringTypeToEnum("String"),Type.STRING);
	}
	@Test
	public void integerToEnumType() {
		assertEquals(parser.convertStringTypeToEnum("integer"),Type.INTEGER);
	}
	@Test
	public void booleanToEnumType() {
		assertEquals(parser.convertStringTypeToEnum("BOOLEAN"),Type.BOOLEAN);
	}
	@Test
	public void characterToEnumType() {
		assertEquals(parser.convertStringTypeToEnum("cHarACTeR"),Type.CHARACTER);
	}
	@Test(expected = IllegalArgumentException.class)
	public void noTypeToEnumType() {
		parser.convertStringTypeToEnum("");
	}
	
	
	//addAll no shortcuts test
	@Test
	public void addAllNoShortcutOptionExistsTest() {
		parser.addAll("option1 option2 option3"," String Integer Boolean");
		assert(parser.optionExists("option1") && parser.optionExists("option2") 
				&& parser.optionExists("option3"));
	}
	
	@Test
	public void addAllNoShortcutParseTest() {
		parser.addAll("option1 option2 option3"," String Integer Boolean");
		parser.parse("--option3=test3");
		assertEquals(parser.getString("option3"),"test3");
	}
	
	@Test
	public void addAllMoreOptionsThanTypesTest() {
		parser.addAll("option1 option2 option3"," String Integer");
		parser.parse("--option3=test3");
		assertEquals(parser.getInteger("option3"),0);
	}
	
	@Test
	public void addAllMoreTypesThanOptionsTest() {
		parser.addAll("option1 option2"," String Boolean Integer");
		parser.parse("--option2=1");
		assertEquals(parser.getBoolean("option2"),true);
	}
	
	@Test
	public void addAllNoShortcutsIgnoreGroupTest() {
		parser.addAll("option2 option1-4a optiona","String Boolean Character");
		parser.parse("--option2=1");
		assertEquals(parser.getBoolean("option2"),true);
	}
	
	@Test
	public void addAllNoShortcutsIgnoreGroupExistsTest() {
		parser.addAll("option1-4a option2","String Boolean");
		assert(!parser.optionExists("option1"));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void addAllNoShortcutsBlankOptionTest() {
		parser.addAll("   ","String Boolean");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void addAllNoShortcutsNullOptionTest() {
		parser.addAll(null,"String Boolean");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void addAllNoShortcutsEmptyOptionTest() {
		parser.addAll("","String Boolean");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void addAllNoShortcutsBlankTypeTest() {
		parser.addAll("option1 option2","   ");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void addAllNoShortcutsNullTypeTest() {
		parser.addAll("option1 option2",null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void addAllNoShortcutsEmptyTypeTest() {
		parser.addAll("option1 option2","");
	}
	
	
	//Add all with no shortcut group initialisation tests
	@Test
	public void groupInitalisationWithoutShortcutsTest() {
		parser.addAll("option7-12 optiona-c optionA-B" ,"Integer String");
		assert(parser.optionExists("option8") && parser.optionExists("optionb") 
				&& parser.optionExists("optionA"));
	}
	
	@Test
	public void groupInitalisationWithoutShortcutsValueTest() {
		parser.addAll("option7-12 optiona-c optionA-B" ,"Integer Character");
		parser.parse("--option11=3");
		assertEquals(parser.getInteger("option11"),3);
	}
	
	@Test
	public void groupInitalisationWithoutShortcutsSkipInvalidOptionTest() {
		parser.addAll("option7-12 optiona-3 optionA-B" ,"Integer Character String");
		assert(!parser.optionExists("optiona"));
	}
	
	@Test
	public void groupInitalisationWithoutShortcutsDoubleDigitRangeTest() {
		parser.addAll("g129-11" ,"Integer Character");
		parser.parse("--g1210=3");
		assertEquals(parser.getInteger("g1210"),3);
	}
	
	@Test
	public void addAllNoShortcutDecreasingIntRangeTest() {
		parser.addAll("g125-2", "Integer");
		parser.parse("--g123=1");
		assertEquals(parser.getBoolean("g123"),true);
	}
	
	@Test
	public void addAllNoShortcutDecreasingCharRangeTest() {
		parser.addAll("gd-a", "Character");
		parser.parse("--gc=e");
		assertEquals(parser.getCharacter("gc"),'e');
	}

	@Test(expected = IllegalArgumentException.class)
	public void addAllShortcutsBlankOptionTest() {
		parser.addAll("   ","o1","String Boolean");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void addAllShortcutsNullOptionTest() {
		parser.addAll(null,"o1","String Boolean");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void addAllShortcutsEmptyOptionTest() {
		parser.addAll("","o1","String Boolean");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void addAllShortcutsBlankTypeTest() {
		parser.addAll("option1 option2","o1","   ");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void addAllShortcutsNullTypeTest() {
		parser.addAll("option1 option2","o1",null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void addAllShortcutsEmptyTypeTest() {
		parser.addAll("option1 option2","o1","");
	}
	
	// addAll with shortcuts test
	@Test
	public void addAllShortcutParseTest() {
		parser.addAll("option1 option2 option3", "o1 o2 o3", " String Integer Boolean");
		parser.parse("-o3=test3");
		assertEquals(parser.getString("o3"), "test3");
	}

	@Test
	public void addAllMoreShortcutParseTest() {
		parser.addAll("option1 option2 option3", "o1 o2 o3 o4 o5", " String Integer Boolean");
		parser.parse("-o3=test3");
		assertEquals(parser.getString("o3"), "test3");
	}

	@Test
	public void addAllLessShortcutParseTest() {
		parser.addAll("option1 option2 option3", "o1 o2", " String Integer Boolean");
		assert(!parser.shortcutExists("option3"));
	}
	
	@Test
	public void addAllWithShortcutLessTypesThanOptionsTest() {
		parser.addAll("option1 option2","o1 o2"," String");
		parser.parse("-o2=1");
		assertEquals(parser.getBoolean("o2"),true);
	}
	
	//Add all with shortcut group initialisation tests
	
	@Test
	public void groupInitalisationOptionsExistWithShortcutsTest() {
		parser.addAll("option7-12 option1 optiona-c optionA-B" ,"o7-12 oR-S ob-e","Integer String");
		assert(parser.optionExists("option8") && parser.optionExists("optionb") 
				&& parser.optionExists("optionA"));
	}
	@Test
	public void groupInitalisationShortcutExistWithShortcutsTest() {
		parser.addAll("option7-12 option1 optiona-c optionA-B" ,"o7-12 oR-S ob-e","Integer String");
		assert(parser.shortcutExists("o8") && parser.shortcutExists("oS") 
				&& parser.shortcutExists("od"));
	}
	
	@Test
	public void addAllGroupWithShortcutOptionValueTest() {
		parser.addAll("option7-12 option1 optiona-c optionA-B" ,"o7-12 oR-S ob-e","Integer String");
		parser.parse("--optionb=test");
		assertEquals(parser.getString("-ob"),"test");
	}
	@Test
	public void addAllGroupWithShortcutShortcutValueTest() {
		parser.addAll("option7-12 option1 optiona-c optionA-B" ,"o7-12 oR-S ob-e","Integer String");
		parser.parse("-oR=test");
		assertEquals(parser.getString("--option1"),"test");
	}
	
	@Test
	public void addAllGroupWithMoreShortcutsThanOptions() {
		parser.addAll("option7-12 option1 optiona-c optionA-B" ,"o7-12 oR-Z ob-e","Integer String");
		assert(!parser.shortcutExists("oZ"));
	}
	
	@Test
	public void addAllGroupWithMoreOptionsThanShortcuts() {
		parser.addAll("option7-12 option1 optiona-c optionA-B" ,"o7-12 og-i","Integer String");
		assert(!parser.shortcutExists("optionA"));
	}
	
	@Test
	public void addAllShortcutDecreasingRangeTest() {
		parser.addAll("g125-2", "oa-g" ,"Integer");
		parser.parse("-ob=1");
		assertEquals(parser.getBoolean("g124"),true);
	}
	
	@Test
	public void addAllShortcutDecreasingShortcutCharRangeTest() {
		parser.addAll("g125-2", "og-a" ,"Integer");
		parser.parse("-of=1");
		assertEquals(parser.getBoolean("g124"),true);
	}
	
	@Test
	public void addAllShortcutDecreasingShortcutIntRangeTest() {
		parser.addAll("g125-2", "o5-2" ,"Integer");
		parser.parse("-o4=1");
		assertEquals(parser.getBoolean("g124"),true);
	}
	
	@Test
	public void addAllShortcutDecreasingOptionCharRangeTest() {
		parser.addAll("gd-a", "o5-2" ,"Integer");
		parser.parse("-o4=1");
		assertEquals(parser.getBoolean("gc"),true);
	}
	
	@Test
	public void addAllShortcutDoubleRangeLettertest() {
		parser.addAll("gz-aa", "o2-55" ,"Integer String");
		parser.parse("--ga=test");
		assertEquals(parser.getString("-o27"),"test");
	}
	
	@Test
	public void addAllOptionRange1Test() {
		parser.addAll("g1-1", "o2-4" ,"Integer");
		parser.parse("--g1=test");
		assertEquals(parser.getString("-o2"),"test");
	}
	
	@Test
	public void addAllInvalidGroupTest() {
		parser.addAll("g123-7ab g1234-7", "o2-4" ,"Integer");
		parser.parse("--g1235=test");
		assertEquals(parser.getString("--g1235"),"test");
	}

	@Test
	public void addAllShortcutsIgnoreGroupTest() {
		parser.addAll("opt2-4 opt1-4a opta","o2-4 oa-d oA", "String Boolean Character");
		parser.parse("--opta=test");
		assertEquals(parser.getString("oA"),"test");
	}
	
	@Test
	public void addAllShortcutsInvalidShortcutTest() {
		parser.addAll("opte opt1-4 opta","oe oa-3 oA-G", "String Boolean Character");
		parser.parse("-oA=test");
		assertEquals(parser.getString("--opta"),"test");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void addAllShortcutsNotGroupShortcutBranchesTest() {
		parser.addAll("option1 option2  option3", null ,"String Boolean");
	}
}
