/**
 * Copyright (c) 2008-2010, http://code.google.com/p/snakeyaml/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.yaml.snakeyaml.reader;

import java.util.regex.Matcher;

import junit.framework.TestCase;

public class ReaderStringTest extends TestCase {

    public void testCheckPrintable() {
        StreamReader reader = new StreamReader("test");
        reader.checkPrintable("test");
        Matcher matcher = StreamReader.NON_PRINTABLE.matcher("test");
        assertFalse(matcher.find());
    }

    public void testCheckNonPrintable() {
        Matcher matcher = StreamReader.NON_PRINTABLE.matcher("test\u0005 fail");
        assertTrue(matcher.find());
        try {
            new StreamReader("test\u0005 fail");
            fail("Non printable Unicode characters must not be accepted.");
        } catch (ReaderException e) {
            assertEquals(
                    "unacceptable character #5 special characters are not allowed\nin \"<string>\", position 4",
                    e.toString());
        }
    }

    public void testForward() {
        StreamReader reader = new StreamReader("test");
        while (reader.peek() != '\u0000') {
            reader.forward(1);
        }
        reader = new StreamReader("test");
        assertEquals('t', reader.peek());
        reader.forward(1);
        assertEquals('e', reader.peek());
        reader.forward(1);
        assertEquals('s', reader.peek());
        reader.forward(1);
        assertEquals('t', reader.peek());
        reader.forward(1);
        assertEquals('\u0000', reader.peek());
    }

    public void testPeekInt() {
        StreamReader reader = new StreamReader("test");
        assertEquals('t', reader.peek(0));
        assertEquals('e', reader.peek(1));
        assertEquals('s', reader.peek(2));
        assertEquals('t', reader.peek(3));
        reader.forward(1);
        assertEquals('e', reader.peek(0));
        assertEquals('s', reader.peek(1));
        assertEquals('t', reader.peek(2));
    }

}
