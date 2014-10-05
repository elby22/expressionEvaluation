package apps;

import java.io.IOException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.StringTokenizer;

import structures.Stack;

public class Expression {

	/**
	 * Expression to be evaluated
	 */
	String expr;                
    
	/**
	 * Scalar symbols in the expression 
	 */
	ArrayList<ScalarSymbol> scalars;   
	
	/**
	 * Array symbols in the expression
	 */
	ArrayList<ArraySymbol> arrays;
    
	/**
	 * Positions of opening brackets
	 */
	ArrayList<Integer> openingBracketIndex; 
    
	/**
	 * Positions of closing brackets
	 */
	ArrayList<Integer> closingBracketIndex; 

    /**
     * String containing all delimiters (characters other than variables and constants), 
     * to be used with StringTokenizer
     */
    public static final String delims = " \t*+-/()[]";
    
    /**
     * Initializes this Expression object with an input expression. Sets all other
     * fields to null.
     * 
     * @param expr Expression
     */
    public Expression(String expr) {
        this.expr = expr;
        scalars = null;
        arrays = null;
        openingBracketIndex = null;
        closingBracketIndex = null;
    }

    /**
     * Matches parentheses and square brackets. Populates the openingBracketIndex and
     * closingBracketIndex array lists in such a way that closingBracketIndex[i] is
     * the position of the bracket in the expression that closes an opening bracket
     * at position openingBracketIndex[i]. For example, if the expression is:
     * <pre>
     *    (a+(b-c))*(d+A[4])
     * </pre>
     * then the method would return true, and the array lists would be set to:
     * <pre>
     *    openingBracketIndex: [0 3 10 14]
     *    closingBracketIndex: [8 7 17 16]
     * </pe>
     * 
     * See the FAQ in project description for more details.
     * 
     * @return True if brackets are matched correctly, false if not
     */
    public boolean isLegallyMatched() {
        openingBracketIndex = new ArrayList<Integer>();
        closingBracketIndex = new ArrayList<Integer>();
		String e = this.expr;
		
		//Check for even number of brackets:
		int pCount = 0;
		int bCount = 0;
		
		for (int i = 0; i < e.length(); i++){
			if(e.charAt(i) == '(') pCount++;
			else if(e.charAt(i) == '[') bCount++;
			else if(e.charAt(i) == ')') pCount--;
			else if(e.charAt(i) == ']') bCount--;
			
			if(pCount < 0 || bCount < 0) return false;
		}
		
		if(pCount != 0 || bCount != 0) return false;
		
		//Get open indices
		for (int i = 0; i < e.length(); i++){
			if(e.charAt(i) == '(') openingBracketIndex.add(i);
			else if(e.charAt(i) == '[') openingBracketIndex.add(i);
		}
		
		for (int i = openingBracketIndex.size() - 1; i >= 0; i--){
			char o = e.charAt(openingBracketIndex.get(i));
			char c = ')';
			if(o == '[') c = ']';
			for (int j = openingBracketIndex.get(i); j < e.length(); j++){
				if (e.charAt(j) == c && !closingBracketIndex.contains(j)){
					closingBracketIndex.add(j);
					break;
				}
			}
		}
		
		//Flip closingBracketIndex
		
		for(int i = 0; i < closingBracketIndex.size() / 2; i++){
			int temp = closingBracketIndex.get(i);
			closingBracketIndex.set(i, closingBracketIndex.get(closingBracketIndex.size() - 1 - i));
			closingBracketIndex.set(closingBracketIndex.size() - 1 - i, temp);
		}
		
		//Check for loose brackets. Ex: (([)])
		for(int i = 0; i < openingBracketIndex.size(); i++){
			int count = 0;
			for(int j = openingBracketIndex.get(i) + 1; j < closingBracketIndex.get(i); j++){
				if (e.charAt(j) == '(' || e.charAt(j) == ')' || e.charAt(j) == '[' || e.charAt(j) == ']'){
					count++;
				}
			}
			if (count % 2 != 0) return false;
		}
    	return true;
    }

