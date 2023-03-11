import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

//import components.map.Map;
//import components.map.Map1L;
//import components.queue.Queue;
//import components.queue.Queue1L;
//import components.set.Set;
//import components.set.Set1L;
//import components.simplereader.SimpleReader;
//import components.simplereader.SimpleReader1L;
//import components.simplewriter.SimpleWriter;
//import components.simplewriter.SimpleWriter1L;

/**
 * Program to count the time of words appear in given input file and output the
 * words at lower case with counted appearance in a table in a HTML file, each
 * pair represent a word, and in alphabetical order in the exist original file.
 *
 * @author Guo-Qian Zeng
 *
 */
public final class WordCounter {

    /**
     * Private constructor so this utility class cannot be instantiated.
     */
    private WordCounter() {
    }

    /**
     * Generates the set of characters in the given {@code String} into the
     * given {@code Set}.
     *
     * @param str
     *            the given {@code String}
     * @param charSet
     *            the {@code Set} to be replaced
     * @replaces charSet
     * @ensures charSet = entries(str)
     */
    private static void generateElements(String str, Set<Character> charSet) {
        assert str != null : "Violation of: str is not null";
        assert charSet != null : "Violation of: charSet is not null";
        Set<Character> characters = charSet;
        for (int i = 0; i < str.length(); i++) {
            if (!characters.contains(str.charAt(i))) {
                characters.add(str.charAt(i));
            }
        }
        charSet.addAll(characters);
    }

    /*
     * Sort
     */

    /**
     * Compare {@code String}s in alphabetical order.
     */
    @SuppressWarnings("serial")
    private static class StringLT
            implements Comparator<String>, java.io.Serializable {
        // implement java.io.Serializable to serialize potential need element

        /**
         * @param o1
         *            string of compare
         * @param o2
         *            string of compare
         * @require o1 and o2 is not null
         * @ensure compare o1 and o2 alphabetically, output negatives if o1 < o2
         *         output zero if o1 = o2, output positive if, o1 > o2, result
         *         would be comparator.
         */
        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    }

    /**
     * Returns the first "word" (maximal length string of characters not in
     * {@code separators}) or "separator string" (maximal length string of
     * characters in {@code separators}), transform upper case word to lower
     * case word, in the given {@code text} starting at the given
     * {@code position}.
     *
     * @param text
     *            the {@code String} from which to get the word or separator
     *            string
     * @param position
     *            the starting index
     * @param separators
     *            the {@code Set} of separator characters
     * @return the first word in lower cases or separator string found in
     *         {@code text} starting at index {@code position}.
     * @requires 0 <= position < |text|
     * @ensures <pre>
     * nextWordOrSeparator =
     *   text[position, position + |nextWordOrSeparator|)  and
     * if entries(text[position, position + 1)) intersection separators = {}
     * then
     *   entries(nextWordOrSeparator) intersection separators = {}  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      intersection separators /= {})
     * else
     *   entries(nextWordOrSeparator) is subset of separators  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      is not subset of separators)
     * </pre>
     */
    private static String nextWordOrSeparator(String text, int position,
            Set<Character> separators) {
        String operand = "";
        int x = position;

        if (separators.contains(text.charAt(position))) {
            while (x < text.length() && separators.contains(text.charAt(x))) {
                x++;
            }
            operand = text.substring(position, x);
        } else {
            while (x < text.length() && !separators.contains(text.charAt(x))) {
                x++;
            }
            operand = text.substring(position, x).toLowerCase();
        }
        return operand;
    }

    /**
     * Read information in word from input line by line, identifying with
     * separatorSet. Update the number of count in the Map and make sure the key
     * is in the Queue;
     *
     * @param separatorSet
     *            the set of characters contains what is considered separators
     * @param data
     *            Map<String, Integer> hold the word name and its appearance
     * @param key
     *            Queue<String> hold the word name to interpret map better later
     * @param text
     *            string of input
     * @update data, key
     * @requires text is not null
     * @ensures update the Map "data" and Queue "key" from the line of input. If
     *          a word has existed in the map, update value of this word, plus
     *          one. If a word does not existed in the map, update the map,
     *          create a new pair of map that the key of string is the name of
     *          word and value should = 1
     */
    private static void updateMapQueue(Set<Character> separatorSet,
            java.util.Map<String, Integer> data,
            java.util.PriorityQueue<String> key, String text) {
        assert text != null : "violation of input is not null";

        int position = 0;
        while (position < text.length()) {
            String token = nextWordOrSeparator(text, position, separatorSet);
            if (!separatorSet.contains(token.charAt(0))) {
                if (!data.containsKey(token)) {
                    data.put(token, 1);
                    key.add(token);
                } else {
                    int rep = data.get(token);
                    rep++;
                    data.remove(token);
                    data.put(token, rep);
                }
            }
            position += token.length();
        }
    }

