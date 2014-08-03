
package org.smslib;

import java.util.List;
import junit.framework.TestCase;
import org.ajwcc.pduUtils.gsm3040.Pdu;
import org.ajwcc.pduUtils.gsm3040.PduParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.message.MsIsdn;
import org.smslib.message.OutboundMessage;

public class Test_PDU extends TestCase
{
	static Logger logger = LoggerFactory.getLogger(Test_PDU.class);

	private static String ALPHABET_7_VERY_VERY_SHORT = "ABCD";

	@SuppressWarnings("unused")
	private static String ALPHABET_7_VERY_SHORT = "ABCDE@$^";

	@SuppressWarnings("unused")
	private static String ALPHABET_7_SHORT = "ABCDEFGHI@$^{}\\\"";

	@SuppressWarnings("unused")
	private static String ALPHABET_7 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@$^{}\\\"[]~|€ !#%&'+-,./*";

	@SuppressWarnings("unused")
	private static String ALPHABET_7_EXT = "ΑΒΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡΣΤΥΦΧΨΩαβγδεζηθικλμνξοπρστυφχψωέύίόάήώς";

	public void test() throws Exception
	{
		String alphabet = ALPHABET_7_VERY_VERY_SHORT;
		for (int i = 1; i <= alphabet.length(); i++)
		{
			logger.debug(String.format("Testing with %d-letter words...", i));
			testCombos(alphabet, i, "");
		}
	}

	private void testCombos(String alphabet, int wordLength, String prefix) throws Exception
	{
		if (wordLength == 0) return;
		else if (wordLength == 1)
		{
			for (int i = 0; i < alphabet.length(); i++)
				testEncoding(prefix + alphabet.substring(i, i + 1));
		}
		else
		{
			for (int j = 0; j < alphabet.length(); j++)
				testCombos(alphabet, wordLength - 1, prefix + alphabet.substring(j, j + 1));
		}
	}

	private void testEncoding(String originalText) throws Exception
	{
		OutboundMessage m = new OutboundMessage("307974000111", originalText);
		List<String> pdus = m.getPdus(new MsIsdn(), 0, false);
		Pdu pdu = new PduParser().parsePdu(pdus.get(0));
		String decodedText = pdu.getDecodedText();
		if (!originalText.equalsIgnoreCase(decodedText)) throw new Exception(String.format("Mismatch! ORIG: %s, DEC: %s", originalText, decodedText));
	}
}
