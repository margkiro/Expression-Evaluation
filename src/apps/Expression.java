package apps;

import java.io.*;
import java.util.*;
import java.util.regex.*;

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
    }

    /**
     * Populates the scalars and arrays lists with symbols for scalar and array
     * variables in the expression. For every variable, a SINGLE symbol is created and stored,
     * even if it appears more than once in the expression.
     * At this time, values for all variables are set to
     * zero - they will be loaded from a file in the loadSymbolValues method.
     */
    public void buildSymbols() {
        arrays = new ArrayList<ArraySymbol>();
        scalars = new ArrayList<ScalarSymbol>();
        
        String temp = "";
        for (int i = 0; i < expr.length(); i++)
        {
            temp = temp + expr.charAt(i);
            if (expr.charAt(i) == '[')
            {
                temp = temp + "~";
            }
        }
        StringTokenizer str = new StringTokenizer(temp, " \t*+-/()]~");

        while (str.hasMoreElements())
        {
            String x = str.nextToken();
            if (x.charAt(x.length()-1) == '[')
            {
                arrays.add(new ArraySymbol(x.substring(0, x.length()-1)));
            }
            else
            {
                if (!Character.isLetter(x.charAt(0)))
                    continue;
                else
                    scalars.add(new ScalarSymbol(x));
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
    		/** COMPLETE THIS METHOD **/
    	return evaluate(expr);
    		//return 0;
    }

    private float evaluate(String expression){
    	if(expression == null || expression.length()==0)
    		return 0;
    	Stack<Float> numbers = new Stack<Float> ();
    	Stack<String> operators = new Stack<String> ();
    	String[] expr = expression.split("(?<=[\\[\\]()\\-+*/])|(?=[\\[\\]()\\-+*/])");
    	float total = 0;
    	int count = 0;
    	int s = 0;
    	int l = 0;
    	
    	for(int i = 0; i<expr.length; i++){
    		//System.out.println("i: "+i);
    		if(expr[i].trim().equals("(")||expr[i].trim().equals("[")){
    			count++;
    			if(count == 1)
    				s = i;
    			//System.out.println("S: "+s);
    		}else if(expr[i].trim().equals(")")|| expr[i].trim().equals("]")){
    			count --;
    			if(count == 0){
    				l = i;
    				//System.out.println("L: " + l);
    				//System.out.println(expression.substring(l+1,expression.length()));
    				//System.out.println("4th: "+expr[4]);
    				//System.out.println("recur: " + expression.substring(s+1,l));
    				float t = evaluate(expression.substring(s+1,l));
    				
    				//System.out.println("T: " +t);
    				expression = expression.substring(0,s)+t+expression.substring(l+1,expression.length());
    				expr = expression.split("(?<=[\\[\\]()\\-+*/])|(?=[\\[\\]()\\-+*/])");
    				i = 0;
    				
    				//System.out.println("expr: "+expression);
    				continue;
    			}
    			
    		}
    		
    	}
    	
    	expr = expression.split("(?<=[\\[\\]()\\-+*/])|(?=[\\[\\]()\\-+*/])");
    	//System.out.println(expr[0]);
    	
    	/*for(int i = 0; i<expr.length; i++){
    		System.out.print(expr[i]);
    	}*/
    	
    	for(int i = expr.length-1; i>=0; i--){
    		if(Character.isDigit(expr[i].trim().charAt(0))){
    			float n =Float.valueOf(expr[i]);
    			numbers.push(n);
    			
    		}else if(Character.isLetter(expr[i].trim().charAt(0))){
    			
    			for(int j = 0; j<scalars.size(); j++){
    				if(scalars.get(j).name.equals(expr[i])){
    					int val = scalars.get(j).value;
    					float scalarVal = Float.valueOf(val);
    					numbers.push(scalarVal);
    					j = 0;
    				}else if(arrays.get(j).name.equals(expr[i])){
    					int [] val = arrays.get(j).values;
    					float [] floatArray = new float[val.length];
    					floatArray [j]  = (float) val[j];
    					numbers.push(floatArray[j]);
    					j = 0;
    				}
    			}
    			
    		}else if(expr[i].trim().equals("*")){
    			float num = numbers.pop();
    			float n = Float.valueOf(expr[i-1]);
    			i--;
    			total = num * n;
    			numbers.push(total);
    		}else if(expr[i].trim().equals("/")){
    			float num = numbers.pop();
    			float n = Float.valueOf(expr[i-1]);
    			i--;
    			total = n / num;
    			numbers.push(total);
    		}else if(expr[i].equals("-")){
        			operators.push(expr[i]);
        			
    		}else if(expr[i].equals("+")){
    			operators.push(expr[i]);
    		}
    	}
    	
    	while(operators.isEmpty() == false){
    		if(operators.peek().equals("-")){
    			operators.pop();
    			float last = numbers.pop();
        		float first = numbers.pop();
    			total = last-first;
    			numbers.push(total);
    		}else if(operators.peek().equals("+")){
    			operators.pop();
    			float last = numbers.pop();
        		float first = numbers.pop();
    			total = first+last;
    			numbers.push(total);
    		}
    	}
    	
    	if(numbers.size() == 1 && expr.length==1){
    		total = numbers.pop();
    	}
    	
    	return total;
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
