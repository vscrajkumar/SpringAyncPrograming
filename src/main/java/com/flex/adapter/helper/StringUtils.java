package com.flex.adapter.helper;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.math.NumberUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flex.adapter.constants.AppConstants;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class StringUtils {
	private static Logger LOG = LoggerFactory.getLogger(StringUtils.class);

	private final static String NON_THIN = "[^iIl1\\.,']";
	private static final Pattern VALID_PATTERN = Pattern.compile("[0-9]+|[A-Z]+");

	public static List<String> parse(String toParse) {
		List<String> chunks = new LinkedList<String>();
		Matcher matcher = VALID_PATTERN.matcher(toParse);
		while (matcher.find()) {
			chunks.add(matcher.group());
		}
		return chunks;
	}

	public static int textWidth(String str) {
		return (int) (str.length() - str.replaceAll(NON_THIN, "").length() / 2);
	}

	public static String ellipsize(String text, int max) {

		if (textWidth(text) <= max)
			return text;

		// Start by chopping off at the word before max
		// This is an over-approximation due to thin-characters...
		int end = text.lastIndexOf(' ', max - 3);

		// Just one long word. Chop it off.
		if (end == -1)
			return text.substring(0, max - 3) + "...";

		// Step forward as long as textWidth allows.
		int newEnd = end;
		do {
			end = newEnd;
			newEnd = text.indexOf(' ', end + 1);

			// No more spaces.
			if (newEnd == -1)
				newEnd = text.length();

		} while (textWidth(text.substring(0, newEnd) + "...") < max);

		return text.substring(0, end) + "...";
	}

	public static String generateNextLetter(String value) {

		/*
		 * int charValue = value.charAt(0); String next = String.valueOf( (char)
		 * (charValue + 1));
		 * 
		 * return next;
		 */

		int charValue = 0;
		String next;

		if (value.equalsIgnoreCase("Z")) {
			return "0";
		}

		if (!NumberUtils.isNumber(value)) {
			charValue = value.charAt(0);
			next = String.valueOf((char) (charValue + 1));
		} else {
			charValue = Integer.valueOf(value);
			next = String.valueOf(charValue + 1);
		}
		return next;

	}

	public static String generateNextRevision(JSONObject processInstruction) throws JSONException {
		Map<String, Object> findFiltersMap = new HashMap<String, Object>();
		JSONObject objectJson = new JSONObject();

		findFiltersMap.put("erpCode", processInstruction.getJSONObject("erp").getString("code"));
		findFiltersMap.put("businessUnitId", processInstruction.getJSONObject("businessUnit").getInt("id"));
		findFiltersMap.put("customerId", processInstruction.getJSONObject("customer").getInt("id"));
		findFiltersMap.put("lineId",
				processInstruction.getJSONArray("lines").getJSONObject(0).getString("description"));
		findFiltersMap.put("side", processInstruction.getString("side"));
		findFiltersMap.put("assemblyNumber", processInstruction.getJSONObject("assembly").getString("assemblyNumber"));
		findFiltersMap.put("assemblyRevision",
				processInstruction.getJSONObject("assembly").getString("assemblyRevision"));
		findFiltersMap.put("documentNumber", processInstruction.getString("documentNumber"));
		findFiltersMap.put("currentRevision", processInstruction.getString("documentRevision"));

		objectJson.put("filters", findFiltersMap);

		HttpResponse<JsonNode> response = null;
		String revision = "";
		try {
			response = Unirest.post(AppConstants.EPI_TARGET + AppConstants.GET_DOCUMENT_LINE_CNT)
					.headers(AppConstants.setupRequestHeaders()).body(objectJson.toString()).asJson();

		} catch (UnirestException e1) {
			e1.printStackTrace();
		}
		if (response.getStatus() == 200) {
			revision = response.getBody().getObject().get("newDocumentRevision").toString();
		}

		return revision;
	}

	public static String generatePreviousLetter(String value) {

		int charValue = 0;
		String next;

		if (value.equalsIgnoreCase("0")) {
			return "Z";
		}

		if (value.equalsIgnoreCase("A")) {
			return null;
		}

		if (!NumberUtils.isNumber(value)) {
			charValue = value.charAt(0);
			next = String.valueOf((char) (charValue - 1));
		} else {
			charValue = Integer.valueOf(value);
			next = String.valueOf(charValue - 1);
		}
		return next;

	}

	public static void genereteStringSequence() {
		// This is the configurable param
		int seqWidth = 2;

		Double charSetSize = 26d;

		// The size of the array will be 26 ^ seqWidth. ie: if 2 chars wide, 26
		// * 26. 3 chars, 26 * 26 * 26
		Double total = Math.pow(charSetSize, (new Integer(seqWidth)).doubleValue());

		StringBuilder[] sbArr = new StringBuilder[total.intValue()];
		// Initializing the Array
		for (int j = 0; j < total; j++) {
			sbArr[j] = new StringBuilder();
		}

		char ch = 'A';
		// Iterating over the entire length for the 'char width' number of times.
		for (int k = seqWidth; k > 0; k--) {
			// Iterating and adding each char to the entire array.
			for (int l = 1; l <= total; l++) {
				sbArr[l - 1].append(ch);
				if ((l % (Math.pow(charSetSize, k - 1d))) == 0) {
					ch++;
					if (ch > 'Z') {
						ch = 'A';
					}
				}
			}
		}

		// Use the stringbuilder array.
		for (StringBuilder builder : sbArr) {
			LOG.info(builder.toString());
		}

	}

	public static String IntToLetter(int letterNumber) {
		if (letterNumber < 27) {
			return Character.toString((char) (letterNumber + 96));
		} else {
			if (letterNumber % 26 == 0) {
				return IntToLetter((letterNumber / 26) - 1) + IntToLetter(((letterNumber - 1) % 26 + 1));
			} else {
				return IntToLetter(letterNumber / 26) + IntToLetter(letterNumber % 26);
			}
		}
	}

	public static Integer intToLetterNumber(int letterNumber) {
		if (letterNumber < 27) {
			return letterNumber + 96;
		} else {
			if (letterNumber % 26 == 0) {
				return (letterNumber / 26) - 1 + (letterNumber - 1) % 26 + 1;
			} else {
				return (letterNumber / 26) + (letterNumber % 26);
			}
		}
	}

	public static String indexToColumnItr(int index, char[] alphabet) {
		if (index <= 0)
			throw new IndexOutOfBoundsException("index must be a positive number");
		if (index <= alphabet.length)
			return Character.toString(alphabet[index - 1]);
		StringBuffer sb = new StringBuffer();
		while (index > 0) {
			sb.insert(0, alphabet[--index % alphabet.length]);
			index /= alphabet.length;
		}
		return sb.toString();
	}

	// Recursive
	public static String indexToColumnRec(int index, char[] alphabet) {
		if (index <= 0)
			throw new IndexOutOfBoundsException("index must be a positive number");
		if (index <= alphabet.length)
			return Character.toString(alphabet[index - 1]);
		return indexToColumnRec(--index / alphabet.length, alphabet) + alphabet[index % alphabet.length];
	}

	public static String[] generateSequence(int size) {
		String[] sequence = new String[size];
		int i = 0;
		for (AlphaIterator it = new AlphaIterator(size); it.hasNext();) {
			sequence[i++] = it.next();
		}
		return sequence;
	}

	public static void main(String... args) {
		// LOG.info(StringUtils.generateNextLetter("AA"));
		// StringUtils.genereteStringSequence();
		/*
		 * for (int i = 1;i<1024;i++) { LOG.info("i="+i+" -> "+IntToLetter(i)); }
		 */
		LOG.info("intToLetterNumber " + IntToLetter(200));

		String input = "abc".toLowerCase(); // note the to lower case in order to treat a and A the same way
		for (int i = 0; i < input.length(); ++i) {
			int position = input.charAt(i) - 'a' + 1;
			LOG.info("" + position);
		}
	}

}
