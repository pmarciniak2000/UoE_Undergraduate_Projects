package st;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Task3_Parser {

	private OptionMap optionMap;

	public Task3_Parser() {
		optionMap = new OptionMap();
	}

	
	/*
	 * addAll Clarifications:
	 * If an invalid shortcut for a valid option is given then that option is added with no shortcut
	 * If an invalid type is given for any option then an illegalArgumentException is thrown
	 */
	public void addAll(String options, String shortcuts, String types) {
		
		if (options == null || options.isEmpty() || options.isBlank()) {
			throw new IllegalArgumentException("Option name invalid");
		}
		
		if (types == null || types.isEmpty() || types.isBlank()) {
			throw new IllegalArgumentException("Types invalid");
		}
		
		if (shortcuts == null) {
			throw new IllegalArgumentException("Shortcut cannot be null");
		}
		
		String[] opts = options.trim().split("\\s+");
		String[] shorts = shortcuts.trim().split("\\s+");
		String[] tps = types.trim().split("\\s+");
		
		
		//map types to groups
		HashMap<Integer,String> typeMappings = new HashMap<Integer, String>();
		for (int i=0; i<opts.length;i++) {
			try {
				typeMappings.put(i, tps[i]);
			}catch(Exception e) {
				typeMappings.put(i, tps[tps.length - 1]);
			}
		}

		ArrayList<String> expandedOptions = new ArrayList<String>();
		ArrayList<String> expandedShortcuts = new ArrayList<String>();
		ArrayList<String> expandedTypes = new ArrayList<String>();
		ArrayList<Integer> ignoreGroups = new ArrayList<Integer>();
		Pattern pattern = Pattern.compile("([A-Za-z0-9_])+(([A-Z]-[A-Z]+)|[a-z]-[a-z]+|[0-9]-[0-9]+)");

		HashMap<Integer,Integer> optionsPerGroup = new HashMap<Integer,Integer>();
		int ignoreIdx = 0;
		for (String option : opts) {
			int optionsInGroup = 0;
			String originalString = option;
			Matcher matcher = pattern.matcher(option);

			//if pattern matches group init
			if (matcher.matches()) {
				String pat = matcher.group(2);
				String[] bounds = pat.split("-");
				String upper = bounds[1];
				String lower = bounds[0];
				try { // integer range
					if (Integer.parseInt(lower) > Integer.parseInt(upper)) {// decreasing range
						for (int j = Integer.parseInt(lower); j >= Integer.parseInt(upper); j--) {
							option = originalString.substring(0, originalString.length() - ((pat.length()))) + j;
							expandedOptions.add(option);
							expandedTypes.add(typeMappings.get(ignoreIdx));
							optionsInGroup++;
						}
					} else {// increasing range
						for (int j = Integer.parseInt(lower); j <= Integer.parseInt(upper); j++) {
							option = originalString.substring(0, originalString.length() - ((pat.length()))) + j;
							expandedOptions.add(option);
							expandedTypes.add(typeMappings.get(ignoreIdx));
							optionsInGroup++;
						}
					}
				} catch (NumberFormatException e) { // character range
					if (lower.charAt(0) > upper.charAt(0)) { // decreasing range
						for (char j = lower.charAt(0); j >= upper.charAt(0); j--) {
							option = originalString.substring(0, originalString.length() - (pat.length())) + j;
							expandedOptions.add(option);
							expandedTypes.add(typeMappings.get(ignoreIdx));
							optionsInGroup++;
						}
					} else {// increasing range
						for (char j = lower.charAt(0); j <= upper.charAt(0); j++) {
							option = originalString.substring(0, originalString.length() - (pat.length())) + j;
							expandedOptions.add(option);
							expandedTypes.add(typeMappings.get(ignoreIdx));
							optionsInGroup++;
						}
					}
				}
				optionsPerGroup.put(ignoreIdx, optionsInGroup);
				option = originalString.substring(0, originalString.length() - (pat.length()));
			  //if not group init and valid inputs then add option normally
			} else if (option != null && !option.isEmpty() && option.matches("[a-zA-Z_][a-zA-Z0-9_]*")){
				expandedOptions.add(option);
				expandedTypes.add(typeMappings.get(ignoreIdx));
				optionsPerGroup.put(ignoreIdx, 1);
			}
			else {
				//option group invalid so add to list of ignored
				ignoreGroups.add(ignoreIdx);
			}
			ignoreIdx++;
		}
		
		for (int idx: ignoreGroups) {
			shorts[idx] = "";
		}
		
		int group=0;
		for (String scut : shorts) {
			String originalString = scut;
			Matcher matcher = pattern.matcher(scut);

			if (matcher.matches()) {
				String pat = matcher.group(2);
				String[] bounds = pat.split("-");
				String upper = bounds[1];
				String lower = bounds[0];
				try { // integer range
					if (Integer.parseInt(lower) > Integer.parseInt(upper)) {// decreasing range
						for (int j = Integer.parseInt(lower); j >= Integer.parseInt(upper); j--) {
							scut = originalString.substring(0, originalString.length() - ((pat.length()))) + j;
							expandedShortcuts.add(scut);
						}
					} else {// increasing range
						for (int j = Integer.parseInt(lower); j <= Integer.parseInt(upper); j++) {
							scut = originalString.substring(0, originalString.length() - ((pat.length()))) + j;
							expandedShortcuts.add(scut);
						}
					}
				} catch (NumberFormatException e) { // character range
					if (lower.charAt(0) > upper.charAt(0)) { // decreasing range
						for (char j = lower.charAt(0); j >= upper.charAt(0); j--) {
							scut = originalString.substring(0, originalString.length() - (pat.length())) + j;
							expandedShortcuts.add(scut);
						}
					} else {// increasing range
						for (char j = lower.charAt(0); j <= upper.charAt(0); j++) {
							scut = originalString.substring(0, originalString.length() - (pat.length())) + j;
							expandedShortcuts.add(scut);
						}
					}
				}
				scut = originalString.substring(0, originalString.length() - (pat.length()));
			} else if (scut != null && !scut.isEmpty() && scut.matches("[a-zA-Z_][a-zA-Z0-9_]*")){
				expandedShortcuts.add(scut);
			}
			//this deals with the case if option valid but shortcuts not and assigns empty shortcut
			else if(!ignoreGroups.contains(group)) {
				for (int i=0;i<optionsPerGroup.get(group);i++)
					expandedShortcuts.add("");
			}
			group++;
		}

		// Now that options expanded can proceed to just iteratively call addOption

		int i = 0;
		for (String option : expandedOptions) {
			String shortcut = "";
			String type = expandedTypes.get(i);
			if (i < expandedShortcuts.size()) {
				shortcut = expandedShortcuts.get(i);
			}
			addOption(new Option(option, convertStringTypeToEnum(type)), shortcut);
			i++;
		}
	}

	public void addAll(String options, String types) {
		
		if (options == null || options.isEmpty() || options.isBlank()) {
			throw new IllegalArgumentException("Option name invalid");
		}
		if (types == null || types.isEmpty() || types.isBlank()) {
			throw new IllegalArgumentException("Type invalid");
		}
		
		String[] opts = options.trim().split("\\s+");
		String[] tps = types.trim().split("\\s+");

		int i = 0;
		ArrayList<String> expandedOptions = new ArrayList<String>();// for easy testing not needed w/o shortcuts
		
		for (String option : opts) {
			String originalString = option;
			Pattern pattern = Pattern.compile("([A-Za-z0-9_])+(([A-Z]-[A-Z]+)|[a-z]-[a-z]+|[0-9]-[0-9]+)");
			Matcher matcher = pattern.matcher(option);

			String type = "";
			if (i >= tps.length) {
				type = tps[tps.length - 1];
			} else {
				type = tps[i];
			}

			if (matcher.matches()) {
				String pat = matcher.group(2);
				String[] bounds = pat.split("-");
				String upper = bounds[1];
				String lower = bounds[0];

				try { // integer range
					if (Integer.parseInt(lower) > Integer.parseInt(upper)) {// decreasing range
						for (int j = Integer.parseInt(lower); j >= Integer.parseInt(upper); j--) {
							option = originalString.substring(0, originalString.length() - ((pat.length()))) + j;
							expandedOptions.add(option);
							addOption(new Option(option, convertStringTypeToEnum(type)), "");
						}
					} else {// increasing range
						for (int j = Integer.parseInt(lower); j <= Integer.parseInt(upper); j++) {
							option = originalString.substring(0, originalString.length() - ((pat.length()))) + j;
							expandedOptions.add(option);
							addOption(new Option(option, convertStringTypeToEnum(type)), "");
						}
					}
				} catch (NumberFormatException e) { // character range
					if (lower.charAt(0) > upper.charAt(0)) { // decreasing range
						for (char j = lower.charAt(0); j >= upper.charAt(0); j--) {
							option = originalString.substring(0, originalString.length() - (pat.length())) + j;
							addOption(new Option(option, convertStringTypeToEnum(type)), "");
							expandedOptions.add(option);
						}
					} else {// increasing range
						for (char j = lower.charAt(0); j <= upper.charAt(0); j++) {
							option = originalString.substring(0, originalString.length() - (pat.length())) + j;
							addOption(new Option(option, convertStringTypeToEnum(type)), "");
							expandedOptions.add(option);
						}
					}
				}

				option = originalString.substring(0, originalString.length() - (pat.length()));
			}
			// else not group initalisation so just add option normally providing it is valid format
			else if (option != null && !option.isEmpty() && option.matches("[a-zA-Z_][a-zA-Z0-9_]*")){
				addOption(new Option(option, convertStringTypeToEnum(type)), "");
				expandedOptions.add(option);
			}
			i++;
		}
	}

	// helper method to convert string to type
	public Type convertStringTypeToEnum(String typeString) {
		typeString = typeString.toLowerCase();
		if (typeString.equals("string")) {
			return Type.STRING;
		} else if (typeString.equals("integer")) {
			return Type.INTEGER;
		} else if (typeString.equals("boolean")) {
			return Type.BOOLEAN;
		} else if (typeString.equals("character")) {
			return Type.CHARACTER;
		} else {
			throw new IllegalArgumentException("Invalid Type Argument");
		}

	}

	public void addOption(Option option, String shortcut) {
		optionMap.store(option, shortcut);
	}

	public void addOption(Option option) {
		optionMap.store(option, "");
	}

	public boolean optionExists(String key) {
		return optionMap.optionExists(key);
	}

	public boolean shortcutExists(String key) {
		return optionMap.shortcutExists(key);
	}

	public boolean optionOrShortcutExists(String key) {
		return optionMap.optionOrShortcutExists(key);
	}

	public int getInteger(String optionName) {
		String value = getString(optionName);
		Type type = getType(optionName);
		int result;
		switch (type) {
		case STRING:
		case INTEGER:
			try {
				result = Integer.parseInt(value);
			} catch (Exception e) {
				try {
					new BigInteger(value);
				} catch (Exception e1) {
				}
				result = 0;
			}
			break;
		case BOOLEAN:
			result = getBoolean(optionName) ? 1 : 0;
			break;
		case CHARACTER:
			result = (int) getCharacter(optionName);
			break;
		default:
			result = 0;
		}
		return result;
	}

	public boolean getBoolean(String optionName) {
		String value = getString(optionName);
		return !(value.toLowerCase().equals("false") || value.equals("0") || value.equals(""));
	}

	public String getString(String optionName) {
		return optionMap.getValue(optionName);
	}

	public char getCharacter(String optionName) {
		String value = getString(optionName);
		return value.equals("") ? '\0' : value.charAt(0);
	}

	public void setShortcut(String optionName, String shortcutName) {
		optionMap.setShortcut(optionName, shortcutName);
	}

	public void replace(String variables, String pattern, String value) {

		variables = variables.replaceAll("\\s+", " ");

		String[] varsArray = variables.split(" ");

		for (int i = 0; i < varsArray.length; ++i) {
			String varName = varsArray[i];
			String var = (getString(varName));
			var = var.replace(pattern, value);
			if (varName.startsWith("--")) {
				String varNameNoDash = varName.substring(2);
				if (optionMap.optionExists(varNameNoDash)) {
					optionMap.setValueWithOptionName(varNameNoDash, var);
				}
			} else if (varName.startsWith("-")) {
				String varNameNoDash = varName.substring(1);
				if (optionMap.shortcutExists(varNameNoDash)) {
					optionMap.setValueWithOptionShortcut(varNameNoDash, var);
				}
			} else {
				if (optionMap.optionExists(varName)) {
					optionMap.setValueWithOptionName(varName, var);
				}
				if (optionMap.shortcutExists(varName)) {
					optionMap.setValueWithOptionShortcut(varName, var);
				}
			}

		}
	}

	private List<CustomPair> findMatches(String text, String regex) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(text);
		// Check all occurrences
		List<CustomPair> pairs = new ArrayList<CustomPair>();
		while (matcher.find()) {
			CustomPair pair = new CustomPair(matcher.start(), matcher.end());
			pairs.add(pair);
		}
		return pairs;
	}

	public int parse(String commandLineOptions) {
		if (commandLineOptions == null) {
			return -1;
		}
		int length = commandLineOptions.length();
		if (length == 0) {
			return -2;
		}

		List<CustomPair> singleQuotePairs = findMatches(commandLineOptions, "(?<=\')(.*?)(?=\')");
		List<CustomPair> doubleQuote = findMatches(commandLineOptions, "(?<=\")(.*?)(?=\")");
		List<CustomPair> assignPairs = findMatches(commandLineOptions, "(?<=\\=)(.*?)(?=[\\s]|$)");

		for (CustomPair pair : singleQuotePairs) {
			String cmd = commandLineOptions.substring(pair.getX(), pair.getY());
			cmd = cmd.replaceAll("\"", "{D_QUOTE}").replaceAll(" ", "{SPACE}").replaceAll("-", "{DASH}").replaceAll("=",
					"{EQUALS}");

			commandLineOptions = commandLineOptions.replace(commandLineOptions.substring(pair.getX(), pair.getY()),
					cmd);
		}

		for (CustomPair pair : doubleQuote) {
			String cmd = commandLineOptions.substring(pair.getX(), pair.getY());
			cmd = cmd.replaceAll("\'", "{S_QUOTE}").replaceAll(" ", "{SPACE}").replaceAll("-", "{DASH}").replaceAll("=",
					"{EQUALS}");

			commandLineOptions = commandLineOptions.replace(commandLineOptions.substring(pair.getX(), pair.getY()),
					cmd);
		}

		for (CustomPair pair : assignPairs) {
			String cmd = commandLineOptions.substring(pair.getX(), pair.getY());
			cmd = cmd.replaceAll("\"", "{D_QUOTE}").replaceAll("\'", "{S_QUOTE}").replaceAll("-", "{DASH}");
			commandLineOptions = commandLineOptions.replace(commandLineOptions.substring(pair.getX(), pair.getY()),
					cmd);
		}

		commandLineOptions = commandLineOptions.replaceAll("--", "-+").replaceAll("\\s+", " ");

		String[] elements = commandLineOptions.split("-");

		for (int i = 0; i < elements.length; ++i) {
			String entry = elements[i];

			if (entry.isBlank()) {
				continue;
			}

			String[] entrySplit = entry.split("[\\s=]", 2);

			boolean isKeyOption = entry.startsWith("+");
			String key = entrySplit[0];
			key = isKeyOption ? key.substring(1) : key;
			String value = "";

			if (entrySplit.length > 1 && !entrySplit[1].isBlank()) {
				String valueWithNoise = entrySplit[1].trim();
				value = valueWithNoise.split(" ")[0];
			}

			// Explicitly convert boolean.
			if (getType(key) == Type.BOOLEAN && (value.toLowerCase().equals("false") || value.equals("0"))) {
				value = "";
			}

			value = value.replace("{S_QUOTE}", "\'").replace("{D_QUOTE}", "\"").replace("{SPACE}", " ")
					.replace("{DASH}", "-").replace("{EQUALS}", "=");

			boolean isUnescapedValueInQuotes = (value.startsWith("\'") && value.endsWith("\'"))
					|| (value.startsWith("\"") && value.endsWith("\""));

			value = value.length() > 1 && isUnescapedValueInQuotes ? value.substring(1, value.length() - 1) : value;

			if (isKeyOption) {
				optionMap.setValueWithOptionName(key, value);
			} else {
				optionMap.setValueWithOptionShortcut(key, value);

			}
		}

		return 0;

	}

	private Type getType(String option) {
		Type type = optionMap.getType(option);
		return type;
	}

	@Override
	public String toString() {
		return optionMap.toString();
	}

	private class CustomPair {

		CustomPair(int x, int y) {
			this.x = x;
			this.y = y;
		}

		private int x;
		private int y;

		public int getX() {
			return this.x;
		}

		public int getY() {
			return this.y;
		}
	}
}