    /**
     * Counting the replication of word in text, update the key, return a map.
     *
     * @param inPath
     *            name and path input
     * @param key
     *            queue preserves keys
     * @return a complete map including words and their count in text
     * @update key
     * @requires input exist, key is not null
     * @ensures return a map of words sort in queue and measure per subject set
     */
    private static Map<String, Integer> counting(String inPath,
            java.util.PriorityQueue<String> key) {

        final String separatorStr = " ;,.?!-~@#$%^&*()_+={}[]|\"\\\'/";
        Set<Character> separatorSet = new java.util.HashSet<Character>();
        generateElements(separatorStr, separatorSet);

        BufferedReader in;

        try {
            in = new BufferedReader(new FileReader(inPath));
        } catch (IOException i) {
            System.err.println("New Failed BudderedReader");
            return null;
        }
        Map<String, Integer> data = new java.util.HashMap();

        try {
            while (in != null) {
                String text = in.readLine();
                updateMapQueue(separatorSet, data, key, text);
            }
        } catch (IOException i) {
            System.err.println("Readline.");
        }
        // We do not need Case_Insensitive_Order as comparator because the words,
        // are already processed.

        try {
            in.close();
            // close the SimpleReader
        } catch (IOException i) {
            System.err.println("BufferedReader Close Error.");
            return null;
        }

        return data;
    }

    /*
     * HTML / table outputting
     */

    /**
     * @param out
     *            output name
     * @param in
     *            place of input
     * @require in exist
     * @ensures output the header
     */
    public static void outputHeader(PrintWriter out, String in) {
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Words Counted in " + in + "</title>");
        out.println("</head>");
        out.println("<body>");
        out.println(" <h2>Words Counted in " + in + "</h2>");
        out.println(" <hr/>");
        out.println(" <table border=\"1\">");
        out.println("  <tr>");
        out.println("   <td><b>words</b></td>");
        out.println("   <td><b>counts</b></td>");
        out.println("  </tr>");
    }

    /**
     * Output a map pair html table.
     *
     * @param in
     *            input file name
     * @param out
     *            output file
     * @require in exist
     * @ensure output table rows
     */
    private static void outputNumbers(String in, PrintWriter out) {
        /*
         * Put your code for myMethod here
         */
        PriorityQueue<String> key = new java.util.PriorityQueue<String>(
                new StringLT());
        java.util.Map<String, Integer> many = counting(in, key);

        while (key.size() > 0) {
            String text = key.poll();
            out.println("  <tr>");
            out.println("   <td>" + text + "</td>");
            out.println("   <td>" + many.get(text) + "</td>");
            out.println("  </tr>");
        }
    }

    /**
     * Output the footer the table and the HTML text.
     *
     * @param out
     *            output file
     * @ensure output the footer and close the editor
     */
    private static void outputFooter(PrintWriter out) {
        out.println(" </table>");
        out.println("</body>");
        out.println("</html>");
    }

    /**
     * Combination of printing HTML file.
     *
     * @param in
     *            the input name
     * @param outputFile
     *            the output name
     * @requires in accessible and exists
     * @ensures output the file
     */
    public static void outputWordCount(String in, String outputFile) {

        PrintWriter out;
        try {
            out = new PrintWriter(
                    new BufferedWriter(new FileWriter(outputFile)));
        } catch (IOException i) {
            System.err.println("New PrintWriter Failed.");
            return;
        }

        outputHeader(out, in);
        outputNumbers(in, out);
        outputFooter(out);

        out.close();
    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        BufferedReader in;
        in = new BufferedReader(new InputStreamReader(System.in));

        String inputFile = null;
        System.out.println("Please enter input: ");
        try {
            inputFile = in.readLine();
        } catch (IOException i) {
            System.err.println("Read.");
        }

        String outputFile = null;
        System.out.println("Please enter output: ");
        try {
            outputFile = in.readLine();
        } catch (IOException i) {
            System.err.println("Read.");
        }

        outputWordCount(inputFile, outputFile);

        /*
         * Close input and output streams
         */
        try {
            in.close();
        } catch (IOException i) {
            System.err.println("");
        }
    }

}