    /**
     * Populates the scalars and arrays lists with symbols for scalar and array
     * variables in the expression. For every varciable, a SINGLE symbol is created and stored,
     * even if it appears more than once in the expression.
     * At this time, values for all variables are set to
     * zero - they will be loaded from a file in the loadSymbolValues method.
     */
    public void buildSymbols() {
    	scalars = new ArrayList<ScalarSymbol>();
    	arrays = new ArrayList<ArraySymbol>();
    	ArrayList<String> tokenList = new ArrayList<String>();
    	StringTokenizer st = new StringTokenizer(expr, delims);
    	while(st.hasMoreTokens()){
    		tokenList.add(st.nextToken());
    	}
    	
//    	Test with:
//    	a-(b+A[B[2]])*d+3
//    	A[2*(a+b)]
//   	(varx + vary*varz[(vara+varb[(a+b)*33])])/55

    	for (int i = 0; i < tokenList.size() - 1; i++){
    		int index = expr.indexOf(tokenList.get(i));
    		//Check for proper index of for token and expr
    		if(i > 0){
    			char c = expr.charAt(index - 1);
    			if(Character.isLetter(c)) continue;	
    		}
    		if(expr.charAt(index + tokenList.get(i).length()) == '['){
    			ArraySymbol a = new ArraySymbol(tokenList.get(i));
    			if(!(arrays.contains(a) || a.name.matches("[-+]?\\d*\\.?\\d+"))) arrays.add(a);
    		}
    	}
    	
    	st = new StringTokenizer(expr, delims);
    	System.out.println(tokenList);
    	while(st.hasMoreTokens()){
    		String s = st.nextToken();
    		System.out.println(s);
    		boolean taken = false;
    		for(int i = 0; i < scalars.size(); i++){
    			if(scalars.get(i).name.equals(s)) taken = true;
    		}
    		for(int i = 0; i < arrays.size(); i++){
    			if(arrays.get(i).name.equals(s)) taken = true;
    		}
    		if(!(taken || s.matches("[-+]?\\d*\\.?\\d+"))){
    			ScalarSymbol a = new ScalarSymbol(s);
    			scalars.add(a);
    		}
    	}
    }
    
    /**
     * Loads values for symbols in the expression
     * 
     * @param sc Scanner for values input
     * @throws IOException If there is a problem with the input 
     */
    public void loadSymbolValues(Scanner sc) 
    throws IOException {
        while (sc.hasNextLine()) {
            StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
            int numTokens = st.countTokens();
            String sym = st.nextToken();
            ScalarSymbol ssymbol = new ScalarSymbol(sym);
            ArraySymbol asymbol = new ArraySymbol(sym);
            int ssi = scalars.indexOf(ssymbol);
            int asi = arrays.indexOf(asymbol);
            if (ssi == -1 && asi == -1) {
            	continue;
            }
            int num = Integer.parseInt(st.nextToken());
            if (numTokens == 2) { // scalar symbol
                scalars.get(ssi).value = num;
            } else { // array symbol
            	asymbol = arrays.get(asi);
            	asymbol.values = new int[num];
                // following are (index,val) pairs
                while (st.hasMoreTokens()) {
                    String tok = st.nextToken();
                    StringTokenizer stt = new StringTokenizer(tok," (,)");
                    int index = Integer.parseInt(stt.nextToken());
                    int val = Integer.parseInt(stt.nextToken());
                    asymbol.values[index] = val;              
                }
            }
        }
    }
    
    /**
     * Evaluates the expression, using RECURSION to evaluate subexpressions and to evaluate array 
     * subscript expressions.
     * 
     * @return Result of evaluation
     */
    public float evaluate() {
    	// COMPLETE THIS METHOD
    	
        // FOLLOWING LINE ADDED TO MAKE COMPILER HAPPY
    	return 0;
    }

    /**
     * Utility method, prints the symbols in the scalars list
     */
    public void printScalars() {
        for (ScalarSymbol ss: scalars) {
            System.out.println(ss);
        }
    }
    
    /**
     * Utility method, prints the symbols in the arrays list
     */
    public void printArrays() {
    	for (ArraySymbol as: arrays) {
    		System.out.println(as);
    	}
    }

}
