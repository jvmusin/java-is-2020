package impl.expression;

import api.expression.ExpressionParser;
import api.expression.ParseException;

import java.util.ArrayDeque;
import java.util.Deque;

public class ExpressionParserImpl implements ExpressionParser {
    private Deque<Character> expression;

    private Deque<Character> toQueue(String s) {
        Deque<Character> q = new ArrayDeque<>(s.length());
        for (int i = 0; i < s.length(); i++) q.addLast(s.charAt(i));
        return q;
    }

    private int peek() {
        while (!expression.isEmpty()) {
            char c = expression.peekFirst();
            if (Character.isWhitespace(c)) {
                expression.pollFirst();
                continue;
            }
            return c;
        }
        return -1;
    }

    private int pop() {
        int res = peek();
        expression.pollFirst();
        return res;
    }

    private boolean isEmpty() {
        return peek() == -1;
    }

    private static boolean isSign(int charCode) {
        return charCode == '-' || charCode == '+';
    }

    private int readNumber(boolean signRequired) throws ParseException {
        int sign = 1;
        if (isSign(peek())) {
            if (pop() == '-') sign = -1;
        } else if (signRequired) {
            int next = peek();
            throw new ParseException(String.format(
                    "Expected sign, but %s found", next == -1 ? "nothing" : "" + (char) next));
        }

        StringBuilder number = new StringBuilder();
        while (Character.isDigit(peek())) number.append((char) pop());

        try {
            return Integer.parseInt((sign == -1 ? '-' : '+') + number.toString());
        } catch (NumberFormatException e) {
            throw new ParseException(e.getMessage());
        }
    }

    @Override
    public int parse(String expression) throws ParseException {
        if (expression == null) throw new IllegalArgumentException("Expression can't be null");
        this.expression = toQueue(expression);

        int result = 0;
        for (boolean first = true; !isEmpty(); first = false)
            result = Math.addExact(result, readNumber(!first));
        return result;
    }
}
